package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.dto.GoodsSalesDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.entity.Orders;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

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

     /**
      * 分页查询订单
      * @param ordersPageQueryDTO 分页查询参数
      * @return 订单列表
      */
    Page<Orders> pageQuery(OrdersPageQueryDTO ordersPageQueryDTO);

     /**
      * 根据id查询订单
      * @param id 订单id
      * @return 订单信息
      */
    @Select("select * from sky_take_out.orders where id = #{id}")
    Orders getById(Long id);

     /**
      * 根据状态查询订单数量
      * @param status 订单状态
      * @return 订单数量
      */
    @Select("select count(*) from sky_take_out.orders where status = #{status}")
    Integer countStatus(Integer status);

     /**
      * 根据状态和订单时间查询订单
      * @param status 订单状态
      * @param orderTime 订单时间
      * @return 订单列表
      */
    @Select("select * from sky_take_out.orders where status = #{status} and order_time < #{orderTime}")
    List<Orders> getByStatusAndOrderTimeLT(Integer status, LocalDateTime orderTime);

     /**
      * 根据Map查询营业额
      * @param map 查询参数
      * @return 营业额
      */
     Double sumByMap(Map map);

     /**
      * 根据动态条件统计订单数量
      * @param map 查询参数
      * @return 订单数量
      */
     Integer countByMap(Map map);

     /**
      * 统计指定时间区间内的销量排名前10的商品数据
      * @param begin 开始时间
      * @param end 结束时间
      * @return 销量排名前10的商品数据列表
      */
     List<GoodsSalesDTO> getSalesTop(LocalDateTime begin, LocalDateTime end);
}
