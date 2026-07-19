package com.whlg.hospital;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class OrderApiTest extends BaseApiTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void shouldCreateAppointment() throws Exception {
        Map<String, Object> request = appointmentRequest();
        request.put("diseaseDesc", "头晕");

        mockMvc.perform(post("/api/appointments")
                        .header("Authorization", auth())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.orderNo").isString());
    }

    @Test
    void shouldRejectDuplicateAppointmentForSameSchedule() throws Exception {
        Map<String, Object> request = appointmentRequest();

        mockMvc.perform(post("/api/appointments")
                        .header("Authorization", auth())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.orderNo").isString());

        mockMvc.perform(post("/api/appointments")
                        .header("Authorization", auth())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    void shouldIgnoreStaleScheduleDepartmentReference() throws Exception {
        jdbcTemplate.update("update t_schedule set department_id = ? where id = ?", 999L, 2L);

        mockMvc.perform(post("/api/appointments")
                        .header("Authorization", auth())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(appointmentRequest())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.orderNo").isString());
    }

    @Test
    void shouldGetAppointmentDetail() throws Exception {
        mockMvc.perform(get("/api/appointments/AP202607170001").header("Authorization", auth()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.patientName").value("张三"));
    }

    @Test
    void shouldRejectOtherUserAppointmentDetail() throws Exception {
        String otherToken = registerAndLogin("13900139001", "123456");

        mockMvc.perform(get("/api/appointments/AP202607170001").header("Authorization", auth(otherToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(403));
    }

    @Test
    void shouldCancelAppointment() throws Exception {
        String orderNo = createAppointment();
        mockMvc.perform(post("/api/appointments/" + orderNo + "/cancel").header("Authorization", auth()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void shouldGetAppointmentSuccess() throws Exception {
        mockMvc.perform(get("/api/appointments/AP202607170001/success").header("Authorization", auth()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.orderNo").value("AP202607170001"));
    }

    @Test
    void shouldRejectCancellingAppointmentTwice() throws Exception {
        String orderNo = createAppointment();
        mockMvc.perform(post("/api/appointments/" + orderNo + "/cancel").header("Authorization", auth()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        mockMvc.perform(post("/api/appointments/" + orderNo + "/cancel").header("Authorization", auth()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    void shouldPayAppointment() throws Exception {
        String orderNo = createAppointment();
        Map<String, Object> request = new HashMap<String, Object>();
        request.put("payMethod", "alipay");

        mockMvc.perform(post("/api/appointments/" + orderNo + "/pay")
                        .header("Authorization", auth())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.success").value(true));
    }

    @Test
    void shouldRejectUnsupportedAppointmentPayMethod() throws Exception {
        String orderNo = createAppointment();
        Map<String, Object> request = new HashMap<String, Object>();
        request.put("payMethod", "cash");

        mockMvc.perform(post("/api/appointments/" + orderNo + "/pay")
                        .header("Authorization", auth())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    void shouldListMyAppointments() throws Exception {
        mockMvc.perform(get("/api/appointments/my").header("Authorization", auth()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.list").isArray())
                .andExpect(jsonPath("$.data.records").doesNotExist());
    }

    @Test
    void shouldFilterAppointmentsByStatus() throws Exception {
        mockMvc.perform(get("/api/appointments/my")
                        .header("Authorization", auth())
                        .param("status", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(1));

        mockMvc.perform(get("/api/appointments/my")
                        .header("Authorization", auth())
                        .param("status", "99"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    void shouldCreateConsult() throws Exception {
        Map<String, Object> request = consultRequest();
        request.put("diseaseDesc", "复诊");

        mockMvc.perform(post("/api/consults")
                        .header("Authorization", auth())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.orderNo").isString());
    }

    @Test
    void shouldGetConsultDetail() throws Exception {
        mockMvc.perform(get("/api/consults/CO202607170001").header("Authorization", auth()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.fee").isNumber())
                .andExpect(jsonPath("$.data.appointmentTime").exists());
    }

    @Test
    void shouldRenderHistoricalConsultWhenDoctorWasDeleted() throws Exception {
        jdbcTemplate.update("update t_consult set doctor_id = ? where id = ?", 999L, 1L);

        mockMvc.perform(get("/api/consults/CO202607170001").header("Authorization", auth()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.doctor").value("医生已下线"));

        mockMvc.perform(get("/api/consults/my").header("Authorization", auth()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.list[0].doctorName").value("医生已下线"));
    }

    @Test
    void shouldGetConsultSuccess() throws Exception {
        mockMvc.perform(get("/api/consults/CO202607170001/success").header("Authorization", auth()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.orderNo").value("CO202607170001"));
    }

    @Test
    void shouldCancelPendingConsult() throws Exception {
        String orderNo = createConsult();
        mockMvc.perform(post("/api/consults/" + orderNo + "/cancel").header("Authorization", auth()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        mockMvc.perform(get("/api/consults/" + orderNo).header("Authorization", auth()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value(5));

        mockMvc.perform(post("/api/consults/" + orderNo + "/cancel").header("Authorization", auth()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    void shouldPayConsult() throws Exception {
        String orderNo = createConsult();
        Map<String, Object> request = new HashMap<String, Object>();
        request.put("payMethod", "wechat");

        mockMvc.perform(post("/api/consults/" + orderNo + "/pay")
                        .header("Authorization", auth())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.success").value(true));
    }

    @Test
    void shouldRejectOtherUserConsultPay() throws Exception {
        String otherToken = registerAndLogin("13900139002", "123456");
        Map<String, Object> request = new HashMap<String, Object>();
        request.put("payMethod", "wechat");

        mockMvc.perform(post("/api/consults/CO202607170001/pay")
                        .header("Authorization", auth(otherToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(403));
    }

    @Test
    void shouldListMyConsults() throws Exception {
        mockMvc.perform(get("/api/consults/my").header("Authorization", auth()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.list").isArray())
                .andExpect(jsonPath("$.data.records").doesNotExist());
    }

    @Test
    void shouldFilterConsultsByLifecycleStatus() throws Exception {
        mockMvc.perform(get("/api/consults/my")
                        .header("Authorization", auth())
                        .param("status", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.list[0].appointmentTime").exists());
    }

    @Test
    void shouldToggleFollowIdempotentlyAndExposeStatus() throws Exception {
        mockMvc.perform(get("/api/doctors/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.followCount").value(180));

        mockMvc.perform(get("/api/follow/2/2").header("Authorization", auth()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.following").value(false));

        mockMvc.perform(post("/api/follow/doctor/2").header("Authorization", auth()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.following").value(true));

        mockMvc.perform(get("/api/doctors/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.followCount").value(181));

        mockMvc.perform(post("/api/follow/doctor/2").header("Authorization", auth()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.following").value(true));

        mockMvc.perform(get("/api/doctors/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.followCount").value(181));

        mockMvc.perform(delete("/api/follow/2/2").header("Authorization", auth()))
                .andExpect(status().isOk());
        mockMvc.perform(delete("/api/follow/2/2").header("Authorization", auth()))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/follow/2/2").header("Authorization", auth()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.following").value(false));

        mockMvc.perform(get("/api/doctors/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.followCount").value(180));
    }

    @Test
    void shouldCreatePayment() throws Exception {
        String orderNo = createAppointment();
        Map<String, Object> request = new HashMap<String, Object>();
        request.put("businessOrderNo", orderNo);
        request.put("businessType", "appointment");
        request.put("actualAmount", new BigDecimal("18.00"));
        request.put("payMethod", "alipay");

        mockMvc.perform(post("/api/payments")
                        .header("Authorization", auth())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.paymentNo").isString());
    }

    @Test
    void shouldRejectUnsupportedBusinessType() throws Exception {
        Map<String, Object> request = new HashMap<String, Object>();
        request.put("businessOrderNo", "AP202607170001");
        request.put("businessType", "unknown");
        request.put("actualAmount", new BigDecimal("18.00"));
        request.put("payMethod", "alipay");

        mockMvc.perform(post("/api/payments")
                        .header("Authorization", auth())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    void shouldHandlePaymentCallback() throws Exception {
        String orderNo = createAppointment();
        Map<String, Object> createRequest = new HashMap<String, Object>();
        createRequest.put("businessOrderNo", orderNo);
        createRequest.put("businessType", "appointment");
        createRequest.put("actualAmount", new BigDecimal("18.00"));
        createRequest.put("payMethod", "alipay");

        MvcResult createResult = mockMvc.perform(post("/api/payments")
                        .header("Authorization", auth())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode createJson = objectMapper.readTree(createResult.getResponse().getContentAsString());
        String paymentNo = createJson.path("data").path("paymentNo").asText();

        Map<String, Object> callbackRequest = new HashMap<String, Object>();
        callbackRequest.put("paymentNo", paymentNo);
        callbackRequest.put("tradeNo", "TRADE-001");
        callbackRequest.put("payStatus", 1);

        mockMvc.perform(post("/api/payments/callback")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(callbackRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void shouldGetPaymentDetail() throws Exception {
        mockMvc.perform(get("/api/payments/AP202607170001").header("Authorization", auth()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.businessOrderNo").value("AP202607170001"));
    }

    @Test
    void shouldRejectOtherUserPaymentDetail() throws Exception {
        String otherToken = registerAndLogin("13900139003", "123456");

        mockMvc.perform(get("/api/payments/AP202607170001").header("Authorization", auth(otherToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(403));
    }

    private Map<String, Object> appointmentRequest() {
        Map<String, Object> request = new HashMap<String, Object>();
        request.put("doctorId", 1);
        request.put("hospitalId", 1);
        request.put("patientId", 1);
        request.put("appointmentDate", LocalDate.now().plusDays(2).toString());
        request.put("appointmentTime", "09:00-10:00");
        return request;
    }

    private String createAppointment() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/appointments")
                        .header("Authorization", auth())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(appointmentRequest())))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).path("data").path("orderNo").asText();
    }

    private Map<String, Object> consultRequest() {
        Map<String, Object> request = new HashMap<String, Object>();
        request.put("doctorId", 1);
        request.put("patientName", "张三");
        request.put("patientPhone", "13800138000");
        request.put("diseaseDesc", "复诊");
        request.put("appointmentTime", LocalDateTime.now().plusDays(1).withNano(0)
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        request.put("duration", 30);
        return request;
    }

    private String createConsult() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/consults")
                        .header("Authorization", auth())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(consultRequest())))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).path("data").path("orderNo").asText();
    }
}
