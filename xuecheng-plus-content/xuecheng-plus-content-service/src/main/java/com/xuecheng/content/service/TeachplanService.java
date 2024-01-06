package com.xuecheng.content.service;

import com.xuecheng.base.exception.ResponseResult;
import com.xuecheng.content.model.dto.SaveTeachplanDto;
import com.xuecheng.content.model.dto.TeachplanDto;

import java.util.List;

/**
 * 课程计划管理
 */
public interface TeachplanService {
    /**
     * 根据课程id，查询课程计划
     * @param courseId
     * @return
     */
    List<TeachplanDto> findTeachplanTree(Long courseId);

    /**
     * @description 只在课程计划
     * @param saveTeachplanDto  课程计划信息
     * @return void
     * @author Mr.M
     * @date 2022/9/9 13:39
     */
    void saveTeachplan(SaveTeachplanDto saveTeachplanDto);

    /**
     *  删除课程或章节
     * @param id
     */
    ResponseResult deleteTeachplan(Long id);

    void operateTeachplan(String operate , Long teachplanId);
}
