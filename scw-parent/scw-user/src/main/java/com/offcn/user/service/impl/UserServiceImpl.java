package com.offcn.user.service.impl;

import com.offcn.user.enums.UserExceptionEnum;
import com.offcn.user.exception.UserException;
import com.offcn.user.mapper.TMemberAddressMapper;
import com.offcn.user.mapper.TMemberMapper;
import com.offcn.user.po.TMember;
import com.offcn.user.po.TMemberAddress;
import com.offcn.user.po.TMemberAddressExample;
import com.offcn.user.po.TMemberExample;
import com.offcn.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private TMemberMapper memberMapper;
    @Autowired
    TMemberAddressMapper memberAddressMapper;

    @Override
    public void RegisterUser(TMember member) {

        //检查系统中此手机号是否存在
        TMemberExample example = new TMemberExample();
        example.createCriteria().andLoginacctEqualTo(member.getLoginacct());
        long l = memberMapper.countByExample(example);
        if (l > 0){
            throw new UserException(UserExceptionEnum.LOGINACCT_EXIST);
        }
        //不存在，保存信息，设置默认信息
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String encode = encoder.encode(member.getUserpswd());
        //设置密码
        member.setUserpswd(encode);
        //设置用户名
        member.setUsername(member.getLoginacct());
        //设置邮箱
        member.setEmail(member.getEmail());
        //实名认证状态 0 - 未实名认证， 1 - 实名认证申请中， 2 - 已实名认证
        member.setAuthstatus("0");
        //用户类型: 0 - 个人， 1 - 企业
        member.setUsertype("0");
        //账户类型: 0 - 企业， 1 - 个体， 2 - 个人， 3 - 政府
        member.setAccttype("2");
        System.out.println("插入数据："+member.getLoginacct());
        memberMapper.insertSelective(member);
    }

    @Override
    public TMember login(String username, String password) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
//        String encode = encoder.encode(password);
        TMemberExample example = new TMemberExample();
        example.createCriteria().andLoginacctEqualTo(username);
        List<TMember> tMembers = memberMapper.selectByExample(example);
        if (tMembers != null && tMembers.size() == 1){
            TMember tMember = tMembers.get(0);
            boolean matches = encoder.matches(password, tMember.getUserpswd());
            return matches?tMember:null;
        }
        return null;
    }

    @Override
    public TMember findMemberById(Integer id) {
        TMember tMember = memberMapper.selectByPrimaryKey(id);
        return tMember;
    }

    @Override
    public List<TMemberAddress> getUserAddress(Integer memberId) {
        TMemberAddressExample example = new TMemberAddressExample();
        example.createCriteria().andMemberidEqualTo(memberId);
        return memberAddressMapper.selectByExample(example);
    }
}
