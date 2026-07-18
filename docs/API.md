可以。根据你这个项目的静态页面结构、数据库表设计，以及现有前端里 `axios.defaults.baseURL = 'http://localhost:8080'` 的配置，我给你拟一份**适配这个医院在线预约挂号系统**的 API 文档草案。  
你们后面写后端时，可以直接按这个接口骨架来实现。

---

# 医院在线预约挂号系统 API 文档（草案）

## 1. 基本信息

### 1.1 项目名称
医院在线预约挂号系统

### 1.2 接口基地址
```text
http://localhost:8080/api
```

### 1.3 请求格式
- `Content-Type: application/json`
- 登录后请求头携带 Token

```http
Authorization: Bearer <token>
```

### 1.4 统一返回格式
```json
{
  "code": 200,
  "message": "success",
  "data": {}
}
```

### 1.5 状态码约定
- `200`：成功
- `400`：参数错误
- `401`：未登录或 token 失效
- `403`：无权限
- `404`：资源不存在
- `500`：服务器异常

---

## 2. 认证模块 API

对应页面：
- `login.html`
- `register.html`
- `change-password.html`
- `Header.vue.js`

对应表：
- `t_user`

### 2.1 用户注册
**POST** `/auth/register`

请求体：
```json
{
  "username": "13800138000",
  "password": "123456",
  "phone": "13800138000",
  "email": "test@qq.com",
  "realName": "张三",
  "gender": 1
}
```

返回：
```json
{
  "code": 200,
  "message": "注册成功",
  "data": {
    "userId": 1
  }
}
```

---

### 2.2 用户登录
**POST** `/auth/login`

请求体：
```json
{
  "phone": "13800138000",
  "password": "123456"
}
```

返回：
```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "token": "jwt_token_xxx",
    "userInfo": {
      "id": 1,
      "phone": "13800138000",
      "realName": "张三",
      "avatar": "img/default.png"
    }
  }
}
```

---

### 2.3 获取当前登录用户
**GET** `/auth/me`

返回：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "phone": "13800138000",
    "realName": "张三"
  }
}
```

---

### 2.4 修改密码
**POST** `/auth/change-password`

请求体：
```json
{
  "oldPassword": "123456",
  "newPassword": "12345678"
}
```

返回：
```json
{
  "code": 200,
  "message": "密码修改成功"
}
```

---

### 2.5 退出登录
**POST** `/auth/logout`

返回：
```json
{
  "code": 200,
  "message": "退出成功"
}
```

---

## 3. 首页模块 API

对应页面：
- `index.html`

对应表：
- `t_hospital`
- `t_doctor`
- `t_disease`
- `t_article`
- `t_department`

### 3.1 首页聚合数据
**GET** `/home/index`

返回：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "banners": [],
    "hotHospitals": [],
    "hotDoctors": [],
    "hotDiseases": [],
    "hotArticles": []
  }
}
```

---

## 4. 医院模块 API

对应页面：
- `hospital.html`
- `hospital-detail.html`
- `follow-hospital.html`

对应表：
- `t_hospital`
- `t_department`
- `t_hospital_department`
- `t_doctor`
- `t_follow`

### 4.1 医院列表
**GET** `/hospitals`

请求参数：
- `page`
- `pageSize`
- `keyword`
- `departmentId`
- `province`
- `city`

示例：
```text
/api/hospitals?page=1&pageSize=8&keyword=人民医院
```

返回：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "list": [],
    "total": 100,
    "page": 1,
    "pageSize": 8
  }
}
```

---

### 4.2 医院详情
**GET** `/hospitals/{id}`

返回：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "name": "第一人民医院",
    "level": "三甲",
    "address": "北京市xxx路",
    "phone": "010-12345678",
    "intro": "医院简介",
    "image": "img/hospital.jpg",
    "departmentCount": 20,
    "doctorCount": 120,
    "followCount": 300
  }
}
```

---

### 4.3 医院科室树
**GET** `/hospitals/{id}/departments`

