package com.taskmanager.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.taskmanager.common.Result;
import com.taskmanager.dto.NewsQuery;
import com.taskmanager.dto.NewsResponse;
import com.taskmanager.service.NewsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/news")
@RequiredArgsConstructor
public class NewsController {

    private final NewsService newsService;

    @GetMapping
    public Result<IPage<NewsResponse>> listNews(NewsQuery query) {
        return Result.success(newsService.listNews(query));
    }

    @GetMapping("/search")
    public Result<IPage<NewsResponse>> searchNews(@RequestParam String keyword,
                                                   @RequestParam(defaultValue = "1") int page,
                                                   @RequestParam(defaultValue = "10") int pageSize) {
        NewsQuery query = new NewsQuery();
        query.setKeyword(keyword);
        query.setPage(page);
        query.setPageSize(pageSize);
        return Result.success(newsService.listNews(query));
    }

    @PostMapping("/refresh")
    public Result<Integer> refreshNews() {
        int count = newsService.refreshNews();
        return Result.success(count);
    }

    @GetMapping("/related")
    public Result<List<NewsResponse>> getRelatedNews() {
        return Result.success(newsService.getRelatedToUser());
    }

    @GetMapping("/sources")
    public Result<List<String>> getSources() {
        return Result.success(newsService.getSources());
    }
}
