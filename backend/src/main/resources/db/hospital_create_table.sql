/*
SQLyog Enterprise v12.09 (64 bit)
MySQL - 5.6.15 : Database - hospital_db
*********************************************************************
*/

/*!40101 SET NAMES utf8 */;

/*!40101 SET SQL_MODE=''*/;

/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;
CREATE DATABASE /*!32312 IF NOT EXISTS*/`hospital_db` /*!40100 DEFAULT CHARACTER SET utf8 */;

USE `hospital_db`;

/*Table structure for table `t_appointment` */

DROP TABLE IF EXISTS `t_appointment`;

CREATE TABLE `t_appointment` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '订单ID',
  `order_no` varchar(50) NOT NULL COMMENT '订单号',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `doctor_id` bigint(20) NOT NULL COMMENT '医生ID',
  `hospital_id` bigint(20) NOT NULL COMMENT '医院ID',
  `patient_name` varchar(50) NOT NULL COMMENT '就诊人姓名',
  `patient_phone` varchar(20) DEFAULT NULL COMMENT '就诊人电话',
  `patient_id_card` varchar(18) DEFAULT NULL COMMENT '就诊人身份证号',
  `patient_gender` tinyint(4) DEFAULT NULL COMMENT '就诊人性别: 1男 2女',
  `patient_age` int(11) DEFAULT NULL COMMENT '就诊人年龄',
  `appointment_date` date NOT NULL COMMENT '预约日期',
  `appointment_time` varchar(20) NOT NULL COMMENT '预约时间段',
  `disease_desc` text COMMENT '病情描述',
  `amount` decimal(10,2) DEFAULT '0.00' COMMENT '挂号费用',
  `status` tinyint(4) DEFAULT '1' COMMENT '状态: 1待支付 2已支付 3已完成 4已取消',
  `pay_time` datetime DEFAULT NULL COMMENT '支付时间',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `order_no` (`order_no`)
) ENGINE=InnoDB AUTO_INCREMENT=2070761007537274883 DEFAULT CHARSET=utf8 COMMENT='挂号订单表';

/*Table structure for table `t_article` */

DROP TABLE IF EXISTS `t_article`;

CREATE TABLE `t_article` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '文章ID',
  `title` varchar(200) NOT NULL COMMENT '文章标题',
  `summary` text COMMENT '文章摘要',
  `content` text COMMENT '文章内容',
  `department_id` bigint(20) DEFAULT NULL COMMENT '所属科室ID',
  `author` varchar(50) DEFAULT NULL COMMENT '作者',
  `image` varchar(255) DEFAULT NULL COMMENT '封面图片',
  `views` int(11) DEFAULT '0' COMMENT '阅读量',
  `status` tinyint(4) DEFAULT '1' COMMENT '状态: 1已发布 0草稿',
  `publish_time` datetime DEFAULT NULL COMMENT '发布时间',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8 COMMENT='健康科普文章表';

/*Table structure for table `t_config` */

DROP TABLE IF EXISTS `t_config`;

CREATE TABLE `t_config` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `config_key` varchar(50) NOT NULL COMMENT '配置键',
  `config_value` varchar(255) DEFAULT NULL COMMENT '配置值',
  `description` varchar(100) DEFAULT NULL COMMENT '描述',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `config_key` (`config_key`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8 COMMENT='系统配置表';

/*Table structure for table `t_consult` */

DROP TABLE IF EXISTS `t_consult`;

CREATE TABLE `t_consult` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '咨询ID',
  `order_no` varchar(50) NOT NULL COMMENT '订单号',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `doctor_id` bigint(20) NOT NULL COMMENT '医生ID',
  `patient_name` varchar(50) NOT NULL COMMENT '咨询人姓名',
  `patient_phone` varchar(20) DEFAULT NULL COMMENT '咨询人电话',
  `disease_desc` text NOT NULL COMMENT '病情描述',
  `appointment_time` datetime DEFAULT NULL COMMENT '预约咨询时间',
  `duration` int(11) DEFAULT '30' COMMENT '咨询时长(分钟)',
  `amount` decimal(10,2) DEFAULT '0.00' COMMENT '咨询费用',
  `status` tinyint(4) DEFAULT '1' COMMENT '状态: 1待支付 2已支付 3咨询中 4已完成 5已取消',
  `pay_time` datetime DEFAULT NULL COMMENT '支付时间',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `order_no` (`order_no`)
) ENGINE=InnoDB AUTO_INCREMENT=2070714321230184450 DEFAULT CHARSET=utf8 COMMENT='电话咨询订单表';

