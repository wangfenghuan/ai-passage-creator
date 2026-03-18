/* generated using openapi-typescript-codegen -- do not edit */
/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { BaseResponseStatisticsVO } from '../models/BaseResponseStatisticsVO';
import type { CancelablePromise } from '../core/CancelablePromise';
import { OpenAPI } from '../core/OpenAPI';
import { request as __request } from '../core/request';
export class StatisticsControllerService {
    /**
     * 获取系统统计数据
     * @returns BaseResponseStatisticsVO OK
     * @throws ApiError
     */
    public static getStatistics(): CancelablePromise<BaseResponseStatisticsVO> {
        return __request(OpenAPI, {
            method: 'GET',
            url: '/statistics/overview',
        });
    }
}
