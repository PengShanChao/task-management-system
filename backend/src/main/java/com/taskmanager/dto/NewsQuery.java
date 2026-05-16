package com.taskmanager.dto;

import lombok.Data;

@Data
public class NewsQuery {
    private String keyword;
    private String source;
    private Integer page = 1;
    private Integer pageSize = 10;
}
