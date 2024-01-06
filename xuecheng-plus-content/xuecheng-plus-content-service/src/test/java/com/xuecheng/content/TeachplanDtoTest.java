package com.xuecheng.content;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.content.mapper.CourseTeacherMapper;
import com.xuecheng.content.mapper.TeachplanMapper;
import com.xuecheng.content.model.dto.TeachplanDto;
import com.xuecheng.content.model.po.Teachplan;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Collections;
import java.util.List;

@SpringBootTest
public class TeachplanDtoTest {
    @Autowired
    private TeachplanMapper teachplanMapper;

    @Test
    public void testTreeNodes(){
        List<TeachplanDto> teachplanDtos = teachplanMapper.selectTreeNodes(117);
        System.out.println(JSON.toJSONString(teachplanDtos));
    }

    @Test
    public void testSort(){
        Teachplan teachplan = teachplanMapper.selectById(275);
        LambdaQueryWrapper<Teachplan> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Teachplan::getCourseId,teachplan.getCourseId());
        queryWrapper.eq(Teachplan::getParentid , teachplan.getParentid());
        queryWrapper.eq(Teachplan::getGrade , teachplan.getGrade());
        queryWrapper.orderByAsc(Teachplan::getOrderby);
        List<Teachplan> teachplans = teachplanMapper.selectList(queryWrapper);
        System.out.println(teachplans);
    }
}
