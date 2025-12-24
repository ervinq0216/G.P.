/*
 Navicat Premium Dump SQL

 Source Server         : localhost_3306
 Source Server Type    : MySQL
 Source Server Version : 80044 (8.0.44)
 Source Host           : localhost:3306
 Source Schema         : hospital_db

 Target Server Type    : MySQL
 Target Server Version : 80044 (8.0.44)
 File Encoding         : 65001

 Date: 25/12/2025 00:55:03
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for t_admin
-- ----------------------------
DROP TABLE IF EXISTS `t_admin`;
CREATE TABLE `t_admin`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户名(账号)',
  `password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '密码',
  `real_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '真实姓名',
  `created_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_username`(`username` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '管理员表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of t_admin
-- ----------------------------
INSERT INTO `t_admin` VALUES (1, 'admin', '123456', '超级管理员', '2025-12-25 00:40:41');

-- ----------------------------
-- Table structure for t_attendance
-- ----------------------------
DROP TABLE IF EXISTS `t_attendance`;
CREATE TABLE `t_attendance`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `doctor_id` bigint NOT NULL COMMENT '医生ID',
  `date` date NOT NULL COMMENT '考勤日期',
  `morning_in` time NULL DEFAULT NULL COMMENT '早上签到时间',
  `morning_out` time NULL DEFAULT NULL COMMENT '早上签退时间',
  `afternoon_in` time NULL DEFAULT NULL COMMENT '下午签到时间',
  `afternoon_out` time NULL DEFAULT NULL COMMENT '下午签退时间',
  `created_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_doc_date`(`doctor_id` ASC, `date` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '医生考勤表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of t_attendance
-- ----------------------------

-- ----------------------------
-- Table structure for t_department
-- ----------------------------
DROP TABLE IF EXISTS `t_department`;
CREATE TABLE `t_department`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `category` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '大类(手术/非手术/诊断)',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '科室名称',
  `intro` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '科室简介',
  `created_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 11 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '科室表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of t_department
-- ----------------------------
INSERT INTO `t_department` VALUES (1, '非手术科室', '心内科', '主治心血管相关疾病', '2025-12-25 00:40:41');
INSERT INTO `t_department` VALUES (2, '非手术科室', '呼吸内科', '主治呼吸系统疾病', '2025-12-25 00:40:41');
INSERT INTO `t_department` VALUES (3, '非手术科室', '消化内科', '主治消化系统疾病', '2025-12-25 00:40:41');
INSERT INTO `t_department` VALUES (4, '非手术科室', '神经内科', '主治神经系统疾病', '2025-12-25 00:40:41');
INSERT INTO `t_department` VALUES (5, '手术科室', '普外科', '普通外科手术', '2025-12-25 00:40:41');
INSERT INTO `t_department` VALUES (6, '手术科室', '骨科', '骨骼肌肉系统疾病', '2025-12-25 00:40:41');
INSERT INTO `t_department` VALUES (7, '手术科室', '神经外科', '脑部及神经系统手术', '2025-12-25 00:40:41');
INSERT INTO `t_department` VALUES (8, '诊断相关', '放射科', 'X光/CT/MRI检查', '2025-12-25 00:40:41');
INSERT INTO `t_department` VALUES (9, '诊断相关', '检验科', '血液/体液化验', '2025-12-25 00:40:41');
INSERT INTO `t_department` VALUES (10, '诊断相关', '超声科', 'B超/彩超检查', '2025-12-25 00:40:41');

-- ----------------------------
-- Table structure for t_doctor
-- ----------------------------
DROP TABLE IF EXISTS `t_doctor`;
CREATE TABLE `t_doctor`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `job_number` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '工号(账号)',
  `password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '密码',
  `real_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '医生姓名',
  `dept_id` bigint NULL DEFAULT NULL COMMENT '所属科室ID',
  `avatar` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '头像(Base64)',
  `gender` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '性别',
  `introduction` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '个人简介',
  `created_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_job_number`(`job_number` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '医生表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of t_doctor
-- ----------------------------
INSERT INTO `t_doctor` VALUES (1, 'D001', '123456', '张医生', 1, NULL, '男', '主任医师，擅长冠心病治疗', '2025-12-25 00:40:41');

-- ----------------------------
-- Table structure for t_leave
-- ----------------------------
DROP TABLE IF EXISTS `t_leave`;
CREATE TABLE `t_leave`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `doctor_id` bigint NOT NULL COMMENT '申请医生ID',
  `type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '请假类型',
  `reason` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '请假理由',
  `start_date` date NOT NULL COMMENT '开始日期',
  `end_date` date NOT NULL COMMENT '结束日期',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'pending' COMMENT '状态(pending/approved/rejected)',
  `created_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '请假申请表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of t_leave
-- ----------------------------

-- ----------------------------
-- Table structure for t_notice
-- ----------------------------
DROP TABLE IF EXISTS `t_notice`;
CREATE TABLE `t_notice`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '标题',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '内容',
  `type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'notice' COMMENT '类型(notice/suggestion)',
  `created_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '通知公告表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of t_notice
-- ----------------------------

-- ----------------------------
-- Table structure for t_patient
-- ----------------------------
DROP TABLE IF EXISTS `t_patient`;
CREATE TABLE `t_patient`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '手机号(账号)',
  `password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '密码',
  `real_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '真实姓名',
  `avatar` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '头像(Base64)',
  `gender` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '性别',
  `birthday` date NULL DEFAULT NULL COMMENT '出生日期',
  `height` double NULL DEFAULT NULL COMMENT '身高(cm)',
  `weight` double NULL DEFAULT NULL COMMENT '体重(kg)',
  `blood_type` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '血型',
  `created_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_phone`(`phone` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '患者表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of t_patient
-- ----------------------------
INSERT INTO `t_patient` VALUES (1, '13800138000', '123456', '李患者', NULL, '男', NULL, NULL, NULL, '', '2025-12-25 00:40:41');

SET FOREIGN_KEY_CHECKS = 1;
