# 医院在线预约挂号系统

> Spring Boot 3.3 + Vue 3 + MyBatis-Plus 3.5 + Vite

## 项目结构

```
├── frontend/          # Vue 3 SPA 前端
│   ├── src/
│   │   ├── api/       # Axios 请求封装
│   │   ├── stores/    # Pinia 状态管理
│   │   ├── router/    # 路由 + 守卫
│   │   ├── components/# 公共组件
│   │   └── views/     # 28 个页面
│   └── package.json
├── backend/           # Spring Boot 3.3 后端
│   ├── src/main/java/com/hospital/
│   │   ├── auth/      # JWT + 认证
│   │   ├── config/    # CORS/Security/MyBatisPlus
│   │   ├── controller/# REST 接口
│   │   ├── entity/    # 17 个实体
│   │   ├── mapper/    # MyBatis-Plus Mapper
│   │   └── service/   # 业务逻辑
│   └── pom.xml
└── docs/              # 项目文档
    ├── API.md
    ├── 项目架构方案.md
    └── 项目分工与接口映射2.md
```

## 技术栈

| 层 | 技术 | 版本 |
|----|------|------|
| 后端 | Spring Boot | 3.3.0 |
| ORM | MyBatis-Plus | 3.5.5 |
| 数据库 | MySQL | 8.0 |
| JWT | hutool-jwt | 5.8.28 |
| API文档 | Knife4j | 4.5.0 |
| 前端 | Vue | 3.4 |
| 构建 | Vite | 5.x |
| 路由 | vue-router | 4.x |
| 状态 | Pinia | 2.x |

## 快速启动

### 1. 数据库
```bash
mysql -u root -p < backend/src/main/resources/db/hospital_create_table.sql
```

### 2. 后端
```bash
cd backend
mvn spring-boot:run
# 启动后访问 http://localhost:8080/api/doc.html 查看接口文档
```

### 3. 前端
```bash
cd frontend
npm install
npm run dev
# 启动后访问 http://localhost:3000
```

## 分支说明

| 分支 | 内容 |
|------|------|
| `dev` | 前后端整合（推荐） |
| `frontend` | 前端 Vue 3 代码 |
| `backend` | 后端 Spring Boot 代码 |
| `main` | 初始空项目 |

## 成员1已完成

- 认证模块：register / login / me / change-password / logout
- JWT 拦截器 + Token 校验
- 公共组件：Header / Footer / Sidebar
- 前后端联调验证通过
