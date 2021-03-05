package com.offcn.user.service;

import com.offcn.user.po.TMember;
import com.offcn.user.po.TMemberAddress;

import java.util.List;

public interface UserService {
    //用户注册
    public void RegisterUser(TMember member);
    //用户登录
    public TMember login(String username,String password);
    //根据用户id查询用户信息
    public TMember findMemberById(Integer id);
    //获取用户地址
    public List<TMemberAddress> getUserAddress(Integer memberId);

}
