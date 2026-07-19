package com.whlg.hospital.util;

import com.aliyun.dypnsapi20170525.Client;
import com.aliyun.dypnsapi20170525.models.SendSmsVerifyCodeRequest;
import com.aliyun.dypnsapi20170525.models.SendSmsVerifyCodeResponse;
import com.aliyun.dypnsapi20170525.models.SendSmsVerifyCodeResponseBody;
import com.aliyun.teaopenapi.models.Config;
import com.aliyun.teautil.models.RuntimeOptions;
import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AliyunSmsUtil {

    private static final Gson GSON = new Gson();

    private final String accessKeyId;
    private final String accessKeySecret;
    private final String endpoint;

    public AliyunSmsUtil(@Value("${aliyun.sms.access-key-id:}") String accessKeyId,
                         @Value("${aliyun.sms.access-key-secret:}") String accessKeySecret,
                         @Value("${aliyun.sms.endpoint:dypnsapi.aliyuncs.com}") String endpoint) {
        this.accessKeyId = accessKeyId;
        this.accessKeySecret = accessKeySecret;
        this.endpoint = endpoint;
    }

    public SmsSendResult sendSmsVerifyCode(String phoneNumber, String signName,
                                           String templateCode, String templateParam,
                                           String outId) throws Exception {
        validateConfig();

        SendSmsVerifyCodeRequest request = new SendSmsVerifyCodeRequest()
                .setPhoneNumber(phoneNumber)
                .setCountryCode("86")
                .setSignName(signName)
                .setTemplateCode(templateCode)
                .setTemplateParam(templateParam)
                .setCodeLength(6L)
                .setCodeType(1L)
                .setValidTime(300L)
                .setInterval(60L)
                .setDuplicatePolicy(1L)
                .setReturnVerifyCode(true);

        if (!isBlank(outId)) {
            request.setOutId(outId);
        }

        RuntimeOptions runtime = new RuntimeOptions();
        SendSmsVerifyCodeResponse response = createClient().sendSmsVerifyCodeWithOptions(request, runtime);
        if (response == null || response.getBody() == null) {
            throw new IllegalStateException("阿里云短信发送失败: 响应为空");
        }
        SendSmsVerifyCodeResponseBody body = response.getBody();
        String responseCode = body.getCode();
        if (!Boolean.TRUE.equals(body.getSuccess()) || !"OK".equalsIgnoreCase(responseCode)) {
            throw new IllegalStateException("阿里云短信发送失败: code="
                    + responseCode + ", message=" + body.getMessage());
        }
        String verifyCode = body.getModel() == null ? null : body.getModel().getVerifyCode();
        if (isBlank(verifyCode)) {
            throw new IllegalStateException("阿里云短信发送失败: 未返回验证码");
        }
        return new SmsSendResult(verifyCode, GSON.toJson(response));
    }

    public SmsSendResult sendSmsVerifyCode(String phoneNumber, String signName,
                                           String templateCode, String templateParam) throws Exception {
        return sendSmsVerifyCode(phoneNumber, signName, templateCode, templateParam, null);
    }

    public static String generateVerificationCode() {
        int code = (int) ((Math.random() * 9 + 1) * 100000);
        return String.format("%06d", code);
    }

    private Client createClient() throws Exception {
        Config config = new Config()
                .setAccessKeyId(accessKeyId)
                .setAccessKeySecret(accessKeySecret);
        config.endpoint = endpoint;
        return new Client(config);
    }

    private void validateConfig() {
        if (isBlank(accessKeyId) || isBlank(accessKeySecret)) {
            throw new IllegalStateException("阿里云短信 AccessKey 未配置");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    public static class SmsSendResult {
        private final String verifyCode;
        private final String providerResponse;

        public SmsSendResult(String verifyCode, String providerResponse) {
            this.verifyCode = verifyCode;
            this.providerResponse = providerResponse;
        }

        public String getVerifyCode() {
            return verifyCode;
        }

        public String getProviderResponse() {
            return providerResponse;
        }
    }
}
