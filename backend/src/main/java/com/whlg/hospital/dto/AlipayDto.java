package com.whlg.hospital.dto;

import lombok.Data;

/**
 * 兼容既有前端字段命名的支付宝支付请求。
 */
@Data
public class AlipayDto {

    /**
     * 商户业务订单号。
     */
    private String widout_trade_no;

    /**
     * 支付金额。
     */
    private String widtotal_amount;

    /**
     * 订单标题。
     */
    private String widsubject;

    /**
     * 订单描述。
     */
    private String widbody;

    public String getBusinessOrderNo() {
        return widout_trade_no;
    }

    public String getAmount() {
        return widtotal_amount;
    }

    public String getSubject() {
        return widsubject;
    }

    public String getBody() {
        return widbody;
    }
}