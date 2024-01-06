package com.xuecheng.content.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.base.exception.CommonError;
import com.xuecheng.base.exception.ResponseResult;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.content.mapper.TeachplanMapper;
import com.xuecheng.content.mapper.TeachplanMediaMapper;
import com.xuecheng.content.model.dto.SaveTeachplanDto;
import com.xuecheng.content.model.dto.TeachplanDto;
import com.xuecheng.content.model.po.Teachplan;
import com.xuecheng.content.model.po.TeachplanMedia;
import com.xuecheng.content.service.TeachplanService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TeachplanServiceImpl implements TeachplanService {
    @Autowired
    private TeachplanMapper teachplanMapper;
    @Autowired
    private TeachplanMediaMapper teachplanMediaMapper;

    @Transactional
    @Override
    public List<TeachplanDto> findTeachplanTree(Long courseId) {
        List<TeachplanDto> teachplanDtos = teachplanMapper.selectTreeNodes(courseId);
        return teachplanDtos;
    }

    @Override
    @Transactional
    public void saveTeachplan(SaveTeachplanDto saveTeachplanDto) {
        // 通过课程计划的是否有id来判断新增和更新
        Long teachplanId = saveTeachplanDto.getId();
        if (teachplanId == null) {
            // 新增
            Teachplan teachplan = new Teachplan();
            BeanUtils.copyProperties(saveTeachplanDto, teachplan);
            //取出同父同级别的课程计划数量
            int count = this.getTeachplanCount(saveTeachplanDto.getCourseId(),
                    saveTeachplanDto.getParentid());
            //设置排序号
            teachplan.setOrderby(count+1);
            teachplanMapper.insert(teachplan);
            System.out.println(JSON.toJSONString(teachplan));
        } else {
            // 修改
            Teachplan teachplan = teachplanMapper.selectById(teachplanId);
            BeanUtils.copyProperties(saveTeachplanDto, teachplan);
            teachplanMapper.updateById(teachplan);
        }
    }

    @Override
    @Transactional
    public ResponseResult deleteTeachplan(Long id) {
        Teachplan teachplan = teachplanMapper.selectById(id);
        // 根据等级，判断是否为小章节
        if (teachplan.getGrade() == 2){
            this.removeSmallChapter(id);
            return null;
        }
        // 大章节需要判断是否还有子节点
        LambdaQueryWrapper<Teachplan> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.eq(Teachplan::getParentid , id);
        Integer count = teachplanMapper.selectCount(queryWrapper);
        if (0 == count.intValue()){
            teachplanMapper.deleteById(id);
            return null;
        }
        return new ResponseResult("120409","课程计划信息还有子级信息，无法操作");
    }

    @Override
    @Transactional
    public void operateTeachplan(String operate, Long teachplanId) {
        Teachplan teachplan = teachplanMapper.selectById(teachplanId);
        Integer exchange = 0;
        if ("moveup".equals(operate)){
            List<Teachplan> teachplans = getItemList(teachplan);
            int indexOf = teachplans.indexOf(teachplan);
            if (indexOf > 0){
                Teachplan teachplanNew = teachplans.get(indexOf - 1);
                exchange = teachplanNew.getOrderby();
                teachplanNew.setOrderby(teachplan.getOrderby());
                teachplan.setOrderby(exchange);
                teachplanMapper.updateById(teachplan);
                teachplanMapper.updateById(teachplanNew);
            }
            return;
        }else if ("movedown".equals(operate)){
            List<Teachplan> teachplans = getItemList(teachplan);
            int indexOf = teachplans.indexOf(teachplan);
            if (indexOf != (teachplans.size() - 1)){
                Teachplan teachplanNew = teachplans.get(indexOf + 1);
                exchange = teachplanNew.getOrderby();
                teachplanNew.setOrderby(teachplan.getOrderby());
                teachplan.setOrderby(exchange);
                teachplanMapper.updateById(teachplan);
                teachplanMapper.updateById(teachplanNew);
            }
            return;
        }
        XueChengPlusException.cast(CommonError.PARAMS_ERROR);
    }

    /**
     * 根据章节id，获得平行的所有章节
     * @param
     */
    private List<Teachplan> getItemList(Teachplan teachplan) {
        LambdaQueryWrapper<Teachplan> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Teachplan::getCourseId,teachplan.getCourseId());
        queryWrapper.eq(Teachplan::getParentid , teachplan.getParentid());
        queryWrapper.eq(Teachplan::getGrade , teachplan.getGrade());
        queryWrapper.orderByAsc(Teachplan::getOrderby);
        return teachplanMapper.selectList(queryWrapper);
    }

    private void removeSmallChapter(Long id) {
        // 小章节直接删除
        teachplanMapper.deleteById(id);
        // 删除小章节关联的媒资信息
        LambdaQueryWrapper<TeachplanMedia> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TeachplanMedia::getTeachplanId , id);
        teachplanMediaMapper.delete(queryWrapper);
    }

    /**
     * @param courseId 课程id
     * @param parentId 父课程计划id
     * @return int 最新排序号
     * @description 获取最新的排序号
     * @author Mr.M
     * @date 2022/9/9 13:43
     */
    private int getTeachplanCount(long courseId, long parentId) {
        LambdaQueryWrapper<Teachplan> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Teachplan::getCourseId, courseId);
        queryWrapper.eq(Teachplan::getParentid, parentId);
        Integer count = teachplanMapper.selectCount(queryWrapper);
        return count;
    }
}
