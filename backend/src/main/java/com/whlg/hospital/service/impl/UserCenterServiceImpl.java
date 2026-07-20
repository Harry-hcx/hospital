package com.whlg.hospital.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.whlg.hospital.dto.CreateFeedbackRequest;
import com.whlg.hospital.dto.CreateReviewRequest;
import com.whlg.hospital.dto.FamilyMemberRequest;
import com.whlg.hospital.dto.UpdateProfileRequest;
import com.whlg.hospital.entity.FamilyMember;
import com.whlg.hospital.entity.Feedback;
import com.whlg.hospital.entity.Follow;
import com.whlg.hospital.entity.Message;
import com.whlg.hospital.entity.Review;
import com.whlg.hospital.entity.User;
import com.whlg.hospital.mapper.DiseaseMapper;
import com.whlg.hospital.mapper.DoctorMapper;
import com.whlg.hospital.mapper.AppointmentMapper;
import com.whlg.hospital.mapper.ConsultMapper;
import com.whlg.hospital.mapper.FamilyMemberMapper;
import com.whlg.hospital.mapper.FeedbackMapper;
import com.whlg.hospital.mapper.FollowMapper;
import com.whlg.hospital.mapper.HospitalMapper;
import com.whlg.hospital.mapper.MessageMapper;
import com.whlg.hospital.mapper.ReviewMapper;
import com.whlg.hospital.mapper.UserMapper;
import com.whlg.hospital.service.UserCenterService;
import com.whlg.hospital.support.ApiException;
import com.whlg.hospital.util.StatusCode;
import com.whlg.hospital.vo.PageResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.Period;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class UserCenterServiceImpl extends ServiceSupport implements UserCenterService {

    private final UserMapper userMapper;
    private final AppointmentMapper appointmentMapper;
    private final ConsultMapper consultMapper;
    private final FamilyMemberMapper familyMemberMapper;
    private final ReviewMapper reviewMapper;
    private final DoctorMapper doctorMapper;
    private final MessageMapper messageMapper;
    private final FeedbackMapper feedbackMapper;
    private final FollowMapper followMapper;
    private final HospitalMapper hospitalMapper;
    private final DiseaseMapper diseaseMapper;

    public UserCenterServiceImpl(UserMapper userMapper,
                                 AppointmentMapper appointmentMapper,
                                 ConsultMapper consultMapper,
                                 FamilyMemberMapper familyMemberMapper,
                                 ReviewMapper reviewMapper,
                                 DoctorMapper doctorMapper,
                                 MessageMapper messageMapper,
                                 FeedbackMapper feedbackMapper,
                                 FollowMapper followMapper,
                                 HospitalMapper hospitalMapper,
                                 DiseaseMapper diseaseMapper) {
        this.userMapper = userMapper;
        this.appointmentMapper = appointmentMapper;
        this.consultMapper = consultMapper;
        this.familyMemberMapper = familyMemberMapper;
        this.reviewMapper = reviewMapper;
        this.doctorMapper = doctorMapper;
        this.messageMapper = messageMapper;
        this.feedbackMapper = feedbackMapper;
        this.followMapper = followMapper;
        this.hospitalMapper = hospitalMapper;
        this.diseaseMapper = diseaseMapper;
    }

    @Override
    public Map<String, Object> getProfile() {
        User user = userMapper.selectById(requireUserId());
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("id", user.getId());
        result.put("realName", user.getRealName());
        result.put("phone", user.getPhone());
        result.put("email", user.getEmail());
        result.put("avatar", user.getAvatar());
        result.put("gender", user.getGender());
        result.put("birthday", user.getBirthday());
        return result;
    }

    @Override
    public void updateProfile(UpdateProfileRequest request) {
        check(request != null, "请求参数不能为空");
        check(request.getBirthday() == null || !request.getBirthday().isAfter(LocalDate.now()), "生日不能晚于今天");
        check(request.getBirthday() == null || request.getBirthday().isAfter(LocalDate.now().minusYears(120)), "生日不合法");
        check(request.getRealName() == null || request.getRealName().trim().isEmpty()
                || request.getRealName().trim().matches("[\\u4e00-\\u9fa5A-Za-z·]{2,20}"), "姓名格式不正确");
        check(request.getGender() == null || request.getGender() == 1 || request.getGender() == 2, "性别不正确");
        check(request.getEmail() == null || request.getEmail().trim().isEmpty()
                || request.getEmail().trim().matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$"), "邮箱格式不正确");
        check(request.getAvatar() == null || request.getAvatar().trim().length() <= 255, "头像地址过长");
        User user = userMapper.selectById(requireUserId());
        user.setRealName(request.getRealName() == null ? null : request.getRealName().trim());
        user.setGender(request.getGender());
        user.setBirthday(request.getBirthday());
        user.setEmail(request.getEmail() == null || request.getEmail().trim().isEmpty() ? null : request.getEmail().trim());
        user.setAvatar(request.getAvatar() == null ? null : request.getAvatar().trim());
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);
    }

    @Override
    public List<Map<String, Object>> listFamilyMembers() {
        Long userId = requireUserId();
        return familyMemberMapper.selectList(new LambdaQueryWrapper<FamilyMember>().eq(FamilyMember::getUserId, userId)).stream()
                .map(this::familyMemberMap)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public Map<String, Object> createFamilyMember(FamilyMemberRequest request) {
        validateFamilyMember(request);
        Long userId = requireUserId();
        ensureFamilyMemberUnique(userId, request, null);
        if (Integer.valueOf(1).equals(request.getIsDefault())) {
            clearDefaultFamilyMember(userId, null);
        }
        FamilyMember familyMember = new FamilyMember();
        fillFamilyMember(familyMember, request);
        familyMember.setUserId(userId);
        familyMember.setCreateTime(LocalDateTime.now());
        familyMember.setUpdateTime(LocalDateTime.now());
        familyMemberMapper.insert(familyMember);
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("id", String.valueOf(familyMember.getId()));
        return result;
    }

    @Override
    @Transactional
    public void updateFamilyMember(Long id, FamilyMemberRequest request) {
        FamilyMember familyMember = familyMemberMapper.selectById(id);
        check(familyMember != null && requireUserId().equals(familyMember.getUserId()), "就诊人不存在");
        validateFamilyMember(request);
        ensureFamilyMemberUnique(familyMember.getUserId(), request, id);
        if (Integer.valueOf(1).equals(request.getIsDefault())) {
            clearDefaultFamilyMember(familyMember.getUserId(), id);
        }
        fillFamilyMember(familyMember, request);
        familyMember.setUpdateTime(LocalDateTime.now());
        familyMemberMapper.updateById(familyMember);
    }

    @Override
    public void deleteFamilyMember(Long id) {
        FamilyMember familyMember = familyMemberMapper.selectById(id);
        check(familyMember != null && requireUserId().equals(familyMember.getUserId()), "就诊人不存在");
        familyMemberMapper.deleteById(id);
    }

    @Override
    public PageResult<Map<String, Object>> listMyReviews(Integer page, Integer pageSize) {
        Long userId = requireUserId();
        return paginate(reviewMapper.selectList(new LambdaQueryWrapper<Review>().eq(Review::getUserId, userId).orderByDesc(Review::getCreateTime)).stream()
                .map(item -> {
                    Map<String, Object> result = new LinkedHashMap<String, Object>();
                    result.put("id", String.valueOf(item.getId()));
                    result.put("orderType", item.getOrderType());
                    result.put("orderId", String.valueOf(item.getOrderId()));
                    result.put("doctorId", item.getDoctorId());
                    com.whlg.hospital.entity.Doctor doctor = doctorMapper.selectById(item.getDoctorId());
                    result.put("doctorName", doctor == null ? null : doctor.getName());
                    result.put("content", item.getContent());
                    result.put("rating", item.getRating());
                    result.put("createTime", item.getCreateTime());
                    return result;
                }).collect(Collectors.toList()), page, pageSize);
    }

    @Override
    @Transactional
    public synchronized Map<String, Object> createReview(CreateReviewRequest request) {
        check(request != null && request.getOrderType() != null && request.getOrderId() != null
                && request.getDoctorId() != null && request.getRating() != null
                && request.getRating() >= 1 && request.getRating() <= 5
                && request.getContent() != null && !request.getContent().trim().isEmpty(), "评价参数不正确");
        Long userId = requireUserId();
        if (Integer.valueOf(1).equals(request.getOrderType())) {
            com.whlg.hospital.entity.Appointment appointment = appointmentMapper.selectById(request.getOrderId());
            check(appointment != null && userId.equals(appointment.getUserId())
                    && (Integer.valueOf(2).equals(appointment.getStatus()) || Integer.valueOf(3).equals(appointment.getStatus()))
                    && request.getDoctorId().equals(appointment.getDoctorId()), "订单不可评价");
        } else if (Integer.valueOf(2).equals(request.getOrderType())) {
            com.whlg.hospital.entity.Consult consult = consultMapper.selectById(request.getOrderId());
            check(consult != null && userId.equals(consult.getUserId())
                    && (Integer.valueOf(2).equals(consult.getStatus()) || Integer.valueOf(4).equals(consult.getStatus()))
                    && request.getDoctorId().equals(consult.getDoctorId()), "订单不可评价");
        } else {
            throw new ApiException(StatusCode.BAD_REQUEST, "订单类型不正确");
        }
        check(reviewMapper.selectOne(new LambdaQueryWrapper<Review>()
                .eq(Review::getUserId, userId)
                .eq(Review::getOrderType, request.getOrderType())
                .eq(Review::getOrderId, request.getOrderId())) == null, "该订单已经评价");
        Review review = new Review();
        review.setOrderType(request.getOrderType());
        review.setOrderId(request.getOrderId());
        review.setDoctorId(request.getDoctorId());
        review.setContent(request.getContent());
        review.setRating(request.getRating());
        review.setUserId(userId);
        review.setCreateTime(LocalDateTime.now());
        reviewMapper.insert(review);
        List<Review> doctorReviews = reviewMapper.selectList(new LambdaQueryWrapper<Review>()
                .eq(Review::getDoctorId, request.getDoctorId()));
        double average = doctorReviews.stream().mapToInt(Review::getRating).average().orElse(5.0D);
        doctorMapper.update(null, new LambdaUpdateWrapper<com.whlg.hospital.entity.Doctor>()
                .eq(com.whlg.hospital.entity.Doctor::getId, request.getDoctorId())
                .set(com.whlg.hospital.entity.Doctor::getRating,
                        BigDecimal.valueOf(average).setScale(1, RoundingMode.HALF_UP)));
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("id", String.valueOf(review.getId()));
        return result;
    }

    @Override
    public PageResult<Map<String, Object>> listMessages(Integer page, Integer pageSize) {
        Long userId = requireUserId();
        return paginate(messageMapper.selectList(new LambdaQueryWrapper<Message>().eq(Message::getUserId, userId).orderByDesc(Message::getCreateTime)).stream()
                .map(this::messageMap).collect(Collectors.toList()), page, pageSize);
    }

    @Override
    public void markMessageRead(Long id) {
        Message message = messageMapper.selectById(id);
        check(message != null && requireUserId().equals(message.getUserId()), "消息不存在");
        message.setIsRead(1);
        messageMapper.updateById(message);
    }

    @Override
    public Map<String, Object> unreadCount() {
        Long userId = requireUserId();
        long count = messageMapper.selectCount(new LambdaQueryWrapper<Message>().eq(Message::getUserId, userId).eq(Message::getIsRead, 0));
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("count", count);
        return result;
    }

    @Override
    public PageResult<Map<String, Object>> listFeedbacks(Integer page, Integer pageSize) {
        Long userId = requireUserId();
        return paginate(feedbackMapper.selectList(new LambdaQueryWrapper<Feedback>().eq(Feedback::getUserId, userId).orderByDesc(Feedback::getCreateTime)).stream()
                .map(item -> {
                    Map<String, Object> result = new LinkedHashMap<String, Object>();
                    result.put("id", item.getId());
                    result.put("type", item.getFeedbackType());
                    result.put("content", item.getContent());
                    result.put("images", item.getImages() == null ? Arrays.asList() : Arrays.asList(item.getImages().split(",")));
                    result.put("status", item.getStatus());
                    result.put("replyContent", item.getReplyContent());
                    result.put("replyTime", item.getReplyTime());
                    result.put("createTime", item.getCreateTime());
                    return result;
                }).collect(Collectors.toList()), page, pageSize);
    }

    @Override
    public Map<String, Object> createFeedback(CreateFeedbackRequest request) {
        check(request != null && request.getFeedbackType() != null && request.getFeedbackType() >= 1 && request.getFeedbackType() <= 3
                && request.getContent() != null && !request.getContent().trim().isEmpty(), "反馈参数不正确");
        Feedback feedback = new Feedback();
        feedback.setUserId(requireUserId());
        feedback.setFeedbackType(request.getFeedbackType());
        feedback.setContent(request.getContent());
        feedback.setImages(request.getImages() == null ? null : String.join(",", request.getImages()));
        feedback.setStatus(1);
        feedback.setCreateTime(LocalDateTime.now());
        feedback.setUpdateTime(LocalDateTime.now());
        feedbackMapper.insert(feedback);
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("id", feedback.getId());
        return result;
    }

    @Override
    public PageResult<Map<String, Object>> listMyFollows(Integer type, Integer page, Integer pageSize) {
        validateFollowType(type, true);
        Long userId = requireUserId();
        return paginate(followMapper.selectList(new LambdaQueryWrapper<Follow>()
                        .eq(Follow::getUserId, userId)
                        .eq(type != null, Follow::getFollowType, type)
                        .orderByDesc(Follow::getCreateTime))
                .stream().map(this::followMap).filter(item -> item != null).collect(Collectors.toList()), page, pageSize);
    }

    @Override
    @Transactional
    public Map<String, Object> createFollow(Integer type, Long targetId) {
        check(targetId != null && (Integer.valueOf(1).equals(type) || Integer.valueOf(2).equals(type)
                || Integer.valueOf(3).equals(type)), "关注参数不正确");
        if (Integer.valueOf(1).equals(type)) {
            com.whlg.hospital.entity.Hospital hospital = hospitalMapper.selectById(targetId);
            check(hospital != null, "医院不存在");
            check(Integer.valueOf(1).equals(hospital.getStatus()), "医院不可用");
        } else if (Integer.valueOf(2).equals(type)) {
            com.whlg.hospital.entity.Doctor doctor = doctorMapper.selectById(targetId);
            check(doctor != null, "医生不存在");
            check(Integer.valueOf(1).equals(doctor.getStatus()), "医生不可用");
        } else {
            check(diseaseMapper.selectById(targetId) != null, "疾病不存在");
        }
        Long userId = requireUserId();
        Follow follow = followMapper.selectOne(new LambdaQueryWrapper<Follow>()
                .eq(Follow::getUserId, userId)
                .eq(Follow::getFollowType, type)
                .eq(Follow::getFollowId, targetId));
        if (follow == null) {
            follow = new Follow();
            follow.setUserId(userId);
            follow.setFollowType(type);
            follow.setFollowId(targetId);
            follow.setCreateTime(LocalDateTime.now());
            followMapper.insert(follow);
            updateFollowCount(type, targetId, 1);
        }
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("id", follow.getId());
        return result;
    }

    @Override
    @Transactional
    public void deleteFollow(Integer type, Long targetId) {
        validateFollowType(type, false);
        check(targetId != null, "关注参数不正确");
        Long userId = requireUserId();
        Follow follow = followMapper.selectOne(new LambdaQueryWrapper<Follow>()
                .eq(Follow::getUserId, userId)
                .eq(Follow::getFollowType, type)
                .eq(Follow::getFollowId, targetId));
        if (follow != null) {
            followMapper.deleteById(follow.getId());
            updateFollowCount(type, targetId, -1);
        }
    }

    private void updateFollowCount(Integer type, Long targetId, int delta) {
        String expression = delta > 0
                ? "follow_count = COALESCE(follow_count, 0) + 1"
                : "follow_count = CASE WHEN follow_count > 0 THEN follow_count - 1 ELSE 0 END";
        if (Integer.valueOf(1).equals(type)) {
            hospitalMapper.update(null, new LambdaUpdateWrapper<com.whlg.hospital.entity.Hospital>()
                    .eq(com.whlg.hospital.entity.Hospital::getId, targetId).setSql(expression));
        } else if (Integer.valueOf(2).equals(type)) {
            doctorMapper.update(null, new LambdaUpdateWrapper<com.whlg.hospital.entity.Doctor>()
                    .eq(com.whlg.hospital.entity.Doctor::getId, targetId).setSql(expression));
        } else {
            diseaseMapper.update(null, new LambdaUpdateWrapper<com.whlg.hospital.entity.Disease>()
                    .eq(com.whlg.hospital.entity.Disease::getId, targetId).setSql(expression));
        }
    }

    private void clearDefaultFamilyMember(Long userId, Long exceptId) {
        LambdaUpdateWrapper<FamilyMember> update = new LambdaUpdateWrapper<FamilyMember>()
                .eq(FamilyMember::getUserId, userId)
                .eq(FamilyMember::getIsDefault, 1)
                .set(FamilyMember::getIsDefault, 0);
        if (exceptId != null) {
            update.ne(FamilyMember::getId, exceptId);
        }
        familyMemberMapper.update(null, update);
    }

    private void fillFamilyMember(FamilyMember familyMember, FamilyMemberRequest request) {
        familyMember.setName(request.getName().trim());
        familyMember.setPhone(request.getPhone().trim());
        familyMember.setRelation(request.getRelation().trim());
        familyMember.setGender(request.getGender());
        familyMember.setBirthday(request.getBirthday());
        familyMember.setIdCard(request.getIdCard() == null ? null : request.getIdCard().trim());
        familyMember.setIsDefault(request.getIsDefault() == null ? 0 : request.getIsDefault());
    }

    private void ensureFamilyMemberUnique(Long userId, FamilyMemberRequest request, Long exceptId) {
        LambdaQueryWrapper<FamilyMember> phoneQuery = new LambdaQueryWrapper<FamilyMember>()
                .eq(FamilyMember::getUserId, userId)
                .eq(FamilyMember::getPhone, request.getPhone().trim());
        if (exceptId != null) {
            phoneQuery.ne(FamilyMember::getId, exceptId);
        }
        check(familyMemberMapper.selectCount(phoneQuery) == 0, "该手机号已存在就诊人，请勿重复添加");

        if (request.getIdCard() == null || request.getIdCard().trim().isEmpty()) {
            return;
        }
        LambdaQueryWrapper<FamilyMember> idCardQuery = new LambdaQueryWrapper<FamilyMember>()
                .eq(FamilyMember::getUserId, userId)
                .eq(FamilyMember::getIdCard, request.getIdCard().trim());
        if (exceptId != null) {
            idCardQuery.ne(FamilyMember::getId, exceptId);
        }
        check(familyMemberMapper.selectCount(idCardQuery) == 0, "该身份证号已存在就诊人，请勿重复添加");
    }

    private void validateFamilyMember(FamilyMemberRequest request) {
        check(request != null, "就诊人信息不能为空");
        check(request.getName() != null && request.getName().trim().matches("[\\u4e00-\\u9fa5A-Za-z·]{2,20}"), "就诊人姓名格式不正确");
        check(request.getGender() != null && (request.getGender() == 1 || request.getGender() == 2), "性别不正确");
        check(request.getBirthday() != null && !request.getBirthday().isAfter(LocalDate.now())
                && request.getBirthday().isAfter(LocalDate.now().minusYears(120)), "生日不合法");
        check(request.getPhone() != null && request.getPhone().trim().matches("^1\\d{10}$"), "手机号格式不正确");
        check(request.getRelation() != null && !request.getRelation().trim().isEmpty()
                && request.getRelation().trim().length() <= 10, "关系不能为空");
        check(request.getIdCard() == null || request.getIdCard().trim().isEmpty()
                || request.getIdCard().trim().matches("^[1-9]\\d{5}(18|19|20)\\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\\d|3[01])\\d{3}[0-9Xx]$"), "身份证号格式不正确");
        check(request != null && request.getName() != null && !request.getName().trim().isEmpty(), "就诊人姓名不能为空");
        check(request.getBirthday() == null || !request.getBirthday().isAfter(LocalDate.now()), "生日不能晚于今天");
        check(request.getIsDefault() == null || request.getIsDefault() == 0 || request.getIsDefault() == 1, "默认就诊人标记不正确");
    }

    private void validateFollowType(Integer type, boolean allowNull) {
        check((allowNull && type == null) || Integer.valueOf(1).equals(type)
                || Integer.valueOf(2).equals(type) || Integer.valueOf(3).equals(type), "非法关注类型");
    }

    private Map<String, Object> familyMemberMap(FamilyMember item) {
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("id", String.valueOf(item.getId()));
        result.put("name", item.getName());
        result.put("phone", item.getPhone());
        result.put("relation", item.getRelation());
        result.put("gender", item.getGender());
        result.put("birthday", item.getBirthday());
        result.put("age", item.getBirthday() == null ? null : Period.between(item.getBirthday(), LocalDate.now()).getYears());
        result.put("idCard", item.getIdCard());
        result.put("isDefault", item.getIsDefault());
        return result;
    }

    private Map<String, Object> messageMap(Message item) {
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("id", item.getId());
        result.put("title", item.getTitle());
        result.put("content", item.getContent());
        result.put("isRead", item.getIsRead());
        result.put("createTime", item.getCreateTime());
        return result;
    }

    private Map<String, Object> followMap(Follow item) {
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("id", item.getId());
        result.put("type", item.getFollowType());
        result.put("followId", item.getFollowId());
        result.put("createTime", item.getCreateTime());
        if (Integer.valueOf(1).equals(item.getFollowType())) {
            com.whlg.hospital.entity.Hospital hospital = hospitalMapper.selectById(item.getFollowId());
            if (hospital == null) {
                return null;
            }
            result.put("name", hospital.getName());
        } else if (Integer.valueOf(2).equals(item.getFollowType())) {
            com.whlg.hospital.entity.Doctor doctor = doctorMapper.selectById(item.getFollowId());
            if (doctor == null) {
                return null;
            }
            result.put("name", doctor.getName());
        } else if (Integer.valueOf(3).equals(item.getFollowType())) {
            com.whlg.hospital.entity.Disease disease = diseaseMapper.selectById(item.getFollowId());
            if (disease == null) {
                return null;
            }
            result.put("name", disease.getName());
        } else {
            throw new ApiException(StatusCode.BAD_REQUEST, "非法关注类型");
        }
        return result;
    }
}
