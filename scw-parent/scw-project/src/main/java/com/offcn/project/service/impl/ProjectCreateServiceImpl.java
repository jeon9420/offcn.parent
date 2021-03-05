package com.offcn.project.service.impl;

import com.alibaba.fastjson.JSON;
import com.offcn.dycommon.enums.ProjectStatusEnume;
import com.offcn.project.contants.ProjectConstant;
import com.offcn.project.enums.ProjectImageTypeEnume;
import com.offcn.project.mapper.*;
import com.offcn.project.po.*;
import com.offcn.project.service.ProjectCreateService;
import com.offcn.project.vo.req.ProjectRedisStorageVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;


@Service
public class ProjectCreateServiceImpl implements ProjectCreateService {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private TProjectMapper projectMapper;
    @Autowired
    private TProjectImagesMapper projectImagesMapper;
    @Autowired
    private TProjectTypeMapper projectTypeMapper;
    @Autowired
    private TProjectTagMapper projectTagMapper;
    @Autowired
    private TReturnMapper returnMapper;

    @Override
    public String initCreateProject(Integer memberId) {
        //生成一个项目令牌
        String token = UUID.randomUUID().toString().replace("-", "");
        //给临时对象的属性赋值
        ProjectRedisStorageVo projectRedisStorageVo = new ProjectRedisStorageVo();
        //存用户Id
        projectRedisStorageVo.setMemberid(memberId);
        //存令牌
        projectRedisStorageVo.setProjectToken(token);
        String jsonString = JSON.toJSONString(projectRedisStorageVo);
        stringRedisTemplate.opsForValue().set(ProjectConstant.TEMP_PROJECT_PREFIX+token,jsonString);
        return token;
    }

    @Override
    public void saveProjectInfo(ProjectStatusEnume projectStatusEnume, ProjectRedisStorageVo projectRedisStorageVo) {
        //创建项目
        TProject project = new TProject();
        BeanUtils.copyProperties(projectRedisStorageVo,project);
        //设置时间
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String format = simpleDateFormat.format(new Date());
        project.setCreatedate(format);
        //设置项目的状态
        project.setStatus(ProjectStatusEnume.DRAFT.getCode()+"");
        //保存项目到数据库
        projectMapper.insertSelective(project);
        //获取项目的id
        Integer projectId = project.getId();
        //保存图片-头图
        String headerImage = projectRedisStorageVo.getHeaderImage();
        TProjectImages projectImages = new TProjectImages(null,projectId,headerImage, ProjectImageTypeEnume.HEADER.getCode());
        projectImagesMapper.insertSelective(projectImages);
        //保存图片-详图
        List<String> detailsImage = projectRedisStorageVo.getDetailsImage();
        if (!CollectionUtils.isEmpty(detailsImage)){
            for (String image : detailsImage){
                TProjectImages detailImages = new TProjectImages(null, projectId, image, ProjectImageTypeEnume.DETAILS.getCode());
                projectImagesMapper.insertSelective(detailImages);
            }
        }
        //保存标签
        List<Integer> tagids = projectRedisStorageVo.getTagids();
        if (!CollectionUtils.isEmpty(tagids)){
            for (Integer tagid : tagids){
                TProjectTag tProjectTag = new TProjectTag(null, projectId, tagid);
                projectTagMapper.insertSelective(tProjectTag);
            }
        }
        //保存分类
        List<Integer> typeids = projectRedisStorageVo.getTypeids();
        if (!CollectionUtils.isEmpty(typeids)){
            for (Integer typeid : typeids){
                TProjectType tProjectType = new TProjectType(null, projectId, typeid);
                projectTypeMapper.insertSelective(tProjectType);
            }
        }
        //保存回报
        List<TReturn> projectReturns = projectRedisStorageVo.getProjectReturns();
        if (!CollectionUtils.isEmpty(projectReturns)){
            for (TReturn tReturn : projectReturns){
                tReturn.setProjectid(projectId);
                returnMapper.insertSelective(tReturn);
            }
        }

        //删除临时数据
        stringRedisTemplate.delete(ProjectConstant.TEMP_PROJECT_PREFIX+projectRedisStorageVo.getProjectToken());
    }
}
