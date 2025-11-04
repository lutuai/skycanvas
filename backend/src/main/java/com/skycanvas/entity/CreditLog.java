package com.skycanvas.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 积分记录实体
 */
@Data
@TableName("tb_credit_log")
public class CreditLog implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 记录ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 变动金额（正数-充值，负数-消费）
     */
    private Integer amount;

    /**
     * 类型：1-充值，2-消费，3-退款
     */
    private Integer type;

    /**
     * 操作后余额
     */
    private Integer balance;

    /**
     * 描述
     */
    private String description;

    /**
     * 关联订单ID（充值时）
     */
    private Long orderId;

    /**
     * 关联任务ID（消费时）
     */
    private Long taskId;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}

