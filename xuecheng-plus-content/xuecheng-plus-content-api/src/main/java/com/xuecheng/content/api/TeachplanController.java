package com.xuecheng.content.api;

import com.xuecheng.base.exception.ResponseResult;
import com.xuecheng.content.model.dto.SaveTeachplanDto;
import com.xuecheng.content.model.dto.TeachplanDto;
import com.xuecheng.content.service.TeachplanService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 课程计划管理相关的接口
 */
@Api(tags = "课程计划编辑接口")
@RestController
public class TeachplanController {
    @Autowired
    private TeachplanService teachplanService;

    @ApiOperation("查询课程计划树形结构")
    @GetMapping("teachplan/{courseId}/tree-nodes")
    public List<TeachplanDto> getTreeNodes(@PathVariable("courseId") Long courseId){
        return teachplanService.findTeachplanTree(courseId);
    }

    @ApiOperation("课程计划创建或修改")
    @PostMapping("/teachplan")
    public void saveTeachplan(@RequestBody SaveTeachplanDto teachplan){
        teachplanService.saveTeachplan(teachplan);
    }

    @ApiOperation("删除课程")
    @DeleteMapping("/teachplan/{teachplanId}")
    public ResponseResult deleteTeachplan(@PathVariable("teachplanId") Long id){
        return teachplanService.deleteTeachplan(id);
    }

    @ApiOperation("移动课程位置")
    @PostMapping("/teachplan/{operate}/{teachplanId}")
    public void operateTeachplan(@PathVariable("operate") String operate,
                               @PathVariable("teachplanId") Long teachplanId){
        teachplanService.operateTeachplan(operate,teachplanId);
    }
}
