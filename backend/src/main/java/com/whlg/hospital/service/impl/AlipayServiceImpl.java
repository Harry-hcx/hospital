package com.whlg.hospital.service.impl;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.whlg.hospital.config.AlipayConfig;
import com.whlg.hospital.service.AlipayService;
import com.whlg.hospital.support.ApiException;
import com.whlg.hospital.util.StatusCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class AlipayServiceImpl implements AlipayService {

    private static final Logger log = LoggerFactory.getLogger(AlipayServiceImpl.class);

    private final AlipayConfig alipayConfig;

    public AlipayServiceImpl(AlipayConfig alipayConfig) {
        this.alipayConfig = alipayConfig;
    }

    @Override
    public String createPageForm(String businessOrderNo, BigDecimal amount, String subject, String body) {
        try {
            AlipayClient alipayClient = createClient();
            AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
            request.setReturnUrl(alipayConfig.getReturnUrl());
            request.setNotifyUrl(alipayConfig.getNotifyUrl());
            request.setBizContent("{"
                    + "\"out_trade_no\":\"" + escapeJson(businessOrderNo) + "\"," 
                    + "\"total_amount\":\"" + amount.toPlainString() + "\"," 
                    + "\"subject\":\"" + escapeJson(subject) + "\"," 
                    + "\"body\":\"" + escapeJson(body) + "\"," 
                    + "\"product_code\":\"FAST_INSTANT_TRADE_PAY\""
                    + "}");
            log.info("Alipay page form built. orderNo={}, amount={}, subject={}, gatewayUrl={}, returnUrl={}, notifyUrl={}",
                    businessOrderNo,
                    amount,
                    subject,
                    alipayConfig.getGatewayUrl(),
                    alipayConfig.getReturnUrl(),
                    alipayConfig.getNotifyUrl());
            return alipayClient.pageExecute(request).getBody();
        } catch (AlipayApiException ex) {
            log.error("Alipay page form create failed. orderNo={}, amount={}, subject={}",
                    businessOrderNo, amount, subject, ex);
            throw new ApiException(StatusCode.ERROR, "支付宝下单失败: " + ex.getErrMsg());
        }
    }

    @Override
    public boolean verifySignature(Map<String, String> params) {
        try {
            boolean verified = AlipaySignature.rsaCheckV1(
                    params,
                    alipayConfig.getAlipayPublicKey(),
                    alipayConfig.getCharset(),
                    alipayConfig.getSignType()
            );
            log.info("Alipay signature checked. orderNo={}, tradeNo={}, tradeStatus={}, verified={}",
                    params == null ? null : params.get("out_trade_no"),
                    params == null ? null : params.get("trade_no"),
                    params == null ? null : params.get("trade_status"),
                    verified);
            return verified;
        } catch (AlipayApiException ex) {
            log.error("Alipay signature verify failed. paramsKeys={}", summarizeKeys(params), ex);
            throw new ApiException(StatusCode.ERROR, "支付宝验签失败: " + ex.getErrMsg());
        }
    }

    @Override
    public String queryTradeStatus(String businessOrderNo, String tradeNo) {
        try {
            AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
            StringBuilder bizContent = new StringBuilder("{")
                    .append("\"out_trade_no\":\"").append(escapeJson(businessOrderNo)).append("\"");
            if (tradeNo != null && !tradeNo.trim().isEmpty()) {
                bizContent.append(",\"trade_no\":\"").append(escapeJson(tradeNo.trim())).append("\"");
            }
            bizContent.append("}");
            request.setBizContent(bizContent.toString());
            AlipayTradeQueryResponse response = createClient().execute(request);
            String tradeStatus = response == null ? null : response.getTradeStatus();
            log.info("Alipay trade queried. orderNo={}, tradeNo={}, tradeStatus={}, subCode={}, bodyPresent={}",
                    businessOrderNo,
                    tradeNo,
                    tradeStatus,
                    response == null ? null : response.getSubCode(),
                    response != null && response.getBody() != null);
            return tradeStatus;
        } catch (AlipayApiException ex) {
            log.error("Alipay trade query failed. orderNo={}, tradeNo={}", businessOrderNo, tradeNo, ex);
            throw new ApiException(StatusCode.ERROR, "支付宝查单失败: " + ex.getErrMsg());
        }
    }

    private AlipayClient createClient() {
        return new DefaultAlipayClient(
                alipayConfig.getGatewayUrl(),
                alipayConfig.getAppId(),
                alipayConfig.getMerchantPrivateKey(),
                "json",
                alipayConfig.getCharset(),
                alipayConfig.getAlipayPublicKey(),
                alipayConfig.getSignType()
        );
    }

    private String escapeJson(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private String summarizeKeys(Map<String, String> params) {
        if (params == null || params.isEmpty()) {
            return "[]";
        }
        return new LinkedHashMap<String, String>(params).keySet().toString();
    }
}
