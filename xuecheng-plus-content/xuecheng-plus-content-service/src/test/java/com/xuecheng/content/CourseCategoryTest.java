package com.xuecheng.content;

import com.alibaba.fastjson.JSON;
import com.xuecheng.base.exception.CommonError;
import com.xuecheng.content.mapper.CourseCategoryMapper;
import com.xuecheng.content.model.dto.CourseCategoryTreeDto;
import com.xuecheng.content.model.po.CourseCategory;
import com.xuecheng.content.service.CourseCategoryService;
import com.xuecheng.content.service.impl.CourseCategoryServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.*;
import java.util.stream.Collectors;

@SpringBootTest
public class CourseCategoryTest {
    @Autowired
    private CourseCategoryMapper courseCategoryMapper;
    @Autowired
    private CourseCategoryServiceImpl courseCategoryService;
    @Test
    public void testCourseCategoryMapper(){
        List<CourseCategory> courseCategoryResult = new ArrayList<>();
        List<CourseCategory> courseCategories = courseCategoryMapper.selectList(null);
        for (CourseCategory courseCategory : courseCategories) {
            if ("1".equals(courseCategory.getParentid())){
                courseCategoryResult.add(courseCategory);
            }
        }
        /*Comparator<CourseCategory> comparator = new Comparator<CourseCategory>() {
            @Override
            public int compare(CourseCategory o1, CourseCategory o2) {
                o1.setId(o1.getId().replace("-",""));
                o2.setId(o2.getId().replace("-",""));
                return Integer.parseInt(o1.getId()) - Integer.parseInt(o2.getId());
            }
        };
        courseCategoryResult.sort(comparator);*/
        // Collections.sort(courseCategoryResult);
        for (CourseCategory courseCategory : courseCategoryResult) {
            System.out.println(courseCategory.getId() + "---" + courseCategory.getName());
        }

    }

    @Test
    public void testTree() throws InterruptedException {
        List<CourseCategory> courseCategories = courseCategoryMapper.selectList(null);
        List<CourseCategoryTreeDto> courseCategoryTreeDtoList =
                courseCategories.stream().map(item -> new CourseCategoryTreeDto(item)).collect(Collectors.toList());
        String id = "1";
        CourseCategory courseCategory = courseCategoryMapper.selectById(id);
        CourseCategoryTreeDto courseCategoryTreeDto = new CourseCategoryTreeDto(courseCategory);
        List<CourseCategoryTreeDto> childs = courseCategoryService.findChilds(courseCategoryTreeDto, courseCategoryTreeDtoList);
        String s = JSON.toJSONString(childs);
        System.out.println(s);
        /*int i = 0;
        for (CourseCategoryTreeDto child : childs) {
            i++;
            System.out.println(child.getId() + " ===> " + child.getName());
            if (child.getChildrenTreeNodes() != null){
                for (CourseCategoryTreeDto child1 : child.getChildrenTreeNodes()){
                    System.out.println(child1.getId() + " ===> " + child1.getName());
                    i++;
                    Thread.sleep(500);
                }
            }
        }
        System.out.println(i);*/
    }
    @Test
    public void testOne(){
        String id = "1";
        CourseCategory courseCategory = courseCategoryMapper.selectById(id);
        System.out.println(courseCategory);
    }
}
