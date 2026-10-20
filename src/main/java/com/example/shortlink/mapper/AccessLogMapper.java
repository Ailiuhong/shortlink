package com.example.shortlink.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.shortlink.entity.AccessLog;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;

public interface AccessLogMapper extends BaseMapper<AccessLog> {

    @Select("""
            SELECT COUNT(*)
            FROM access_log
            WHERE short_code = #{shortCode}
              AND visited_at >= #{since}
            """)
    long countSince(@Param("shortCode") String shortCode, @Param("since") LocalDateTime since);
}
