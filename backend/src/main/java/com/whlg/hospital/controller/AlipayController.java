package com.whlg.hospital.controller;

import com.whlg.hospital.config.AlipayConfig;
import com.whlg.hospital.dto.AlipayDto;
import com.whlg.hospital.dto.PaymentCallbackRequest;
import com.whlg.hospital.service.AlipayService;
import com.whlg.hospital.service.OrderService;
import com.whlg.hospital.util.R;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 支付宝渠道适配入口。
 */
@RestController
@RequestMapping("/api/payments/alipay")
public class AlipayController {

    private static final Logger log = LoggerFactory.getLogger(AlipayController.class);

    private final OrderService orderService;
    private final AlipayService alipayService;
    private final AlipayConfig alipayConfig;

    public AlipayController(OrderService orderService,
                            AlipayService alipayService,
                            AlipayConfig alipayConfig) {
        this.orderService = orderService;
        this.alipayService = alipayService;
        this.alipayConfig = alipayConfig;
    }

    @PostMapping("/page")
    public R<Map<String, Object>> createPagePayment(@RequestBody AlipayDto request) {
        log.info("Alipay page create requested. orderNo={}, amount={}, subject={}",
                request == null ? null : request.getBusinessOrderNo(),
                request == null ? null : request.getAmount(),
                request == null ? null : request.getSubject());
        String formHtml = alipayService.createPageForm(
                request.getBusinessOrderNo(),
                new BigDecimal(request.getAmount()),
                request.getSubject(),
                request.getBody()
        );
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("channel", AlipayConfig.CHANNEL);
        result.put("businessOrderNo", request.getBusinessOrderNo());
        result.put("formHtml", formHtml);
        result.put("notifyPath", AlipayConfig.NOTIFY_PATH);
        result.put("returnPath", AlipayConfig.RETURN_PATH);
        return R.ok(result);
    }

    @PostMapping("/notify")
    public String notifyCallback(HttpServletRequest request) {
        Map<String, String> params = extractParams(request);
        log.info("Alipay notify callback arrived. remoteAddr={}, orderNo={}, tradeStatus={}, params={}",
                request.getRemoteAddr(),
                params.get("out_trade_no"),
                params.get("trade_status"),
                summarizeParams(params));
        boolean verified = alipayService.verifySignature(params);
        log.info("Alipay notify signature verified. orderNo={}, verified={}", params.get("out_trade_no"), verified);
        if (!verified) {
            return "fail";
        }
        if (isTradePaid(params.get("trade_status"))) {
            orderService.paymentCallback(buildSuccessCallback(params));
            log.info("Alipay notify callback processed as paid. orderNo={}, tradeNo={}",
                    params.get("out_trade_no"), params.get("trade_no"));
        }
        return "success";
    }

    @GetMapping("/return")
    public void returnCallback(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Map<String, String> params = extractParams(request);
        String orderNo = params.get("out_trade_no");
        String tradeStatus = params.get("trade_status");
        String tradeNo = params.get("trade_no");
        log.info("Alipay return callback arrived. remoteAddr={}, orderNo={}, tradeStatus={}, query={}, params={}",
                request.getRemoteAddr(),
                orderNo,
                tradeStatus,
                request.getQueryString(),
                summarizeParams(params));
        try {
            boolean verified = alipayService.verifySignature(params);
            log.info("Alipay return signature verified. orderNo={}, verified={}", orderNo, verified);
            if (!verified) {
                respondRedirectPage(response, buildPayPageUrl(orderNo, "verify_failed"), "支付结果校验失败，正在返回支付页...");
                return;
            }

            String confirmedTradeStatus = tradeStatus;
            if (confirmedTradeStatus == null || confirmedTradeStatus.isEmpty()) {
                confirmedTradeStatus = alipayService.queryTradeStatus(orderNo, tradeNo);
                log.info("Alipay return trade status queried. orderNo={}, tradeNo={}, queriedTradeStatus={}",
                        orderNo, tradeNo, confirmedTradeStatus);
            }

            if (isTradePaid(confirmedTradeStatus)) {
                orderService.paymentCallback(buildSuccessCallback(params, orderNo, tradeNo));
                String successUrl = buildSuccessUrl(orderNo);
                log.info("Alipay return callback processed as paid. orderNo={}, tradeNo={}, tradeStatus={}, redirectUrl={}",
                        orderNo, tradeNo, confirmedTradeStatus, successUrl);
                respondRedirectPage(response, successUrl, "支付成功，正在跳转结果页...");
                return;
            }
            String payUrl = buildPayPageUrl(orderNo, "cancelled");
            log.info("Alipay return callback not paid. orderNo={}, tradeNo={}, tradeStatus={}, redirectUrl={}",
                    orderNo, tradeNo, confirmedTradeStatus, payUrl);
            respondRedirectPage(response, payUrl, "支付未完成，正在返回支付页...");
        } catch (Exception ex) {
            log.error("Alipay return callback failed. orderNo={}, tradeStatus={}, params={}",
                    orderNo, tradeStatus, summarizeParams(params), ex);
            respondRedirectPage(response, buildPayPageUrl(orderNo, "processing"), "支付结果确认中，正在返回支付页...");
        }
    }

