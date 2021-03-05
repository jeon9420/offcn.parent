package com.offcn.order.service.impl;

import com.offcn.dycommon.enums.OrderStatusEnumes;
import com.offcn.dycommon.response.AppResponse;
import com.offcn.order.mapper.TOrderMapper;
import com.offcn.order.po.TOrder;
import com.offcn.order.service.OrderService;
import com.offcn.order.service.ProjectServiceFeign;
import com.offcn.order.vo.req.OrderInfoSubmitVo;
import com.offcn.order.vo.resp.TReturn;
import com.offcn.utils.AppDateUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private TOrderMapper orderMapper;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private ProjectServiceFeign projectServiceFeign;

    @Override
    public TOrder saveOrder(OrderInfoSubmitVo orderInfoSubmitVo) {
        TOrder order = new TOrder();
        String accessToken = orderInfoSubmitVo.getAccessToken();
        String memberId = stringRedisTemplate.opsForValue().get(accessToken);
        order.setMemberid(Integer.parseInt(memberId));
        BeanUtils.copyProperties(orderInfoSubmitVo,order);
        //生成订单号
        String orderNum = UUID.randomUUID().toString().replace("-", "");
        order.setOrdernum(orderNum);
        //设置支付状态-未支付
        order.setStatus(OrderStatusEnumes.UNPAY.getCode()+"");
        //发票状态
        order.setInvoice(orderInfoSubmitVo.getInvoice().toString());
        //创建时间
        order.setCreatedate(AppDateUtils.getFormatTime());

        //远程调用
        AppResponse<List<TReturn>> response = projectServiceFeign.getReturnList(orderInfoSubmitVo.getProjectid());
        List<TReturn> returnList = response.getData();
        TReturn myReturn = null;
        for (TReturn tReturn : returnList){
            if (tReturn.getId().intValue() == orderInfoSubmitVo.getReturnid().intValue()){
                myReturn = tReturn;
                break;
            }
        }
        //算钱：支持的数量*每笔支持的金额+运费
        Integer money = order.getRtncount() * myReturn.getSupportmoney() + myReturn.getFreight();
        order.setMoney(money);
        orderMapper.insertSelective(order);
        return order;
    }
}
