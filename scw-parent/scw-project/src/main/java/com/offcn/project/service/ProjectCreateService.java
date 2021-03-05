package com.offcn.project.service;

import com.offcn.dycommon.enums.ProjectStatusEnume;
import com.offcn.project.vo.req.ProjectRedisStorageVo;

public interface ProjectCreateService {
    //初始化项目
    public String initCreateProject(Integer memberId);
    //保存项目
    public void saveProjectInfo(ProjectStatusEnume projectStatusEnume, ProjectRedisStorageVo projectRedisStorageVo);
}
