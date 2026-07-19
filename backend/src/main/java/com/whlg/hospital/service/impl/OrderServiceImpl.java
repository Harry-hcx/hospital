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
import com.whlg.hospital.service.OrderService;
import com.whlg.hospital.support.ApiException;
import com.whlg.hospital.util.StatusCode;
import com.whlg.hospital.vo.PageResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.Period;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl extends ServiceSupport implements OrderService {

    private final AppointmentMapper appointmentMapper;
    private final ConsultMapper consultMapper;
    private final DoctorMapper doctorMapper;
    private final FamilyMemberMapper familyMemberMapper;
    private final HospitalMapper hospitalMapper;
    private final MessageMapper messageMapper;
    private final PaymentFlowMapper paymentFlowMapper;
    private final ScheduleMapper scheduleMapper;

    public OrderServiceImpl(AppointmentMapper appointmentMapper,
                            ConsultMapper consultMapper,
                            DoctorMapper doctorMapper,
                            FamilyMemberMapper familyMemberMapper,
                            HospitalMapper hospitalMapper,
                            MessageMapper messageMapper,
                            PaymentFlowMapper paymentFlowMapper,
                            ScheduleMapper scheduleMapper) {
        this.appointmentMapper = appointmentMapper;
        this.consultMapper = consultMapper;
        this.doctorMapper = doctorMapper;
        this.familyMemberMapper = familyMemberMapper;
        this.hospitalMapper = hospitalMapper;
        this.messageMapper = messageMapper;
        this.paymentFlowMapper = paymentFlowMapper;
        this.scheduleMapper = scheduleMapper;
    }

    @Override
    @Transactional
    public Map<String, Object> createAppointment(CreateAppointmentRequest request) {
        check(request != null && request.getScheduleId() != null && request.getFamilyMemberId() != null, "预约参数不能为空");
        Long userId = requireUserId();
        Schedule schedule = scheduleMapper.selectById(request.getScheduleId());
        FamilyMember familyMember = familyMemberMapper.selectById(request.getFamilyMemberId());
        check(schedule != null, "排班不存在");
        check(familyMember != null && userId.equals(familyMember.getUserId()), "就诊人不存在");
        check(Integer.valueOf(1).equals(schedule.getStatus())
                && schedule.getScheduleDate() != null
                && !schedule.getScheduleDate().isBefore(LocalDate.now()), "排班不可预约");
        Doctor doctor = doctorMapper.selectById(schedule.getDoctorId());
        check(doctor != null && Integer.valueOf(1).equals(doctor.getStatus()), "医生不可预约");
        check(doctor.getHospitalId() != null && doctor.getHospitalId().equals(schedule.getHospitalId())
                && doctor.getDepartmentId() != null && doctor.getDepartmentId().equals(schedule.getDepartmentId()),
                "排班归属不一致");
        check(schedule.getRemainCount() != null && schedule.getRemainCount() > 0
                && schedule.getTotalCount() != null && schedule.getTotalCount() > 0
                && schedule.getRemainCount() <= schedule.getTotalCount(), "号源数据异常");
        Long existing = appointmentMapper.selectCount(new LambdaQueryWrapper<Appointment>()
                .eq(Appointment::getUserId, userId)
                .eq(Appointment::getScheduleId, schedule.getId())
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
        appointment.setScheduleId(schedule.getId());
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
        return result;
    }

    @Override
    public Map<String, Object> getAppointment(String orderNo) {
        Appointment appointment = requireOwnedAppointment(orderNo);
        Doctor doctor = doctorMapper.selectById(appointment.getDoctorId());
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("orderNo", appointment.getOrderNo());
        result.put("doctor", doctor.getName());
        result.put("hospital", hospitalMapper.selectById(appointment.getHospitalId()).getName());
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
        Schedule schedule = appointment.getScheduleId() == null ? scheduleMapper.selectOne(new LambdaQueryWrapper<Schedule>()
                .eq(Schedule::getDoctorId, appointment.getDoctorId())
                .eq(Schedule::getScheduleDate, appointment.getAppointmentDate())
                .eq(Schedule::getTimeSlot, appointment.getAppointmentTime())) : scheduleMapper.selectById(appointment.getScheduleId());
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
    public Map<String, Object> payAppointment(String orderNo, PayRequest request) {
        Appointment appointment = requireOwnedAppointment(orderNo);
        check(Integer.valueOf(1).equals(appointment.getStatus()), "当前订单不可支付");
        validatePayType(request == null ? null : request.getPayType());
        appointment.setStatus(2);
        appointment.setPayTime(LocalDateTime.now());
        appointment.setUpdateTime(LocalDateTime.now());
        appointmentMapper.updateById(appointment);

        PaymentFlow paymentFlow = paymentFlowMapper.selectOne(new LambdaQueryWrapper<PaymentFlow>().eq(PaymentFlow::getBusinessOrderNo, orderNo));
        if (paymentFlow == null) {
            paymentFlow = new PaymentFlow();
            paymentFlow.setBusinessOrderNo(orderNo);
            paymentFlow.setBusinessType(1);
            paymentFlow.setActualAmount(appointment.getAmount());
            paymentFlow.setCreateTime(LocalDateTime.now());
        }
        paymentFlow.setPayMethod(request.getPayType());
        paymentFlow.setThirdPartyTradeNo(nextOrderNo("TP"));
        paymentFlow.setPayStatus(1);
        paymentFlow.setPaySuccessTime(LocalDateTime.now());
        paymentFlow.setUpdateTime(LocalDateTime.now());
        savePaymentFlow(paymentFlow);

        Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("success", true);
        return result;
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
                    result.put("scheduleId", item.getScheduleId());
                    result.put("doctorName", doctorMapper.selectById(item.getDoctorId()).getName());
                    result.put("hospitalName", hospitalMapper.selectById(item.getHospitalId()).getName());
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
        check(request != null && request.getDoctorId() != null && request.getFamilyMemberId() != null, "咨询参数不能为空");
        Long userId = requireUserId();
        Doctor doctor = doctorMapper.selectById(request.getDoctorId());
        FamilyMember familyMember = familyMemberMapper.selectById(request.getFamilyMemberId());
        check(doctor != null && Integer.valueOf(1).equals(doctor.getStatus()), "医生不可咨询");
        check(familyMember != null && userId.equals(familyMember.getUserId()), "就诊人不存在");
        check(doctor.getPrice() != null && doctor.getPrice().compareTo(BigDecimal.ZERO) >= 0, "咨询价格异常");

        Consult consult = new Consult();
        consult.setOrderNo(nextOrderNo("CO"));
        consult.setUserId(userId);
        consult.setDoctorId(doctor.getId());
        consult.setPatientName(familyMember.getName());
        consult.setPatientPhone(familyMember.getPhone());
        consult.setDiseaseDesc(request.getDiseaseDesc());
        consult.setAppointmentTime(LocalDateTime.now().plusDays(1));
        consult.setDuration(15);
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
    public Map<String, Object> getConsult(String orderNo) {
        Consult consult = requireOwnedConsult(orderNo);
        Doctor doctor = doctorMapper.selectById(consult.getDoctorId());
        check(doctor != null, "医生不存在");
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("orderNo", consult.getOrderNo());
        result.put("doctor", doctor.getName());
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
        check(Integer.valueOf(1).equals(consult.getStatus()), "当前咨询不可取消");
        consult.setStatus(5);
        consult.setUpdateTime(LocalDateTime.now());
        consultMapper.updateById(consult);
        createMessage(consult.getUserId(), "咨询已取消", "咨询订单 " + consult.getOrderNo() + " 已取消。");
    }

    @Override
    public Map<String, Object> payConsult(String orderNo, PayRequest request) {
        Consult consult = requireOwnedConsult(orderNo);
        check(Integer.valueOf(1).equals(consult.getStatus()), "当前咨询不可支付");
        validatePayType(request == null ? null : request.getPayType());
        consult.setStatus(2);
        consult.setPayTime(LocalDateTime.now());
        consult.setUpdateTime(LocalDateTime.now());
        consultMapper.updateById(consult);

        PaymentFlow paymentFlow = paymentFlowMapper.selectOne(new LambdaQueryWrapper<PaymentFlow>().eq(PaymentFlow::getBusinessOrderNo, orderNo));
        if (paymentFlow == null) {
            paymentFlow = new PaymentFlow();
            paymentFlow.setBusinessOrderNo(orderNo);
            paymentFlow.setBusinessType(2);
            paymentFlow.setActualAmount(consult.getAmount());
            paymentFlow.setCreateTime(LocalDateTime.now());
        }
        paymentFlow.setPayMethod(request.getPayType());
        paymentFlow.setThirdPartyTradeNo(nextOrderNo("TP"));
        paymentFlow.setPayStatus(1);
        paymentFlow.setPaySuccessTime(LocalDateTime.now());
        paymentFlow.setUpdateTime(LocalDateTime.now());
        savePaymentFlow(paymentFlow);

        Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("success", true);
        return result;
    }

    @Override
    public PageResult<Map<String, Object>> listMyConsults(Integer page, Integer pageSize, Integer status) {
        Long userId = requireUserId();
        check(status == null || (status >= 1 && status <= 5), "咨询状态不支持");
        List<Map<String, Object>> records = consultMapper.selectList(new LambdaQueryWrapper<Consult>()
                        .eq(Consult::getUserId, userId)
                        .eq(status != null, Consult::getStatus, status)
                        .orderByDesc(Consult::getCreateTime))
                .stream().map(item -> {
                    Map<String, Object> result = new LinkedHashMap<String, Object>();
                    result.put("id", item.getId());
                    result.put("orderNo", item.getOrderNo());
                    result.put("doctorId", item.getDoctorId());
                    result.put("doctorName", doctorMapper.selectById(item.getDoctorId()).getName());
                    result.put("patientName", item.getPatientName());
                    result.put("fee", item.getAmount());
                    result.put("duration", item.getDuration());
                    result.put("appointmentTime", item.getAppointmentTime());
                    result.put("status", item.getStatus());
                    return result;
                }).collect(Collectors.toList());
        return paginate(records, page, pageSize);
    }

    @Override
    public Map<String, Object> createPayment(CreatePaymentRequest request) {
        check(request != null, "请求体不能为空");
        check(request.getBusinessOrderNo() != null && !request.getBusinessOrderNo().trim().isEmpty(), "业务订单号不能为空");
        check(request.getAmount() != null && request.getAmount().compareTo(BigDecimal.ZERO) > 0, "支付金额必须大于0");
        int businessType = resolveBusinessType(request.getBusinessType());
        validatePayType(request.getPayType());
        ensureBusinessOwner(request.getBusinessOrderNo(), businessType);
        BigDecimal businessAmount = getBusinessAmount(request.getBusinessOrderNo(), businessType);
        check(businessAmount.compareTo(request.getAmount()) == 0, "支付金额不匹配");

        PaymentFlow paymentFlow = paymentFlowMapper.selectOne(new LambdaQueryWrapper<PaymentFlow>().eq(PaymentFlow::getBusinessOrderNo, request.getBusinessOrderNo()));
        if (paymentFlow == null) {
            paymentFlow = new PaymentFlow();
            paymentFlow.setBusinessOrderNo(request.getBusinessOrderNo());
            paymentFlow.setBusinessType(businessType);
            paymentFlow.setCreateTime(LocalDateTime.now());
        }
        paymentFlow.setPayMethod(request.getPayType());
        paymentFlow.setActualAmount(businessAmount);
        paymentFlow.setPayStatus(0);
        paymentFlow.setThirdPartyTradeNo(nextOrderNo("PAY"));
        paymentFlow.setUpdateTime(LocalDateTime.now());
        savePaymentFlow(paymentFlow);

        Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("paymentNo", paymentFlow.getThirdPartyTradeNo());
        return result;
    }

    @Override
    public void paymentCallback(PaymentCallbackRequest request) {
        check(request != null && request.getPaymentNo() != null && request.getTradeNo() != null
                && request.getPayStatus() != null, "支付回调参数不完整");
        PaymentFlow paymentFlow = paymentFlowMapper.selectOne(new LambdaQueryWrapper<PaymentFlow>().eq(PaymentFlow::getThirdPartyTradeNo, request.getPaymentNo()));
        check(paymentFlow != null, "支付流水不存在");
        check(request.getPayStatus() >= 0 && request.getPayStatus() <= 3, "支付状态不支持");
        if (Integer.valueOf(1).equals(paymentFlow.getPayStatus())) {
            check(Integer.valueOf(1).equals(request.getPayStatus())
                    && request.getTradeNo().equals(paymentFlow.getThirdPartyTradeNo()), "支付状态不可重复变更");
            return;
        }
        if (Integer.valueOf(1).equals(request.getPayStatus())) {
            updateBusinessPaidState(paymentFlow.getBusinessOrderNo(), paymentFlow.getBusinessType());
        }
        paymentFlow.setThirdPartyTradeNo(request.getTradeNo());
        paymentFlow.setPayStatus(request.getPayStatus());
        paymentFlow.setPaySuccessTime(Integer.valueOf(1).equals(request.getPayStatus()) ? LocalDateTime.now() : null);
        paymentFlow.setOriginalCallback("callback-success");
        paymentFlow.setUpdateTime(LocalDateTime.now());
        paymentFlowMapper.updateById(paymentFlow);
    }

    @Override
    public Map<String, Object> getPayment(String businessOrderNo) {
        PaymentFlow paymentFlow = paymentFlowMapper.selectOne(new LambdaQueryWrapper<PaymentFlow>().eq(PaymentFlow::getBusinessOrderNo, businessOrderNo));
        if (paymentFlow == null) {
            throw new ApiException(StatusCode.NOT_FOUND, "支付流水不存在");
        }
        ensureBusinessOwner(businessOrderNo, paymentFlow.getBusinessType());
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("paymentNo", paymentFlow.getThirdPartyTradeNo());
        result.put("businessOrderNo", paymentFlow.getBusinessOrderNo());
        result.put("amount", paymentFlow.getActualAmount());
        result.put("payType", paymentFlow.getPayMethod());
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
        } else {
            Consult consult = consultMapper.selectOne(new LambdaQueryWrapper<Consult>().eq(Consult::getOrderNo, businessOrderNo));
            check(consult != null && Integer.valueOf(1).equals(consult.getStatus()), "咨询订单状态不可支付");
            consult.setStatus(2);
            consult.setPayTime(LocalDateTime.now());
            consult.setUpdateTime(LocalDateTime.now());
            consultMapper.updateById(consult);
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

    private void validatePayType(Integer payType) {
        check(payType != null && (Integer.valueOf(1).equals(payType) || Integer.valueOf(2).equals(payType)), "支付方式不支持");
    }

    private String nextOrderNo(String prefix) {
        return prefix + System.currentTimeMillis() + java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 8);
    }
}
