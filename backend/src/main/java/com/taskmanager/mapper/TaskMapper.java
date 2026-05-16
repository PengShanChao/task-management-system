package com.taskmanager.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.taskmanager.entity.Task;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface TaskMapper extends BaseMapper<Task> {

    @Select("SELECT COUNT(*) FROM task WHERE user_id = #{userId} AND deleted = 0")
    long countByUserId(Long userId);

    @Select("SELECT COUNT(*) FROM task WHERE user_id = #{userId} AND status = #{status} AND deleted = 0")
    long countByUserIdAndStatus(Long userId, String status);

    @Select("SELECT COUNT(*) FROM task WHERE user_id = #{userId} AND priority = 'HIGH' AND status != 'DONE' AND deleted = 0")
    long countHighPriorityByUserId(Long userId);

    @Select("SELECT COUNT(*) FROM task WHERE user_id = #{userId} AND deadline < NOW() AND status != 'DONE' AND deleted = 0")
    long countOverdueByUserId(Long userId);
}
