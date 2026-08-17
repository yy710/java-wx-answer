/*
 Navicat Premium Dump SQL

 Source Server         : localhost_3306
 Source Server Type    : MySQL
 Source Server Version : 80029 (8.0.29)
 Source Host           : localhost:3306
 Source Schema         : city-walk

 Target Server Type    : MySQL
 Target Server Version : 80029 (8.0.29)
 File Encoding         : 65001

 Date: 15/05/2026 08:58:37
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for category
-- ----------------------------
DROP TABLE IF EXISTS `category`;
CREATE TABLE `category`  (
  `id` bigint NOT NULL COMMENT 'id',
  `title` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '标题',
  `pic` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '图片',
  `descr` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '简介',
  `seq` int NULL DEFAULT NULL COMMENT '排序',
  `status` tinyint NULL DEFAULT 1 COMMENT '是否启用',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '分类信息' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for introduce
-- ----------------------------
DROP TABLE IF EXISTS `introduce`;
CREATE TABLE `introduce`  (
  `id` bigint NOT NULL COMMENT 'id',
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '标题',
  `author` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '作者',
  `descr` text CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL COMMENT '详情',
  `view_num` int NULL DEFAULT 0 COMMENT '浏览次数',
  `status` tinyint NULL DEFAULT 0 COMMENT '是否启用',
  `seq` int NULL DEFAULT 1 COMMENT '排序',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '介绍信息' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for invite_set
-- ----------------------------
DROP TABLE IF EXISTS `invite_set`;
CREATE TABLE `invite_set`  (
  `id` bigint NOT NULL COMMENT 'id',
  `reward_amount` decimal(10, 2) NOT NULL COMMENT '奖励积分数',
  `reward_limit` int NOT NULL COMMENT '奖励限制次数',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '拉新奖励设置' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for payment
-- ----------------------------
DROP TABLE IF EXISTS `payment`;
CREATE TABLE `payment`  (
  `id` bigint NOT NULL COMMENT 'id',
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '标题',
  `descr` text CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL COMMENT '描述',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '修改时间',
  `delete_flag` tinyint NULL DEFAULT 0 COMMENT '是否删除',
  `status` tinyint NULL DEFAULT 0 COMMENT '是否启用',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '活动支付信息' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for reward_set
-- ----------------------------
DROP TABLE IF EXISTS `reward_set`;
CREATE TABLE `reward_set`  (
  `id` bigint NOT NULL COMMENT 'id',
  `type` tinyint NOT NULL COMMENT '1-风险阅读 2-视频学习',
  `reward_limit` int NOT NULL COMMENT '奖励次数',
  `first_flag` tinyint NOT NULL COMMENT '是否首次奖励',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '奖励设置' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for risk_warning
-- ----------------------------
DROP TABLE IF EXISTS `risk_warning`;
CREATE TABLE `risk_warning`  (
  `id` bigint NOT NULL COMMENT 'id',
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '标题',
  `reward_amount` decimal(10, 2) NULL DEFAULT NULL COMMENT '奖励积分数',
  `reward_num` int NULL DEFAULT 0 COMMENT '获得积分奖励人数',
  `time_min` int NULL DEFAULT 1 COMMENT '最少阅读多少秒',
  `status` tinyint NOT NULL COMMENT '是否开启',
  `seq` int NULL DEFAULT 1 COMMENT '排序',
  `view_num` int NULL DEFAULT 0 COMMENT '阅读人数',
  `descr` text CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL COMMENT '介绍信息',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '修改时间',
  `category_id` bigint NULL DEFAULT NULL COMMENT '分类id',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '风险提示' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for risk_warning_record
-- ----------------------------
DROP TABLE IF EXISTS `risk_warning_record`;
CREATE TABLE `risk_warning_record`  (
  `risk_warn_id` bigint NOT NULL COMMENT '风险阅读id',
  `user_id` bigint NOT NULL COMMENT '用户id',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  UNIQUE INDEX `risk_warn_id`(`risk_warn_id` ASC, `user_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '风险阅读记录' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for sign_activity
-- ----------------------------
DROP TABLE IF EXISTS `sign_activity`;
CREATE TABLE `sign_activity`  (
  `id` bigint NOT NULL COMMENT 'id',
  `title` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '标题',
  `descr` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '介绍',
  `start_time` datetime NOT NULL COMMENT '开始时间',
  `end_time` datetime NOT NULL COMMENT '结束时间',
  `reward_amount` decimal(10, 2) NULL DEFAULT NULL COMMENT '每日奖励积分',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '签到活动' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for sys_log_info
-- ----------------------------
DROP TABLE IF EXISTS `sys_log_info`;
CREATE TABLE `sys_log_info`  (
  `id` bigint NOT NULL COMMENT 'ID',
  `user_id` bigint NULL DEFAULT NULL COMMENT '操作人id',
  `username` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '操作用户',
  `operation` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '用户操作',
  `method` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '请求方法',
  `params` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '请求参数',
  `time` bigint NULL DEFAULT NULL COMMENT '执行时长(毫秒)',
  `ip` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'IP地址',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '日志类型',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '系统日志' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for sys_permission
-- ----------------------------
DROP TABLE IF EXISTS `sys_permission`;
CREATE TABLE `sys_permission`  (
  `id` bigint NOT NULL,
  `pid` bigint NULL DEFAULT 0 COMMENT '父级权限id',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '名称',
  `permission` int NULL DEFAULT NULL COMMENT '权限值',
  `icon` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '图标',
  `type` int NULL DEFAULT NULL COMMENT '权限类型：0->目录；1->菜单；2->按钮（接口绑定权限）',
  `uri` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '前端资源路径',
  `status` int NULL DEFAULT NULL COMMENT '启用状态；0->禁用；1->启用',
  `sort` int NULL DEFAULT NULL COMMENT '排序',
  `code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT 'code',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '修改时间',
  `delete_flag` tinyint NULL DEFAULT 0 COMMENT '逻辑删除标识 0-未删除 1-已删除',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '后台用户权限表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for sys_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role`  (
  `id` bigint NOT NULL COMMENT '后台角色信息id',
  `code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '角色code',
  `name` varchar(150) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '角色名称',
  `delete_flag` tinyint NULL DEFAULT 0 COMMENT '逻辑删除标识',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最近一次修改时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '系统角色信息数据' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for sys_role_permission
-- ----------------------------
DROP TABLE IF EXISTS `sys_role_permission`;
CREATE TABLE `sys_role_permission`  (
  `role_id` bigint NOT NULL COMMENT '角色id',
  `permission_id` bigint NOT NULL COMMENT '权限id'
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '角色-权限关联数据' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for sys_user
-- ----------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user`  (
  `id` bigint NOT NULL,
  `username` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '账户',
  `password` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '密码',
  `icon` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '头像',
  `nick_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '昵称',
  `status` int NULL DEFAULT 1 COMMENT '状态',
  `note` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '备注',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `delete_flag` int NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `id`(`id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '后台用户表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for sys_user_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_role`;
CREATE TABLE `sys_user_role`  (
  `user_id` bigint NOT NULL COMMENT '用户id',
  `role_id` bigint NOT NULL COMMENT '角色id'
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '用户-角色关联表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for ticket_activity
-- ----------------------------
DROP TABLE IF EXISTS `ticket_activity`;
CREATE TABLE `ticket_activity`  (
  `id` bigint NOT NULL COMMENT 'id',
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '标题',
  `start_time` datetime NOT NULL COMMENT '开始时间',
  `end_time` datetime NOT NULL COMMENT '结束时间',
  `start_ticket_time` datetime NULL DEFAULT NULL COMMENT '开始投票时间',
  `end_ticket_time` datetime NULL DEFAULT NULL COMMENT '结束投票时间',
  `ticket_limit` int NULL DEFAULT NULL COMMENT '每个用户每天多少票',
  `ticket_user_num` int NULL DEFAULT 0 COMMENT '投票人数',
  `ticket_total` int NULL DEFAULT 0 COMMENT '总票数',
  `view_num` int NULL DEFAULT 0 COMMENT '浏览量',
  `ticket_user_multiple` int NULL DEFAULT NULL COMMENT '投票人数显示倍数',
  `ticket_multiple` int NULL DEFAULT NULL COMMENT '总票数显示倍数',
  `descr` text CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL COMMENT '规则说明',
  `view_multiple` int NULL DEFAULT NULL COMMENT '浏览量显示倍数',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '投票活动' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for ticket_activity_video
-- ----------------------------
DROP TABLE IF EXISTS `ticket_activity_video`;
CREATE TABLE `ticket_activity_video`  (
  `ticket_activity_id` bigint NOT NULL COMMENT '投票活动id',
  `video_id` bigint NOT NULL COMMENT '视频id',
  `seq` int NULL DEFAULT 1 COMMENT '排序',
  `reward_amount` decimal(10, 2) NULL DEFAULT 0.00 COMMENT '奖励积分数',
  `min_time` int NULL DEFAULT NULL COMMENT '观看最小时长'
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '投票活动-视频信息' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for ticket_record
-- ----------------------------
DROP TABLE IF EXISTS `ticket_record`;
CREATE TABLE `ticket_record`  (
  `id` bigint NOT NULL COMMENT 'id',
  `user_id` bigint NOT NULL COMMENT '用户id',
  `ticket_activity_id` bigint NOT NULL COMMENT '投票活动id',
  `ticket_video_id` bigint NOT NULL COMMENT '活动下的视频id',
  `ip_addr` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT 'ip地址',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '投票活动-记录' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for topic
-- ----------------------------
DROP TABLE IF EXISTS `topic`;
CREATE TABLE `topic`  (
  `id` bigint NOT NULL COMMENT 'id',
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '标题',
  `reward_amount` decimal(10, 2) NOT NULL COMMENT '奖励积分数',
  `daily_task_enabled` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否作为每日答题题库',
  `finance_category` varchar(64) NULL COMMENT '金融知识分类',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '题目信息' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for topic_activity
-- ----------------------------
DROP TABLE IF EXISTS `topic_activity`;
CREATE TABLE `topic_activity`  (
  `id` bigint NOT NULL COMMENT 'id',
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '标题',
  `start_time` datetime NOT NULL COMMENT '开始时间',
  `end_time` datetime NOT NULL COMMENT '结束时间',
  `view_num` int NULL DEFAULT 0 COMMENT '浏览次数',
  `user_num` int NULL DEFAULT 0 COMMENT '参与人数',
  `limit_num` int NULL DEFAULT 1 COMMENT '每日答题线路上限',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '修改时间',
  `reward_extra` decimal(10, 2) NULL DEFAULT NULL COMMENT '全部点亮额外奖励',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '答题活动' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for topic_item
-- ----------------------------
DROP TABLE IF EXISTS `topic_item`;
CREATE TABLE `topic_item`  (
  `id` bigint NOT NULL COMMENT 'id',
  `topic_id` bigint NOT NULL COMMENT '题目id',
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '标题',
  `seq` int NULL DEFAULT 1 COMMENT '排序',
  `answer_flag` tinyint NOT NULL COMMENT '是否正确选项',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '答题活动-题目选项' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for topic_line
-- ----------------------------
DROP TABLE IF EXISTS `topic_line`;
CREATE TABLE `topic_line`  (
  `id` bigint NOT NULL COMMENT 'id',
  `topic_activity_id` bigint NOT NULL COMMENT '活动id',
  `title` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '标题',
  `seq` int NULL DEFAULT 1 COMMENT '排序',
  `light_seq` int NULL DEFAULT 1 COMMENT '点亮顺序',
  `status` tinyint NULL DEFAULT NULL COMMENT '是否开启',
  `topic_num` int NULL DEFAULT 5 COMMENT '随机题目数',
  `descr` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '描述说明',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '答题活动-线路' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for topic_record
-- ----------------------------
DROP TABLE IF EXISTS `topic_record`;
CREATE TABLE `topic_record`  (
  `id` bigint NOT NULL,
  `user_id` bigint NOT NULL COMMENT '用户id',
  `topic_activity_id` bigint NOT NULL COMMENT '答题活动id',
  `topic_activity_title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '答题活动名称',
  `topic_line_id` bigint NOT NULL COMMENT '答题线路id',
  `topic_line_title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '答题线路名称',
  `reward_amount` decimal(10, 2) NULL DEFAULT 0.00 COMMENT '奖励积分数',
  `used_time` int NULL DEFAULT NULL COMMENT '耗时秒',
  `right_num` int NOT NULL COMMENT '答对题目数',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `total_num` int NOT NULL COMMENT '总题目数',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '用户答题记录' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for topic_record_activity
-- ----------------------------
DROP TABLE IF EXISTS `topic_record_activity`;
CREATE TABLE `topic_record_activity`  (
  `id` bigint NOT NULL COMMENT 'id',
  `topic_activity_id` bigint NOT NULL COMMENT '答题活动id',
  `user_id` bigint NOT NULL COMMENT '用户id',
  `total_reward_amount` decimal(10, 2) NOT NULL COMMENT '奖励积分数',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '答题活动记录' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for topic_record_single
-- ----------------------------
DROP TABLE IF EXISTS `topic_record_single`;
CREATE TABLE `topic_record_single`  (
  `id` bigint NOT NULL COMMENT 'id',
  `topic_id` bigint NOT NULL COMMENT '题目id',
  `topic_title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '题目名称',
  `user_id` bigint NOT NULL COMMENT '用户id',
  `right_flag` tinyint NOT NULL COMMENT '是否答对',
  `reward_amount` decimal(10, 2) NULL DEFAULT NULL COMMENT '答对奖励积分数',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '趣味答题记录' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for topic_record_single_item
-- ----------------------------
DROP TABLE IF EXISTS `topic_record_single_item`;
CREATE TABLE `topic_record_single_item`  (
  `topic_record_single_id` bigint NOT NULL COMMENT '答题记录题目id',
  `topic_item_id` bigint NOT NULL COMMENT '题目选项id',
  `topic_item_title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '题目选项名称',
  `check_flag` tinyint NOT NULL COMMENT '是否选中',
  `answer_flag` tinyint NOT NULL COMMENT '是否是答案'
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '趣味答题记录-题目选项' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for topic_record_topic
-- ----------------------------
DROP TABLE IF EXISTS `topic_record_topic`;
CREATE TABLE `topic_record_topic`  (
  `id` bigint NOT NULL COMMENT 'id',
  `topic_record_id` bigint NOT NULL COMMENT '答题记录id',
  `topic_id` bigint NOT NULL COMMENT '题目id',
  `user_id` bigint NOT NULL COMMENT '用户id',
  `topic_title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '题目标题',
  `reward_amount` decimal(10, 2) NOT NULL COMMENT '奖励积分',
  `answer_flag` tinyint NOT NULL COMMENT '答案是否正确',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '答题记录题目' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for topic_record_topic_item
-- ----------------------------
DROP TABLE IF EXISTS `topic_record_topic_item`;
CREATE TABLE `topic_record_topic_item`  (
  `topic_record_topic_id` bigint NOT NULL COMMENT '答题记录题目id',
  `topic_item_id` bigint NOT NULL COMMENT '题目选项id',
  `topic_item_title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '题目选项名称',
  `check_flag` tinyint NOT NULL COMMENT '是否选中',
  `answer_flag` tinyint NOT NULL COMMENT '是否是答案'
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '答题记录-题目选项' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for user_wallet
-- ----------------------------
DROP TABLE IF EXISTS `user_wallet`;
CREATE TABLE `user_wallet`  (
  `id` bigint NOT NULL COMMENT 'id',
  `user_id` bigint NOT NULL COMMENT '用户id',
  `type` tinyint NOT NULL COMMENT '类型',
  `amount` decimal(10, 2) NOT NULL COMMENT '数量',
  `version` int NULL DEFAULT 0 COMMENT '乐观锁版本号',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `user_id`(`user_id` ASC, `type` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '用户钱包' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for user_wallet_record
-- ----------------------------
DROP TABLE IF EXISTS `user_wallet_record`;
CREATE TABLE `user_wallet_record`  (
  `id` bigint NOT NULL COMMENT 'id',
  `wallet_id` bigint NOT NULL COMMENT '钱包id',
  `change_amount` decimal(10, 2) NOT NULL COMMENT '变动数',
  `after_amount` decimal(10, 2) NOT NULL COMMENT '剩余数',
  `event_id` bigint NOT NULL COMMENT '事件id',
  `event_type` tinyint NOT NULL COMMENT '事件类型',
  `status` tinyint NULL DEFAULT 1 COMMENT '是否成功',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '用户钱包记录' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for video
-- ----------------------------
DROP TABLE IF EXISTS `video`;
CREATE TABLE `video`  (
  `id` bigint NOT NULL COMMENT 'id',
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '标题',
  `status` tinyint NULL DEFAULT NULL COMMENT '是否开启',
  `descr` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '文字介绍',
  `url_video` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '视频链接',
  `url_pic` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '封面图',
  `daily_task_enabled` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否作为每日消保视频',
  `duration_seconds` int unsigned NULL COMMENT '可信视频时长（秒）',
  `ticket_total` int NULL DEFAULT 0 COMMENT '票数',
  `seq` int NULL DEFAULT 1 COMMENT '排序',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '投票或积分视频' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for video_activity
-- ----------------------------
DROP TABLE IF EXISTS `video_activity`;
CREATE TABLE `video_activity`  (
  `id` bigint NOT NULL COMMENT 'id',
  `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '标题',
  `descr` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '规则说明',
  `reward_limit` int NULL DEFAULT 1 COMMENT '奖励限制',
  `start_time` datetime NOT NULL COMMENT '开始时间',
  `end_time` datetime NOT NULL COMMENT '结束时间',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '视频积分活动' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for video_activity_video
-- ----------------------------
DROP TABLE IF EXISTS `video_activity_video`;
CREATE TABLE `video_activity_video`  (
  `video_activity_id` bigint NOT NULL DEFAULT 1 COMMENT '视频活动id',
  `video_id` bigint NOT NULL COMMENT '视频id',
  `reward_amount` decimal(10, 2) NOT NULL COMMENT '奖励积分数',
  `seq` int NULL DEFAULT NULL COMMENT '排序',
  `min_time` int NULL DEFAULT NULL COMMENT '观看最小时长'
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '视频活动-视频信息' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for yk_agreement
-- ----------------------------
DROP TABLE IF EXISTS `yk_agreement`;
CREATE TABLE `yk_agreement`  (
  `id` bigint NOT NULL,
  `type` int NULL DEFAULT 1 COMMENT '类型',
  `content` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL,
  `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
  `url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '路径',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '协议' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for yk_user
-- ----------------------------
DROP TABLE IF EXISTS `yk_user`;
CREATE TABLE `yk_user`  (
  `id` bigint NOT NULL COMMENT 'id',
  `open_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '微信openId',
  `mobile` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '手机号',
  `pic` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '头像',
  `nick_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '昵称',
  `status` tinyint NULL DEFAULT 1 COMMENT '是否启用',
  `delete_flag` tinyint NULL DEFAULT 0 COMMENT '是否删除',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '修改时间',
  `parent_id` bigint NULL DEFAULT 0 COMMENT '邀请人id',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX ```account```(`mobile` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '小程序用户信息' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Daily points and poster extension (J-01, new deployments)
-- ----------------------------
CREATE TABLE `daily_task_config` (
  `id` tinyint unsigned NOT NULL,
  `enabled` tinyint(1) NOT NULL DEFAULT 0,
  `quiz_enabled` tinyint(1) NOT NULL DEFAULT 1,
  `video_enabled` tinyint(1) NOT NULL DEFAULT 1,
  `affair_enabled` tinyint(1) NOT NULL DEFAULT 1,
  `share_enabled` tinyint(1) NOT NULL DEFAULT 1,
  `wallet_max_points` decimal(10,2) NOT NULL DEFAULT 4000.00,
  `daily_positive_max_points` decimal(10,2) NOT NULL DEFAULT 60.00,
  `quiz_daily_attempts` smallint unsigned NOT NULL DEFAULT 1,
  `quiz_question_count` smallint unsigned NOT NULL DEFAULT 2,
  `quiz_reward_per_correct` decimal(10,2) NOT NULL DEFAULT 10.00,
  `quiz_time_limit_seconds` int unsigned NOT NULL DEFAULT 120,
  `video_daily_count` smallint unsigned NOT NULL DEFAULT 1,
  `video_reward_points` decimal(10,2) NOT NULL DEFAULT 20.00,
  `video_min_watch_ratio` decimal(5,4) NOT NULL DEFAULT 0.5000,
  `affair_daily_count` smallint unsigned NOT NULL DEFAULT 1,
  `affair_reward_points` decimal(10,2) NOT NULL DEFAULT 10.00,
  `share_daily_count` smallint unsigned NOT NULL DEFAULT 1,
  `share_reward_points` decimal(10,2) NOT NULL DEFAULT 10.00,
  `poster_cache_days` smallint unsigned NOT NULL DEFAULT 7,
  `version` bigint unsigned NOT NULL DEFAULT 1,
  `updated_by` bigint NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  CONSTRAINT `ck_daily_task_config_singleton` CHECK (`id` = 1),
  CONSTRAINT `ck_daily_task_config_ratio` CHECK (`video_min_watch_ratio` >= 0 AND `video_min_watch_ratio` <= 1)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='每日任务配置';
INSERT INTO `daily_task_config` (`id`) VALUES (1);
CREATE TABLE `daily_task_quiz_session` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT, `user_id` bigint NOT NULL, `task_date` date NOT NULL,
  `status` varchar(16) NOT NULL, `question_count` smallint unsigned NOT NULL,
  `reward_per_correct` decimal(10,2) NOT NULL, `time_limit_seconds` int unsigned NOT NULL,
  `started_at` datetime NOT NULL, `deadline_at` datetime NOT NULL, `submitted_at` datetime NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP, PRIMARY KEY (`id`),
  UNIQUE KEY `uk_quiz_user_date` (`user_id`,`task_date`), KEY `idx_quiz_status_deadline` (`status`,`deadline_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='每日答题会话';
CREATE TABLE `daily_task_quiz_question` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT, `session_id` bigint unsigned NOT NULL,
  `sequence_no` smallint unsigned NOT NULL, `topic_id` bigint NOT NULL, `question_snapshot` json NOT NULL,
  `correct_option_snapshot` varchar(32) NOT NULL, `user_option` varchar(32) NULL,
  `is_correct` tinyint(1) NULL, `requested_points` decimal(10,2) NOT NULL DEFAULT 0.00,
  `awarded_points` decimal(10,2) NOT NULL DEFAULT 0.00, PRIMARY KEY (`id`),
  UNIQUE KEY `uk_quiz_question_order` (`session_id`,`sequence_no`), KEY `idx_quiz_question_topic` (`topic_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='每日答题题目快照';
CREATE TABLE `daily_task_video_session` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT, `user_id` bigint NOT NULL, `task_date` date NOT NULL,
  `video_id` bigint NOT NULL, `video_duration_seconds` int unsigned NOT NULL, `min_watch_ratio` decimal(5,4) NOT NULL,
  `required_watch_seconds` int unsigned NOT NULL, `credited_watch_seconds` int unsigned NOT NULL DEFAULT 0,
  `last_position_seconds` decimal(12,3) NOT NULL DEFAULT 0, `last_heartbeat_at` datetime NULL,
  `status` varchar(16) NOT NULL, `version` bigint unsigned NOT NULL DEFAULT 1, `started_at` datetime NOT NULL,
  `claimed_at` datetime NULL, PRIMARY KEY (`id`), UNIQUE KEY `uk_video_user_date` (`user_id`,`task_date`),
  KEY `idx_video_last_heartbeat` (`last_heartbeat_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='每日视频观看会话';
CREATE TABLE `daily_task_claim` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT, `user_id` bigint NOT NULL, `task_date` date NOT NULL,
  `task_type` varchar(32) NOT NULL, `source` varchar(64) NOT NULL, `requested_points` decimal(10,2) NOT NULL,
  `awarded_points` decimal(10,2) NOT NULL DEFAULT 0.00, `award_reason` varchar(32) NOT NULL,
  `wallet_record_id` bigint NULL, `completed_at` datetime NOT NULL, PRIMARY KEY (`id`),
  UNIQUE KEY `uk_daily_task_claim` (`user_id`,`task_date`,`task_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='每日任务完成记录';
CREATE TABLE `daily_task_action_token` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT, `token_hash` char(64) NOT NULL, `user_id` bigint NOT NULL,
  `action_type` varchar(16) NOT NULL, `asset_type` varchar(16) NOT NULL, `asset_id` varchar(128) NOT NULL,
  `expires_at` datetime NOT NULL, `used_at` datetime NULL, `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`), UNIQUE KEY `uk_action_token_hash` (`token_hash`), KEY `idx_action_token_user_expiry` (`user_id`,`expires_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='保存分享一次性令牌';
CREATE TABLE `poster_template` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT, `name` varchar(128) NOT NULL, `status` varchar(16) NOT NULL,
  `template_version` int unsigned NOT NULL, `canvas_width` int unsigned NOT NULL, `canvas_height` int unsigned NOT NULL,
  `restricted_json` json NOT NULL, `preview_url` varchar(512) NULL, `published_at` datetime NULL, `created_by` bigint NOT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP, `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`), UNIQUE KEY `uk_poster_template_version` (`id`,`template_version`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='海报模板';
CREATE TABLE `poster_template_asset` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT, `template_id` bigint unsigned NOT NULL, `asset_key` varchar(128) NOT NULL,
  `file_url` varchar(512) NOT NULL, `mime_type` varchar(64) NOT NULL, `width` int unsigned NOT NULL,
  `height` int unsigned NOT NULL, `sha256` char(64) NOT NULL, `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`), UNIQUE KEY `uk_poster_asset_key` (`template_id`,`asset_key`), UNIQUE KEY `uk_poster_asset_hash` (`sha256`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='海报素材';
CREATE TABLE `poster_generation` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT, `user_id` bigint NOT NULL, `template_id` bigint unsigned NOT NULL,
  `template_version` int unsigned NOT NULL, `input_sha256` char(64) NOT NULL, `result_url` varchar(512) NULL,
  `status` varchar(16) NOT NULL, `expires_at` datetime NOT NULL, `error_code` varchar(64) NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP, PRIMARY KEY (`id`),
  UNIQUE KEY `uk_poster_generation_cache` (`user_id`,`template_id`,`template_version`,`input_sha256`), KEY `idx_poster_generation_expiry` (`expires_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='海报生成记录';
CREATE TABLE `reward_claims` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT, `user_id` bigint NOT NULL, `event_id` varchar(128) NOT NULL,
  `event_type` int NOT NULL, `task_date` date NOT NULL, `requested_points` decimal(10,2) NOT NULL,
  `awarded_points` decimal(10,2) NOT NULL DEFAULT 0.00, `status` varchar(16) NOT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP, `claimed_at` datetime NULL, PRIMARY KEY (`id`),
  UNIQUE KEY `uk_reward_claim_event` (`user_id`,`event_id`), KEY `idx_reward_claim_positive_day` (`user_id`,`task_date`,`awarded_points`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='统一奖励幂等记录';

SET FOREIGN_KEY_CHECKS = 1;
