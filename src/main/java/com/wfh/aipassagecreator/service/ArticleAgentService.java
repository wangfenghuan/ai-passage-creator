package com.wfh.aipassagecreator.service;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.wfh.aipassagecreator.constant.PromptConstant;
import com.wfh.aipassagecreator.model.dto.article.ArticleState;
import com.wfh.aipassagecreator.model.dto.image.ImageRequest;
import com.wfh.aipassagecreator.model.enums.ArticleStyleEnum;
import com.wfh.aipassagecreator.model.enums.ImageMethodEnum;
import com.wfh.aipassagecreator.model.enums.SseMessageTypeEnum;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * @Title: ArticleAgentService
 * @Author wangfenghuan
 * @Package com.wfh.aipassagecreator.service
 * @Date 2026/3/11 14:44
 * @description:
 */
@Service
@Slf4j
public class ArticleAgentService {

    @Resource
    private DashScopeChatModel chatModel;

    @Resource
    private ImageServiceStrategy imageServiceStrategy;

    @Resource
    private S3Service s3Service;

    private final Gson gson = new Gson();



    public void execute(ArticleState state, Consumer<String> streamHandler) {
        log.info("智能体1:生成标题: {}", state.getTaskId());
        agent1GenTitle(state);
        streamHandler.accept(SseMessageTypeEnum.AGENT1_COMPLETE.getValue());
    }


    /**
     * 智能体1生成标题
     * @param state
     */
    private void agent1GenTitle(ArticleState state){
        String prompt = PromptConstant.AGENT1_TITLE_PROMPT.replace("{topic}", state.getTopic()) + getStylePrompt(state.getStyle());
        String content = callLlm(prompt);
        ArticleState.TitleResult titleResult = parseJsonResponse(content, ArticleState.TitleResult.class, "标题");
        state.setTitle(titleResult);
        log.info("智能体1:生成标题成功, mainTitle = {}", titleResult.getMainTitle());
    }

    /**
     * 智能体2生成大纲
     * @param state
     * @param stramHandler
     */
    private void agent2GenOutline(ArticleState state, Consumer<String> stramHandler){
        String prompt = PromptConstant.AGENT2_OUTLINE_PROMPT
                .replace("{mainTitle}", state.getTitle().getMainTitle())
                .replace("{subTitle}", state.getTitle().getSubTitle())
                + getStylePrompt(state.getStyle());
        String content = callLlmWithStreaming(prompt, stramHandler, SseMessageTypeEnum.AGENT2_STREAMING);
        ArticleState.OutlineResult outlineResult = parseJsonResponse(content, ArticleState.OutlineResult.class, "大纲");
        state.setOutline(outlineResult);
        log.info("智能体2: 大纲生成成功, section = {}", outlineResult.getSections().size());
    }

    /**
     * 智能体3流式生成
     * @param state
     * @param streamHandler
     */
    private void agent3GenContent(ArticleState state, Consumer<String> streamHandler){
        String outlineText = gson.toJson(state.getOutline().getSections());
        String prompt = PromptConstant.AGENT3_CONTENT_PROMPT
                .replace("{mainTitle}", state.getTitle().getMainTitle())
                .replace("{subTitle}", state.getTitle().getSubTitle())
                .replace("{outline}", outlineText)
                + getStylePrompt(state.getStyle());
        String content = callLlmWithStreaming(prompt, streamHandler, SseMessageTypeEnum.AGENT3_STREAMING);
        state.setContent(content);
        log.info("智能体3:正文生成成功, len = {}", content.length());
    }

    /**
     * 智能体4分析配图需求
     * @param state
     */
    private void agent4AnalyzeImageRequirements(ArticleState state) {
        // 构建可用配图方式说明
        String availableMethods = buildAvailableMethodsDescription(state.getEnabledImageMethods());

        String prompt = PromptConstant.AGENT4_IMAGE_REQUIREMENTS_PROMPT
                .replace("{mainTitle}", state.getTitle().getMainTitle())
                .replace("{content}", state.getContent())
                .replace("{availableMethods}", availableMethods);

        String content = callLlm(prompt);
        ArticleState.Agent4Result agent4Result = parseJsonResponse(
                content,
                ArticleState.Agent4Result.class,
                "配图需求"
        );

        // 更新正文为包含占位符的版本
        state.setContent(agent4Result.getContentWithPlaceholders());
        state.setImageRequirements(agent4Result.getImageRequirements());
        log.info("智能体4：配图需求分析成功, count={}, 已在正文中插入占位符",
                agent4Result.getImageRequirements().size());
    }


