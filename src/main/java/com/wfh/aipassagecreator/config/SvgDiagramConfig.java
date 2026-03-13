package com.wfh.aipassagecreator.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * SVG 概念示意图生成配置
 *
 */
@Configuration
@ConfigurationProperties(prefix = "svg-diagram")
@Data
public class SvgDiagramConfig {

    /**
     * 默认宽度
     */
    private Integer defaultWidth = 600;

    /**
     * 默认高度
     */
    private Integer defaultHeight = 600;

    /**
     * COS 存储文件夹
     */
    private String folder = "svg-diagrams";
}
