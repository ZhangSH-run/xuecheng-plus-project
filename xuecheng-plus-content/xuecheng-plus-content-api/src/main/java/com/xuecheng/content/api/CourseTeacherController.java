package com.xuecheng.content.api;

import com.xuecheng.content.model.po.CourseTeacher;
import com.xuecheng.content.service.CourseTeacherService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "课程教师管理接口")
@RestController
public class CourseTeacherController {
    @Autowired
    private CourseTeacherService courseTeacherService;

    @ApiOperation("根据课程id,查询授课教师")
    @GetMapping("/courseTeacher/list/{courseId}")
    public List<CourseTeacher> getCourseTeacher(@PathVariable("courseId") Long courseId){
        return courseTeacherService.getCourseTeacherByCourseId(courseId);
    }

    @ApiOperation("添加或修改授课教师")
    @PostMapping("/courseTeacher")
    public CourseTeacher addOrUpdateCourseTeacher(@RequestBody CourseTeacher courseTeacher){
        if (courseTeacher.getId() == null || courseTeacher.getId().intValue() == 0){
            return courseTeacherService.addCourseTeacher(courseTeacher);
        }else {
            return courseTeacherService.updateCourseTeacher(courseTeacher);
        }
    }

    @ApiOperation("修改教师信息")
    @PutMapping("/courseTeacher")
    public CourseTeacher updateCourseTeacher(@RequestBody CourseTeacher courseTeacher){
        return courseTeacherService.updateCourseTeacher(courseTeacher);
    }

    @ApiOperation("删除教师")
    @DeleteMapping("/courseTeacher/course/{courseId}/{teacherId}")
    public void deleteCourseTeacher(@PathVariable("courseId") Long courseId,
                                    @PathVariable("teacherId") Long teacherId){
        courseTeacherService.deleteCourseTeacher(courseId,teacherId);
    }
}
