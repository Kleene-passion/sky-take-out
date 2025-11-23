package com.sky.mapper;

import com.sky.entity.OrderDetail;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface OrderDetailMapper {
    /**
     * 向订单详情表批量插入数据
     * @param orderDetailList
     */
    void insertBatch(List<OrderDetail> orderDetailList);

     /**
      * 根据订单id查询订单详情
      * @param orderId 订单id
      * @return 订单详情列表
      */
     @Select("select * from sky_take_out.order_detail where order_id = #{orderId}")
    List<OrderDetail> getByOrderId(Long orderId);
}
