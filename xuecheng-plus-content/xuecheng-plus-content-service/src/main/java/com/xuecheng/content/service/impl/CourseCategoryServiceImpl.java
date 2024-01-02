package com.xuecheng.content.service.impl;

import com.xuecheng.content.mapper.CourseCategoryMapper;
import com.xuecheng.content.model.dto.CourseCategoryTreeDto;
import com.xuecheng.content.model.po.CourseCategory;
import com.xuecheng.content.service.CourseCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CourseCategoryServiceImpl implements CourseCategoryService{
    @Autowired
    private CourseCategoryMapper courseCategoryMapper;
    @Override
    public List<CourseCategoryTreeDto> queryTreeNodes(String id) {
        // 获取根节点
        CourseCategory courseCategory = courseCategoryMapper.selectById(id);
        // 将根节点封装为CourseCategoryTreeDto对象
        CourseCategoryTreeDto courseCategoryTreeDto = new CourseCategoryTreeDto(courseCategory);
        // 获取所有节点
        List<CourseCategory> courseCategories = courseCategoryMapper.selectList(null);
        // 将所有节点封装为CourseCategoryTreeDto对象
        List<CourseCategoryTreeDto> courseCategoryTreeDtoList =
                courseCategories.stream().map(item -> new CourseCategoryTreeDto(item)).collect(Collectors.toList());
        // 调用递归查询，将子节点封住进父结点中
        List<CourseCategoryTreeDto> resultList = this.findChilds(courseCategoryTreeDto, courseCategoryTreeDtoList);

        return resultList;
    }

    /**
     * 递归查询当前节点的所有子节点
     * @param courseCategoryTreeDto 当前节点
     * @param courseCategoryTreeDtoList 所有节点的集合
     * @return
     */
    public List<CourseCategoryTreeDto> findChilds(CourseCategoryTreeDto courseCategoryTreeDto,
                                   List<CourseCategoryTreeDto> courseCategoryTreeDtoList) {
        List<CourseCategoryTreeDto> childrenList = new ArrayList<>();

        for (CourseCategoryTreeDto categoryTreeDto : courseCategoryTreeDtoList) {
            if (courseCategoryTreeDto.getId().equals(categoryTreeDto.getParentid())){
                List<CourseCategoryTreeDto> childs = findChilds(categoryTreeDto,courseCategoryTreeDtoList);
                if (childs.size() > 0){
                    categoryTreeDto.setChildrenTreeNodes(childs);
                }
                childrenList.add(categoryTreeDto);
            }
        }
        Collections.sort(childrenList);
        return childrenList;
    }
}
