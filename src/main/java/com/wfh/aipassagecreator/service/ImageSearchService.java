package com.wfh.aipassagecreator.service;

import com.wfh.aipassagecreator.model.dto.image.ImageData;
import com.wfh.aipassagecreator.model.dto.image.ImageRequest;
import com.wfh.aipassagecreator.model.enums.ImageMethodEnum;

/**
 * @Title: ImageSerchService
 * @Author wangfenghuan
 * @Package com.wfh.aipassagecreator.service
 * @Date 2026/3/12 11:03
 * @description:
 */
public interface ImageSearchService {

    /**
     * 根据请求获取图片
     * @param request
     * @return
     */
    default String getImage(ImageRequest request){
        String param = request.getEffectiveParam(getMethod().isAiGenerated());
        return searchImage(param);
    }

    /**
     * 获取图片数据
     * @param request
     * @return
     */
    default ImageData getImageData(ImageRequest request){
        String url = getImage(request);
        return ImageData.fromUrl(url);
    }

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

    /**
     * 服务是否可用
     * @return
     */
    default boolean isAvailable(){
        return true;
    }

}
