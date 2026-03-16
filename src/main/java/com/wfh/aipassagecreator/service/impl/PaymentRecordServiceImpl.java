package com.wfh.aipassagecreator.service.impl;


import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.Refund;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.stripe.param.RefundCreateParams;
import com.stripe.param.checkout.SessionCreateParams;
import com.wfh.aipassagecreator.common.ErrorCode;
import com.wfh.aipassagecreator.config.StripeConfig;
import com.wfh.aipassagecreator.constant.UserConstant;
import com.wfh.aipassagecreator.exception.BusinessException;
import com.wfh.aipassagecreator.mapper.UserMapper;
import com.wfh.aipassagecreator.model.entity.PaymentRecord;
import com.wfh.aipassagecreator.model.entity.User;
import com.wfh.aipassagecreator.model.enums.PaymentStatusEnum;
import com.wfh.aipassagecreator.model.enums.ProductTypeEnum;
import com.wfh.aipassagecreator.model.enums.UserRoleEnum;
import com.wfh.aipassagecreator.service.PaymentRecordService;
import com.wfh.aipassagecreator.mapper.PaymentRecordMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
* @author fenghuanwang
* @description 针对表【payment_record(支付记录表)】的数据库操作Service实现
* @createDate 2026-03-16 11:15:05
*/
@Service
@Slf4j
public class PaymentRecordServiceImpl extends ServiceImpl<PaymentRecordMapper, PaymentRecord>
    implements PaymentRecordService{

    private static final String CURRENCY_USD = "usd";
    private static final long CENTS_MULTIPLIER = 100L;

    @Resource
    private StripeConfig stripeConfig;

    @Resource
    private UserMapper userMapper;

    @Resource
    private PaymentRecordMapper paymentRecordMapper;

    @Override
    public String createVipPaymentSession(Long userId) throws StripeException {
        User user = getUserOrthrow(userId);
        validateNotVip(user);

        ProductTypeEnum productType = ProductTypeEnum.VIP_PERMANENT;
        Session session = createStripeSession(userId, productType);
        log.info("创建支付会话, userId={}, sessionId={}", userId, session.getId());
        return session.getUrl();
    }

    private void validateNotVip(User user) {
        if (UserConstant.VIP_ROLE.equals(user.getUserRole())) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "您已经是永久会员");
        }
    }

    private User getUserOrthrow(Long userId) {
        User user = userMapper.selectOneById(userId);
        if (user == null){
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "用户不存在");
        }
        return user;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handlePaymentSuccess(Session session) {
        String sessionId = session.getId();
        String userId = session.getMetadata().get("userId");
        String paymentIntentId = session.getPaymentIntent();

        PaymentRecord record = findPaymentRecordBySessionId(sessionId);
        if (record == null) {
            log.warn("支付记录不存在, sessionId={}", sessionId);
            return;
        }

        // 幂等性检查
        if (PaymentStatusEnum.SUCCEEDED.getValue().equals(record.getStatus())) {
            log.info("支付记录已处理, sessionId={}", sessionId);
            return;
        }

        updatePaymentStatus(record.getId(), PaymentStatusEnum.SUCCEEDED, paymentIntentId);
        upgradeUserToVip(Long.valueOf(userId));

        log.info("支付成功，用户已升级为 VIP, userId={}, sessionId={}", userId, sessionId);
    }

    /**
     * 升级用户为 VIP
     */
    private void upgradeUserToVip(Long userId) {
        User user = new User();
        user.setId(userId);
        user.setVipTime(new Date());
        user.setUserRole(UserConstant.VIP_ROLE);
        userMapper.update(user);
    }


    /**
     * 更新支付状态
     * @param id
     * @param paymentStatusEnum
     * @param recordId
     */
    private void updatePaymentStatus(Long id, PaymentStatusEnum paymentStatusEnum, String recordId) {
        PaymentRecord updateRecord = new PaymentRecord();
        updateRecord.setId(Long.valueOf(recordId) );
        updateRecord.setStatus(paymentStatusEnum.getValue());
        updateRecord.setStripePaymentIntentId(recordId);
        paymentRecordMapper.update(updateRecord);
    }

    private PaymentRecord findPaymentRecordBySessionId(String sessionId) {
        return null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean handleRefund(Long userId, String reason) throws StripeException {
        User user = getUserOrThrow(userId);
        validateIsVip(user);

        PaymentRecord paymentRecord = findLatestSuccessfulPayment(userId);
        if (paymentRecord == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "未找到支付记录");
        }

        if (paymentRecord.getStripePaymentIntentId() == null) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "支付记录无效");
        }

        Refund refund = createStripeRefund(paymentRecord.getStripePaymentIntentId());
        if (!"succeeded".equals(refund.getStatus())) {
            return false;
        }

        updateRefundRecord(paymentRecord.getId(), reason);
        revokeVipStatus(userId);

        log.info("退款成功，已取消 VIP 身份, userId={}, refundId={}", userId, refund.getId());
        return true;
    }

    private PaymentRecord findLatestSuccessfulPayment(Long userId) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq("userId", userId)
                .eq("status", PaymentStatusEnum.SUCCEEDED.getValue())
                .eq("productType", ProductTypeEnum.VIP_PERMANENT.getValue())
                .orderBy("createTime", false)
                .limit(1);
        return paymentRecordMapper.selectOneByQuery(queryWrapper);
    }

    private void validateIsVip(User user) {
        String userRole = user.getUserRole();
        if (!userRole.equals(UserConstant.VIP_ROLE)){
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "你还不是vip");
        }
    }

    private Refund createStripeRefund(String stripePaymentIntentId) throws StripeException {
        RefundCreateParams params = RefundCreateParams.builder()
                .setPaymentIntent(stripePaymentIntentId)
                .setReason(RefundCreateParams.Reason.REQUESTED_BY_CUSTOMER)
                .build();
        return Refund.create(params);
    }

    private void updateRefundRecord(Long id, String reason) {
        PaymentRecord updateRecord = new PaymentRecord();
        updateRecord.setId(id);
        updateRecord.setStatus(PaymentStatusEnum.REFUNDED.getValue());
        updateRecord.setRefundTime(new Date());
        updateRecord.setRefundReason(reason);
        paymentRecordMapper.update(updateRecord);
    }

    private User getUserOrThrow(Long userId) {
        User user = userMapper.selectOneById(userId);
        if (user == null){
            throw new  BusinessException(ErrorCode.NOT_FOUND_ERROR, "用户不存在");
        }
        return user;
    }

    /**
     * 撤销用户 VIP 身份
     */
    private void revokeVipStatus(Long userId) {
        User updateUser = new User();
        updateUser.setId(userId);
        updateUser.setVipTime(null);
        updateUser.setUserRole(UserConstant.DEFAULT_ROLE);
        updateUser.setQuota(UserConstant.DEFAULT_QUOTA);
        userMapper.update(updateUser);
    }


    @Override
    public Event constructEvent(String payload, String sigHeader) throws Exception {
        return Webhook.constructEvent(payload, sigHeader, stripeConfig.getWebhookSecret());
    }


    @Override
    public List<PaymentRecord> getPaymentRecords(Long userId) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq("userId", userId)
                .orderBy("createTime", false);
        return paymentRecordMapper.selectListByQuery(queryWrapper);
    }

    /**
     * 创建 Stripe 支付会话
     */
    private Session createStripeSession(Long userId, ProductTypeEnum productType) throws StripeException {
        long amountInCents = productType.getPrice().multiply(new BigDecimal(CENTS_MULTIPLIER)).longValue();

        SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(stripeConfig.getSuccessUrl())
                .setCancelUrl(stripeConfig.getCancelUrl())
                .addLineItem(buildLineItem(productType, amountInCents))
                .putMetadata("userId", String.valueOf(userId))
                .putMetadata("productType", productType.getValue())
                .build();

        return Session.create(params);
    }

    /**
     * 构建支付行项目
     */
    private SessionCreateParams.LineItem buildLineItem(ProductTypeEnum productType, long amountInCents) {
        return SessionCreateParams.LineItem.builder()
                .setPriceData(
                        SessionCreateParams.LineItem.PriceData.builder()
                                .setCurrency(CURRENCY_USD)
                                .setUnitAmount(amountInCents)
                                .setProductData(
                                        SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                .setName(productType.getDescription())
                                                .setDescription("解锁全部高级功能，无限创作配额，终身有效")
                                                .build()
                                )
                                .build()
                )
                .setQuantity(1L)
                .build();
    }


}




