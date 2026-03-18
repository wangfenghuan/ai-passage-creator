/* generated using openapi-typescript-codegen -- do not edit */
/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
export type PaymentRecord = {
    id?: number;
    userId?: number;
    stripeSessionId?: string;
    stripePaymentIntentId?: string;
    amount?: number;
    currency?: string;
    status?: string;
    productType?: string;
    description?: string;
    refundTime?: string;
    refundReason?: string;
    createTime?: string;
    updateTime?: string;
};

