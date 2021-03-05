package com.offcn.project.controller;

import com.alibaba.fastjson.JSON;
import com.offcn.dycommon.enums.ProjectStatusEnume;
import com.offcn.dycommon.response.AppResponse;
import com.offcn.project.contants.ProjectConstant;
import com.offcn.project.po.TReturn;
import com.offcn.project.service.ProjectCreateService;
import com.offcn.project.vo.BaseVo;
import com.offcn.project.vo.req.ProjectBaseInfoVo;
import com.offcn.project.vo.req.ProjectRedisStorageVo;
import com.offcn.project.vo.req.ProjectReturnVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;


@Api(tags = "项目基本功能模块（创建、保存、项目信息获取、文件上传等）")
@Slf4j
@RestController
@RequestMapping("/project")
public class ProjectCreateController {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private ProjectCreateService projectCreateService;

    @ApiOperation("项目发起第一步-阅读同意协议")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "accessToken", value = "会员的令牌", required = true)
    })//@ApiImplicitParams：描述所有参数；@ApiImplicitParam描述某个参数
    @GetMapping("/init")
    public AppResponse<String> init(BaseVo vo){
        //获取用户令牌
        String accessToken = vo.getAccessToken();
        //根据用户令牌获取用户id
        String memberId = stringRedisTemplate.opsForValue().get(accessToken);
       if (StringUtils.isEmpty(memberId)){
           AppResponse.fail("没有权限，请先登录");
       }
        int id = Integer.parseInt(memberId);
       //获取项目令牌
        String projectToken = projectCreateService.initCreateProject(id);
        return AppResponse.ok(projectToken);
    }

    //保存项目的基本信息
    @ApiOperation("项目发起第二步-保存项目的基本信息")
    @GetMapping("/save")
    public AppResponse<String> savebaseInfo(ProjectBaseInfoVo vo){
        //根据项目的令牌查找临时对象
        String s = stringRedisTemplate.opsForValue().get(ProjectConstant.TEMP_PROJECT_PREFIX + vo.getProjectToken());
        //把json字符串转换成存储的临时对象
        ProjectRedisStorageVo projectRedisStorageVo = JSON.parseObject(s, ProjectRedisStorageVo.class);
        BeanUtils.copyProperties(vo,projectRedisStorageVo);
        //临时对象存入redis
        String jsonString = JSON.toJSONString(projectRedisStorageVo);
        stringRedisTemplate.opsForValue().set(ProjectConstant.TEMP_PROJECT_PREFIX + vo.getProjectToken(),jsonString);
        return AppResponse.ok(projectRedisStorageVo.getProjectToken());
    }

    //保存回报详细
    @ApiOperation(value = "项目的第三步-保存项目的回报详细")
    @PostMapping("/saveReturn")
    public AppResponse<String> saveReturnInfo(@RequestBody List<ProjectReturnVo> pro){
        ProjectReturnVo projectReturnVo = pro.get(0);
        //获取项目令牌
        String token = projectReturnVo.getProjectToken();
        String s = stringRedisTemplate.opsForValue().get(ProjectConstant.TEMP_PROJECT_PREFIX+token);
        //把json字符串转换成存储的临时对象
        ProjectRedisStorageVo projectRedisStorageVo = JSON.parseObject(s, ProjectRedisStorageVo.class);
        //将页面的数据封装到临时对象的回报列表里面
        ArrayList<TReturn> returns = new ArrayList<>();
        for (ProjectReturnVo returnVo : pro){
            TReturn tReturn = new TReturn();
            BeanUtils.copyProperties(returnVo,tReturn);
            returns.add(tReturn);
        }
        projectRedisStorageVo.setProjectReturns(returns);
        String jsonString = JSON.toJSONString(projectRedisStorageVo);
        stringRedisTemplate.opsForValue().set(ProjectConstant.TEMP_PROJECT_PREFIX+token,jsonString);
        return AppResponse.ok(token);
    }

    //保存项目
    @ApiOperation("第4步-保存项目及回报信息等")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "accessToken", value = "会员的令牌", required = true),
            @ApiImplicitParam(name = "projectToken", value = "项目令牌", required = true),
            @ApiImplicitParam(name = "ops", value = "用户操作类型 0-保存草稿 1-提交审核", required = true)
    })//@ApiImplicitParams：描述所有参数；@ApiImplicitParam描述某个参数
    @PostMapping("/saveproject")
    public AppResponse<Object> saveProject(String accessToken,String projectToken,String ops){
        String memberId = stringRedisTemplate.opsForValue().get(accessToken);
        if (StringUtils.isEmpty(memberId)){
            return AppResponse.fail("请先登录");
        }
        String s = stringRedisTemplate.opsForValue().get(ProjectConstant.TEMP_PROJECT_PREFIX + projectToken);
        ProjectRedisStorageVo projectRedisStorageVo = JSON.parseObject(s, ProjectRedisStorageVo.class);
        if (!StringUtils.isEmpty(ops)){
            if ("1".equals(ops)){
                ProjectStatusEnume auth = ProjectStatusEnume.SUBMIT_AUTH;
                projectCreateService.saveProjectInfo(auth,projectRedisStorageVo);
                return AppResponse.ok("ok");
            }else if ("0".equals(ops)){
                ProjectStatusEnume draft = ProjectStatusEnume.DRAFT;
                projectCreateService.saveProjectInfo(draft,projectRedisStorageVo);
                return AppResponse.ok("ok");
            }else {
                AppResponse<Object> fail = AppResponse.fail(null);
                fail.setMsg("保存失败");
                return fail;
            }
        }
        return null;
    }
}
