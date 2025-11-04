package com.skycanvas.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.skycanvas.common.PageResult;
import com.skycanvas.entity.CreditLog;
import com.skycanvas.entity.User;
import com.skycanvas.mapper.CreditLogMapper;
import com.skycanvas.mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 积分服务
 */
@Slf4j
@Service
public class CreditService {

    @Autowired
    private CreditLogMapper creditLogMapper;

    @Autowired
    private UserMapper userMapper;

    /**
     * 充值积分
     * @param userId 用户ID
     * @param amount 积分数量
     * @param orderId 订单ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void recharge(Long userId, Integer amount, Long orderId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 更新用户积分
        user.setCredits(user.getCredits() + amount);
        userMapper.updateById(user);

        // 记录积分流水
        CreditLog log = new CreditLog();
        log.setUserId(userId);
        log.setAmount(amount);
        log.setType(1);  // 充值
        log.setBalance(user.getCredits());
        log.setDescription("充值" + amount + "积分");
        log.setOrderId(orderId);
        creditLogMapper.insert(log);

        log.info("用户{}充值积分成功: +{}, 余额: {}", userId, amount, user.getCredits());
    }

    /**
     * 消费积分
     * @param userId 用户ID
     * @param amount 积分数量
     * @param taskId 任务ID
     * @param description 描述
     */
    @Transactional(rollbackFor = Exception.class)
    public void consume(Long userId, Integer amount, Long taskId, String description) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 检查积分余额
        if (user.getCredits() < amount) {
            throw new RuntimeException("积分余额不足");
        }

        // 扣除积分
        user.setCredits(user.getCredits() - amount);
        userMapper.updateById(user);

        // 记录积分流水
        CreditLog log = new CreditLog();
        log.setUserId(userId);
        log.setAmount(-amount);  // 负数表示消费
        log.setType(2);  // 消费
        log.setBalance(user.getCredits());
        log.setDescription(description);
        log.setTaskId(taskId);
        creditLogMapper.insert(log);

        log.info("用户{}消费积分成功: -{}, 余额: {}", userId, amount, user.getCredits());
    }

    /**
     * 退回积分（任务失败时）
     * @param userId 用户ID
     * @param amount 积分数量
     * @param taskId 任务ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void refund(Long userId, Integer amount, Long taskId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 退回积分
        user.setCredits(user.getCredits() + amount);
        userMapper.updateById(user);

        // 记录积分流水
        CreditLog log = new CreditLog();
        log.setUserId(userId);
        log.setAmount(amount);
        log.setType(3);  // 退款
        log.setBalance(user.getCredits());
        log.setDescription("任务失败，退回" + amount + "积分");
        log.setTaskId(taskId);
        creditLogMapper.insert(log);

        log.info("用户{}退回积分成功: +{}, 余额: {}", userId, amount, user.getCredits());
    }

    /**
     * 获取积分记录
     * @param userId 用户ID
     * @param current 当前页
     * @param size 每页大小
     */
    public PageResult<CreditLog> getCreditLogs(Long userId, Long current, Long size) {
        Page<CreditLog> page = new Page<>(current, size);
        LambdaQueryWrapper<CreditLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CreditLog::getUserId, userId);
        wrapper.orderByDesc(CreditLog::getCreateTime);

        Page<CreditLog> result = creditLogMapper.selectPage(page, wrapper);
        return PageResult.of(result.getRecords(), result.getTotal(), current, size);
    }

    /**
     * 获取用户积分余额
     */
    public Integer getBalance(Long userId) {
        User user = userMapper.selectById(userId);
        return user != null ? user.getCredits() : 0;
    }
}