/*Table structure for table `t_department` */

DROP TABLE IF EXISTS `t_department`;

CREATE TABLE `t_department` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '科室ID',
  `name` varchar(50) NOT NULL COMMENT '科室名称',
  `description` text COMMENT '科室描述',
  `parent_id` bigint(20) DEFAULT '0' COMMENT '父级科室ID',
  `sort_order` int(11) DEFAULT '0' COMMENT '排序',
  `status` tinyint(4) DEFAULT '1' COMMENT '状态: 1正常 0禁用',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=58 DEFAULT CHARSET=utf8 COMMENT='科室表';

/*Table structure for table `t_disease` */

DROP TABLE IF EXISTS `t_disease`;

CREATE TABLE `t_disease` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '疾病ID',
  `department_id` bigint(20) DEFAULT NULL COMMENT '所属科室ID（关联二级科室）',
  `name` varchar(100) NOT NULL COMMENT '疾病名称',
  `description` text COMMENT '疾病描述',
  `alias` varchar(100) DEFAULT NULL COMMENT '别名',
  `location` varchar(200) DEFAULT NULL COMMENT '发病部位',
  `treatment` varchar(500) DEFAULT NULL COMMENT '治疗方法',
  `symptoms` varchar(500) DEFAULT NULL COMMENT '常见症状',
  `treatment_period` varchar(50) DEFAULT NULL COMMENT '治疗周期',
  `cure_rate` varchar(50) DEFAULT NULL COMMENT '治愈率',
  `examinations` varchar(500) DEFAULT NULL COMMENT '临床检查',
  `follow_count` int(11) DEFAULT '0' COMMENT '关注数量',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=utf8mb4 COMMENT='疾病表';

/*Table structure for table `t_doctor` */

DROP TABLE IF EXISTS `t_doctor`;

CREATE TABLE `t_doctor` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '医生ID',
  `name` varchar(50) NOT NULL COMMENT '医生姓名',
  `gender` tinyint(4) DEFAULT NULL COMMENT '性别: 1男 2女',
  `title` varchar(50) DEFAULT NULL COMMENT '职称',
  `department_id` bigint(20) NOT NULL COMMENT '所属科室ID',
  `hospital_id` bigint(20) NOT NULL COMMENT '所属医院ID',
  `avatar` varchar(255) DEFAULT 'img/doctor-male-doc.jpg' COMMENT '医生头像',
  `phone` varchar(20) DEFAULT NULL COMMENT '联系电话',
  `intro` text COMMENT '医生简介',
  `expertise` text COMMENT '擅长领域',
  `consult_count` int(11) DEFAULT '0' COMMENT '接诊次数',
  `rating` decimal(3,2) DEFAULT '5.00' COMMENT '评分',
  `follow_count` int(11) DEFAULT '0' COMMENT '关注数量',
  `online_status` tinyint(4) DEFAULT '1' COMMENT '在线状态: 1在线 2离线',
  `price` decimal(10,2) DEFAULT '0.00' COMMENT '咨询价格',
  `registration_price` decimal(10,2) DEFAULT '50.00' COMMENT '挂号价格',
  `status` tinyint(4) DEFAULT '1' COMMENT '状态: 1正常 0禁用',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=23 DEFAULT CHARSET=utf8 COMMENT='医生表';

