/* generated using openapi-typescript-codegen -- do not edit */
/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { BaseResponseBoolean } from '../models/BaseResponseBoolean';
import type { BaseResponseListPaymentRecord } from '../models/BaseResponseListPaymentRecord';
import type { BaseResponseString } from '../models/BaseResponseString';
import type { CancelablePromise } from '../core/CancelablePromise';
import { OpenAPI } from '../core/OpenAPI';
import { request as __request } from '../core/request';
export class PaymentControllerService {
    /**
     * 申请退款
     * @param reason
     * @returns BaseResponseBoolean OK
     * @throws ApiError
     */
    public static refund(
        reason?: string,
    ): CancelablePromise<BaseResponseBoolean> {
        return __request(OpenAPI, {
            method: 'POST',
            url: '/payment/refund',
            query: {
                'reason': reason,
            },
        });
    }
    /**
     * 创建 VIP 支付会话
     * @returns BaseResponseString OK
     * @throws ApiError
     */
    public static createVipPaymentSession(): CancelablePromise<BaseResponseString> {
        return __request(OpenAPI, {
            method: 'POST',
            url: '/payment/create-vip-session',
        });
    }
    /**
     * 获取当前用户支付记录
     * @returns BaseResponseListPaymentRecord OK
     * @throws ApiError
     */
    public static getPaymentRecords(): CancelablePromise<BaseResponseListPaymentRecord> {
        return __request(OpenAPI, {
            method: 'GET',
            url: '/payment/records',
        });
    }
}
