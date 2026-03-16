package com.wfh.aipassagecreator.model.entity;


import java.math.BigDecimal;
import java.util.Date;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.mybatisflex.core.keygen.KeyGenerators;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 支付记录表
 * @TableName payment_record
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(value = "payment_record", camelToUnderline = false)
public class PaymentRecord {
    /**
     * 主键
     */
    @Id(keyType = KeyType.Generator, value = KeyGenerators.snowFlakeId)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * Stripe Checkout Session ID
     */
    private String stripeSessionId;

    /**
     * Stripe 支付意向ID
     */
    private String stripePaymentIntentId;

    /**
     * 金额（美元）
     */
    private BigDecimal amount;

    /**
     * 货币
     */
    private String currency;

    /**
     * 状态：PENDING/SUCCEEDED/FAILED/REFUNDED
     */
    private String status;

    /**
     * 产品类型：VIP_PERMANENT
     */
    private String productType;

    /**
     * 描述
     */
    private String description;

    /**
     * 退款时间
     */
    private Date refundTime;

    /**
     * 退款原因
     */
    private String refundReason;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

}