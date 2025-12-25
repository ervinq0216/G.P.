package com.hospital.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hospital.entity.Schedule;
import org.apache.ibatis.annotations.Mapper;

/**
 * 排班表 Mapper 接口
 * 确保该文件名为 ScheduleMapper.java
 */
@Mapper
public interface ScheduleMapper extends BaseMapper<Schedule> {
}