package com.hospital.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hospital.entity.Schedule;
import org.apache.ibatis.annotations.Mapper;

/**
 * 排班表 Mapper 接口
 */
@Mapper
public interface ScheduleMapper extends BaseMapper<Schedule> {
}