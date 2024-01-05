package com.xuecheng.content;

import com.alibaba.fastjson.JSON;
import com.xuecheng.content.mapper.CourseTeacherMapper;
import com.xuecheng.content.mapper.TeachplanMapper;
import com.xuecheng.content.model.dto.TeachplanDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

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
}
