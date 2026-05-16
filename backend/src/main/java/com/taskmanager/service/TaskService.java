package com.taskmanager.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.taskmanager.dto.*;

public interface TaskService {
    IPage<TaskResponse> listTasks(TaskQuery query);
    TaskResponse getTask(Long id);
    TaskResponse createTask(TaskRequest request);
    TaskResponse updateTask(Long id, TaskRequest request);
    void deleteTask(Long id);
}
