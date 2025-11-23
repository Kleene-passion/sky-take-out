package com.sky.mapper;

import com.sky.entity.Orders;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface OrderMapper {
    /**
     * 向订单表插入1条数据
     * @param orders
     */
    void insert(Orders orders);

     /**
      * 根据订单号查询订单
      * @param outTradeNo 订单号
      * @return 订单信息
      */
    @Select("select * from sky_take_out.orders where number = #{outTradeNo}")
    Orders getByNumber(String outTradeNo);

     /**
      * 更新订单信息
      * @param orders 订单信息
      */
    void update(Orders orders);
}
