package com.wfh.aipassagecreator.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.wfh.aipassagecreator.config.PexelsConfig;
import com.wfh.aipassagecreator.model.enums.ImageMethodEnum;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.stereotype.Service;

import java.io.IOException;

import static com.wfh.aipassagecreator.constant.ArticleConstant.*;

/**
 * @Title: PexelsService
 * @Author wangfenghuan
 * @Package com.wfh.aipassagecreator.service
 * @Date 2026/3/12 19:47
 * @description:
 */
@Slf4j
@Service
public class PexelsService implements ImageSerchService{

    private final OkHttpClient httpClient = new OkHttpClient();

    @Resource
    private PexelsConfig pexelsConfig;

    @Override
    public String searchImage(String keywords) {
        String url = buildSearchUrl(keywords);
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", pexelsConfig.getApiKey())
                .build();
        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()){
                log.error("API调用失败: {}", response.code());
                return null;
            }
            String responseBody = response.body().string();
        }catch (IOException e){
            log.error("API调用异常: {}", e.getCause().getMessage());
            return null;
        }
        return url;
    }

    /**
     * 构建搜索url
     * @param keywords
     * @return
     */
    private String buildSearchUrl(String keywords) {
        return String.format("%s?query=%s&per_page=%d&orientation=%s",
                PEXELS_API_URL,
                keywords,
                PEXELS_PER_PAGE,
                PEXELS_ORIENTATION_LANDSCAPE);
    }

    @Override
    public ImageMethodEnum getMethod() {
        return ImageMethodEnum.PEXELS;
    }

    @Override
    public String getFallbackImage(int position) {
        return String.format(PICSUM_URL_TEMPLATE, position);
    }

    /**
     * 从响应中提取图片 URL
     *
     * @param responseBody 响应体
     * @param keywords     搜索关键词（用于日志）
     * @return 图片 URL，未找到返回 null
     */
    private String extractImageUrl(String responseBody, String keywords) {
        JsonObject jsonObject = JsonParser.parseString(responseBody).getAsJsonObject();
        JsonArray photos = jsonObject.getAsJsonArray("photos");

        if (photos.isEmpty()) {
            log.warn("Pexels 未检索到图片: {}", keywords);
            return null;
        }

        JsonObject photo = photos.get(0).getAsJsonObject();
        JsonObject src = photo.getAsJsonObject("src");
        return src.get("large").getAsString();
    }
}
