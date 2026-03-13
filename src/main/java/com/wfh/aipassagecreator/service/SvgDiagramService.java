package com.wfh.aipassagecreator.service;

import cn.hutool.core.util.StrUtil;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.wfh.aipassagecreator.config.SvgDiagramConfig;
import com.wfh.aipassagecreator.constant.PromptConstant;
import com.wfh.aipassagecreator.model.dto.image.ImageData;
import com.wfh.aipassagecreator.model.dto.image.ImageRequest;
import com.wfh.aipassagecreator.model.enums.ImageMethodEnum;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;

import static com.wfh.aipassagecreator.constant.ArticleConstant.PICSUM_URL_TEMPLATE;

/**
 * SVG 概念示意图生成服务
 * 使用 AI 生成 SVG 代码，适合概念示意、思维导图样式、关系展示等场景
 */
@Service
@Slf4j
public class SvgDiagramService implements ImageSearchService {

    @Resource
    private SvgDiagramConfig svgDiagramConfig;

    @Resource
    private DashScopeChatModel chatModel;

    @Override
    public String searchImage(String keywords) {
        // 此方法已废弃，请使用 getImageData()
        return null;
    }

    @Override
    public ImageData getImageData(ImageRequest request) {
        String requirement = request.getEffectiveParam(true);
        return generateSvgDiagramData(requirement);
    }

    /**
     * 生成 SVG 概念示意图数据
     *
     * @param requirement 示意图需求描述
     * @return ImageData 包含 SVG 字节数据，生成失败返回 null
     */
    public ImageData generateSvgDiagramData(String requirement) {
        if (StrUtil.isBlank(requirement)) {
            log.warn("SVG 图表需求为空");
            return null;
        }

        try {
            // 1. 调用 LLM 生成 SVG 代码
            String svgCode = callLlmToGenerateSvg(requirement);

            if (StrUtil.isBlank(svgCode)) {
                log.error("LLM 未生成 SVG 代码");
                return null;
            }

            // 2. 验证 SVG 格式
            if (!isValidSvg(svgCode)) {
                log.error("生成的 SVG 代码格式无效");
                return null;
            }

            // 3. 转换为字节数据
            byte[] svgBytes = svgCode.getBytes(StandardCharsets.UTF_8);
            
            log.info("SVG 概念示意图生成成功, size={} bytes", svgBytes.length);
            return ImageData.fromBytes(svgBytes, "image/svg+xml");

        } catch (Exception e) {
            log.error("SVG 概念示意图生成异常, requirement={}", requirement, e);
            return null;
        }
    }

    private boolean isValidSvg(String svgCode) {
        if (svgCode == null || svgCode.trim().isEmpty()) {
            return false;
        }
        try {
            // 1. 创建解析器
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true); // 开启命名空间支持
            // 【重要】简单禁止外部实体，防止安全风险
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            // 2. 尝试解析
            Document doc = builder.parse(new InputSource(new StringReader(svgCode)));
            // 3. 检查根节点是否为 "svg"
            String rootTag = doc.getDocumentElement().getTagName();
            return "svg".equalsIgnoreCase(rootTag);

        } catch (Exception e) {
            // 任何异常（XML格式错、IO错等）都视为无效
            return false;
        }
    }

    /**
     * 调用 LLM 生成 SVG 代码
     */
    private String callLlmToGenerateSvg(String requirement) {
        String prompt = PromptConstant.SVG_DIAGRAM_GENERATION_PROMPT
                .replace("{requirement}", requirement);

        log.info("开始调用 LLM 生成 SVG 概念示意图");

        ChatResponse response = chatModel.call(new Prompt(new UserMessage(prompt)));
        String svgCode = response.getResult().getOutput().getText().trim();

        // 提取 SVG 代码（移除可能的 markdown 代码块标记）
        svgCode = extractSvgCode(svgCode);

        return svgCode;
    }

    /**
     * 提取 SVG 代码（去除 markdown 代码块）
     */
    private String extractSvgCode(String text) {
        if (text == null) {
            return null;
        }

        // 去除 markdown 代码块标记
        text = text.replace("```xml", "").replace("```svg", "").replace("```", "").trim();

        // 确保包含 XML 声明
        if (!text.startsWith("<?xml")) {
            // 如果没有 XML 声明但有 <svg 标签，添加声明
            if (text.contains("<svg")) {
                text = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + text;
            }
        }

        return text;
    }

    @Override
    public ImageMethodEnum getMethod() {
        return ImageMethodEnum.SVG_DIAGRAM;
    }

    @Override
    public String getFallbackImage(int position) {
        return String.format(PICSUM_URL_TEMPLATE, position);
    }
}
