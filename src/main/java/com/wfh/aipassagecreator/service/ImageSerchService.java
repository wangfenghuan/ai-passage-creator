package com.wfh.aipassagecreator.service;

import com.wfh.aipassagecreator.model.enums.ImageMethodEnum;
import org.springframework.stereotype.Service;

/**
 * @Title: ImageSerchService
 * @Author wangfenghuan
 * @Package com.wfh.aipassagecreator.service
 * @Date 2026/3/12 11:03
 * @description:
 */
public interface ImageSerchService {

    /**
     * 根据关键词检索图片
     * @param keywords
     * @return
     */
    String searchImage(String keywords);

    /**
     * 获取图片的检索方式
     * @return
     */
    ImageMethodEnum getMethod();

    /**
     * 获取降级图片URL
     * @param position
     * @return
     */
    String getFallbackImage(int position);

}
