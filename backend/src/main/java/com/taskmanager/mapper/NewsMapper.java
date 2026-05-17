package com.taskmanager.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.taskmanager.entity.News;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface NewsMapper extends BaseMapper<News> {

    @Select("SELECT * FROM news WHERE url = #{url} LIMIT 1")
    News selectByUrl(String url);

    @Select("SELECT * FROM news WHERE title LIKE CONCAT('%', #{keyword}, '%') OR summary LIKE CONCAT('%', #{keyword}, '%') ORDER BY published_at DESC LIMIT #{limit}")
    List<News> selectByKeyword(@Param("keyword") String keyword, @Param("limit") int limit);

    @Select("SELECT DISTINCT source FROM news WHERE source IS NOT NULL AND source != '' ORDER BY source")
    List<String> selectDistinctSources();
}
