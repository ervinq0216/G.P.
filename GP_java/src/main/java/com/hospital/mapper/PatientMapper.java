package com.hospital.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hospital.entity.Patient;
import org.apache.ibatis.annotations.Mapper;

/**
 * 患者数据访问接口
 * 继承 BaseMapper 自动获得 CRUD 功能
 */
@Mapper
public interface PatientMapper extends BaseMapper<Patient> {
}