    private PaymentCallbackRequest buildSuccessCallback(Map<String, String> params) {
        return buildSuccessCallback(params, params.get("out_trade_no"), params.get("trade_no"));
    }

    private PaymentCallbackRequest buildSuccessCallback(Map<String, String> params, String orderNo, String tradeNo) {
        PaymentCallbackRequest request = new PaymentCallbackRequest();
        String resolvedOrderNo = firstNotBlank(orderNo, params.get("out_trade_no"));
        request.setPaymentNo(resolvedOrderNo);
        request.setTradeNo(firstNotBlank(tradeNo, params.get("trade_no"), resolvedOrderNo));
        request.setPayStatus(1);
        return request;
    }

    private String firstNotBlank(String... values) {
        if (values == null) {
            return null;
        }
        for (String value : values) {
            if (value != null && !value.trim().isEmpty()) {
                return value.trim();
            }
        }
        return null;
    }

    private Map<String, String> extractParams(HttpServletRequest request) {
        Map<String, String> params = new LinkedHashMap<String, String>();
        Map<String, String[]> requestParams = request.getParameterMap();
        for (Map.Entry<String, String[]> entry : requestParams.entrySet()) {
            String[] values = entry.getValue();
            if (values == null || values.length == 0) {
                continue;
            }
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < values.length; i++) {
                if (i > 0) {
                    builder.append(",");
                }
                builder.append(values[i]);
            }
            params.put(entry.getKey(), builder.toString());
        }
        return params;
    }

    private boolean isTradePaid(String tradeStatus) {
        return "TRADE_SUCCESS".equals(tradeStatus) || "TRADE_FINISHED".equals(tradeStatus);
    }

    private String buildSuccessUrl(String orderNo) {
        return alipayConfig.getFrontendBaseUrl() + (isConsultOrder(orderNo)
                ? "/consult/success/" + orderNo
                : "/reservation/success/" + orderNo) + "?payResult=success";
    }

    private String buildPayPageUrl(String orderNo, String payResult) {
        return alipayConfig.getFrontendBaseUrl() + (isConsultOrder(orderNo)
                ? "/consult/pay/" + orderNo
                : "/reservation/pay/" + orderNo) + "?payResult=" + payResult;
    }

    private boolean isConsultOrder(String orderNo) {
        return orderNo != null && orderNo.startsWith("CO");
    }

    private String summarizeParams(Map<String, String> params) {
        Map<String, String> summary = new LinkedHashMap<String, String>();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            String key = entry.getKey();
            if ("sign".equals(key)) {
                summary.put(key, mask(entry.getValue()));
                continue;
            }
            if ("app_id".equals(key) || "trade_no".equals(key) || "out_trade_no".equals(key)
                    || "trade_status".equals(key) || "total_amount".equals(key) || "seller_id".equals(key)
                    || "gmt_payment".equals(key) || "charset".equals(key)) {
                summary.put(key, entry.getValue());
            }
        }
        return summary.toString();
    }

    private String mask(String value) {
        if (value == null || value.length() <= 8) {
            return value;
        }
        return value.substring(0, 4) + "..." + value.substring(value.length() - 4);
    }

    private void respondRedirectPage(HttpServletResponse response, String redirectUrl, String message) throws IOException {
        response.setCharacterEncoding("utf-8");
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter writer = response.getWriter();
        writer.write("<!DOCTYPE html><html><head><meta charset=\"UTF-8\">"
                + "<meta http-equiv=\"refresh\" content=\"1;url=" + escapeHtml(redirectUrl) + "\">"
                + "<title>支付结果确认中</title>"
                + "</head><body style=\"font-family:Arial,sans-serif;padding:32px;line-height:1.8;\">"
                + "<h2>" + escapeHtml(message) + "</h2>"
                + "<p>如果没有自动跳转，请点击下方链接继续。</p>"
                + "<p><a href=\"" + escapeHtml(redirectUrl) + "\">继续跳转</a></p>"
                + "<script>setTimeout(function(){window.location.replace('" + escapeJs(redirectUrl) + "');}, 800);</script>"
                + "</body></html>");
        writer.flush();
    }

    private String escapeHtml(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    private String escapeJs(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\").replace("'", "\\'");
    }
}
