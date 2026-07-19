-- ===================================================================
-- 医院在线预约挂号系统 - 数据库初始化脚本
-- 数据库名称: hospital_db
-- 适用: MySQL 8.0+
-- ===================================================================

CREATE DATABASE IF NOT EXISTS hospital_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE hospital_db;

-- ===================================================================
-- 一、建表
-- ===================================================================

DROP TABLE IF EXISTS t_user_token;
DROP TABLE IF EXISTS t_payment_flow;
DROP TABLE IF EXISTS t_consult;
DROP TABLE IF EXISTS t_appointment;
DROP TABLE IF EXISTS t_schedule;
DROP TABLE IF EXISTS t_review;
DROP TABLE IF EXISTS t_feedback;
DROP TABLE IF EXISTS t_message;
DROP TABLE IF EXISTS t_follow;
DROP TABLE IF EXISTS t_family_member;
DROP TABLE IF EXISTS t_article;
DROP TABLE IF EXISTS t_disease;
DROP TABLE IF EXISTS t_doctor;
DROP TABLE IF EXISTS t_hospital_department;
DROP TABLE IF EXISTS t_department;
DROP TABLE IF EXISTS t_hospital;
DROP TABLE IF EXISTS t_config;
DROP TABLE IF EXISTS t_user;

CREATE TABLE t_user (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  username VARCHAR(64) NOT NULL,
  password VARCHAR(128) NOT NULL,
  phone VARCHAR(32) NOT NULL,
  email VARCHAR(128),
  real_name VARCHAR(64),
  gender INT,
  birthday DATE,
  avatar VARCHAR(255),
  status INT DEFAULT 1,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_username (username),
  UNIQUE KEY uk_phone (phone)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE t_user_token (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  token VARCHAR(255) NOT NULL,
  status INT NOT NULL DEFAULT 1,
  expire_time DATETIME,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE t_family_member (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  name VARCHAR(64) NOT NULL,
  gender INT,
  birthday DATE,
  phone VARCHAR(32),
  id_card VARCHAR(32),
  relation VARCHAR(32),
  is_default INT DEFAULT 0,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE t_hospital (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(128) NOT NULL,
  level VARCHAR(32),
  address VARCHAR(255),
  phone VARCHAR(32),
  intro TEXT,
  image VARCHAR(255),
  province VARCHAR(64),
  city VARCHAR(64),
  district VARCHAR(64),
  department_count INT DEFAULT 0,
  doctor_count INT DEFAULT 0,
  follow_count INT DEFAULT 0,
  status INT DEFAULT 1,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE t_department (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(128) NOT NULL,
  description VARCHAR(255),
  parent_id BIGINT DEFAULT 0,
  sort_order INT DEFAULT 0,
  status INT DEFAULT 1,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE t_hospital_department (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  hospital_id BIGINT NOT NULL,
  department_id BIGINT NOT NULL,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE t_doctor (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(64) NOT NULL,
  gender INT,
  title VARCHAR(64),
  department_id BIGINT,
  hospital_id BIGINT,
  avatar VARCHAR(255),
  phone VARCHAR(32),
  intro TEXT,
  expertise VARCHAR(255),
  consult_count INT DEFAULT 0,
  rating DECIMAL(3,1) DEFAULT 5.0,
  follow_count INT DEFAULT 0,
  online_status INT DEFAULT 1,
  price DECIMAL(10,2) DEFAULT 0.00,
  registration_price DECIMAL(10,2) DEFAULT 0.00,
  status INT DEFAULT 1,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE t_schedule (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  doctor_id BIGINT NOT NULL,
  hospital_id BIGINT NOT NULL,
  department_id BIGINT NOT NULL,
  schedule_date DATE,
  time_slot VARCHAR(32),
  total_count INT DEFAULT 0,
  remain_count INT DEFAULT 0,
  status INT DEFAULT 1,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE t_appointment (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  order_no VARCHAR(64) NOT NULL,
  user_id BIGINT NOT NULL,
  doctor_id BIGINT NOT NULL,
  hospital_id BIGINT NOT NULL,
  patient_name VARCHAR(64),
  patient_phone VARCHAR(32),
  patient_id_card VARCHAR(32),
  patient_gender INT,
  patient_age INT,
  appointment_date DATE,
  appointment_time VARCHAR(32),
  disease_desc VARCHAR(255),
  amount DECIMAL(10,2),
  status INT DEFAULT 1,
  pay_time DATETIME,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_order_no (order_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE t_consult (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  order_no VARCHAR(64) NOT NULL,
  user_id BIGINT NOT NULL,
  doctor_id BIGINT NOT NULL,
  patient_name VARCHAR(64),
  patient_phone VARCHAR(32),
  disease_desc VARCHAR(255),
  appointment_time DATETIME,
  duration INT DEFAULT 15,
  amount DECIMAL(10,2),
  status INT DEFAULT 1,
  pay_time DATETIME,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_order_no (order_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE t_payment_flow (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  business_order_no VARCHAR(64) NOT NULL,
  business_type INT,
  pay_method INT,
  third_party_trade_no VARCHAR(64),
  actual_amount DECIMAL(10,2),
  pay_status INT DEFAULT 0,
  pay_success_time DATETIME,
  original_callback TEXT,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE t_article (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  title VARCHAR(128) NOT NULL,
  summary VARCHAR(255),
  content TEXT,
  department_id BIGINT,
  author VARCHAR(64),
  image VARCHAR(255),
  views INT DEFAULT 0,
  status INT DEFAULT 1,
  publish_time DATETIME,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE t_disease (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  department_id BIGINT,
  name VARCHAR(128) NOT NULL,
  description TEXT,
  alias VARCHAR(128),
  location VARCHAR(128),
  treatment TEXT,
  symptoms TEXT,
  treatment_period VARCHAR(64),
  cure_rate VARCHAR(64),
  examinations TEXT,
  follow_count INT DEFAULT 0,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE t_follow (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  follow_type INT,
  follow_id BIGINT,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE t_review (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  order_type INT,
  order_id BIGINT,
  user_id BIGINT,
  doctor_id BIGINT,
  rating INT,
  content VARCHAR(255),
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE t_feedback (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT,
  feedback_type INT,
  content VARCHAR(255),
  images VARCHAR(500),
  status INT DEFAULT 0,
  reply_content VARCHAR(255),
  reply_time DATETIME,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE t_message (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT,
  title VARCHAR(128),
  content VARCHAR(255),
  is_read INT DEFAULT 0,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE t_config (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  config_key VARCHAR(64),
  config_value TEXT,
  description VARCHAR(255),
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ===================================================================
-- 二、种子数据
-- ===================================================================

-- 用户（密码明文 "123456"）
INSERT INTO t_user (id, username, password, phone, email, real_name, gender, birthday, avatar, status, create_time, update_time) VALUES
(1, '13800138000', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5Eh', '13800138000', 'harry@example.com', '张三', 1, '1990-01-01', '/avatar/user-1.png', 1, NOW(), NOW());

-- 科室
INSERT INTO t_department (id, name, description, parent_id, sort_order, status, create_time) VALUES
(1, '内科', '内科', 0, 1, 1, NOW()),
(2, '心血管内科', '心血管内科', 1, 1, 1, NOW()),
(3, '外科', '外科', 0, 2, 1, NOW()),
(4, '骨科', '骨科', 3, 1, 1, NOW()),
(5, '儿科', '儿科', 0, 3, 1, NOW()),
(6, '妇产科', '妇产科', 0, 4, 1, NOW()),
(7, '神经内科', '神经内科', 1, 2, 1, NOW()),
(8, '皮肤科', '皮肤科', 0, 5, 1, NOW()),
(9, '眼科', '眼科', 0, 6, 1, NOW()),
(10, '耳鼻喉科', '耳鼻喉科', 0, 7, 1, NOW());

-- 医院
INSERT INTO t_hospital (id, name, level, address, phone, intro, image, province, city, district, department_count, doctor_count, follow_count, status, create_time, update_time) VALUES
(1, '北京第一人民医院', '三甲', '北京市朝阳区健康路1号', '010-10000001', '北京第一人民医院是一所集医疗、教学、科研为一体的综合性三级甲等医院，拥有先进的医疗设备和专业的医疗团队。', '/img/hospital-1.png', '北京', '北京', '朝阳区', 10, 50, 120, 1, NOW(), NOW()),
(2, '上海瑞康医院', '三甲', '上海市浦东新区仁心路8号', '021-20000002', '上海瑞康医院专注于慢病管理与康复治疗，在心血管疾病、糖尿病等领域具有丰富的临床经验。', '/img/hospital-2.png', '上海', '上海', '浦东新区', 8, 30, 80, 1, NOW(), NOW()),
(3, '广州华康医院', '三甲', '广州市天河区中山大道100号', '020-30000003', '广州华康医院是华南地区知名的综合性医院，拥有多个国家级重点专科。', '/img/hospital-3.png', '广东', '广州', '天河区', 12, 60, 200, 1, NOW(), NOW());

-- 医院-科室关联
INSERT INTO t_hospital_department (id, hospital_id, department_id, create_time) VALUES
(1, 1, 1, NOW()), (2, 1, 2, NOW()), (3, 1, 3, NOW()), (4, 1, 4, NOW()),
(5, 1, 5, NOW()), (6, 1, 7, NOW()), (7, 1, 8, NOW()), (8, 1, 9, NOW()),
(9, 2, 1, NOW()), (10, 2, 2, NOW()), (11, 2, 3, NOW()), (12, 2, 6, NOW()),
(13, 3, 1, NOW()), (14, 3, 2, NOW()), (15, 3, 3, NOW()), (16, 3, 4, NOW()),
(17, 3, 5, NOW()), (18, 3, 7, NOW()), (19, 3, 10, NOW());

-- 医生
INSERT INTO t_doctor (id, name, gender, title, department_id, hospital_id, avatar, phone, intro, expertise, consult_count, rating, follow_count, online_status, price, registration_price, status, create_time, update_time) VALUES
(1, '李主任', 1, '主任医师', 2, 1, '/img/doctor-1.png', '13811110001', '从事心血管内科临床工作30余年，擅长冠心病、高血压、心力衰竭等疾病的诊疗，在介入治疗方面有丰富经验。', '冠心病,高血压,心衰', 2680, 4.9, 300, 1, 39.90, 18.00, 1, NOW(), NOW()),
(2, '王医生', 2, '副主任医师', 4, 1, '/img/doctor-2.png', '13811110002', '专注骨科临床工作15年，擅长关节损伤、骨折康复及运动医学，在膝关节置换方面有独到见解。', '膝关节,髋关节,骨折康复', 1520, 4.7, 180, 1, 29.90, 20.00, 1, NOW(), NOW()),
(3, '周医生', 1, '主治医师', 2, 2, '/img/doctor-3.png', '13811110003', '擅长高血压、糖尿病等慢性病的长期管理与随访，注重患者健康教育。', '高血压,糖尿病', 980, 4.6, 120, 1, 25.00, 15.00, 1, NOW(), NOW()),
(4, '赵医生', 2, '主任医师', 5, 1, '/img/doctor-4.png', '13811110004', '儿科主任医师，从事儿科临床工作25年，擅长儿童呼吸系统疾病及新生儿疾病诊疗。', '儿童哮喘,小儿肺炎,新生儿护理', 2100, 4.8, 250, 1, 35.00, 20.00, 1, NOW(), NOW()),
(5, '孙医生', 1, '副主任医师', 7, 3, '/img/doctor-5.png', '13811110005', '神经内科专家，擅长脑血管疾病、帕金森病及癫痫的诊疗。', '脑卒中,帕金森,癫痫', 1800, 4.7, 200, 1, 32.00, 18.00, 1, NOW(), NOW());

-- 排班（未来7天）
INSERT INTO t_schedule (id, doctor_id, hospital_id, department_id, schedule_date, time_slot, total_count, remain_count, status, create_time) VALUES
(1, 1, 1, 2, DATE_ADD(CURDATE(), INTERVAL 1 DAY), '08:00-09:00', 20, 8, 1, NOW()),
(2, 1, 1, 2, DATE_ADD(CURDATE(), INTERVAL 1 DAY), '09:00-10:00', 20, 12, 1, NOW()),
(3, 1, 1, 2, DATE_ADD(CURDATE(), INTERVAL 2 DAY), '08:00-09:00', 20, 5, 1, NOW()),
(4, 2, 1, 4, DATE_ADD(CURDATE(), INTERVAL 1 DAY), '14:00-15:00', 15, 3, 1, NOW()),
(5, 2, 1, 4, DATE_ADD(CURDATE(), INTERVAL 3 DAY), '14:00-15:00', 15, 10, 1, NOW()),
(6, 3, 2, 2, DATE_ADD(CURDATE(), INTERVAL 1 DAY), '09:00-10:00', 10, 6, 1, NOW()),
(7, 3, 2, 2, DATE_ADD(CURDATE(), INTERVAL 2 DAY), '09:00-10:00', 10, 8, 1, NOW()),
(8, 4, 1, 5, DATE_ADD(CURDATE(), INTERVAL 1 DAY), '08:00-09:00', 15, 10, 1, NOW()),
(9, 5, 3, 7, DATE_ADD(CURDATE(), INTERVAL 1 DAY), '10:00-11:00', 12, 7, 1, NOW());

-- 文章
INSERT INTO t_article (id, title, summary, content, department_id, author, image, views, status, publish_time, create_time, update_time) VALUES
(1, '高血压日常管理指南', '从饮食到运动的完整建议，帮助您科学管理血压', '<h3>高血压概述</h3><p>高血压是最常见的慢性病之一，也是心脑血管疾病最主要的危险因素。科学的日常管理对控制血压至关重要。</p><h3>饮食建议</h3><p>1. 低盐饮食：每日食盐摄入量不超过6克<br>2. 多吃蔬菜水果<br>3. 控制脂肪摄入</p><h3>运动建议</h3><p>每周至少进行150分钟中等强度有氧运动。</p>', 2, '健康编辑部', '/img/article-1.png', 1200, 1, NOW(), NOW(), NOW()),
(2, '膝关节康复指南', '常见康复动作和注意事项，助您早日恢复', '<h3>膝关节损伤概述</h3><p>膝关节是人体最大最复杂的关节，损伤后需要科学的康复训练。</p><h3>康复动作</h3><p>1. 直腿抬高<br>2. 靠墙半蹲<br>3. 踝泵运动</p>', 4, '康复科', '/img/article-2.png', 860, 1, NOW(), NOW(), NOW()),
(3, '儿童常见疾病预防', '了解儿童常见疾病的预防措施，守护孩子健康', '<h3>儿童健康防护</h3><p>儿童免疫系统尚未发育完全，需要家长格外关注。</p><h3>常见疾病</h3><p>1. 感冒<br>2. 手足口病<br>3. 腹泻</p>', 5, '儿科', '/img/article-3.png', 950, 1, NOW(), NOW(), NOW()),
(4, '糖尿病饮食控制', '科学饮食，控制血糖，享受健康生活', '<h3>糖尿病饮食原则</h3><p>合理控制总热量摄入，均衡营养。</p><h3>推荐食物</h3><p>1. 全谷物<br>2. 绿叶蔬菜<br>3. 优质蛋白</p>', 2, '营养科', '/img/article-4.png', 780, 1, NOW(), NOW(), NOW());

-- 疾病
INSERT INTO t_disease (id, department_id, name, description, alias, location, treatment, symptoms, treatment_period, cure_rate, examinations, follow_count, create_time, update_time) VALUES
(1, 2, '高血压', '高血压是指以体循环动脉血压增高为主要特征的慢性疾病，可伴有心、脑、肾等器官的功能或器质性损害。', '血压高', '血管', '药物治疗+生活方式干预', '头晕、头痛、心悸、耳鸣', '长期管理', '可控', '血压监测、心电图、超声心动图', 220, NOW(), NOW()),
(2, 4, '半月板损伤', '半月板是膝关节内的纤维软骨结构，损伤后会引起膝关节疼痛、肿胀和活动受限。', '半月板撕裂', '膝关节', '保守治疗+康复训练，严重者需手术', '膝痛、肿胀、关节弹响、交锁', '3-6个月', '较高', 'MRI、X线、关节镜检查', 160, NOW(), NOW()),
(3, 5, '小儿肺炎', '小儿肺炎是婴幼儿时期常见的呼吸系统疾病，多由细菌或病毒感染引起。', '儿童肺炎', '肺部', '抗生素/抗病毒治疗+对症支持', '发热、咳嗽、气促、呼吸困难', '1-2周', '高', '血常规、胸片、病原学检查', 180, NOW(), NOW()),
(4, 7, '脑卒中', '脑卒中又称中风，是由于脑部血管突然破裂或因血管阻塞导致血液不能流入大脑而引起脑组织损伤的疾病。', '中风', '脑部', '急性期溶栓/取栓+康复治疗', '偏瘫、言语障碍、面瘫、头晕', '急性期后长期康复', '取决于救治及时性', 'CT、MRI、血管造影', 300, NOW(), NOW());

-- 就诊人
INSERT INTO t_family_member (id, user_id, name, gender, birthday, phone, id_card, relation, is_default, create_time, update_time) VALUES
(1, 1, '张三', 1, '1990-01-01', '13800138000', '110101199001010011', '本人', 1, NOW(), NOW()),
(2, 1, '李四', 2, '1992-02-02', '13800138001', '110101199202020022', '配偶', 0, NOW(), NOW()),
(3, 1, '张小宝', 1, '2020-05-15', '13800138002', NULL, '子女', 0, NOW(), NOW());

-- 挂号订单
INSERT INTO t_appointment (id, order_no, user_id, doctor_id, hospital_id, patient_name, patient_phone, patient_id_card, patient_gender, patient_age, appointment_date, appointment_time, disease_desc, amount, status, pay_time, create_time, update_time) VALUES
(1, 'AP202607170001', 1, 1, 1, '张三', '13800138000', '110101199001010011', 1, 36, DATE_ADD(CURDATE(), INTERVAL 1 DAY), '08:00-09:00', '血压偏高，头晕', 18.00, 2, NOW(), NOW(), NOW());

-- 咨询订单
INSERT INTO t_consult (id, order_no, user_id, doctor_id, patient_name, patient_phone, disease_desc, appointment_time, duration, amount, status, pay_time, create_time, update_time) VALUES
(1, 'CO202607170001', 1, 1, '张三', '13800138000', '复诊咨询，血压控制情况', DATE_ADD(NOW(), INTERVAL 1 DAY), 15, 39.90, 2, NOW(), NOW(), NOW());

-- 支付流水
INSERT INTO t_payment_flow (id, business_order_no, business_type, pay_method, third_party_trade_no, actual_amount, pay_status, pay_success_time, original_callback, create_time, update_time) VALUES
(1, 'AP202607170001', 1, 2, 'WX-AP-0001', 18.00, 1, NOW(), 'callback-ok', NOW(), NOW()),
(2, 'CO202607170001', 2, 1, 'ALI-CO-0001', 39.90, 1, NOW(), 'callback-ok', NOW(), NOW());

-- 评价
INSERT INTO t_review (id, order_type, order_id, user_id, doctor_id, rating, content, create_time) VALUES
(1, 1, 1, 1, 1, 5, '李主任非常专业，讲解很清楚，态度也很好！', NOW());

-- 反馈
INSERT INTO t_feedback (id, user_id, feedback_type, content, images, status, reply_content, reply_time, create_time, update_time) VALUES
(1, 1, 1, '首页轮播图加载较慢，希望能优化', '/img/f1.png,/img/f2.png', 1, '感谢您的反馈，我们会尽快优化。', NOW(), NOW(), NOW());

-- 消息
INSERT INTO t_message (id, user_id, title, content, is_read, create_time) VALUES
(1, 1, '挂号成功', '您的挂号订单已支付成功，请按时就诊。', 0, NOW()),
(2, 1, '咨询提醒', '您的电话咨询将在明天开始，请保持电话畅通。', 1, NOW()),
(3, 1, '系统通知', '欢迎使用医院在线预约挂号系统！', 0, NOW());

-- 关注
INSERT INTO t_follow (id, user_id, follow_type, follow_id, create_time) VALUES
(1, 1, 1, 1, NOW()),
(2, 1, 2, 1, NOW()),
(3, 1, 3, 1, NOW());

-- 系统配置
INSERT INTO t_config (id, config_key, config_value, description, update_time) VALUES
(1, 'paymentMethods', '[{"label":"支付宝","value":1},{"label":"微信","value":2}]', '支付方式配置', NOW()),
(2, 'homeBanners', '[{"image":"/img/banner-1.png","link":"/doctor/1"},{"image":"/img/banner-2.png","link":"/hospital/1"},{"image":"/img/banner-3.png","link":"/article/1"}]', '首页轮播图配置', NOW()),
(3, 'siteName', '医院在线预约挂号系统', '网站名称', NOW()),
(4, 'siteDescription', '汇聚优质医疗资源，让就医更简单', '网站描述', NOW());