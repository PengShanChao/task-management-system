package com.taskmanager.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.taskmanager.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
