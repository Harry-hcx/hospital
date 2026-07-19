INSERT INTO t_user (id, username, password, phone, email, real_name, gender, birthday, avatar, status, create_time, update_time) VALUES
(1, '13800138000', '123456', '13800138000', 'harry@example.com', '张三', 1, DATE '1990-01-01', '/avatar/user-1.png', 1, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP());

INSERT INTO t_department (id, name, description, parent_id, sort_order, status, create_time) VALUES
(1, '内科', '内科', 0, 1, 1, CURRENT_TIMESTAMP()),
(2, '心血管内科', '心血管内科', 1, 1, 1, CURRENT_TIMESTAMP()),
(3, '外科', '外科', 0, 2, 1, CURRENT_TIMESTAMP()),
(4, '骨科', '骨科', 3, 1, 1, CURRENT_TIMESTAMP());

INSERT INTO t_hospital (id, name, level, address, phone, intro, image, province, city, department_count, doctor_count, follow_count, status, create_time, update_time) VALUES
(1, '北京第一人民医院', '三甲', '北京市朝阳区健康路1号', '010-10000001', '综合性三甲医院', '/img/hospital-1.png', '北京', '北京', 2, 2, 120, 1, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
(2, '上海瑞康医院', '三甲', '上海市浦东新区仁心路8号', '021-20000002', '专注慢病管理', '/img/hospital-2.png', '上海', '上海', 2, 1, 80, 1, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP());

INSERT INTO t_hospital_department (id, hospital_id, department_id, create_time) VALUES
(1, 1, 1, CURRENT_TIMESTAMP()),
(2, 1, 2, CURRENT_TIMESTAMP()),
(3, 1, 3, CURRENT_TIMESTAMP()),
(4, 1, 4, CURRENT_TIMESTAMP()),
(5, 2, 1, CURRENT_TIMESTAMP()),
(6, 2, 2, CURRENT_TIMESTAMP());

INSERT INTO t_doctor (id, name, gender, title, department_id, hospital_id, avatar, phone, intro, expertise, consult_count, rating, follow_count, online_status, price, registration_price, status, create_time, update_time) VALUES
(1, '李主任', 1, '主任医师', 2, 1, '/img/doctor-1.png', '13811110001', '擅长冠心病与高血压诊疗', '冠心病,高血压,心衰', 2680, 4.9, 300, 1, 39.90, 18.00, 1, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
(2, '王医生', 2, '副主任医师', 4, 1, '/img/doctor-2.png', '13811110002', '擅长关节损伤与康复', '膝关节,髋关节,骨折康复', 1520, 4.7, 180, 1, 29.90, 20.00, 1, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
(3, '周医生', 1, '主治医师', 2, 2, '/img/doctor-3.png', '13811110003', '擅长慢病随访', '高血压,糖尿病', 980, 4.6, 120, 1, 25.00, 15.00, 1, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP());

INSERT INTO t_schedule (id, doctor_id, hospital_id, department_id, schedule_date, time_slot, total_count, remain_count, status, create_time) VALUES
(1, 1, 1, 2, DATEADD('DAY', 1, CURRENT_DATE()), '08:00-09:00', 20, 8, 1, CURRENT_TIMESTAMP()),
(2, 1, 1, 2, DATEADD('DAY', 2, CURRENT_DATE()), '09:00-10:00', 20, 5, 1, CURRENT_TIMESTAMP()),
(3, 2, 1, 4, DATEADD('DAY', 1, CURRENT_DATE()), '14:00-15:00', 15, 3, 1, CURRENT_TIMESTAMP());

INSERT INTO t_article (id, title, summary, content, department_id, author, image, views, status, publish_time, create_time, update_time) VALUES
(1, '高血压日常管理', '从饮食到运动的完整建议', '文章正文1', 2, '编辑部', '/img/article-1.png', 1200, 1, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
(2, '膝关节康复指南', '常见康复动作和注意事项', '文章正文2', 4, '编辑部', '/img/article-2.png', 860, 1, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP());

INSERT INTO t_disease (id, department_id, name, description, alias, location, treatment, symptoms, treatment_period, cure_rate, examinations, follow_count, create_time, update_time) VALUES
(1, 2, '高血压', '高血压描述', '血压高', '血管', '药物+生活方式', '头晕、头痛', '长期', '可控', '血压监测', 220, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
(2, 4, '半月板损伤', '半月板损伤描述', '半月板撕裂', '膝关节', '康复+手术', '膝痛、肿胀', '3个月', '较高', 'MRI', 160, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP());

INSERT INTO t_family_member (id, user_id, name, gender, birthday, phone, id_card, relation, is_default, create_time, update_time) VALUES
(1, 1, '张三', 1, DATE '1990-01-01', '13800138000', '110101199001010011', '本人', 1, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
(2, 1, '李四', 2, DATE '1992-02-02', '13800138001', '110101199202020022', '配偶', 0, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP());

INSERT INTO t_appointment (id, order_no, user_id, doctor_id, hospital_id, patient_name, patient_phone, patient_id_card, patient_gender, patient_age, appointment_date, appointment_time, disease_desc, amount, status, pay_time, create_time, update_time) VALUES
(1, 'AP202607170001', 1, 1, 1, '张三', '13800138000', '110101199001010011', 1, 36, DATEADD('DAY', 1, CURRENT_DATE()), '08:00-09:00', '血压偏高', 18.00, 2, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP());

INSERT INTO t_consult (id, order_no, user_id, doctor_id, patient_name, patient_phone, disease_desc, appointment_time, duration, amount, status, pay_time, create_time, update_time) VALUES
(1, 'CO202607170001', 1, 1, '张三', '13800138000', '复诊咨询', DATEADD('DAY', 1, CURRENT_TIMESTAMP()), 15, 39.90, 2, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP());

INSERT INTO t_payment_flow (id, business_order_no, business_type, pay_method, third_party_trade_no, actual_amount, pay_status, pay_success_time, original_callback, create_time, update_time) VALUES
(1, 'AP202607170001', 1, 2, 'WX-AP-0001', 18.00, 1, CURRENT_TIMESTAMP(), 'callback-ok', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
(2, 'CO202607170001', 2, 1, 'ALI-CO-0001', 39.90, 1, CURRENT_TIMESTAMP(), 'callback-ok', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP());

INSERT INTO t_review (id, order_type, order_id, user_id, doctor_id, rating, content, create_time) VALUES
(1, 1, 1, 1, 1, 5, '医生讲解很清楚', CURRENT_TIMESTAMP());

INSERT INTO t_feedback (id, user_id, feedback_type, content, images, status, reply_content, reply_time, create_time, update_time) VALUES
(1, 1, 1, '首页轮播图加载较慢', '/img/f1.png,/img/f2.png', 1, NULL, NULL, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP());

INSERT INTO t_message (id, user_id, title, content, is_read, create_time) VALUES
(1, 1, '挂号成功', '您的挂号订单已支付成功', 0, CURRENT_TIMESTAMP()),
(2, 1, '咨询提醒', '您的电话咨询将在明天开始', 1, CURRENT_TIMESTAMP());

INSERT INTO t_follow (id, user_id, follow_type, follow_id, create_time) VALUES
(1, 1, 1, 1, CURRENT_TIMESTAMP()),
(2, 1, 2, 1, CURRENT_TIMESTAMP()),
(3, 1, 3, 1, CURRENT_TIMESTAMP());

INSERT INTO t_config (id, config_key, config_value, description, update_time) VALUES
(1, 'paymentMethods', '[{"label":"支付宝","value":1},{"label":"微信","value":2}]', '支付方式', CURRENT_TIMESTAMP()),
(2, 'homeBanners', '[{"image":"/img/banner-1.png","link":"/doctor/1"}]', '首页轮播', CURRENT_TIMESTAMP());