    /**
     * 智能体5生图
     * @param state
     * @param streamHandler
     */
    /**
     * 智能体5：生成配图（串行执行，支持混用多种配图方式，统一上传到 COS）
     */
    private void agent5GenImages(ArticleState state, Consumer<String> streamHandler) {
        List<ArticleState.ImageResult> imageResults = new ArrayList<>();

        for (ArticleState.ImageRequirement requirement : state.getImageRequirements()) {
            String imageSource = requirement.getImageSource();
            log.info("智能体5：开始获取配图, position={}, imageSource={}, keywords={}",
                    requirement.getPosition(), imageSource, requirement.getKeywords());

            // 构建图片请求对象
            ImageRequest imageRequest = ImageRequest.builder()
                    .keywords(requirement.getKeywords())
                    .prompt(requirement.getPrompt())
                    .position(requirement.getPosition())
                    .type(requirement.getType())
                    .build();

            // 使用策略模式获取图片并统一上传到 COS
            ImageServiceStrategy.ImageResult result = imageServiceStrategy.getImageAndUpload(imageSource, imageRequest);

            String cosUrl = result.getUrl();
            ImageMethodEnum method = result.getMethod();

            // 创建配图结果（URL 已经是 COS 地址）
            ArticleState.ImageResult imageResult = buildImageResult(requirement, cosUrl, method);
            imageResults.add(imageResult);

            // 推送单张配图完成
            String imageCompleteMessage = SseMessageTypeEnum.IMAGE_COMPLETE.getStreamingPrefix() + gson.toJson(imageResult);
            streamHandler.accept(imageCompleteMessage);

            log.info("智能体5：配图获取并上传成功, position={}, method={}, cosUrl={}",
                    requirement.getPosition(), method.getValue(), cosUrl);
        }

        state.setImages(imageResults);
        log.info("智能体5：所有配图生成并上传完成, count={}", imageResults.size());
    }


    /**
     * 图文合成：将配图插入正文对应位置
     */
    private void mergeImagesIntoContent(ArticleState state) {
        String content = state.getContent();
        List<ArticleState.ImageResult> images = state.getImages();

        if (images == null || images.isEmpty()) {
            state.setFullContent(content);
            return;
        }

        String fullContent = content;

        // 遍历所有配图，根据占位符替换为实际图片
        for (ArticleState.ImageResult image : images) {
            String placeholder = image.getPlaceholderId();
            if (placeholder != null && !placeholder.isEmpty()) {
                String imageMarkdown = "![" + image.getDescription() + "](" + image.getUrl() + ")";
                fullContent = fullContent.replace(placeholder, imageMarkdown);
            }
        }

        state.setFullContent(fullContent);
        log.info("图文合成完成, fullContentLength={}", fullContent.length());
    }



    /**
     * 同步调用llm
     *
     * @param prompt
     * @return
     */
    private String callLlm(String prompt) {
        ChatResponse response = chatModel.call(new Prompt(new UserMessage(prompt)));
        return response.getResult().getOutput().getText();
    }

    /**
     * 流式调用llm
     *
     * @param prompt
     * @param streamHandler
     * @param messageType
     * @return
     */
    private String callLlmWithStreaming(String prompt, Consumer<String> streamHandler, SseMessageTypeEnum messageType) {
        StringBuilder stringBuilder = new StringBuilder();
        Flux<ChatResponse> responseFlux = chatModel.stream(new Prompt(new UserMessage(prompt)));
        responseFlux.doOnNext(chatResponse -> {
                    String chunk = chatResponse.getResult().getOutput().getText();
                    if (chunk != null && !chunk.isEmpty()) {
                        stringBuilder.append(chunk);
                        streamHandler.accept(messageType.getStreamingPrefix() + chunk);
                    }
                })
                .doOnError(err -> log.error("LLM流式调用失败, messaegType = {}", messageType, err))
                .blockLast();
        return stringBuilder.toString();
    }

