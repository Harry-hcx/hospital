package com.whlg.hospital.service;

import java.math.BigDecimal;
import java.util.Map;

public interface AlipayService {

    String createPageForm(String businessOrderNo, BigDecimal amount, String subject, String body);

    boolean verifySignature(Map<String, String> params);

    String queryTradeStatus(String businessOrderNo, String tradeNo);
}