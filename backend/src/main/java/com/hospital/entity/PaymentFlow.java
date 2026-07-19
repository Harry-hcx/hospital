package com.hospital.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 支付流水表
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_payment_flow")
public class PaymentFlow {
    
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 业务订单号（挂号订单号或咨询订单号）
     */
    private String businessOrderNo;
    
    /**
     * 业务类型：1-挂号订单 2-咨询订单
     */
    private Integer businessType;
    
    /**
     * 支付方式：1-支付宝 2-微信
     */
    private Integer payMethod;
    
    /**
     * 第三方交易号（支付宝或微信返回的交易号）
     */
    private String thirdPartyTradeNo;
    
    /**
     * 实付金额
     */
    private BigDecimal actualAmount;
    
    /**
     * 支付状态：0-待支付 1-已支付 2-已退款 3-已关闭
     */
    private Integer payStatus;
    
    /**
     * 支付成功时间
     */
    private LocalDateTime paySuccessTime;
    
    /**
     * 原始回调报文（用于对账和排查问题）
     */
    private String originalCallback;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
