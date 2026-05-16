package com.taskmanager.dto;

import lombok.Data;

@Data
public class TaskQuery {
    private String status;
    private String priority;
    private String deadlineFrom;
    private Long userId;
    private Integer page = 1;
    private Integer pageSize = 10;
}
