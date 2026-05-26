# 新疆特色作物富硒品质预测与智慧决策平台

面向新疆智慧农业、特色作物富硒品质预测、土壤环境评估、抗逆栽培和科研训练场景的全栈系统。当前版本已经从可运行初版扩展为包含真实数据库交互、规则算法、图表联动、报告预览、JWT 鉴权和完整初始化脚本的项目版本。

## 技术栈

- 前端：Vue 3、Vite、TypeScript、Element Plus、Pinia、Vue Router、Axios、ECharts
- 后端：Spring Boot 3、Java 17、Maven、MyBatis-Plus、Spring Validation、JWT、Swagger/OpenAPI
- 数据库：MySQL 8
- 部署辅助：Docker Compose 启动 MySQL

## 项目结构

```text
.
├── backend/                 # Spring Boot 3 后端
├── frontend/                # Vue 3 + Vite 前端
├── database/
│   ├── init.sql             # 完整建表脚本
│   ├── seed.sql             # 初始化数据脚本
│   ├── 01_schema.sql        # Docker 初始化占位，避免重复建表
│   └── 02_init_data.sql     # Docker 初始化占位，避免重复导入
├── docs/
│   ├── api.md               # 接口摘要
│   ├── data-sources.md      # 数据来源说明
│   └── system-overview.md   # 系统与算法说明
├── docker-compose.yml
└── README.md
```

## 启动 MySQL

```bash
docker compose up -d mysql
```

首次启动会按文件名顺序执行 `database` 目录脚本，其中实际建表和数据导入在：

- `database/init.sql`
- `database/seed.sql`

如需重建数据库：

```bash
docker compose down -v
docker compose up -d mysql
```

数据库连接信息：

- 地址：`localhost:3306`
- 数据库：`xj_selenium_agri`
- 用户：`root`
- 密码：`root123456`

也可以手动初始化：

```bash
mysql -uroot -proot123456 --default-character-set=utf8mb4 < database/init.sql
mysql -uroot -proot123456 --default-character-set=utf8mb4 < database/seed.sql
```

## 启动后端

后端要求 Java 17。

```bash
cd backend
mvn spring-boot:run
```

Windows PowerShell 可指定 JDK：

```powershell
cd backend
$env:JAVA_HOME="D:\jdk-17.0.15"
$env:Path="$env:JAVA_HOME\bin;$env:Path"
mvn spring-boot:run
```

后端地址：

- API：`http://localhost:8080/api`
- Swagger：`http://localhost:8080/swagger-ui.html`

## 启动前端

```bash
cd frontend
npm install
npm run dev
```

前端地址：`http://localhost:5173`

Vite 已配置 `/api` 代理到 `http://localhost:8080`。

## 测试账号

- 账号：`admin`
- 密码：`123456`

密码以 BCrypt 哈希写入 `sys_user`，不是明文存储。

## 核心功能

- JWT 登录、当前用户信息、路由鉴权、接口统一鉴权
- 首页驾驶舱：地块、作物、样本、预测、富硒适宜地块、风险预警、作物分布、等级分布、趋势、排行
- 地块信息管理：完整增删改查，支持地区、土壤类型、风险等级和名称编号搜索
- 作物种植档案：关联地块，展示地块硒含量、pH、风险等级，支持分页 CRUD
- 土壤样本管理：pH、有机质、氮磷钾、硒、盐分、电导率、重金属风险维护
- 气象数据管理：温度、降水、日照、太阳辐射、风速和湿度维护与趋势图
- 富硒品质预测：可解释规则算法，输出预测硒含量、等级、可信度、贡献图和建议，并保存历史
- 土壤环境评估：0-100 分综合评分、风险等级、单项评价、雷达图和改良建议
- 决策建议中心：按地块和作物动态生成富硒、土壤、水肥、风险、推广建议
- 报告管理：生成、预览、浏览器打印或导出 PDF，报告记录保存到数据库
- 数据来源管理：登记公开来源、模拟数据、采集时间、说明
- 操作日志、统一返回格式、全局异常处理、参数校验、CORS

## 初始化数据规模

- 新疆典型地区：乌鲁木齐、昌吉、石河子、阿克苏、喀什、库尔勒、吐鲁番、哈密、伊犁
- 地块数据：30 条
- 作物品种：覆盖棉花、小麦、玉米、葡萄、枸杞、番茄
- 作物种植档案：50 条
- 土壤样本：100 条
- 气象数据：90 条
- 示例预测、土壤评估、决策建议、报告和数据来源记录

## 构建检查

```bash
cd backend
mvn -q -DskipTests package

cd ../frontend
npm run build
```

## 数据说明

项目记录了新疆统计局、统计公报、国家统计局棉花公告、中国土壤数据库、土壤科学数据中心、NASA POWER API 等公开来源作为口径参考。当前业务初始化数据为合理模拟数据，均在 `data_source` 表和 [docs/data-sources.md](docs/data-sources.md) 标注。
