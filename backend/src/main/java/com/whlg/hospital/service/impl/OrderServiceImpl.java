package com.whlg.hospital.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.whlg.hospital.dto.CreateAppointmentRequest;
import com.whlg.hospital.dto.CreateConsultRequest;
import com.whlg.hospital.dto.CreatePaymentRequest;
import com.whlg.hospital.dto.PayRequest;
import com.whlg.hospital.dto.PaymentCallbackRequest;
import com.whlg.hospital.entity.Appointment;
import com.whlg.hospital.entity.Consult;
import com.whlg.hospital.entity.Doctor;
import com.whlg.hospital.entity.FamilyMember;
import com.whlg.hospital.entity.Message;
import com.whlg.hospital.entity.PaymentFlow;
import com.whlg.hospital.entity.Schedule;
import com.whlg.hospital.mapper.AppointmentMapper;
import com.whlg.hospital.mapper.ConsultMapper;
import com.whlg.hospital.mapper.DoctorMapper;
import com.whlg.hospital.mapper.FamilyMemberMapper;
import com.whlg.hospital.mapper.HospitalMapper;
import com.whlg.hospital.mapper.MessageMapper;
import com.whlg.hospital.mapper.PaymentFlowMapper;
import com.whlg.hospital.mapper.ScheduleMapper;
import com.whlg.hospital.service.AlipayService;
import com.whlg.hospital.service.OrderService;
import com.whlg.hospital.support.ApiException;
import com.whlg.hospital.util.StatusCode;
import com.whlg.hospital.vo.PageResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl extends ServiceSupport implements OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderServiceImpl.class);

    private final AppointmentMapper appointmentMapper;
    private final ConsultMapper consultMapper;
    private final DoctorMapper doctorMapper;
    private final FamilyMemberMapper familyMemberMapper;
    private final HospitalMapper hospitalMapper;
    private final MessageMapper messageMapper;
    private final PaymentFlowMapper paymentFlowMapper;
    private final ScheduleMapper scheduleMapper;
    private final AlipayService alipayService;

    public OrderServiceImpl(AppointmentMapper appointmentMapper,
                            ConsultMapper consultMapper,
                            DoctorMapper doctorMapper,
                            FamilyMemberMapper familyMemberMapper,
                            HospitalMapper hospitalMapper,
                            MessageMapper messageMapper,
                            PaymentFlowMapper paymentFlowMapper,
                            ScheduleMapper scheduleMapper,
                            AlipayService alipayService) {
        this.appointmentMapper = appointmentMapper;
        this.consultMapper = consultMapper;
        this.doctorMapper = doctorMapper;
        this.familyMemberMapper = familyMemberMapper;
        this.hospitalMapper = hospitalMapper;
        this.messageMapper = messageMapper;
        this.paymentFlowMapper = paymentFlowMapper;
        this.scheduleMapper = scheduleMapper;
        this.alipayService = alipayService;
    }

    @Override
    @Transactional
    public Map<String, Object> createAppointment(CreateAppointmentRequest request) {
        Long patientId = request == null ? null : request.resolvePatientId();
        check(request != null && request.getDoctorId() != null && request.getHospitalId() != null
                && patientId != null && request.getAppointmentDate() != null
                && request.getAppointmentTime() != null && !request.getAppointmentTime().trim().isEmpty(), "预约参数不能为空");
        Long userId = requireUserId();
        Doctor doctor = doctorMapper.selectById(request.getDoctorId());
        FamilyMember familyMember = resolveFamilyMember(request, patientId, userId);
        check(doctor != null && Integer.valueOf(1).equals(doctor.getStatus()), "医生不可预约");
        check(request.getHospitalId().equals(doctor.getHospitalId()), "医生不属于所选医院");
        check(familyMember != null && userId.equals(familyMember.getUserId()), "就诊人不存在");
        Schedule schedule = resolveSchedule(request, doctor);
        check(Integer.valueOf(1).equals(schedule.getStatus())
                && schedule.getScheduleDate() != null
                && !schedule.getScheduleDate().isBefore(LocalDate.now()), "排班不可预约");
        check(isScheduleTimeAvailable(schedule), "排班已过期");
        check(doctor.getHospitalId() != null && doctor.getHospitalId().equals(schedule.getHospitalId()),
                "排班归属不一致");
        check(schedule.getRemainCount() != null && schedule.getRemainCount() > 0
                && schedule.getTotalCount() != null && schedule.getTotalCount() > 0
                && schedule.getRemainCount() <= schedule.getTotalCount(), "号源数据异常");
        Long existing = appointmentMapper.selectCount(new LambdaQueryWrapper<Appointment>()
                .eq(Appointment::getUserId, userId)
                .eq(Appointment::getDoctorId, doctor.getId())
                .eq(Appointment::getHospitalId, doctor.getHospitalId())
                .eq(Appointment::getAppointmentDate, schedule.getScheduleDate())
                .eq(Appointment::getAppointmentTime, schedule.getTimeSlot())
                .ne(Appointment::getStatus, 4));
        check(existing == 0, "该排班已预约");

        int updated = scheduleMapper.update(null, new LambdaUpdateWrapper<Schedule>()
                .eq(Schedule::getId, schedule.getId())
                .eq(Schedule::getStatus, 1)
                .gt(Schedule::getRemainCount, 0)
                .setSql("remain_count = remain_count - 1, status = CASE WHEN remain_count <= 1 THEN 2 ELSE 1 END"));
        check(updated == 1, "号源不足");

        Appointment appointment = new Appointment();
        appointment.setOrderNo(nextOrderNo("AP"));
        appointment.setUserId(userId);
        appointment.setDoctorId(doctor.getId());
        appointment.setHospitalId(doctor.getHospitalId());
        appointment.setPatientName(familyMember.getName());
        appointment.setPatientPhone(familyMember.getPhone());
        appointment.setPatientIdCard(familyMember.getIdCard());
        appointment.setPatientGender(familyMember.getGender());
        appointment.setPatientAge(familyMember.getBirthday() == null ? null
                : Period.between(familyMember.getBirthday(), schedule.getScheduleDate()).getYears());
        appointment.setAppointmentDate(schedule.getScheduleDate());
        appointment.setAppointmentTime(schedule.getTimeSlot());
        appointment.setDiseaseDesc(request.getDiseaseDesc());
        appointment.setAmount(doctor.getRegistrationPrice());
        appointment.setStatus(1);
        appointment.setCreateTime(LocalDateTime.now());
        appointment.setUpdateTime(LocalDateTime.now());
        appointmentMapper.insert(appointment);
        createMessage(userId, "预约已创建", "预约订单 " + appointment.getOrderNo() + " 已创建，请在确认信息后完成后续操作。");

        Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("orderNo", appointment.getOrderNo());
        result.put("amount", appointment.getAmount());
        return result;
    }

    @Override
    public Map<String, Object> getAppointment(String orderNo) {
        Appointment appointment = requireOwnedAppointment(orderNo);
        Doctor doctor = doctorMapper.selectById(appointment.getDoctorId());
        com.whlg.hospital.entity.Hospital hospital = hospitalMapper.selectById(appointment.getHospitalId());
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("orderNo", appointment.getOrderNo());
        result.put("doctor", doctor == null ? "医生已下线" : doctor.getName());
        result.put("hospital", hospital == null ? "医院已下线" : hospital.getName());
        result.put("date", appointment.getAppointmentDate());
        result.put("timeSlot", appointment.getAppointmentTime());
        result.put("patientName", appointment.getPatientName());
        result.put("status", appointment.getStatus());
        result.put("fee", appointment.getAmount());
        result.put("createTime", appointment.getCreateTime());
        return result;
    }

    @Override
    @Transactional
    public void cancelAppointment(String orderNo) {
        Appointment appointment = requireOwnedAppointment(orderNo);
        check(Integer.valueOf(1).equals(appointment.getStatus()), "当前订单不可取消");
        Schedule schedule = scheduleMapper.selectOne(new LambdaQueryWrapper<Schedule>()
                .eq(Schedule::getDoctorId, appointment.getDoctorId())
                .eq(Schedule::getHospitalId, appointment.getHospitalId())
                .eq(Schedule::getScheduleDate, appointment.getAppointmentDate())
                .eq(Schedule::getTimeSlot, appointment.getAppointmentTime()));
        check(schedule != null, "对应排班不存在");
        check(schedule.getTotalCount() != null && schedule.getRemainCount() != null, "排班号源数据异常");
        appointment.setStatus(4);
        appointment.setUpdateTime(LocalDateTime.now());
        appointmentMapper.updateById(appointment);
        int updated = scheduleMapper.update(null, new LambdaUpdateWrapper<Schedule>()
                .eq(Schedule::getId, schedule.getId())
                .lt(Schedule::getRemainCount, schedule.getTotalCount())
                .setSql("remain_count = remain_count + 1, status = 1"));
        check(updated == 1, "排班号源恢复失败");
        createMessage(appointment.getUserId(), "预约已取消", "预约订单 " + appointment.getOrderNo() + " 已取消，号源已释放。");
    }

    @Override
    @Transactional
    public void completeAppointment(String orderNo) {
        Appointment appointment = requireOwnedAppointment(orderNo);
        check(Integer.valueOf(2).equals(appointment.getStatus()), "仅已支付订单可确认完成");
        appointment.setStatus(3);
        appointment.setUpdateTime(LocalDateTime.now());
        appointmentMapper.updateById(appointment);
        createMessage(appointment.getUserId(), "预约已完成", "预约订单 " + appointment.getOrderNo() + " 已确认完成。");
    }

    @Override
    @Transactional
    public Map<String, Object> payAppointment(String orderNo, PayRequest request) {
        Appointment appointment = requireOwnedAppointment(orderNo);
        check(Integer.valueOf(1).equals(appointment.getStatus()), "当前订单不可支付");
        int payMethod = resolvePayMethod(request == null ? null : request.getPayMethod());
        PaymentFlow paymentFlow = upsertPendingPaymentFlow(orderNo, 1, payMethod, appointment.getAmount());
        log.info("Appointment pay requested. orderNo={}, payMethod={}, amount={}, paymentFlowStatus={}",
                orderNo, paymentMethodName(payMethod), appointment.getAmount(), paymentFlow.getPayStatus());

        if (isWechat(payMethod)) {
            updateBusinessPaidState(orderNo, 1);
            markPaymentSuccess(paymentFlow, nextOrderNo("WX"), "wechat-success");
            return buildWechatPayResult(orderNo, paymentFlow, "/reservation/success/" + orderNo);
        }

        String formHtml = alipayService.createPageForm(
                orderNo,
                appointment.getAmount(),
                "预约挂号订单-" + orderNo,
                "预约挂号支付"
        );
        return buildAlipayPayResult(orderNo, paymentFlow, formHtml);
    }

    @Override
    public PageResult<Map<String, Object>> listMyAppointments(Integer page, Integer pageSize, Integer status) {
        Long userId = requireUserId();
        check(status == null || (status >= 1 && status <= 4), "预约状态不支持");
        List<Map<String, Object>> records = appointmentMapper.selectList(new LambdaQueryWrapper<Appointment>()
                        .eq(Appointment::getUserId, userId)
                        .eq(status != null, Appointment::getStatus, status)
                        .orderByDesc(Appointment::getCreateTime))
                .stream().map(item -> {
                    Map<String, Object> result = new LinkedHashMap<String, Object>();
                    result.put("id", item.getId());
                    result.put("orderNo", item.getOrderNo());
                    result.put("doctorId", item.getDoctorId());
                    result.put("hospitalId", item.getHospitalId());
                    Doctor doctor = doctorMapper.selectById(item.getDoctorId());
                    com.whlg.hospital.entity.Hospital hospital = hospitalMapper.selectById(item.getHospitalId());
                    result.put("doctorName", doctor == null ? "医生已下线" : doctor.getName());
                    result.put("hospitalName", hospital == null ? "医院已下线" : hospital.getName());
                    result.put("appointmentDate", item.getAppointmentDate());
                    result.put("appointmentTime", item.getAppointmentTime());
                    result.put("patientName", item.getPatientName());
                    result.put("amount", item.getAmount());
                    result.put("status", item.getStatus());
                    return result;
                }).collect(Collectors.toList());
        return paginate(records, page, pageSize);
    }

    @Override
    @Transactional
    public Map<String, Object> createConsult(CreateConsultRequest request) {
        check(request != null && request.getDoctorId() != null
                && request.getPatientName() != null && !request.getPatientName().trim().isEmpty()
                && request.getPatientPhone() != null && !request.getPatientPhone().trim().isEmpty()
                && request.getAppointmentTime() != null && request.getDuration() != null, "咨询参数不能为空");
        check(request.getDuration() > 0 && request.getDuration() <= 180, "咨询时长不正确");
        check(!request.getAppointmentTime().isBefore(LocalDateTime.now()), "咨询时间不能早于当前时间");
        Long userId = requireUserId();
        Doctor doctor = doctorMapper.selectById(request.getDoctorId());
        check(doctor != null && Integer.valueOf(1).equals(doctor.getStatus()), "医生不可咨询");
        check(doctor.getPrice() != null && doctor.getPrice().compareTo(BigDecimal.ZERO) >= 0, "咨询价格异常");
        Schedule schedule = resolveConsultSchedule(request, doctor);
        int updated = scheduleMapper.update(null, new LambdaUpdateWrapper<Schedule>()
                .eq(Schedule::getId, schedule.getId())
                .eq(Schedule::getStatus, 1)
                .gt(Schedule::getRemainCount, 0)
                .setSql("remain_count = remain_count - 1, status = CASE WHEN remain_count <= 1 THEN 2 ELSE 1 END"));
        check(updated == 1, "咨询号源不足");

        Consult consult = new Consult();
        consult.setOrderNo(nextOrderNo("CO"));
        consult.setUserId(userId);
        consult.setDoctorId(doctor.getId());
        consult.setPatientName(request.getPatientName().trim());
        consult.setPatientPhone(request.getPatientPhone().trim());
        consult.setDiseaseDesc(request.getDiseaseDesc());
        consult.setAppointmentTime(request.getAppointmentTime());
        consult.setDuration(request.getDuration());
        consult.setAmount(doctor.getPrice());
        consult.setStatus(1);
        consult.setCreateTime(LocalDateTime.now());
        consult.setUpdateTime(LocalDateTime.now());
        consultMapper.insert(consult);
        doctorMapper.update(null, new LambdaUpdateWrapper<Doctor>()
                .eq(Doctor::getId, doctor.getId())
                .setSql("consult_count = COALESCE(consult_count, 0) + 1"));
        createMessage(userId, "咨询已创建", "咨询订单 " + consult.getOrderNo() + " 已创建。");

        Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("orderNo", consult.getOrderNo());
        return result;
    }

    @Override
    @Transactional
    public Map<String, Object> getConsult(String orderNo) {
        Consult consult = requireOwnedConsult(orderNo);
        expireConsultIfDue(consult);
        Doctor doctor = doctorMapper.selectById(consult.getDoctorId());
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("orderNo", consult.getOrderNo());
        result.put("doctor", doctor == null ? "医生已下线" : doctor.getName());
        result.put("patientName", consult.getPatientName());
        result.put("status", consult.getStatus());
        result.put("fee", consult.getAmount());
        result.put("duration", consult.getDuration());
        result.put("appointmentTime", consult.getAppointmentTime());
        result.put("createTime", consult.getCreateTime());
        return result;
    }

    @Override
    @Transactional
    public void cancelConsult(String orderNo) {
        Consult consult = requireOwnedConsult(orderNo);
        expireConsultIfDue(consult);
        check(Integer.valueOf(1).equals(consult.getStatus()), "当前咨询不可取消");
        consult.setStatus(5);
        consult.setUpdateTime(LocalDateTime.now());
        consultMapper.updateById(consult);
        check(restoreConsultSchedule(consult), "咨询号源恢复失败");
        createMessage(consult.getUserId(), "咨询已取消", "咨询订单 " + consult.getOrderNo() + " 已取消。");
    }

    @Override
    @Transactional
    public Map<String, Object> payConsult(String orderNo, PayRequest request) {
        Consult consult = requireOwnedConsult(orderNo);
        expireConsultIfDue(consult);
        check(Integer.valueOf(1).equals(consult.getStatus()), "当前咨询不可支付");
        int payMethod = resolvePayMethod(request == null ? null : request.getPayMethod());
        PaymentFlow paymentFlow = upsertPendingPaymentFlow(orderNo, 2, payMethod, consult.getAmount());
        log.info("Consult pay requested. orderNo={}, payMethod={}, amount={}, paymentFlowStatus={}",
                orderNo, paymentMethodName(payMethod), consult.getAmount(), paymentFlow.getPayStatus());

        if (isWechat(payMethod)) {
            updateBusinessPaidState(orderNo, 2);
            markPaymentSuccess(paymentFlow, nextOrderNo("WX"), "wechat-success");
            return buildWechatPayResult(orderNo, paymentFlow, "/consult/success/" + orderNo);
        }

        String formHtml = alipayService.createPageForm(
                orderNo,
                consult.getAmount(),
                "在线咨询订单-" + orderNo,
                "在线咨询支付"
        );
        return buildAlipayPayResult(orderNo, paymentFlow, formHtml);
    }

    @Override
    @Transactional
    public PageResult<Map<String, Object>> listMyConsults(Integer page, Integer pageSize, Integer status) {
        expirePastPendingConsults();
        Long userId = requireUserId();
        check(status == null || (status >= 1 && status <= 6), "咨询状态不支持");
        List<Map<String, Object>> records = consultMapper.selectList(new LambdaQueryWrapper<Consult>()
                        .eq(Consult::getUserId, userId)
                        .eq(status != null, Consult::getStatus, status)
                        .orderByDesc(Consult::getCreateTime))
                .stream().map(item -> {
                    Map<String, Object> result = new LinkedHashMap<String, Object>();
                    result.put("id", item.getId());
                    result.put("orderNo", item.getOrderNo());
                    result.put("doctorId", item.getDoctorId());
                    Doctor doctor = doctorMapper.selectById(item.getDoctorId());
                    result.put("doctorName", doctor == null ? "医生已下线" : doctor.getName());
                    result.put("patientName", item.getPatientName());
                    result.put("fee", item.getAmount());
                    result.put("duration", item.getDuration());
                    result.put("appointmentTime", item.getAppointmentTime());
                    result.put("status", item.getStatus());
                    return result;
                }).collect(Collectors.toList());
        return paginate(records, page, pageSize);
    }

    @Scheduled(fixedDelay = 60000)
    @Transactional
    public void expirePastPendingConsults() {
        List<Consult> pendingConsults = consultMapper.selectList(new LambdaQueryWrapper<Consult>()
                .eq(Consult::getStatus, 1)
                .le(Consult::getAppointmentTime, LocalDateTime.now()));
        pendingConsults.forEach(this::expireConsultIfDue);
    }

    @Override
    public Map<String, Object> createPayment(CreatePaymentRequest request) {
        check(request != null, "请求体不能为空");
        check(request.getBusinessOrderNo() != null && !request.getBusinessOrderNo().trim().isEmpty(), "业务订单号不能为空");
        check(request.getActualAmount() != null && request.getActualAmount().compareTo(BigDecimal.ZERO) > 0, "支付金额必须大于0");
        int businessType = resolveBusinessType(request.getBusinessType());
        int payMethod = resolvePayMethod(request.getPayMethod());
        ensureBusinessOwner(request.getBusinessOrderNo(), businessType);
        BigDecimal businessAmount = getBusinessAmount(request.getBusinessOrderNo(), businessType);
        check(businessAmount.compareTo(request.getActualAmount()) == 0, "支付金额不匹配");

        PaymentFlow paymentFlow = upsertPendingPaymentFlow(request.getBusinessOrderNo(), businessType, payMethod, businessAmount);
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("paymentNo", paymentFlow.getBusinessOrderNo());
        result.put("businessOrderNo", paymentFlow.getBusinessOrderNo());
        result.put("payMethod", paymentMethodName(paymentFlow.getPayMethod()));
        result.put("payStatus", paymentFlow.getPayStatus());
        return result;
    }

    @Override
    @Transactional
    public void paymentCallback(PaymentCallbackRequest request) {
        check(request != null && request.getPaymentNo() != null && request.getTradeNo() != null
                && request.getPayStatus() != null, "支付回调参数不完整");
        check(request.getPayStatus() >= 0 && request.getPayStatus() <= 3, "支付状态不支持");
        log.info("Payment callback received. paymentNo={}, tradeNo={}, payStatus={}",
                request.getPaymentNo(), request.getTradeNo(), request.getPayStatus());

        PaymentFlow paymentFlow = paymentFlowMapper.selectOne(new LambdaQueryWrapper<PaymentFlow>()
                .eq(PaymentFlow::getBusinessOrderNo, request.getPaymentNo()));
        check(paymentFlow != null, "支付流水不存在");
        log.info("Payment flow loaded. paymentNo={}, businessType={}, payMethod={}, currentStatus={}",
                paymentFlow.getBusinessOrderNo(), paymentFlow.getBusinessType(), paymentFlow.getPayMethod(), paymentFlow.getPayStatus());

        if (Integer.valueOf(1).equals(paymentFlow.getPayStatus())) {
            log.info("Payment callback ignored because flow already paid. paymentNo={}, tradeNo={}",
                    request.getPaymentNo(), request.getTradeNo());
            return;
        }

        if (Integer.valueOf(1).equals(request.getPayStatus())) {
            updateBusinessPaidState(paymentFlow.getBusinessOrderNo(), paymentFlow.getBusinessType());
            markPaymentSuccess(paymentFlow, request.getTradeNo(), "callback-success");
            log.info("Payment callback marked success. paymentNo={}, tradeNo={}, businessType={}",
                    request.getPaymentNo(), request.getTradeNo(), paymentFlow.getBusinessType());
            return;
        }

        paymentFlow.setPayStatus(request.getPayStatus());
        paymentFlow.setOriginalCallback("callback-non-success");
        paymentFlow.setUpdateTime(LocalDateTime.now());
        paymentFlowMapper.updateById(paymentFlow);
        log.info("Payment callback saved non-success status. paymentNo={}, payStatus={}",
                request.getPaymentNo(), request.getPayStatus());
    }

    @Override
    public Map<String, Object> getPayment(String businessOrderNo) {
        PaymentFlow paymentFlow = paymentFlowMapper.selectOne(new LambdaQueryWrapper<PaymentFlow>().eq(PaymentFlow::getBusinessOrderNo, businessOrderNo));
        if (paymentFlow == null) {
            throw new ApiException(StatusCode.NOT_FOUND, "支付流水不存在");
        }
        ensureBusinessOwner(businessOrderNo, paymentFlow.getBusinessType());
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("paymentNo", paymentFlow.getBusinessOrderNo());
        result.put("businessOrderNo", paymentFlow.getBusinessOrderNo());
        result.put("actualAmount", paymentFlow.getActualAmount());
        result.put("payMethod", paymentMethodName(paymentFlow.getPayMethod()));
        result.put("payStatus", paymentFlow.getPayStatus());
        result.put("payTime", paymentFlow.getPaySuccessTime());
        return result;
    }

    private void savePaymentFlow(PaymentFlow paymentFlow) {
        if (paymentFlow.getId() == null) {
            paymentFlowMapper.insert(paymentFlow);
        } else {
            paymentFlowMapper.updateById(paymentFlow);
        }
    }

    private PaymentFlow upsertPendingPaymentFlow(String businessOrderNo, Integer businessType, Integer payMethod, BigDecimal amount) {
        PaymentFlow paymentFlow = paymentFlowMapper.selectOne(new LambdaQueryWrapper<PaymentFlow>()
                .eq(PaymentFlow::getBusinessOrderNo, businessOrderNo));
        if (paymentFlow == null) {
            paymentFlow = new PaymentFlow();
            paymentFlow.setBusinessOrderNo(businessOrderNo);
            paymentFlow.setBusinessType(businessType);
            paymentFlow.setCreateTime(LocalDateTime.now());
        }
        paymentFlow.setPayMethod(payMethod);
        paymentFlow.setActualAmount(amount);
        paymentFlow.setPayStatus(0);
        paymentFlow.setPaySuccessTime(null);
        paymentFlow.setOriginalCallback(null);
        paymentFlow.setUpdateTime(LocalDateTime.now());
        savePaymentFlow(paymentFlow);
        log.info("Pending payment flow upserted. orderNo={}, businessType={}, payMethod={}, amount={}, payStatus={}",
                businessOrderNo, businessType, payMethod, amount, paymentFlow.getPayStatus());
        return paymentFlow;
    }

    private void markPaymentSuccess(PaymentFlow paymentFlow, String tradeNo, String callbackFlag) {
        paymentFlow.setThirdPartyTradeNo(tradeNo);
        paymentFlow.setPayStatus(1);
        paymentFlow.setPaySuccessTime(LocalDateTime.now());
        paymentFlow.setOriginalCallback(callbackFlag);
        paymentFlow.setUpdateTime(LocalDateTime.now());
        paymentFlowMapper.updateById(paymentFlow);
        log.info("Payment flow marked success. orderNo={}, tradeNo={}, callbackFlag={}",
                paymentFlow.getBusinessOrderNo(), tradeNo, callbackFlag);
    }

    private Map<String, Object> buildWechatPayResult(String orderNo, PaymentFlow paymentFlow, String successPath) {
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("businessOrderNo", orderNo);
        result.put("paymentNo", paymentFlow.getBusinessOrderNo());
        result.put("payMethod", "wechat");
        result.put("payStatus", 1);
        result.put("redirectUrl", successPath);
        result.put("success", true);
        return result;
    }

    private Map<String, Object> buildAlipayPayResult(String orderNo, PaymentFlow paymentFlow, String formHtml) {
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("businessOrderNo", orderNo);
        result.put("paymentNo", paymentFlow.getBusinessOrderNo());
        result.put("payMethod", "alipay");
        result.put("payStatus", 0);
        result.put("formHtml", formHtml);
        result.put("success", true);
        return result;
    }

    private Appointment requireOwnedAppointment(String orderNo) {
        Appointment appointment = appointmentMapper.selectOne(new LambdaQueryWrapper<Appointment>().eq(Appointment::getOrderNo, orderNo));
        if (appointment == null) {
            throw new ApiException(StatusCode.NOT_FOUND, "挂号订单不存在");
        }
        ensureOrderOwner(appointment.getUserId());
        return appointment;
    }

    private Consult requireOwnedConsult(String orderNo) {
        Consult consult = consultMapper.selectOne(new LambdaQueryWrapper<Consult>().eq(Consult::getOrderNo, orderNo));
        if (consult == null) {
            throw new ApiException(StatusCode.NOT_FOUND, "咨询订单不存在");
        }
        ensureOrderOwner(consult.getUserId());
        return consult;
    }

    private void ensureBusinessOwner(String businessOrderNo, Integer businessType) {
        if (Integer.valueOf(1).equals(businessType)) {
            Appointment appointment = appointmentMapper.selectOne(new LambdaQueryWrapper<Appointment>().eq(Appointment::getOrderNo, businessOrderNo));
            check(appointment != null, "挂号订单不存在");
            ensureOrderOwner(appointment.getUserId());
            return;
        }
        if (Integer.valueOf(2).equals(businessType)) {
            Consult consult = consultMapper.selectOne(new LambdaQueryWrapper<Consult>().eq(Consult::getOrderNo, businessOrderNo));
            check(consult != null, "咨询订单不存在");
            ensureOrderOwner(consult.getUserId());
            return;
        }
        throw new ApiException(StatusCode.BAD_REQUEST, "业务类型不支持");
    }

    private void createMessage(Long userId, String title, String content) {
        Message message = new Message();
        message.setUserId(userId);
        message.setTitle(title);
        message.setContent(content);
        message.setIsRead(0);
        message.setCreateTime(LocalDateTime.now());
        messageMapper.insert(message);
    }

    private BigDecimal getBusinessAmount(String businessOrderNo, Integer businessType) {
        if (Integer.valueOf(1).equals(businessType)) {
            return appointmentMapper.selectOne(new LambdaQueryWrapper<Appointment>().eq(Appointment::getOrderNo, businessOrderNo)).getAmount();
        }
        return consultMapper.selectOne(new LambdaQueryWrapper<Consult>().eq(Consult::getOrderNo, businessOrderNo)).getAmount();
    }

    private void updateBusinessPaidState(String businessOrderNo, Integer businessType) {
        if (Integer.valueOf(1).equals(businessType)) {
            Appointment appointment = appointmentMapper.selectOne(new LambdaQueryWrapper<Appointment>().eq(Appointment::getOrderNo, businessOrderNo));
            check(appointment != null && Integer.valueOf(1).equals(appointment.getStatus()), "挂号订单状态不可支付");
            appointment.setStatus(2);
            appointment.setPayTime(LocalDateTime.now());
            appointment.setUpdateTime(LocalDateTime.now());
            appointmentMapper.updateById(appointment);
            log.info("Appointment marked paid. orderNo={}, status={}, payTime={}",
                    businessOrderNo, appointment.getStatus(), appointment.getPayTime());
        } else {
            Consult consult = consultMapper.selectOne(new LambdaQueryWrapper<Consult>().eq(Consult::getOrderNo, businessOrderNo));
            check(consult != null && Integer.valueOf(1).equals(consult.getStatus()), "咨询订单状态不可支付");
            consult.setStatus(2);
            consult.setPayTime(LocalDateTime.now());
            consult.setUpdateTime(LocalDateTime.now());
            consultMapper.updateById(consult);
            log.info("Consult marked paid. orderNo={}, status={}, payTime={}",
                    businessOrderNo, consult.getStatus(), consult.getPayTime());
        }
    }

    private void ensureOrderOwner(Long ownerUserId) {
        if (!requireUserId().equals(ownerUserId)) {
            throw new ApiException(StatusCode.FORBIDDEN, "无权限访问该订单");
        }
    }

    private int resolveBusinessType(String businessType) {
        if ("appointment".equals(businessType)) {
            return 1;
        }
        if ("consult".equals(businessType)) {
            return 2;
        }
        throw new ApiException(StatusCode.BAD_REQUEST, "业务类型不支持");
    }

    private int resolvePayMethod(String payMethod) {
        if ("wechat".equalsIgnoreCase(payMethod)) {
            return 1;
        }
        if ("alipay".equalsIgnoreCase(payMethod)) {
            return 2;
        }
        throw new ApiException(StatusCode.BAD_REQUEST, "支付方式不支持");
    }

    private boolean isWechat(Integer payMethod) {
        return Integer.valueOf(1).equals(payMethod);
    }

    private String paymentMethodName(Integer payMethod) {
        return Integer.valueOf(1).equals(payMethod) ? "wechat" : "alipay";
    }

    private FamilyMember resolveFamilyMember(CreateAppointmentRequest request, Long patientId, Long userId) {
        FamilyMember familyMember = familyMemberMapper.selectById(patientId);
        if (familyMember != null && userId.equals(familyMember.getUserId())) {
            return familyMember;
        }

        LambdaQueryWrapper<FamilyMember> query = new LambdaQueryWrapper<FamilyMember>()
                .eq(FamilyMember::getUserId, userId);
        if (notBlank(request.getPatientPhone())) {
            query.eq(FamilyMember::getPhone, request.getPatientPhone().trim());
        } else if (notBlank(request.getPatientIdCard())) {
            query.eq(FamilyMember::getIdCard, request.getPatientIdCard().trim());
        } else if (notBlank(request.getPatientName())) {
            query.eq(FamilyMember::getName, request.getPatientName().trim());
        } else {
            return null;
        }

        familyMember = familyMemberMapper.selectOne(query);
        if (familyMember != null) {
            return familyMember;
        }

        FamilyMember created = new FamilyMember();
        created.setUserId(userId);
        created.setName(request.getPatientName());
        created.setPhone(request.getPatientPhone());
        created.setIdCard(request.getPatientIdCard());
        created.setGender(request.getPatientGender());
        created.setBirthday(request.getPatientBirthday());
        created.setRelation(notBlank(request.getPatientRelation()) ? request.getPatientRelation() : "本人");
        created.setIsDefault(0);
        created.setCreateTime(LocalDateTime.now());
        created.setUpdateTime(LocalDateTime.now());
        familyMemberMapper.insert(created);
        return created;
    }

    private boolean notBlank(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private Schedule resolveSchedule(CreateAppointmentRequest request, Doctor doctor) {
        List<Schedule> schedules = scheduleMapper.selectList(new LambdaQueryWrapper<Schedule>()
                .eq(Schedule::getDoctorId, request.getDoctorId())
                .eq(Schedule::getHospitalId, request.getHospitalId())
                .eq(Schedule::getScheduleDate, request.getAppointmentDate())
                .eq(Schedule::getStatus, 1)
                .gt(Schedule::getRemainCount, 0)
                .orderByAsc(Schedule::getTimeSlot));
        String requestedTime = request.getAppointmentTime().trim();
        Schedule matched = schedules.stream().filter(item -> requestedTime.equals(item.getTimeSlot())).findFirst().orElse(null);
        if (matched == null && ("上午".equals(requestedTime) || "下午".equals(requestedTime) || "晚上".equals(requestedTime))) {
            matched = schedules.stream().filter(item -> matchesPeriod(item.getTimeSlot(), requestedTime)).findFirst().orElse(null);
        }
        check(matched != null, "排班不存在");
        return matched;
    }

    private Schedule resolveConsultSchedule(CreateConsultRequest request, Doctor doctor) {
        check(request.getAppointmentTime().getSecond() == 0 && request.getAppointmentTime().getNano() == 0,
                "咨询时间必须选择完整排班");
        String requestedStart = request.getAppointmentTime().toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm"));
        Schedule schedule = scheduleMapper.selectList(new LambdaQueryWrapper<Schedule>()
                        .eq(Schedule::getDoctorId, doctor.getId())
                        .eq(Schedule::getHospitalId, doctor.getHospitalId())
                        .eq(Schedule::getScheduleDate, request.getAppointmentTime().toLocalDate())
                        .eq(Schedule::getStatus, 1)
                        .gt(Schedule::getRemainCount, 0))
                .stream()
                .filter(item -> item.getTimeSlot() != null && item.getTimeSlot().startsWith(requestedStart + "-"))
                .findFirst()
                .orElse(null);
        check(schedule != null, "咨询时间不在医生可预约排班内");
        return schedule;
    }

    private Schedule findConsultSchedule(Consult consult) {
        String requestedStart = consult.getAppointmentTime().toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm"));
        return scheduleMapper.selectList(new LambdaQueryWrapper<Schedule>()
                        .eq(Schedule::getDoctorId, consult.getDoctorId())
                        .eq(Schedule::getScheduleDate, consult.getAppointmentTime().toLocalDate()))
                .stream()
                .filter(item -> item.getTimeSlot() != null && item.getTimeSlot().startsWith(requestedStart + "-"))
                .findFirst()
                .orElse(null);
    }

    private boolean restoreConsultSchedule(Consult consult) {
        Schedule schedule = findConsultSchedule(consult);
        if (schedule == null || schedule.getTotalCount() == null || schedule.getRemainCount() == null) {
            return false;
        }
        int updated = scheduleMapper.update(null, new LambdaUpdateWrapper<Schedule>()
                .eq(Schedule::getId, schedule.getId())
                .lt(Schedule::getRemainCount, schedule.getTotalCount())
                .setSql("remain_count = remain_count + 1, status = 1"));
        return updated == 1;
    }

    private void expireConsultIfDue(Consult consult) {
        if (!Integer.valueOf(1).equals(consult.getStatus())
                || consult.getAppointmentTime() == null
                || consult.getAppointmentTime().isAfter(LocalDateTime.now())) {
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        int updated = consultMapper.update(null, new LambdaUpdateWrapper<Consult>()
                .eq(Consult::getId, consult.getId())
                .eq(Consult::getStatus, 1)
                .le(Consult::getAppointmentTime, now)
                .set(Consult::getStatus, 6)
                .set(Consult::getUpdateTime, now));
        if (updated != 1) {
            return;
        }
        consult.setStatus(6);
        consult.setUpdateTime(now);
        if (!restoreConsultSchedule(consult)) {
            log.warn("Expired consult schedule could not be released. orderNo={}", consult.getOrderNo());
        }
        paymentFlowMapper.update(null, new LambdaUpdateWrapper<PaymentFlow>()
                .eq(PaymentFlow::getBusinessOrderNo, consult.getOrderNo())
                .eq(PaymentFlow::getBusinessType, 2)
                .eq(PaymentFlow::getPayStatus, 0)
                .set(PaymentFlow::getPayStatus, 3)
                .set(PaymentFlow::getUpdateTime, now));
        createMessage(consult.getUserId(), "咨询订单已过期", "咨询订单 " + consult.getOrderNo() + " 已超过预约开始时间，已自动关闭。");
    }

    private boolean matchesPeriod(String timeSlot, String period) {
        if (timeSlot == null || timeSlot.length() < 2) {
            return false;
        }
        try {
            int hour = Integer.parseInt(timeSlot.substring(0, 2));
            return "上午".equals(period) ? hour < 12 : "下午".equals(period) ? hour >= 12 && hour < 18 : hour >= 18;
        } catch (NumberFormatException ignored) {
            return timeSlot.contains(period);
        }
    }

    private boolean isScheduleTimeAvailable(Schedule schedule) {
        LocalDate scheduleDate = schedule.getScheduleDate();
        LocalDate today = LocalDate.now();
        if (scheduleDate.isBefore(today)) {
            return false;
        }
        if (scheduleDate.isAfter(today)) {
            return true;
        }
        LocalTime endTime = parseScheduleEndTime(schedule.getTimeSlot());
        return endTime == null || endTime.isAfter(LocalTime.now());
    }

    private LocalTime parseScheduleEndTime(String timeSlot) {
        if (timeSlot == null || timeSlot.trim().isEmpty()) {
            return null;
        }
        Matcher matcher = Pattern.compile("(\\d{1,2}):(\\d{2})").matcher(timeSlot);
        LocalTime endTime = null;
        while (matcher.find()) {
            try {
                endTime = LocalTime.of(Integer.parseInt(matcher.group(1)), Integer.parseInt(matcher.group(2)));
            } catch (RuntimeException ignored) {
                return null;
            }
        }
        return endTime;
    }

    private String nextOrderNo(String prefix) {
        return prefix + System.currentTimeMillis() + java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 8);
    }
}
