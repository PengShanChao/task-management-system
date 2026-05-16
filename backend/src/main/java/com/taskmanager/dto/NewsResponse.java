package com.taskmanager.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NewsResponse {
    private Long id;
    private String title;
    private String summary;
    private String url;
    private String source;
    private LocalDateTime publishedAt;
    private LocalDateTime createdAt;
    private Boolean relatedToCurrentUserTasks;
}
