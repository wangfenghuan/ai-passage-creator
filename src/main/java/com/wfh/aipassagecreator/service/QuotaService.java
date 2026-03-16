package com.wfh.aipassagecreator.service;

import com.wfh.aipassagecreator.model.entity.User;

/**
 * @Title: QuotaService
 * @Author wangfenghuan
 * @Package com.wfh.aipassagecreator.service
 * @Date 2026/3/16 16:46
 * @description:
 */
public interface QuotaService {

    /**
     * 检查用户是否有足够的配额
     *
     * @param user 用户
     * @return 是否有配额
     */
    boolean hasQuota(User user);

    /**
     * 消耗配额（扣减1次）
     *
     * @param user 用户
     */
    void consumeQuota(User user);

    /**
     * 检查并消耗配额（原子操作）
     * 如果配额不足会抛出异常
     *
     * @param user 用户
     */
    void checkAndConsumeQuota(User user);

}
