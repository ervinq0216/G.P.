package com.hospital.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 通知公告实体
 */
@Data
@TableName("t_notice")
public class Notice {
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 标题
     */
    private String title;

    /**
     * 内容
     */
    private String content;

    /**
     * 类型: notice(通知), suggestion(健康建议)
     */
    private String type;

    /**
     * 目标类型:
     * all (全部人员)
     * patient (仅患者)
     * dept (特定科室医生)
     */
    private String targetType;

    /**
     * 目标科室ID (仅当 targetType = dept 时有效)
     */
    private Long targetDeptId;

    /**
     * 发布时间
     */
    private LocalDateTime createdTime;
}