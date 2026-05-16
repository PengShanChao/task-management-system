package com.taskmanager.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.taskmanager.dto.*;
import com.taskmanager.entity.Task;
import com.taskmanager.entity.User;
import com.taskmanager.mapper.TaskMapper;
import com.taskmanager.mapper.UserMapper;
import com.taskmanager.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

    private final TaskMapper taskMapper;
    private final UserMapper userMapper;

    @Override
    public IPage<TaskResponse> listTasks(TaskQuery query) {
        User currentUser = getCurrentUser();

        LambdaQueryWrapper<Task> wrapper = new LambdaQueryWrapper<Task>()
                .eq(query.getStatus() != null, Task::getStatus, query.getStatus())
                .eq(query.getPriority() != null, Task::getPriority, query.getPriority())
                .apply(query.getDeadlineFrom() != null,
                        "deadline >= {0}", query.getDeadlineFrom())
                .orderByAsc(Task::getDeadline);

        if (!"ADMIN".equals(currentUser.getRole())) {
            wrapper.eq(Task::getUserId, currentUser.getId());
        } else if (query.getUserId() != null) {
            wrapper.eq(Task::getUserId, query.getUserId());
        }

        Page<Task> page = new Page<>(query.getPage(), query.getPageSize());
        IPage<Task> taskPage = taskMapper.selectPage(page, wrapper);

        return taskPage.convert(this::toResponse);
    }

    @Override
    public TaskResponse getTask(Long id) {
        Task task = taskMapper.selectById(id);
        if (task == null) {
            throw new IllegalArgumentException("Task not found");
        }
        checkOwnership(task);
        return toResponse(task);
    }

    @Override
    public TaskResponse createTask(TaskRequest request) {
        Task task = new Task();
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setStatus(request.getStatus() != null ? request.getStatus() : "TODO");
        task.setPriority(request.getPriority() != null ? request.getPriority() : "MEDIUM");
        task.setDeadline(request.getDeadline());
        task.setUserId(getCurrentUserId());
        taskMapper.insert(task);
        return toResponse(task);
    }

    @Override
    public TaskResponse updateTask(Long id, TaskRequest request) {
        Task task = taskMapper.selectById(id);
        if (task == null) {
            throw new IllegalArgumentException("Task not found");
        }
        checkOwnership(task);
        if (request.getTitle() != null) task.setTitle(request.getTitle());
        if (request.getDescription() != null) task.setDescription(request.getDescription());
        if (request.getStatus() != null) task.setStatus(request.getStatus());
        if (request.getPriority() != null) task.setPriority(request.getPriority());
        if (request.getDeadline() != null) task.setDeadline(request.getDeadline());
        if (request.getUserId() != null) {
            User currentUser = getCurrentUser();
            if (!"ADMIN".equals(currentUser.getRole())) {
                throw new AccessDeniedException("Only admin can reassign tasks");
            }
            task.setUserId(request.getUserId());
        }
        taskMapper.updateById(task);
        return toResponse(task);
    }

    @Override
    public void deleteTask(Long id) {
        Task task = taskMapper.selectById(id);
        if (task == null) {
            throw new IllegalArgumentException("Task not found");
        }
        checkOwnership(task);
        taskMapper.deleteById(id);
    }

    private void checkOwnership(Task task) {
        User currentUser = getCurrentUser();
        if (!"ADMIN".equals(currentUser.getRole()) && !task.getUserId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Access denied");
        }
    }

    private User getCurrentUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    private TaskResponse toResponse(Task task) {
        User user = userMapper.selectById(task.getUserId());
        return TaskResponse.builder()
                .id(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .status(task.getStatus())
                .priority(task.getPriority())
                .deadline(task.getDeadline())
                .userId(task.getUserId())
                .username(user != null ? user.getUsername() : null)
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .build();
    }

    private Long getCurrentUserId() {
        return getCurrentUser().getId();
    }
}
