package com.taskmanager.controller;

import com.taskmanager.common.Result;
import com.taskmanager.dto.DashboardResponse;
import com.taskmanager.entity.User;
import com.taskmanager.mapper.TaskMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final TaskMapper taskMapper;

    @GetMapping("/stats")
    public Result<DashboardResponse> getStats() {
        Long userId = getCurrentUserId();
        long total = taskMapper.countByUserId(userId);
        long todo = taskMapper.countByUserIdAndStatus(userId, "TODO");
        long inProgress = taskMapper.countByUserIdAndStatus(userId, "IN_PROGRESS");
        long done = taskMapper.countByUserIdAndStatus(userId, "DONE");
        long highPriority = taskMapper.countHighPriorityByUserId(userId);
        long overdue = taskMapper.countOverdueByUserId(userId);
        double rate = total > 0 ? (double) done / total * 100 : 0;

        DashboardResponse response = DashboardResponse.builder()
                .totalTasks(total)
                .todoCount(todo)
                .inProgressCount(inProgress)
                .doneCount(done)
                .highPriorityCount(highPriority)
                .overdueCount(overdue)
                .completionRate(Math.round(rate * 100.0) / 100.0)
                .build();
        return Result.success(response);
    }

    private Long getCurrentUserId() {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return user.getId();
    }
}
