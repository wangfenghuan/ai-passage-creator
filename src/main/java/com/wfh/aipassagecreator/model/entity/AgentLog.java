package com.wfh.aipassagecreator.model.entity;

import java.util.Date;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.mybatisflex.core.keygen.KeyGenerators;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 智能体执行日志表
 * @TableName agent_log
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(value = "agent_log", camelToUnderline = false)
public class AgentLog {
    /**
     * id
     */
    @Id(keyType = KeyType.Generator, value = KeyGenerators.snowFlakeId)
    private Long id;

    /**
     * 任务ID
     */
    private String taskId;

    /**
     * 智能体名称
     */
    private String agentName;

    /**
     * 开始时间
     */
    private Date startTime;

    /**
     * 结束时间
     */
    private Date endTime;

    /**
     * 耗时（毫秒）
     */
    private Integer durationMs;

    /**
     * 状态：SUCCESS/FAILED
     */
    private String status;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 使用的Prompt
     */
    private String prompt;

    /**
     * 输入数据（JSON格式）
     */
    private Object inputData;

    /**
     * 输出数据（JSON格式）
     */
    private Object outputData;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 是否删除
     */
    @Column(isLogicDelete = true)
    private Integer isDelete;

}