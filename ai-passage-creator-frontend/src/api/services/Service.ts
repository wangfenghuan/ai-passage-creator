/* generated using openapi-typescript-codegen -- do not edit */
/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { ArticleAiModifyOutlineRequest } from '../models/ArticleAiModifyOutlineRequest';
import type { ArticleConfirmOutlineRequest } from '../models/ArticleConfirmOutlineRequest';
import type { ArticleConfirmTitleRequest } from '../models/ArticleConfirmTitleRequest';
import type { ArticleCreateRequest } from '../models/ArticleCreateRequest';
import type { ArticleQueryRequest } from '../models/ArticleQueryRequest';
import type { BaseResponseAgentExecutionStats } from '../models/BaseResponseAgentExecutionStats';
import type { BaseResponseArticleVO } from '../models/BaseResponseArticleVO';
import type { BaseResponseBoolean } from '../models/BaseResponseBoolean';
import type { BaseResponseListOutlineSection } from '../models/BaseResponseListOutlineSection';
import type { BaseResponsePageArticleVO } from '../models/BaseResponsePageArticleVO';
import type { BaseResponseString } from '../models/BaseResponseString';
import type { BaseResponseVoid } from '../models/BaseResponseVoid';
import type { DeleteRequest } from '../models/DeleteRequest';
import type { SseEmitter } from '../models/SseEmitter';
import type { CancelablePromise } from '../core/CancelablePromise';
import { OpenAPI } from '../core/OpenAPI';
import { request as __request } from '../core/request';
export class Service {
    /**
     * 分页查询文章列表
     * @param requestBody
     * @returns BaseResponsePageArticleVO OK
     * @throws ApiError
     */
    public static listArticle(
        requestBody: ArticleQueryRequest,
    ): CancelablePromise<BaseResponsePageArticleVO> {
        return __request(OpenAPI, {
            method: 'POST',
            url: '/article/list',
            body: requestBody,
            mediaType: 'application/json',
        });
    }
    /**
     * 删除文章
     * @param requestBody
     * @returns BaseResponseBoolean OK
     * @throws ApiError
     */
    public static deleteArticle(
        requestBody: DeleteRequest,
    ): CancelablePromise<BaseResponseBoolean> {
        return __request(OpenAPI, {
            method: 'POST',
            url: '/article/delete',
            body: requestBody,
            mediaType: 'application/json',
        });
    }
    /**
     * @param requestBody
     * @returns BaseResponseString OK
     * @throws ApiError
     */
    public static create(
        requestBody: ArticleCreateRequest,
    ): CancelablePromise<BaseResponseString> {
        return __request(OpenAPI, {
            method: 'POST',
            url: '/article/create',
            body: requestBody,
            mediaType: 'application/json',
        });
    }
    /**
     * 确认标题并输入补充描述
     * @param requestBody
     * @returns BaseResponseVoid OK
     * @throws ApiError
     */
    public static confirmTitle(
        requestBody: ArticleConfirmTitleRequest,
    ): CancelablePromise<BaseResponseVoid> {
        return __request(OpenAPI, {
            method: 'POST',
            url: '/article/confirm-title',
            body: requestBody,
            mediaType: 'application/json',
        });
    }
    /**
     * 确认大纲
     * @param requestBody
     * @returns BaseResponseVoid OK
     * @throws ApiError
     */
    public static confirmOutline(
        requestBody: ArticleConfirmOutlineRequest,
    ): CancelablePromise<BaseResponseVoid> {
        return __request(OpenAPI, {
            method: 'POST',
            url: '/article/confirm-outline',
            body: requestBody,
            mediaType: 'application/json',
        });
    }
    /**
     * AI 修改大纲
     * @param requestBody
     * @returns BaseResponseListOutlineSection OK
     * @throws ApiError
     */
    public static aiModifyOutline(
        requestBody: ArticleAiModifyOutlineRequest,
    ): CancelablePromise<BaseResponseListOutlineSection> {
        return __request(OpenAPI, {
            method: 'POST',
            url: '/article/ai-modify-outline',
            body: requestBody,
            mediaType: 'application/json',
        });
    }
    /**
     * 获取文章详情
     * @param taskId
     * @returns BaseResponseArticleVO OK
     * @throws ApiError
     */
    public static getArticle(
        taskId: string,
    ): CancelablePromise<BaseResponseArticleVO> {
        return __request(OpenAPI, {
            method: 'GET',
            url: '/article/{taskId}',
            path: {
                'taskId': taskId,
            },
        });
    }
    /**
     * 获取文章生成进度(SSE)
     * @param taskId
     * @returns SseEmitter OK
     * @throws ApiError
     */
    public static getProgress(
        taskId: string,
    ): CancelablePromise<SseEmitter> {
        return __request(OpenAPI, {
            method: 'GET',
            url: '/article/progress/{taskId}',
            path: {
                'taskId': taskId,
            },
        });
    }
    /**
     * 获取任务执行日志
     * @param taskId
     * @returns BaseResponseAgentExecutionStats OK
     * @throws ApiError
     */
    public static getExecutionLogs(
        taskId: string,
    ): CancelablePromise<BaseResponseAgentExecutionStats> {
        return __request(OpenAPI, {
            method: 'GET',
            url: '/article/execution-logs/{taskId}',
            path: {
                'taskId': taskId,
            },
        });
    }
}
