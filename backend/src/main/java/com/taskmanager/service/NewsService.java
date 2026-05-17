package com.taskmanager.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.taskmanager.dto.NewsQuery;
import com.taskmanager.dto.NewsResponse;

import java.util.List;

public interface NewsService {
    IPage<NewsResponse> listNews(NewsQuery query);

    int refreshNews();

    List<NewsResponse> getNewsByTaskId(Long taskId);

    void linkTaskToNews(Long taskId, Long newsId);

    void unlinkTaskFromNews(Long taskId, Long newsId);

    List<NewsResponse> getRelatedToUser();

    List<String> getSources();
}
