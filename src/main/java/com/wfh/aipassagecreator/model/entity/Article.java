package com.wfh.aipassagecreator.model.entity;

import java.io.Serializable;
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
 * 文章表
 * @TableName article
 */
@Table(value = "article", camelToUnderline = false)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Article implements Serializable {
    /**
     * id
     */
    @Id(keyType = KeyType.Generator, value = KeyGenerators.snowFlakeId)
    private Long id;

    /**
     * 任务ID（UUID）
     */
    private String taskId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 选题
     */
    private String topic;

    /**
     * 主标题
     */
    private String mainTitle;

    /**
     * 副标题
     */
    private String subTitle;

    /**
     * 大纲（JSON格式）
     */
    private Object outline;

    /**
     * 正文（Markdown格式）
     */
    private String content;

    /**
     * 完整图文（Markdown格式，含配图）
     */
    private String fullContent;

    /**
     * 封面图 URL
     */
    private String coverImage;

    /**
     * 配图列表（JSON数组）
     */
    private Object images;

    /**
     * 状态：PENDING/PROCESSING/COMPLETED/FAILED
     */
    private String status;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 完成时间
     */
    private Date completedTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 是否删除
     */
    @Column(isLogicDelete = true)
    private Integer isDelete;

    private static final long serialVersionUID = 1L;

}