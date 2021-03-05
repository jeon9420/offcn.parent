package com.offcn.project.controller;

import com.fasterxml.jackson.databind.util.BeanUtil;
import com.offcn.dycommon.response.AppResponse;
import com.offcn.project.po.*;
import com.offcn.project.service.ProjectInfoService;
import com.offcn.project.vo.resp.ProjectDetailVo;
import com.offcn.project.vo.resp.ProjectVo;
import com.offcn.utils.OSSTemplate;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Api(tags = "项目模块（文件上传）")
@Slf4j
@RestController
@RequestMapping("/project")
public class ProjectController {
    @Autowired
    private OSSTemplate ossTemplate;
    @Autowired
    private ProjectInfoService projectInfoService;

    @ApiOperation("文件上传")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "file", value = "文件地址", required = true)
    })//@ApiImplicitParams：描述所有参数；@ApiImplicitParam描述某个参数
    @PostMapping("/upload")
    public AppResponse upload(@RequestParam("file") MultipartFile[] files) throws IOException {
        List<String> fileList = new ArrayList<>();
        if (files != null && files.length > 0){
            for (MultipartFile file : files){
                if (!file.isEmpty()){
                    String upload = ossTemplate.upload(file.getInputStream(), file.getOriginalFilename());
                    fileList.add(upload);
                }
            }
        }
        HashMap<Object, Object> map = new HashMap<>();
        map.put("urls",fileList);
        log.debug("oss的信息：{},图片地址：{}",ossTemplate,fileList);
        return AppResponse.ok(map);
    }

    @ApiOperation("得到回报列表")
    //根据项目id查询回报列表
    @GetMapping("/details/returns/{projectId}")
    public AppResponse<List<TReturn>> getReturnList(@PathVariable("projectId") Integer projectId){
        List<TReturn> returnList = projectInfoService.getReturnList(projectId);
        return AppResponse.ok(returnList);
    }

    @ApiOperation("获取系统的所有项目")
    @GetMapping("/findAllProject")
    public AppResponse findAllProject(){
        ArrayList<ProjectVo> projectVos = new ArrayList<>();
        //获取系统的所有项目
        List<TProject> projectList = projectInfoService.findAllProject();
        //创建ProjectVo对象
        ProjectVo projectVo = new ProjectVo();
        //遍历项目集合
        for (TProject project : projectList){
            BeanUtils.copyProperties(project,projectVo);
            //获取每个项目的id
            Integer id = project.getId();
            //获取所有图片
            List<TProjectImages> imageList = projectInfoService.findAllProjectImages(id);
            for (TProjectImages projectImages : imageList){
                if (projectImages.getImgtype() == 0){
                    projectVo.setHeaderImage(projectImages.getImgurl());
                }
            }
            projectVos.add(projectVo);
        }
        return AppResponse.ok(projectVos);
    }

    //查询一个项目的详情
    @ApiOperation("查询项目详情")
    @GetMapping("/findOneProject/{projectId}")
    public AppResponse<ProjectDetailVo> findOneProject(@PathVariable("projectId") Integer projectId){
        TProject project = projectInfoService.findProject(projectId);
        ProjectDetailVo projectDetailVo = new ProjectDetailVo();
        // 1、查出这个项目的所有图片
        List<TProjectImages> imagesList = projectInfoService.findAllProjectImages(projectId);
        List<String> detailsImage = projectDetailVo.getDetailsImage();
        if (detailsImage == null){
            detailsImage = new ArrayList();
        }
        for (TProjectImages projectImages : imagesList){
            if (projectImages.getImgtype() == 0){
                projectDetailVo.setHeaderImage(projectImages.getImgurl());
            }else {
                detailsImage.add(projectImages.getImgurl());
            }
        }
        projectDetailVo.setDetailsImage(detailsImage);
        //项目的所有支持回报；
        List<TReturn> returnList = projectInfoService.getReturnList(projectId);
        projectDetailVo.setProjectReturns(returnList);
        BeanUtils.copyProperties(project,projectDetailVo);
        return AppResponse.ok(projectDetailVo);
    }

    @ApiOperation("获取系统所有的项目标签")
    @GetMapping("findAllTag")
    public AppResponse<List<TTag>> findAllTag(){
        List<TTag> tagList = projectInfoService.findAllTag();
        return AppResponse.ok(tagList);
    }

    @ApiOperation("获取系统所有的分类")
    @GetMapping("findAllType")
    public AppResponse<List<TType>> findAllType(){
        return AppResponse.ok(projectInfoService.findAllType());
    }

    @ApiOperation("获取项目回报详细信息")
    @GetMapping("findReturn/{returnId}")
    public AppResponse<TReturn> findReturn(@PathVariable("returnId") Integer returnId){
        TReturn tReturn = projectInfoService.findReturn(returnId);
        return AppResponse.ok(tReturn);
    }
}
