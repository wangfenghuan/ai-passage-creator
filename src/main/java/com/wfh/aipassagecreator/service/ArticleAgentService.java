package com.wfh.aipassagecreator.service;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.wfh.aipassagecreator.constant.PromptConstant;
import com.wfh.aipassagecreator.model.dto.article.ArticleState;
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
    private ImageSerchService imageSearchService;

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
        String prompt = PromptConstant.AGENT1_TITLE_PROMPT.replace("{topic}", state.getTopic());
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
                .replace("{subTitle}", state.getTitle().getSubTitle());
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
                .replace("{outline}", outlineText);
        String content = callLlmWithStreaming(prompt, streamHandler, SseMessageTypeEnum.AGENT3_STREAMING);
        state.setContent(content);
        log.info("智能体3:正文生成成功, len = {}", content.length());
    }

    /**
     * 智能体4分析配图需求
     * @param state
     */
    private void agent4AnalyzeRequirements(ArticleState state){
        String prompt = PromptConstant.AGENT4_IMAGE_REQUIREMENTS_PROMPT
                .replace("{mainTitle}", state.getTitle().getMainTitle())
                .replace("{content}", state.getContent());
        String content = callLlm(prompt);
        List<ArticleState.ImageRequirement> imageRequirements = parseJsonListResponse(content, new TypeToken<List<ArticleState.ImageRequirement>>() {
        }, "配图需求");
        state.setImageRequirements(imageRequirements);
        log.info("智能体4:配图需求分析成功, count = {}", imageRequirements.size());
    }

    /**
     * 智能体5生图
     * @param state
     * @param streamHandler
     */
    private void agent5GenImages(ArticleState state, Consumer<String> streamHandler){
        List<ArticleState.ImageResult> imageResults = new ArrayList<>();
        for (ArticleState.ImageRequirement imageRequirement : state.getImageRequirements()) {

            log.info("智能体5:开始检索配图, position= {}, keywords = {}", imageRequirement.getPosition(), imageRequirement.getKeywords());
            // 调用图片检索服务
            String imageUrl = imageSearchService.searchImage(imageRequirement.getKeywords());
            // 降级策略
            ImageMethodEnum method = imageSearchService.getMethod();
            if (imageUrl == null){
                imageUrl = imageSearchService.getFallbackImage(imageRequirement.getPosition());
                method = ImageMethodEnum.PICSUM;
                log.warn("智能体5,图片检索失败，使用降级方案, posotion = {}", imageRequirement.getPosition());
            }
            ArticleState.ImageResult imageResult = buildImageResult(imageRequirement, imageUrl, method);
            imageResults.add(imageResult);
            // 推送单张配图完成
            String imageCompeleteMsg = SseMessageTypeEnum.IMAGE_COMPLETE.getStreamingPrefix() + gson.toJson(imageResult);
            streamHandler.accept(imageCompeleteMsg);
            log.warn("智能体5,图片检索成功，count = {}", imageResults.size());
        }
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

        StringBuilder fullContent = new StringBuilder();

        // 按行处理正文，在章节标题后插入对应图片
        String[] lines = content.split("\n");
        for (String line : lines) {
            fullContent.append(line).append("\n");

            // 检查是否是章节标题（以 ## 开头）
            if (line.startsWith("## ")) {
                String sectionTitle = line.substring(3).trim();
                insertImageAfterSection(fullContent, images, sectionTitle);
            }
        }

        state.setFullContent(fullContent.toString());
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
}
