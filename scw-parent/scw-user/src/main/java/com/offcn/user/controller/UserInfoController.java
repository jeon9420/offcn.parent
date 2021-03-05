package com.offcn.user.controller;

import com.offcn.dycommon.response.AppResponse;
import com.offcn.user.po.TMemberAddress;
import com.offcn.user.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Api(tags = "用户信息")
@Slf4j
@RestController
@RequestMapping("/user")
public class UserInfoController {

    @Autowired
    private UserService userService;
    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @ApiOperation("获取用户地址")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "用户令牌",name = "accessToken",required = true)
    })
    @GetMapping("/findUserAddress")
    public AppResponse<Object> findUserAddress(String accessToken){
        String memberId = stringRedisTemplate.opsForValue().get(accessToken);
        if (StringUtils.isEmpty(memberId)){
            return AppResponse.fail("请先登录");
        }
        List<TMemberAddress> userAddress = userService.getUserAddress(Integer.parseInt(memberId));
        return AppResponse.ok(userAddress);
    }
}
