package com.whlg.hospital.service;

import com.whlg.hospital.dto.CreateAppointmentRequest;
import com.whlg.hospital.dto.CreateConsultRequest;
import com.whlg.hospital.dto.CreatePaymentRequest;
import com.whlg.hospital.dto.PayRequest;
import com.whlg.hospital.dto.PaymentCallbackRequest;
import com.whlg.hospital.vo.PageResult;

import java.util.Map;

public interface OrderService {

    Map<String, Object> createAppointment(CreateAppointmentRequest request);

    Map<String, Object> getAppointment(String orderNo);

    void cancelAppointment(String orderNo);

    Map<String, Object> payAppointment(String orderNo, PayRequest request);

    PageResult<Map<String, Object>> listMyAppointments(Integer page, Integer pageSize, Integer status);

    Map<String, Object> createConsult(CreateConsultRequest request);

    Map<String, Object> getConsult(String orderNo);

    void cancelConsult(String orderNo);

    Map<String, Object> payConsult(String orderNo, PayRequest request);

    PageResult<Map<String, Object>> listMyConsults(Integer page, Integer pageSize, Integer status);

    Map<String, Object> createPayment(CreatePaymentRequest request);

    void paymentCallback(PaymentCallbackRequest request);

    Map<String, Object> getPayment(String businessOrderNo);
}