/*Table structure for table `t_family_member` */

DROP TABLE IF EXISTS `t_family_member`;

CREATE TABLE `t_family_member` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '成员ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID(户主)',
  `name` varchar(50) NOT NULL COMMENT '姓名',
  `gender` tinyint(4) DEFAULT NULL COMMENT '性别: 1男 2女',
  `birthday` date DEFAULT NULL COMMENT '生日',
  `phone` varchar(20) DEFAULT NULL COMMENT '联系电话',
  `id_card` varchar(20) DEFAULT NULL COMMENT '身份证号',
  `relation` varchar(20) NOT NULL COMMENT '与本人关系 1:本人 2:配偶 3:父母 4:子女 5:兄弟姐妹 6:其他',
  `is_default` tinyint(4) DEFAULT '0' COMMENT '是否默认: 1是 0否',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2069353628308467714 DEFAULT CHARSET=utf8 COMMENT='就诊成员表';

/*Table structure for table `t_feedback` */

DROP TABLE IF EXISTS `t_feedback`;

CREATE TABLE `t_feedback` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '反馈ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `feedback_type` tinyint(4) NOT NULL COMMENT '反馈类型: 1系统问题 2服务问题 3医生问题 4其他问题',
  `content` text NOT NULL COMMENT '反馈内容',
  `images` varchar(500) DEFAULT NULL COMMENT '反馈图片(逗号分隔)',
  `status` tinyint(4) DEFAULT '1' COMMENT '状态: 1待处理 2已处理',
  `reply_content` text COMMENT '回复内容',
  `reply_time` datetime DEFAULT NULL COMMENT '回复时间',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2069771466764533762 DEFAULT CHARSET=utf8 COMMENT='反馈表';

/*Table structure for table `t_follow` */

DROP TABLE IF EXISTS `t_follow`;

CREATE TABLE `t_follow` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '关注ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `follow_type` tinyint(4) NOT NULL COMMENT '关注类型: 1医院 2医生 3疾病',
  `follow_id` bigint(20) NOT NULL COMMENT '关注对象ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '关注时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_follow` (`user_id`,`follow_type`,`follow_id`)
) ENGINE=InnoDB AUTO_INCREMENT=2069767684295049219 DEFAULT CHARSET=utf8 COMMENT='关注表';

/*Table structure for table `t_hospital` */

DROP TABLE IF EXISTS `t_hospital`;

CREATE TABLE `t_hospital` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '医院ID',
  `name` varchar(100) NOT NULL COMMENT '医院名称',
  `level` varchar(20) DEFAULT NULL COMMENT '医院等级',
  `address` varchar(255) DEFAULT NULL COMMENT '医院地址',
  `phone` varchar(20) DEFAULT NULL COMMENT '联系电话',
  `intro` text COMMENT '医院简介',
  `image` varchar(255) DEFAULT 'img/hospital.jpg' COMMENT '医院图片',
  `province` varchar(50) DEFAULT NULL COMMENT '省份',
  `city` varchar(50) DEFAULT NULL COMMENT '城市',
  `department_count` int(11) DEFAULT '0' COMMENT '科室数量',
  `doctor_count` int(11) DEFAULT '0' COMMENT '医生数量',
  `follow_count` int(11) DEFAULT '0' COMMENT '关注数量',
  `status` tinyint(4) DEFAULT '1' COMMENT '状态: 1正常 0禁用',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=27 DEFAULT CHARSET=utf8 COMMENT='医院表';

/*Table structure for table `t_hospital_department` */

DROP TABLE IF EXISTS `t_hospital_department`;

CREATE TABLE `t_hospital_department` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `hospital_id` bigint(20) NOT NULL COMMENT '医院ID',
  `department_id` bigint(20) NOT NULL COMMENT '科室ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_hospital_department` (`hospital_id`,`department_id`)
) ENGINE=InnoDB AUTO_INCREMENT=65 DEFAULT CHARSET=utf8 COMMENT='医院-科室关联表';

