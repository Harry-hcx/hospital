package com.whlg.hospital.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 支付宝沙箱配置。
 */
@Component
public class AlipayConfig {

    public static final String CHANNEL = "ALIPAY";
    public static final String NOTIFY_PATH = "/api/payments/alipay/notify";
    public static final String RETURN_PATH = "/api/payments/alipay/return";

    private final String appId;
    private final String merchantPrivateKey;
    private final String alipayPublicKey;
    private final String gatewayUrl;
    private final String charset;
    private final String signType;
    private final String serverBaseUrl;
    private final String frontendBaseUrl;

    public AlipayConfig(@Value("${alipay.app-id}") String appId,
                        @Value("${alipay.merchant-private-key}") String merchantPrivateKey,
                        @Value("${alipay.alipay-public-key}") String alipayPublicKey,
                        @Value("${alipay.gateway-url}") String gatewayUrl,
                        @Value("${alipay.charset:utf-8}") String charset,
                        @Value("${alipay.sign-type:RSA2}") String signType,
                        @Value("${alipay.server-base-url:http://localhost:8080}") String serverBaseUrl,
                        @Value("${alipay.frontend-base-url:http://localhost:3000}") String frontendBaseUrl) {
        this.appId = appId;
        this.merchantPrivateKey = merchantPrivateKey;
        this.alipayPublicKey = alipayPublicKey;
        this.gatewayUrl = gatewayUrl;
        this.charset = charset;
        this.signType = signType;
        this.serverBaseUrl = trimTrailingSlash(serverBaseUrl);
        this.frontendBaseUrl = trimTrailingSlash(frontendBaseUrl);
    }

    public String getAppId() {
        return appId;
    }

    public String getMerchantPrivateKey() {
        return merchantPrivateKey;
    }

    public String getAlipayPublicKey() {
        return alipayPublicKey;
    }

    public String getGatewayUrl() {
        return gatewayUrl;
    }

    public String getCharset() {
        return charset;
    }

    public String getSignType() {
        return signType;
    }

    public String getNotifyUrl() {
        return serverBaseUrl + NOTIFY_PATH;
    }

    public String getReturnUrl() {
        return serverBaseUrl + RETURN_PATH;
    }

    public String getFrontendBaseUrl() {
        return frontendBaseUrl;
    }

    private String trimTrailingSlash(String value) {
        if (value == null || value.isEmpty()) {
            return "";
        }
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }
}