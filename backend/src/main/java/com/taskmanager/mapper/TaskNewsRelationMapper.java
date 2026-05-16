package com.taskmanager.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.taskmanager.entity.TaskNewsRelation;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface TaskNewsRelationMapper extends BaseMapper<TaskNewsRelation> {

    @Select("SELECT news_id FROM task_news_relation WHERE task_id = #{taskId}")
    List<Long> selectNewsIdsByTaskId(Long taskId);

    @Select("SELECT COUNT(*) FROM task_news_relation WHERE task_id = #{taskId} AND news_id = #{newsId}")
    int existsRelation(@Param("taskId") Long taskId, @Param("newsId") Long newsId);

    @Select("<script>SELECT DISTINCT news_id FROM task_news_relation WHERE task_id IN <foreach item='id' collection='taskIds' open='(' separator=',' close=')'>#{id}</foreach></script>")
    List<Long> selectNewsIdsByUserTaskIds(@Param("taskIds") List<Long> taskIds);

    @Delete("DELETE FROM task_news_relation WHERE task_id = #{taskId} AND news_id = #{newsId}")
    int deleteRelation(@Param("taskId") Long taskId, @Param("newsId") Long newsId);
}