返回：
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "name": "内科",
      "children": [
        {
          "id": 11,
          "name": "心内科"
        }
      ]
    }
  ]
}
```

---

### 4.4 医院医生列表
**GET** `/hospitals/{id}/doctors`

请求参数：
- `page`
- `pageSize`
- `departmentId`

---

### 4.5 关注医院
**POST** `/follow/hospital/{hospitalId}`

---

### 4.6 取消关注医院
**DELETE** `/follow/hospital/{hospitalId}`

---

## 5. 科室模块 API

对应页面：
- `hospital.html`
- `doctor.html`
- `disease.html`
- `search-hospital.html`
- `search-doctor.html`
- `search-disease.html`

对应表：
- `t_department`

### 5.1 科室树列表
**GET** `/departments/tree`

返回：
```json
{
  "code": 200,
  "message": "success",
  "data": [{
            "id": 13,
            "name": "心内科",
            "children": [
                {
                    "id": 25,
                    "name": "心力衰竭科"
                },
                {
                    "id": 26,
                    "name": "冠心病科"
                },
                {
                    "id": 27,
                    "name": "高血压科"
                },
                {
                    "id": 28,
                    "name": "心律失常科"
                }
            ]
        },
        ...
      ]
}
```

---

### 5.2 一级科室列表
**GET** `/departments/primary`//暂时弃用但保留

---

### 5.3 二级科室列表
**GET** `/departments/{parentId}/children`//暂时弃用但保留

---

## 6. 医生模块 API

对应页面：
- `doctor.html`
- `doctor-detail.html`
- `follow-doctor.html`

对应表：
- `t_doctor`
- `t_schedule`
- `t_review`
- `t_follow`

### 6.1 医生列表
**GET** `/doctors`

请求参数：
- `page`
- `pageSize`
- `keyword`
- `departmentId`
- `hospitalId`

---

### 6.2 医生详情
**GET** `/doctors/{id}`

返回：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "name": "李医生",
    "title": "主任医师",
    "hospitalName": "第一人民医院",
    "departmentName": "心内科",
    "intro": "医生简介",
    "expertise": "擅长领域",
    "rating": 4.9,
    "followCount": 120
  }
}
```

---

### 6.3 医生排班
**GET** `/doctors/{id}/schedules`

请求参数：
- `startDate`
- `days`

返回：
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "date": "2026-07-17",
      "timeSlot": "上午",
      "remainCount": 10,
      "totalCount": 20
    }
  ]
}
```

---

### 6.4 关注医生
**POST** `/follow/doctor/{doctorId}`

### 6.5 取消关注医生
**DELETE** `/follow/doctor/{doctorId}`

---

## 7. 疾病模块 API

对应页面：
- `disease.html`
- `disease-detail.html`
- `follow-disease.html`

对应表：
- `t_disease`
- `t_follow`

### 7.1 疾病列表
**GET** `/diseases`

请求参数：
- `page`
- `pageSize`
- `keyword`
- `departmentId`

---

### 7.2 疾病详情
**GET** `/diseases/{id}`

返回：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "name": "高血压",
    "alias": "血压高",
    "location": "心血管系统",
    "treatment": "药物治疗",
    "symptoms": "头晕、头痛",
    "cureRate": "可控制",
    "examinations": "血压检测"
  }
}
```

---

### 7.3 关注疾病
**POST** `/follow/disease/{diseaseId}`

### 7.4 取消关注疾病
**DELETE** `/follow/disease/{diseaseId}`

---

## 8. 文章模块 API

对应页面：
- `article.html`
- `article-detail.html`
- `search-article.html`

对应表：
- `t_article`
- `t_department`

### 8.1 文章列表
**GET** `/articles`

请求参数：
- `page`
- `pageSize`
- `keyword`
- `departmentId`

---

### 8.2 文章详情
**GET** `/articles/{id}`

---

## 9. 搜索模块 API

对应页面：
- `search.html`
- `search-hospital.html`
- `search-doctor.html`
- `search-disease.html`
- `search-article.html`

### 9.1 全局搜索
**GET** `/search/global`

请求参数：
- `keyword`
- `type`  
  - `hospital`
  - `doctor`
  - `disease`
  - `article`

