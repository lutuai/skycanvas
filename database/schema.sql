-- SkyCanvas数据库设计
-- 创建数据库
CREATE DATABASE IF NOT EXISTS `skycanvas` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE `skycanvas`;

-- 用户表
CREATE TABLE `tb_user` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `openid` VARCHAR(100) NOT NULL COMMENT '微信openid',
    `unionid` VARCHAR(100) DEFAULT NULL COMMENT '微信unionid',
    `nickname` VARCHAR(50) DEFAULT NULL COMMENT '用户昵称',
    `avatar` VARCHAR(255) DEFAULT NULL COMMENT '用户头像',
    `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
    `credits` INT(11) NOT NULL DEFAULT 0 COMMENT '积分余额',
    `total_videos` INT(11) NOT NULL DEFAULT 0 COMMENT '总生成视频数',
    `status` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '用户状态：0-正常，1-禁用',
    `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否删除：0-未删除，1-已删除',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_openid` (`openid`),
    KEY `idx_phone` (`phone`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 积分记录表
CREATE TABLE `tb_credit_log` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '记录ID',
    `user_id` BIGINT(20) NOT NULL COMMENT '用户ID',
    `amount` INT(11) NOT NULL COMMENT '变动金额（正数-充值，负数-消费）',
    `type` TINYINT(1) NOT NULL COMMENT '类型：1-充值，2-消费，3-退款',
    `balance` INT(11) NOT NULL COMMENT '操作后余额',
    `description` VARCHAR(200) DEFAULT NULL COMMENT '描述',
    `order_id` BIGINT(20) DEFAULT NULL COMMENT '关联订单ID',
    `task_id` BIGINT(20) DEFAULT NULL COMMENT '关联任务ID',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='积分记录表';

-- 视频生成任务表
CREATE TABLE `tb_video_task` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '任务ID',
    `user_id` BIGINT(20) NOT NULL COMMENT '用户ID',
    `task_id` VARCHAR(100) NOT NULL COMMENT '第三方任务ID',
    `provider` VARCHAR(50) NOT NULL COMMENT '服务提供商',
    `prompt` TEXT NOT NULL COMMENT '提示词',
    `params` TEXT COMMENT '生成参数（JSON格式）',
    `status` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '任务状态：0-队列中，1-生成中，2-成功，3-失败',
    `progress` INT(3) NOT NULL DEFAULT 0 COMMENT '进度（0-100）',
    `video_url` VARCHAR(500) DEFAULT NULL COMMENT '视频URL',
    `cover_url` VARCHAR(500) DEFAULT NULL COMMENT '封面URL',
    `duration` INT(11) DEFAULT NULL COMMENT '视频时长（秒）',
    `cost_credits` INT(11) NOT NULL COMMENT '消耗积分',
    `error_msg` VARCHAR(500) DEFAULT NULL COMMENT '错误信息',
    `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否删除',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `complete_time` DATETIME DEFAULT NULL COMMENT '完成时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_task_id` (`task_id`),
    KEY `idx_status` (`status`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='视频生成任务表';

-- 作品表
CREATE TABLE `tb_work` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '作品ID',
    `user_id` BIGINT(20) NOT NULL COMMENT '用户ID',
    `video_task_id` BIGINT(20) NOT NULL COMMENT '关联视频任务ID',
    `title` VARCHAR(100) DEFAULT NULL COMMENT '作品标题',
    `description` VARCHAR(500) DEFAULT NULL COMMENT '作品描述',
    `tags` VARCHAR(200) DEFAULT NULL COMMENT '标签（JSON数组）',
    `cover_url` VARCHAR(500) NOT NULL COMMENT '封面URL',
    `video_url` VARCHAR(500) NOT NULL COMMENT '视频URL',
    `duration` INT(11) NOT NULL COMMENT '视频时长（秒）',
    `is_public` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否公开：0-私密，1-公开',
    `view_count` INT(11) NOT NULL DEFAULT 0 COMMENT '浏览次数',
    `like_count` INT(11) NOT NULL DEFAULT 0 COMMENT '点赞次数',
    `comment_count` INT(11) NOT NULL DEFAULT 0 COMMENT '评论次数',
    `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否删除',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_video_task_id` (`video_task_id`),
    KEY `idx_is_public` (`is_public`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='作品表';

-- 社区帖子表
CREATE TABLE `tb_post` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '帖子ID',
    `user_id` BIGINT(20) NOT NULL COMMENT '用户ID',
    `work_id` BIGINT(20) NOT NULL COMMENT '作品ID',
    `content` VARCHAR(500) DEFAULT NULL COMMENT '内容',
    `like_count` INT(11) NOT NULL DEFAULT 0 COMMENT '点赞数',
    `comment_count` INT(11) NOT NULL DEFAULT 0 COMMENT '评论数',
    `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否删除',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_work_id` (`work_id`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='社区帖子表';

-- 评论表
CREATE TABLE `tb_comment` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '评论ID',
    `post_id` BIGINT(20) NOT NULL COMMENT '帖子ID',
    `user_id` BIGINT(20) NOT NULL COMMENT '用户ID',
    `content` VARCHAR(500) NOT NULL COMMENT '评论内容',
    `parent_id` BIGINT(20) DEFAULT NULL COMMENT '父评论ID（支持回复）',
    `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否删除',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_post_id` (`post_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_parent_id` (`parent_id`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评论表';

-- 点赞表
CREATE TABLE `tb_like` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '点赞ID',
    `user_id` BIGINT(20) NOT NULL COMMENT '用户ID',
    `target_id` BIGINT(20) NOT NULL COMMENT '目标ID（帖子ID或评论ID）',
    `target_type` TINYINT(1) NOT NULL COMMENT '目标类型：1-帖子，2-评论',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_target` (`user_id`, `target_id`, `target_type`),
    KEY `idx_target` (`target_id`, `target_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='点赞表';

-- 订单表
CREATE TABLE `tb_order` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '订单ID',
    `user_id` BIGINT(20) NOT NULL COMMENT '用户ID',
    `order_no` VARCHAR(50) NOT NULL COMMENT '订单号',
    `package_id` BIGINT(20) NOT NULL COMMENT '套餐ID',
    `amount` DECIMAL(10,2) NOT NULL COMMENT '订单金额（元）',
    `credits` INT(11) NOT NULL COMMENT '积分数量',
    `status` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '订单状态：0-待支付，1-已支付，2-已取消，3-已退款',
    `pay_method` TINYINT(1) DEFAULT NULL COMMENT '支付方式：1-微信支付',
    `transaction_id` VARCHAR(100) DEFAULT NULL COMMENT '第三方交易号',
    `pay_time` DATETIME DEFAULT NULL COMMENT '支付时间',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_no` (`order_no`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_status` (`status`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单表';

-- 积分套餐表
CREATE TABLE `tb_credit_package` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '套餐ID',
    `name` VARCHAR(50) NOT NULL COMMENT '套餐名称',
    `credits` INT(11) NOT NULL COMMENT '积分数量',
    `price` DECIMAL(10,2) NOT NULL COMMENT '价格（元）',
    `bonus_credits` INT(11) NOT NULL DEFAULT 0 COMMENT '赠送积分',
    `sort` INT(11) NOT NULL DEFAULT 0 COMMENT '排序',
    `is_active` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否启用：0-禁用，1-启用',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_is_active` (`is_active`),
    KEY `idx_sort` (`sort`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='积分套餐表';

-- 插入默认积分套餐数据
INSERT INTO `tb_credit_package` (`name`, `credits`, `price`, `bonus_credits`, `sort`, `is_active`) VALUES
('体验套餐', 100, 10.00, 0, 1, 1),
('进阶套餐', 300, 30.00, 20, 2, 1),
('热门套餐', 600, 50.00, 100, 3, 1),
('超值套餐', 1200, 100.00, 300, 4, 1);

-- 登录日志表
CREATE TABLE `tb_login_log` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '日志ID',
    `user_id` BIGINT(20) DEFAULT NULL COMMENT '用户ID',
    `login_ip` VARCHAR(50) DEFAULT NULL COMMENT '登录IP',
    `login_location` VARCHAR(100) DEFAULT NULL COMMENT '登录地点',
    `device_type` VARCHAR(50) DEFAULT NULL COMMENT '设备类型',
    `browser` VARCHAR(50) DEFAULT NULL COMMENT '浏览器',
    `os` VARCHAR(50) DEFAULT NULL COMMENT '操作系统',
    `status` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '登录状态：0-失败，1-成功',
    `fail_reason` VARCHAR(200) DEFAULT NULL COMMENT '失败原因',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='登录日志表';

