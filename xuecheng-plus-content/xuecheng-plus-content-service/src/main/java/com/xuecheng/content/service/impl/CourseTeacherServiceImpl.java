package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.base.exception.CommonError;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.content.mapper.CourseTeacherMapper;
import com.xuecheng.content.model.po.CourseTeacher;
import com.xuecheng.content.service.CourseTeacherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CourseTeacherServiceImpl implements CourseTeacherService {
    @Autowired
    private CourseTeacherMapper courseTeacherMapper;

    @Override
    @Transactional
    public List<CourseTeacher> getCourseTeacherByCourseId(Long courseId) {
        LambdaQueryWrapper<CourseTeacher> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CourseTeacher::getCourseId,courseId);
        List<CourseTeacher> courseTeachers = courseTeacherMapper.selectList(queryWrapper);
        return courseTeachers;
    }

    @Override
    @Transactional
    public CourseTeacher addCourseTeacher(CourseTeacher courseTeacher) {
        int i = courseTeacherMapper.insert(courseTeacher);
        if (i <= 0){
            XueChengPlusException.cast(CommonError.UNKOWN_ERROR);
        }
        return courseTeacher;
    }

    @Override
    @Transactional
    public CourseTeacher updateCourseTeacher(CourseTeacher courseTeacher) {
        int i = courseTeacherMapper.updateById(courseTeacher);
        if (i <= 0){
            XueChengPlusException.cast(CommonError.UNKOWN_ERROR);
        }
        return courseTeacher;
    }

    @Override
    @Transactional
    public void deleteCourseTeacher(Long courseId, Long teacherId) {
        int i = 0;
        if (teacherId == null || teacherId == 0){
            LambdaQueryWrapper<CourseTeacher> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(CourseTeacher::getCourseId , courseId);
            i = courseTeacherMapper.delete(queryWrapper);
        }else {
            i = courseTeacherMapper.deleteById(teacherId);
        }
        if (i < 0){
            XueChengPlusException.cast(CommonError.UNKOWN_ERROR);
        }
    }
}
