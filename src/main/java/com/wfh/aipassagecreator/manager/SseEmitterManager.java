package com.wfh.aipassagecreator.manager;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.wfh.aipassagecreator.constant.ArticleConstant.SSE_RECONNECT_TIME_MS;
import static com.wfh.aipassagecreator.constant.ArticleConstant.SSE_TIMEOUT_MS;

/**
 * @Title: SseEmiterManager
 * @Author wangfenghuan
 * @Package com.wfh.aipassagecreator.manager
 * @Date 2026/3/12 20:12
 * @description:
 */
@Component
@Slf4j
public class SseEmitterManager {

    private final Map<String, SseEmitter> emitterMap = new ConcurrentHashMap<>();

    public SseEmitter createEmitter(String taskId){
        SseEmitter sseEmitter = new SseEmitter(SSE_TIMEOUT_MS);
        sseEmitter.onTimeout(() -> {
            log.warn("sse连接超时， taskId: {}", taskId);
            emitterMap.remove(taskId);
        });
        // 设置完成回调
        sseEmitter.onCompletion(() -> {
            log.info("SSE连接完成, taskId: {}", taskId);
            emitterMap.remove(taskId);
        });
        // 设置错误回调
        sseEmitter.onError((e) -> {
            log.error("SSE连接错误, taskId: {}", taskId);
            emitterMap.remove(taskId);
        });
        emitterMap.put(taskId, sseEmitter);
        log.info("SSE连接已经创建， taskId: {}", taskId);
        return sseEmitter;
    }

    public void send(String taskId, String message){
        SseEmitter sseEmitter = emitterMap.get(taskId);
        if (sseEmitter == null){
            log.warn("sse不存在");
            return;
        }

        try {
            sseEmitter.send(SseEmitter.event()
                    .data(message)
                    .reconnectTime(SSE_RECONNECT_TIME_MS));
            log.info("消息发送成功");
        } catch (IOException e) {
            log.error("消息发送失败: taskId = {}", taskId);
            emitterMap.remove(taskId);
        }
    }

    /**
     * 完成连接
     *
     * @param taskId 任务ID
     */
    public void complete(String taskId) {
        SseEmitter emitter = emitterMap.get(taskId);
        if (emitter == null) {
            log.warn("SSE Emitter 不存在, taskId={}", taskId);
            return;
        }

        try {
            emitter.complete();
            log.info("SSE 连接已完成, taskId={}", taskId);
        } catch (Exception e) {
            log.error("SSE 连接完成失败, taskId={}", taskId, e);
        } finally {
            emitterMap.remove(taskId);
        }
    }

    /**
     * 检查 Emitter 是否存在
     *
     * @param taskId 任务ID
     * @return 是否存在
     */
    public boolean exists(String taskId) {
        return emitterMap.containsKey(taskId);
    }

}
