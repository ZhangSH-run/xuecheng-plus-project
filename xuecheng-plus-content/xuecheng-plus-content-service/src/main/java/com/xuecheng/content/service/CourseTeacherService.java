package com.xuecheng.content.service;

import com.xuecheng.content.model.po.CourseTeacher;

import java.util.List;

public interface CourseTeacherService {
    /**
     * 根据课程id，获取教师信息
     * @param
     * @return
     */
    List<CourseTeacher> getCourseTeacherByCourseId(Long courseId);

    /**
     * 给课程添加教师
     * @param
     * @return
     */
    CourseTeacher addCourseTeacher(CourseTeacher courseTeacher);

    /**
     * 修改教师信息
     * @param courseTeacher
     * @return
     */
    CourseTeacher updateCourseTeacher(CourseTeacher courseTeacher);

    /**
     * 删除教师
     * @param courseId
     * @param teacherId
     */
    void deleteCourseTeacher(Long courseId , Long teacherId);
}
