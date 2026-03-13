package com.wfh.aipassagecreator.controller;

import com.mybatisflex.core.paginate.Page;
import com.wfh.aipassagecreator.annotation.AuthCheck;
import com.wfh.aipassagecreator.common.BaseResponse;
import com.wfh.aipassagecreator.common.DeleteRequest;
import com.wfh.aipassagecreator.common.ErrorCode;
import com.wfh.aipassagecreator.common.ResultUtils;
import com.wfh.aipassagecreator.exception.ThrowUtils;
import com.wfh.aipassagecreator.manager.SseEmitterManager;
import com.wfh.aipassagecreator.model.dto.article.ArticleCreateRequest;
import com.wfh.aipassagecreator.model.dto.article.ArticleQueryRequest;
import com.wfh.aipassagecreator.model.entity.User;
import com.wfh.aipassagecreator.model.enums.ArticleStyleEnum;
import com.wfh.aipassagecreator.model.vo.ArticleVO;
import com.wfh.aipassagecreator.service.ArticleAsyncService;
import com.wfh.aipassagecreator.service.ArticleService;
import com.wfh.aipassagecreator.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/article")
@Tag(name = "文章接口")
@Slf4j
public class ArticleController {

    @Resource
    private ArticleService articleService;

    @Resource
    private ArticleAsyncService articleAsyncService;

    @Resource
    private SseEmitterManager sseEmitterManager;

    @Resource
    private UserService userService;



    @PostMapping("/create")
    public BaseResponse<String> create(@RequestBody ArticleCreateRequest request, HttpServletRequest httpRequest) {
        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(request.getTopic() == null || request.getTopic().trim().isEmpty(),
                ErrorCode.PARAMS_ERROR, "选题不能为空");
        // 校验风格参数（允许为空）
        ThrowUtils.throwIf(!ArticleStyleEnum.isValid(request.getStyle()),
                ErrorCode.PARAMS_ERROR, "无效的文章风格");

        User loginUser = userService.getLoginUser(httpRequest);

        // 创建任务（包含风格参数）
        String taskId = articleService.createArticleTask(
                request.getTopic(),
                loginUser,
                request.getStyle()
                );

        // 异步执行（传递风格和配图方式）
        articleAsyncService.executeAritcleGen(
                taskId,
                request.getTopic(),
                request.getStyle(),
                request.getEnabledImageMethods());

        return ResultUtils.success(taskId);
    }


    /**
     * SSE 进度推送
     */
    @GetMapping("/progress/{taskId}")
    @Operation(summary = "获取文章生成进度(SSE)")
    public SseEmitter getProgress(@PathVariable String taskId, HttpServletRequest httpServletRequest) {
        ThrowUtils.throwIf(taskId == null || taskId.trim().isEmpty(),
                ErrorCode.PARAMS_ERROR, "任务ID不能为空");

        // 校验权限（内部会检查任务是否存在以及用户是否有权限访问）
        User loginUser = userService.getLoginUser(httpServletRequest);
        articleService.getArticleDetail(taskId, loginUser);

        // 创建 SSE Emitter
        SseEmitter emitter = sseEmitterManager.createEmitter(taskId);

        log.info("SSE 连接已建立, taskId={}", taskId);
        return emitter;
    }

    /**
     * 获取文章详情
     */
    @GetMapping("/{taskId}")
    @Operation(summary = "获取文章详情")
    @AuthCheck(mustRole = "user")
    public BaseResponse<ArticleVO> getArticle(@PathVariable String taskId,
                                              HttpServletRequest httpServletRequest) {
        ThrowUtils.throwIf(taskId == null || taskId.trim().isEmpty(),
                ErrorCode.PARAMS_ERROR, "任务ID不能为空");

        User loginUser = userService.getLoginUser(httpServletRequest);
        ArticleVO articleVO = articleService.getArticleDetail(taskId, loginUser);

        return ResultUtils.success(articleVO);
    }

    /**
     * 分页查询文章列表
     */
    @PostMapping("/list")
    @Operation(summary = "分页查询文章列表")
    @AuthCheck(mustRole = "user")
    public BaseResponse<Page<ArticleVO>> listArticle(@RequestBody ArticleQueryRequest request,
                                                     HttpServletRequest httpServletRequest) {
        User loginUser = userService.getLoginUser(httpServletRequest);
        Page<ArticleVO> articleVOPage = articleService.listArticleByPage(request, loginUser);

        return ResultUtils.success(articleVOPage);
    }

    /**
     * 删除文章
     */
    @PostMapping("/delete")
    @Operation(summary = "删除文章")
    @AuthCheck(mustRole = "user")
    public BaseResponse<Boolean> deleteArticle(@RequestBody DeleteRequest deleteRequest,
                                               HttpServletRequest httpServletRequest) {
        ThrowUtils.throwIf(deleteRequest == null || deleteRequest.getId() == null,
                ErrorCode.PARAMS_ERROR);

        User loginUser = userService.getLoginUser(httpServletRequest);
        boolean result = articleService.deleteArticle(deleteRequest.getId(), loginUser);

        return ResultUtils.success(result);
    }


}
