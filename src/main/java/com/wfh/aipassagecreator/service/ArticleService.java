package com.wfh.aipassagecreator.service;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.service.IService;
import com.wfh.aipassagecreator.model.dto.article.ArticleQueryRequest;
import com.wfh.aipassagecreator.model.dto.article.ArticleState;
import com.wfh.aipassagecreator.model.entity.Article;
import com.wfh.aipassagecreator.model.entity.User;
import com.wfh.aipassagecreator.model.enums.ArticleStatusEnum;
import com.wfh.aipassagecreator.model.vo.ArticleVO;

/**
* @author fenghuanwang
* @description 针对表【article(文章表)】的数据库操作Service
* @createDate 2026-03-11 14:36:19
*/
public interface ArticleService extends IService<Article> {

    String createArticleTask(String topic, User loginUser);

    Article getByTaskId(String taskId);

    void updateArticleStatus(String taskId, ArticleStatusEnum status, String errorMessage);

    void saveArticleContent(String taskId, ArticleState state);

    Page<ArticleVO> listArticleByPage(ArticleQueryRequest request, User loginUser);

    boolean deleteArticle(Long id, User loginUser);

    ArticleVO getArticleDetail(String taskId, User loginUser);
}
