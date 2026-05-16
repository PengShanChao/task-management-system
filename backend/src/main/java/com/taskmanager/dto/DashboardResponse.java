package com.taskmanager.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DashboardResponse {
    private long totalTasks;
    private long todoCount;
    private long inProgressCount;
    private long doneCount;
    private long highPriorityCount;
    private long overdueCount;
    private double completionRate;
}
