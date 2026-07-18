package com.whlg.hospital.util;

import com.aliyun.dysmsapi20170525.Client;
import com.aliyun.dysmsapi20170525.models.SendSmsRequest;
import com.aliyun.dysmsapi20170525.models.SendSmsResponse;
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
                         @Value("${aliyun.sms.endpoint:dysmsapi.aliyuncs.com}") String endpoint) {
        this.accessKeyId = accessKeyId;
        this.accessKeySecret = accessKeySecret;
        this.endpoint = endpoint;
    }

    public String sendSmsVerifyCode(String phoneNumber, String signName,
                                    String templateCode, String templateParam,
                                    String outId) throws Exception {
        validateConfig();

        SendSmsRequest request = new SendSmsRequest()
                .setPhoneNumbers(phoneNumber)
                .setSignName(signName)
                .setTemplateCode(templateCode)
                .setTemplateParam(templateParam);

        if (!isBlank(outId)) {
            request.setOutId(outId);
        }

        RuntimeOptions runtime = new RuntimeOptions();
        SendSmsResponse response = createClient().sendSmsWithOptions(request, runtime);
        return GSON.toJson(response);
    }

    public String sendSmsVerifyCode(String phoneNumber, String signName,
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
}