/*Table structure for table `t_message` */

DROP TABLE IF EXISTS `t_message`;

CREATE TABLE `t_message` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '消息ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `title` varchar(100) NOT NULL COMMENT '消息标题',
  `content` text COMMENT '消息内容',
  `is_read` tinyint(4) DEFAULT '0' COMMENT '是否已读: 1是 0否',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8 COMMENT='消息表';

/*Table structure for table `t_payment_flow` */

DROP TABLE IF EXISTS `t_payment_flow`;

CREATE TABLE `t_payment_flow` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `business_order_no` varchar(64) NOT NULL COMMENT '业务订单号（挂号订单号或咨询订单号）',
  `business_type` tinyint(4) NOT NULL DEFAULT '1' COMMENT '业务类型：1-挂号订单 2-咨询订单',
  `pay_method` tinyint(4) NOT NULL DEFAULT '1' COMMENT '支付方式：1-支付宝 2-微信',
  `third_party_trade_no` varchar(128) DEFAULT NULL COMMENT '第三方交易号（支付宝或微信返回的交易号）',
  `actual_amount` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '实付金额',
  `pay_status` tinyint(4) NOT NULL DEFAULT '0' COMMENT '支付状态：0-待支付 1-已支付 2-已退款 3-已关闭',
  `pay_success_time` datetime DEFAULT NULL COMMENT '支付成功时间',
  `original_callback` text COMMENT '原始回调报文（用于对账和排查问题）',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='支付流水表';

/*Table structure for table `t_review` */

DROP TABLE IF EXISTS `t_review`;

CREATE TABLE `t_review` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '评价ID',
  `order_type` tinyint(4) NOT NULL COMMENT '订单类型: 1挂号 2咨询',
  `order_id` bigint(20) NOT NULL COMMENT '订单ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `doctor_id` bigint(20) NOT NULL COMMENT '医生ID',
  `rating` int(11) NOT NULL COMMENT '评分(1-5)',
  `content` text COMMENT '评价内容',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8 COMMENT='评价表';

/*Table structure for table `t_schedule` */

DROP TABLE IF EXISTS `t_schedule`;

CREATE TABLE `t_schedule` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '排班ID',
  `doctor_id` bigint(20) NOT NULL COMMENT '医生ID',
  `hospital_id` bigint(20) NOT NULL COMMENT '医院ID',
  `department_id` bigint(20) NOT NULL COMMENT '科室ID',
  `schedule_date` date NOT NULL COMMENT '排班日期',
  `time_slot` varchar(20) NOT NULL COMMENT '时间段(08:00-08:30等)',
  `total_count` int(11) DEFAULT '20' COMMENT '总号源',
  `remain_count` int(11) DEFAULT '20' COMMENT '剩余号源',
  `status` tinyint(4) DEFAULT '1' COMMENT '状态: 1可预约 0已约满',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=17 DEFAULT CHARSET=utf8 COMMENT='医生排班表';

/*Table structure for table `t_user` */

DROP TABLE IF EXISTS `t_user`;

CREATE TABLE `t_user` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` varchar(50) NOT NULL COMMENT '用户名',
  `password` varchar(100) NOT NULL COMMENT '密码',
  `phone` varchar(20) NOT NULL COMMENT '手机号',
  `email` varchar(100) DEFAULT NULL COMMENT '邮箱',
  `real_name` varchar(50) DEFAULT NULL COMMENT '真实姓名',
  `gender` tinyint(4) DEFAULT NULL COMMENT '性别: 1男 2女',
  `birthday` date DEFAULT NULL COMMENT '生日',
  `avatar` varchar(255) DEFAULT 'img/default-avatar.png' COMMENT '头像',
  `status` tinyint(4) DEFAULT '1' COMMENT '状态: 1正常 0禁用',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `username` (`username`),
  UNIQUE KEY `phone` (`phone`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8 COMMENT='用户表';

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;
