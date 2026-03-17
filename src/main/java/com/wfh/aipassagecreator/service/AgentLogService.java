package com.wfh.aipassagecreator.service;

import com.mybatisflex.core.service.IService;
import com.wfh.aipassagecreator.model.entity.AgentLog;
import com.wfh.aipassagecreator.model.vo.AgentExecutionStats;

import java.util.List;

/**
* @author fenghuanwang
* @description 针对表【agent_log(智能体执行日志表)】的数据库操作Service
* @createDate 2026-03-16 20:45:06
*/
public interface AgentLogService extends IService<AgentLog> {

    /**
     * 异步保存日志
     *
     * @param log 日志对象
     */
    void saveLogAsync(AgentLog log);

    /**
     * 根据任务ID获取所有日志
     *
     * @param taskId 任务ID
     * @return 日志列表
     */
    List<AgentLog> getLogsByTaskId(String taskId);

    /**
     * 获取任务执行统计信息
     *
     * @param taskId 任务ID
     * @return 执行统计
     */
    AgentExecutionStats getExecutionStats(String taskId);
}
