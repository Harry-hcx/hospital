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
  status INT,
  create_time TIMESTAMP,
  update_time TIMESTAMP
);

CREATE TABLE t_user_token (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  token VARCHAR(255) NOT NULL,
  status INT NOT NULL,
  expire_time TIMESTAMP,
  create_time TIMESTAMP,
  update_time TIMESTAMP
);

CREATE TABLE t_family_member (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  name VARCHAR(64) NOT NULL,
  gender INT,
  birthday DATE,
  phone VARCHAR(32),
  id_card VARCHAR(32),
  relation VARCHAR(32),
  is_default INT,
  create_time TIMESTAMP,
  update_time TIMESTAMP
);

CREATE TABLE t_hospital (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(128) NOT NULL,
  level VARCHAR(32),
  address VARCHAR(255),
  phone VARCHAR(32),
  intro CLOB,
  image VARCHAR(255),
  province VARCHAR(64),
  city VARCHAR(64),
  department_count INT,
  doctor_count INT,
  follow_count INT,
  status INT,
  create_time TIMESTAMP,
  update_time TIMESTAMP
);

CREATE TABLE t_department (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(128) NOT NULL,
  description VARCHAR(255),
  parent_id BIGINT,
  sort_order INT,
  status INT,
  create_time TIMESTAMP
);

CREATE TABLE t_hospital_department (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  hospital_id BIGINT NOT NULL,
  department_id BIGINT NOT NULL,
  create_time TIMESTAMP
);

CREATE TABLE t_doctor (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(64) NOT NULL,
  gender INT,
  title VARCHAR(64),
  department_id BIGINT,
  hospital_id BIGINT,
  avatar VARCHAR(255),
  phone VARCHAR(32),
  intro CLOB,
  expertise VARCHAR(255),
  consult_count INT,
  rating DECIMAL(3,1),
  follow_count INT,
  online_status INT,
  price DECIMAL(10,2),
  registration_price DECIMAL(10,2),
  status INT,
  create_time TIMESTAMP,
  update_time TIMESTAMP
);

CREATE TABLE t_schedule (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  doctor_id BIGINT NOT NULL,
  hospital_id BIGINT NOT NULL,
  department_id BIGINT NOT NULL,
  schedule_date DATE,
  time_slot VARCHAR(32),
  total_count INT,
  remain_count INT,
  status INT,
  create_time TIMESTAMP
);

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
  status INT,
  pay_time TIMESTAMP,
  create_time TIMESTAMP,
  update_time TIMESTAMP
);

CREATE TABLE t_consult (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  order_no VARCHAR(64) NOT NULL,
  user_id BIGINT NOT NULL,
  doctor_id BIGINT NOT NULL,
  patient_name VARCHAR(64),
  patient_phone VARCHAR(32),
  disease_desc VARCHAR(255),
  appointment_time TIMESTAMP,
  duration INT,
  amount DECIMAL(10,2),
  status INT,
  pay_time TIMESTAMP,
  create_time TIMESTAMP,
  update_time TIMESTAMP
);

CREATE TABLE t_payment_flow (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  business_order_no VARCHAR(64) NOT NULL,
  business_type INT,
  pay_method INT,
  third_party_trade_no VARCHAR(64),
  actual_amount DECIMAL(10,2),
  pay_status INT,
  pay_success_time TIMESTAMP,
  original_callback CLOB,
  create_time TIMESTAMP,
  update_time TIMESTAMP
);

CREATE TABLE t_article (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  title VARCHAR(128) NOT NULL,
  summary VARCHAR(255),
  content CLOB,
  department_id BIGINT,
  author VARCHAR(64),
  image VARCHAR(255),
  views INT,
  status INT,
  publish_time TIMESTAMP,
  create_time TIMESTAMP,
  update_time TIMESTAMP
);

CREATE TABLE t_disease (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  department_id BIGINT,
  name VARCHAR(128) NOT NULL,
  description CLOB,
  alias VARCHAR(128),
  location VARCHAR(128),
  treatment CLOB,
  symptoms CLOB,
  treatment_period VARCHAR(64),
  cure_rate VARCHAR(64),
  examinations CLOB,
  follow_count INT,
  create_time TIMESTAMP,
  update_time TIMESTAMP
);

CREATE TABLE t_follow (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  follow_type INT,
  follow_id BIGINT,
  create_time TIMESTAMP
);

CREATE TABLE t_review (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  order_type INT,
  order_id BIGINT,
  user_id BIGINT,
  doctor_id BIGINT,
  rating INT,
  content VARCHAR(255),
  create_time TIMESTAMP
);

CREATE TABLE t_feedback (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT,
  feedback_type INT,
  content VARCHAR(255),
  images VARCHAR(500),
  status INT,
  reply_content VARCHAR(255),
  reply_time TIMESTAMP,
  create_time TIMESTAMP,
  update_time TIMESTAMP
);

CREATE TABLE t_message (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT,
  title VARCHAR(128),
  content VARCHAR(255),
  is_read INT,
  create_time TIMESTAMP
);

CREATE TABLE t_config (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  config_key VARCHAR(64),
  config_value CLOB,
  description VARCHAR(255),
  update_time TIMESTAMP
);