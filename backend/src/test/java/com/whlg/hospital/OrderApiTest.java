package com.whlg.hospital;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class OrderApiTest extends BaseApiTest {

    @Test
    void shouldCreateAppointment() throws Exception {
        Map<String, Object> request = new HashMap<String, Object>();
        request.put("scheduleId", 1);
        request.put("familyMemberId", 1);
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
        Map<String, Object> request = new HashMap<String, Object>();
        request.put("scheduleId", 1);
        request.put("familyMemberId", 1);

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
        mockMvc.perform(post("/api/appointments/AP202607170001/cancel").header("Authorization", auth()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void shouldRejectCancellingAppointmentTwice() throws Exception {
        mockMvc.perform(post("/api/appointments/AP202607170001/cancel").header("Authorization", auth()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        mockMvc.perform(post("/api/appointments/AP202607170001/cancel").header("Authorization", auth()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    void shouldPayAppointment() throws Exception {
        Map<String, Object> request = new HashMap<String, Object>();
        request.put("payType", 2);

        mockMvc.perform(post("/api/appointments/AP202607170001/pay")
                        .header("Authorization", auth())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.success").value(true));
    }

    @Test
    void shouldRejectUnsupportedAppointmentPayType() throws Exception {
        Map<String, Object> request = new HashMap<String, Object>();
        request.put("payType", 9);

        mockMvc.perform(post("/api/appointments/AP202607170001/pay")
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
                .andExpect(jsonPath("$.data.records").isArray());
    }

    @Test
    void shouldFilterAppointmentsByStatus() throws Exception {
        mockMvc.perform(get("/api/appointments/my")
                        .header("Authorization", auth())
                        .param("status", "1"))
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
        Map<String, Object> request = new HashMap<String, Object>();
        request.put("doctorId", 1);
        request.put("familyMemberId", 1);
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
    void shouldCancelPendingConsult() throws Exception {
        mockMvc.perform(post("/api/consults/CO202607170001/cancel").header("Authorization", auth()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        mockMvc.perform(get("/api/consults/CO202607170001").header("Authorization", auth()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value(5));

        mockMvc.perform(post("/api/consults/CO202607170001/cancel").header("Authorization", auth()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    void shouldPayConsult() throws Exception {
        Map<String, Object> request = new HashMap<String, Object>();
        request.put("payType", 1);

        mockMvc.perform(post("/api/consults/CO202607170001/pay")
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
        request.put("payType", 1);

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
                .andExpect(jsonPath("$.data.records").isArray());
    }

    @Test
    void shouldFilterConsultsByLifecycleStatus() throws Exception {
        mockMvc.perform(get("/api/consults/my")
                        .header("Authorization", auth())
                        .param("status", "4"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.records[0].appointmentTime").exists());
    }

    @Test
    void shouldToggleFollowIdempotentlyAndExposeStatus() throws Exception {
        mockMvc.perform(get("/api/doctors/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.followCount").value(180));

        mockMvc.perform(get("/api/follow/2/2").header("Authorization", auth()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.following").value(false));

        mockMvc.perform(post("/api/follow/2/2").header("Authorization", auth()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.following").value(true));

        mockMvc.perform(get("/api/doctors/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.followCount").value(181));

        mockMvc.perform(post("/api/follow/2/2").header("Authorization", auth()))
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
        Map<String, Object> request = new HashMap<String, Object>();
        request.put("businessOrderNo", "AP202607170001");
        request.put("businessType", "appointment");
        request.put("amount", new BigDecimal("18.00"));
        request.put("payType", 2);

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
        request.put("amount", new BigDecimal("18.00"));
        request.put("payType", 2);

        mockMvc.perform(post("/api/payments")
                        .header("Authorization", auth())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    void shouldHandlePaymentCallback() throws Exception {
        Map<String, Object> createRequest = new HashMap<String, Object>();
        createRequest.put("businessOrderNo", "AP202607170001");
        createRequest.put("businessType", "appointment");
        createRequest.put("amount", new BigDecimal("18.00"));
        createRequest.put("payType", 2);

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
                        .header("X-Payment-Signature", "test-payment-secret")
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
}