    /**
     * 解析json响应
     *
     * @param content
     * @param clazz
     * @param name
     * @param <T>
     * @return
     */
    private <T> T parseJsonResponse(String content, Class<T> clazz, String name) {
        try {
            return gson.fromJson(content, clazz);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 解析json列表响应
     *
     * @param content
     * @param typeToken
     * @param name
     * @param <T>
     * @return
     */
    private <T> T parseJsonListResponse(String content, TypeToken<T> typeToken, String name) {
        try {
            return gson.fromJson(content, typeToken);
        } catch (JsonSyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 构建配图结果
     * @param requirement
     * @param imageUrl
     * @param method
     * @return
     */
    private ArticleState.ImageResult buildImageResult(ArticleState.ImageRequirement requirement,
                                                      String imageUrl,
                                                      ImageMethodEnum method) {
        ArticleState.ImageResult imageResult = new ArticleState.ImageResult();
        imageResult.setPosition(requirement.getPosition());
        imageResult.setUrl(imageUrl);
        imageResult.setMethod(method.getValue());
        imageResult.setKeywords(requirement.getKeywords());
        imageResult.setSectionTitle(requirement.getSectionTitle());
        imageResult.setDescription(requirement.getType());
        imageResult.setPlaceholderId(requirement.getPlaceholderId());  // 记录占位符ID
        return imageResult;
    }


    /**
     * 在章节标题后面插入对应的图片
     * @param fullContent
     * @param imageResults
     * @param sectionTitle
     */
    private void insertImageAfterSection(StringBuilder fullContent,
                                         List<ArticleState.ImageResult> imageResults,
                                         String sectionTitle){
        for (ArticleState.ImageResult imageResult : imageResults) {
            if (imageResult.getPosition() > 1 && imageResult.getSectionTitle() != null && sectionTitle.contains(imageResult.getSectionTitle().trim())){
                fullContent.append("\n![").append(imageResult.getDescription())
                        .append("](").append(imageResult.getUrl()).append(")\n");
                break;
            }
        }
    }

    /**
     * 根据风格获取对应的 Prompt 附加内容
     */
    private String getStylePrompt(String style) {
        if (style == null || style.isEmpty()) {
            return "";
        }

        ArticleStyleEnum styleEnum = ArticleStyleEnum.getEnumByValue(style);
        if (styleEnum == null) {
            return "";
        }

        return switch (styleEnum) {
            case TECH -> PromptConstant.STYLE_TECH_PROMPT;
            case EMOTIONAL -> PromptConstant.STYLE_EMOTIONAL_PROMPT;
            case EDUCATIONAL -> PromptConstant.STYLE_EDUCATIONAL_PROMPT;
            case HUMOROUS -> PromptConstant.STYLE_HUMOROUS_PROMPT;
        };
    }

    /**
     * 获取所有配图方式的完整描述
     */
    private String getAllMethodsDescription() {
        return """
               - PEXELS: 适合真实场景、产品照片、人物照片、自然风景等写实图片
               - NANO_BANANA: 适合创意插画、信息图表、需要文字渲染、抽象概念、艺术风格等 AI 生成图片
               - MERMAID: 适合流程图、架构图、时序图、关系图、甘特图等结构化图表
               - ICONIFY: 适合图标、符号、小型装饰性图标（如：箭头、勾选、星星、心形等）
               - EMOJI_PACK: 适合表情包、搞笑图片、轻松幽默的配图
               - SVG_DIAGRAM: 适合概念示意图、思维导图样式、逻辑关系展示（不涉及精确数据）
               """;
    }

    /**
     * 构建可用配图方式说明
     */
    private String buildAvailableMethodsDescription(List<String> enabledMethods) {
        // 如果为空或 null，表示支持所有方式
        if (enabledMethods == null || enabledMethods.isEmpty()) {
            return getAllMethodsDescription();
        }

        // 只描述允许的方式
        StringBuilder sb = new StringBuilder();
        for (String method : enabledMethods) {
            ImageMethodEnum methodEnum = ImageMethodEnum.getByValue(method);
            if (methodEnum != null && !methodEnum.isFallback()) {
                sb.append("   - ").append(methodEnum.getValue())
                        .append(": ").append(getMethodUsageDescription(methodEnum))
                        .append("\n");
            }
        }
        return sb.toString();
    }

    /**
     * 获取配图方式的使用说明
     */
    private String getMethodUsageDescription(ImageMethodEnum method) {
        return switch (method) {
            case PEXELS -> "适合真实场景、产品照片、人物照片、自然风景等写实图片";
            case NANO_BANANA -> "适合创意插画、信息图表、需要文字渲染、抽象概念、艺术风格等 AI 生成图片";
            case MERMAID -> "适合流程图、架构图、时序图、关系图、甘特图等结构化图表";
            case ICONIFY -> "适合图标、符号、小型装饰性图标（如：箭头、勾选、星星、心形等）";
            case EMOJI_PACK -> "适合表情包、搞笑图片、轻松幽默的配图";
            case SVG_DIAGRAM -> "适合概念示意图、思维导图样式、逻辑关系展示（不涉及精确数据）";
            default -> method.getDescription();
        };
    }
}
