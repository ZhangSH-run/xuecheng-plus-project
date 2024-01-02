package com.xuecheng.content.model.dto;

import com.xuecheng.content.model.po.CourseCategory;
import lombok.Data;
import lombok.ToString;

import java.util.List;

@Data
public class CourseCategoryTreeDto extends CourseCategory implements Comparable<CourseCategoryTreeDto>{
    private List<CourseCategoryTreeDto> childrenTreeNodes;

    public CourseCategoryTreeDto(){
    }
    public CourseCategoryTreeDto(CourseCategory courseCategory){
        this.setId(courseCategory.getId());
        this.setName(courseCategory.getName());
        this.setLabel(courseCategory.getLabel());
        this.setParentid(courseCategory.getParentid());
        this.setIsShow(courseCategory.getIsShow());
        this.setOrderby(courseCategory.getOrderby());
        this.setIsLeaf(courseCategory.getIsLeaf());
    }

    @Override
    public int compareTo(CourseCategoryTreeDto o2) {
        String s1 = this.getId().replace("-", "");
        String s2 = o2.getId().replace("-", "");
        return Integer.parseInt(s1) - Integer.parseInt(s2);
    }
}
