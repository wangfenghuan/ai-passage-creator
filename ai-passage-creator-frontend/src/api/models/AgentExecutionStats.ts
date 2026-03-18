/* generated using openapi-typescript-codegen -- do not edit */
/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { AgentLog } from './AgentLog';
export type AgentExecutionStats = {
    taskId?: string;
    totalDurationMs?: number;
    agentCount?: number;
    agentDurations?: Record<string, number>;
    overallStatus?: string;
    logs?: Array<AgentLog>;
};

