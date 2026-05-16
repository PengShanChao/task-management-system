package com.taskmanager.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("task_news_relation")
public class TaskNewsRelation {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long taskId;
    private Long newsId;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
