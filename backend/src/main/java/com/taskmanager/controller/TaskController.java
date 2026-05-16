package com.taskmanager.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.taskmanager.common.Result;
import com.taskmanager.dto.NewsResponse;
import com.taskmanager.dto.TaskQuery;
import com.taskmanager.dto.TaskRequest;
import com.taskmanager.dto.TaskResponse;
import com.taskmanager.service.NewsService;
import com.taskmanager.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;
    private final NewsService newsService;

    @GetMapping
    public Result<IPage<TaskResponse>> listTasks(TaskQuery query) {
        return Result.success(taskService.listTasks(query));
    }

    @GetMapping("/{id}")
    public Result<TaskResponse> getTask(@PathVariable Long id) {
        return Result.success(taskService.getTask(id));
    }

    @PostMapping
    public Result<TaskResponse> createTask(@Valid @RequestBody TaskRequest request) {
        return Result.success(taskService.createTask(request));
    }

    @PutMapping("/{id}")
    public Result<TaskResponse> updateTask(@PathVariable Long id, @Valid @RequestBody TaskRequest request) {
        return Result.success(taskService.updateTask(id, request));
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
        return Result.success();
    }

    @GetMapping("/{taskId}/news")
    public Result<List<NewsResponse>> getTaskNews(@PathVariable Long taskId) {
        return Result.success(newsService.getNewsByTaskId(taskId));
    }

    @PostMapping("/{taskId}/news/{newsId}")
    public Result<Void> linkNews(@PathVariable Long taskId, @PathVariable Long newsId) {
        newsService.linkTaskToNews(taskId, newsId);
        return Result.success();
    }

    @DeleteMapping("/{taskId}/news/{newsId}")
    public Result<Void> unlinkNews(@PathVariable Long taskId, @PathVariable Long newsId) {
        newsService.unlinkTaskFromNews(taskId, newsId);
        return Result.success();
    }
}
