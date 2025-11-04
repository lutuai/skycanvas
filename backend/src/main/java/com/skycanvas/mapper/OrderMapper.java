package com.skycanvas.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.skycanvas.entity.Order;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单Mapper
 */
@Mapper
public interface OrderMapper extends BaseMapper<Order> {
}