返回：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "hospital": [],
    "doctor": [],
    "disease": [],
    "article": []
  }
}
```

---

## 10. 预约挂号模块 API

对应页面：
- `reservation.html`
- `reservation-pay.html`
- `reservation-success.html`

对应表：
- `t_schedule`
- `t_appointment`
- `t_payment_flow`
- `t_family_member`

### 10.1 创建挂号订单
**POST** `/appointments`

请求体：
```json
{
  "doctorId": 1,
  "hospitalId": 1,
  "patientId": 2,
  "appointmentDate": "2026-07-18",
  "appointmentTime": "上午",
  "diseaseDesc": "头痛两天"
}
```

返回：
```json
{
  "code": 200,
  "message": "创建成功",
  "data": {
    "orderNo": "A202607170001",
    "amount": 50.00
  }
}
```

---

### 10.2 预约订单详情
**GET** `/appointments/{orderNo}`

---

### 10.3 我的挂号订单
**GET** `/appointments/my`

请求参数：
- `page`
- `pageSize`
- `status`

---

### 10.4 取消挂号订单
**POST** `/appointments/{orderNo}/cancel`

---

### 10.5 挂号支付
**POST** `/appointments/{orderNo}/pay`

请求体：
```json
{
  "payMethod": "wechat"
}
```

---

### 10.6 预约成功页数据
**GET** `/appointments/{orderNo}/success`

---

## 11. 电话咨询模块 API

对应页面：
- `phone-consult.html`
- `consult-pay.html`
- `consult-success.html`

对应表：
- `t_consult`
- `t_payment_flow`

### 11.1 创建咨询订单
**POST** `/consults`

请求体：
```json
{
  "doctorId": 1,
  "patientName": "张三",
  "patientPhone": "13800138000",
  "diseaseDesc": "咳嗽三天",
  "appointmentTime": "2026-07-18 14:00:00",
  "duration": 30
}
```

---

### 11.2 咨询订单详情
**GET** `/consults/{orderNo}`

---

### 11.3 我的咨询订单
**GET** `/consults/my`

请求参数：
- `page`
- `pageSize`
- `status`

---

### 11.4 咨询支付
**POST** `/consults/{orderNo}/pay`

---

### 11.5 咨询成功页数据
**GET** `/consults/{orderNo}/success`

---

## 12. 支付流水 API

对应表：
- `t_payment_flow`

### 12.1 创建支付流水
**POST** `/payments`

请求体：
```json
{
  "businessOrderNo": "A202607170001",
  "businessType": "appointment",
  "payMethod": "wechat",
  "actualAmount": 50.00
}
```

---

### 12.2 支付结果回调
**POST** `/payments/callback`

---

### 12.3 查询支付流水
**GET** `/payments/{businessOrderNo}`

---

## 13. 个人中心 API

对应页面：
- `personal-info.html`
- `family-members.html`
- `my-appointment.html`
- `my-consult.html`
- `my-reviews.html`
- `my-feedback.html`
- `my-messages.html`

### 13.1 获取个人信息
**GET** `/user/profile`

### 13.2 修改个人信息
**PUT** `/user/profile`

---

### 13.3 就诊人列表
**GET** `/family-members`

### 13.4 新增就诊人
**POST** `/family-members`

### 13.5 编辑就诊人
**PUT** `/family-members/{id}`

### 13.6 删除就诊人
**DELETE** `/family-members/{id}`

---

### 13.7 我的评价列表
**GET** `/reviews/my`

### 13.8 提交评价
**POST** `/reviews`

请求体：
```json
{
  "orderType": 1,
  "orderId": 1,
  "doctorId": 1,
  "rating": 5,
  "content": "医生很耐心"
}
```

---

### 13.9 我的消息列表
**GET** `/messages`

### 13.10 标记已读
**POST** `/messages/{id}/read`

---

### 13.11 提交反馈
**POST** `/feedbacks`

请求体：
```json
{
  "feedbackType": 1,
  "content": "页面加载慢",
  "images": ["img1.png", "img2.png"]
}
```

---

### 13.12 我的反馈列表
**GET** `/feedbacks`

---

## 14. 关注模块 API

对应表：
- `t_follow`

### 14.1 我的关注列表
**GET** `/follow/my`

请求参数：
- `type`
  - `1` 医院
  - `2` 医生
  - `3` 疾病

### 14.2 取消关注
**DELETE** `/follow/{type}/{id}`

---

## 15. 系统配置 API

对应表：
- `t_config`

### 15.1 查询配置
**GET** `/configs/{key}`

示例：
- `site.name`
- `site.logo`
- `appointment.fee`

---

# 16. 关键表与接口映射

- `t_user` → 登录注册、个人信息
- `t_hospital` → 医院列表、医院详情
- `t_department` → 科室树、筛选条件
- `t_doctor` → 医生列表、医生详情
- `t_schedule` → 排班、预约号源
- `t_appointment` → 挂号订单
- `t_consult` → 咨询订单
- `t_payment_flow` → 支付记录
- `t_article` → 科普文章
- `t_disease` → 疾病百科
- `t_follow` → 关注/取关
- `t_review` → 评价
- `t_feedback` → 反馈
- `t_message` → 消息通知
- `t_family_member` → 就诊人管理
- `t_config` → 系统配置

---

# 17. 建议的后端分层

为了后面实现方便，建议后端按这几个层拆：

- `controller`：只接收请求和返回结果
- `service`：处理业务逻辑
- `repository/mapper`：操作数据库
- `dto/vo`：前后端数据转换
- `auth`：登录鉴权与 Token 校验

---

如果你愿意，我下一步可以直接帮你继续补两样东西，二选一都行：

1. **把这份 API 文档整理成表格版**，更像正式课程设计文档  
2. **直接补完整 Swagger 风格接口文档**，带请求参数、响应示例、错误码

你要的话我就继续往下整理。