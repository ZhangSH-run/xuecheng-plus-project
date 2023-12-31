package com.xuecheng.content;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xuecheng.content.mapper.CourseBaseMapper;
import com.xuecheng.content.model.po.CourseBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
public class CourseBaseMapperTest {
    @Autowired
    private CourseBaseMapper courseBaseMapper;

    @Test
    public void testCourseBaseMapper(){

        // 分页查询
        Page<CourseBase> page = new Page(2,5);
        LambdaQueryWrapper<CourseBase> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.eq(CourseBase::getUsers,"初级人员");
        Page<CourseBase> courseBasePage = courseBaseMapper.selectPage(page, queryWrapper);
        System.err.println(courseBasePage.getTotal());
        List<CourseBase> courseBaseList = courseBasePage.getRecords();
        for (CourseBase courseBase : courseBaseList) {
            System.out.println(courseBase);
        }
    }
}
