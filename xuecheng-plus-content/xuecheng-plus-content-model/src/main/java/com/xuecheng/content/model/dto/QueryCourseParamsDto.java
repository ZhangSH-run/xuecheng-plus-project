package com.xuecheng.content.model.dto;

import lombok.Data;

/**
 * 课程查询参数
 */
@Data
public class QueryCourseParamsDto {
    // 审核状态
    private String auditStatus;
    // 课程名称
    private String courseName;
    // 发布状态
    private String publishStatus;
}
