package com.offcn.project.service;

import com.offcn.project.po.*;

import java.util.List;

public interface ProjectInfoService {
    //获取所有的回报信息
    public List<TReturn> getReturnList(Integer projectId);
    //获取系统中所有的项目
    public List<TProject> findAllProject();
    //获取所有的图片
    public List<TProjectImages> findAllProjectImages(Integer projectId);
    //根据项目id查询项目
    public TProject findProject(Integer id);
    //获取系统所有的项目标签
    public List<TTag> findAllTag();
    //获取系统所有的项目分类
    public List<TType> findAllType();
    //获取项目回报详细信息
    public TReturn findReturn(Integer returnId);
}
