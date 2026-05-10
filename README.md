# 苍穹外卖 + AI 智能小助手

全栈外卖管理项目：**Spring Boot 3 后端** + **Vue2 + TypeScript 管理端**，集成 **LangChain4j**、**阿里云百炼（DashScope）**、**Pinecone 向量库**、**MongoDB 会话记忆** 等。

## 项目结构

├── sky-take-out/      # 后端项目 (Spring Boot 3)

└── project-sky-admin-vue-ts/     # 前端项目 (Vue)

## 一、项目概述
本项目基于知名的“苍穹外卖”Java后端项目进行构建，完整实现了传统外卖业务所需的核心功能，包括用户端，管理端的商品、订单、购物车、地址管理等全套业务流程。其中由于商家小程序的申请限制，本项目中关于微信支付的逻辑进行了简化处理，具体实现代码可去B站看原视频。

为结合时代发展趋势，我在**管理端**之上，扩展了面向运营、管理员的智能助手：支持**流式对话**、**多轮会话记忆（MongoDB）**、**基于向量库的RAG检索（Pinencone + 通义嵌入）**，并通过**LangChain4j Tools**对接现有业务（如员工、订单统计等），形成**[对话 + 数据 + 知识]**一体的管理辅助场景。

这不仅是一个传统的简单Java业务项目，还是一个**Java后端与前沿AI技术结合的创新实践**。但由于接触人工智能时间短，所以我并未深入进行改造，只实现了ai小助手这一功能，用于了解学习AI的调用。

## 二、核心创新点：AI智能小助手
- 1.从[纯CRUD后台] 到 [对话式管理辅助]
在原有订单、菜品、员工等模块外，增加自然语言入口，降低查数、查规则、写说明的操作成本。

- 2.RAG + 向量库
用 Embedding 模型 将用户问题与知识片段对齐，经 Pinecone 做相似度检索，把检索结果注入大模型上下文，使回答可结合业务知识文档（而不仅依赖模型预训练）。

- 3.工具调用（Function Calling / Tools）与现有代码融合
助手可调 Mapper / Service（如员工列表、按日期营业额等），实现[对话驱动查询]，与纯聊天机器人区分。

- 4.会话隔离与持久化
使用 memoryId + Mongo 存储，多会话互不串台，且可复盘历史。

## 三、遇到的难题与解决方案
- Spring Boot 3 升级与依赖兼容
由于部分依赖限制，本项目升级为Spring Boot 3，统一由Boot 3 兼容依赖

- ThreadLocal 上下文传递问题
LangChain4j 的工具调用运行于独立线程，无法直接访问 Web 请求线程中的 ThreadLocal 用户上下文。 于是我在发起 AI 请求时，将当前管理员 ID 显式注入 Prompt 上下文，并由大模型在工具调用阶段自行解析并传参，彻底解耦 AI 执行链路与 Web 线程上下文，避免上下文污染与线程安全问题。

- 同步逻辑与异步流式体验的统一
后端业务仍基于同步 Service 实现，而前端需支持异步流式响应。基于 Project Reactor 的 Flux响应式模型，我将业务处理与流式输出解耦，在不改造核心业务逻辑的前提下，实现稳定的 SSE 流式返回与前端打字机效果。

- 静态资源部署后仍是旧页面
即使是改了路由和菜单，前端浏览器侧栏仍然不变。后来我发现是 nginx 文件里面的 html/sky 仍然是原来的前端文件，需要更改，于是重新将前端文件 build 并将其整包替换成 dist。


## 四、项目功能
除了 AI 创新点以外，本项目保留了原版“苍穹外卖”的所有核心功能
### 管理端
- 员工管理 
- 分类管理 
- 菜品管理 
- 套餐管理 
- 订单管理 
- 工作台数据统计

### 用户端
- 微信授权登录 
- 浏览商品、菜品 
- 购物车操作 
- 地址管理 
- 下单与支付 
- 历史订单查看

### 技术栈
- 后端核心：Spring Boot 3、Spring MVC、Spring Validation、MyBatis、Spring Task（定时任务）、Spring WebSocket 
- AI 集成：LangChain4j、阿里云 DashScope（通义千问对话 / 文本向量）、RAG（Embedding + 检索增强） 
- 向量检索：Pinecone（Serverless 向量索引；与嵌入模型维度对齐） 
- 数据库：MySQL 8 
- 连接池：Druid（druid-spring-boot-3-starter，适配 Spring Boot 3） 
- 文档型存储：MongoDB（多轮对话 / ChatMemory 持久化，按业务启用） 
- 缓存：Redis（已集成；Spring Boot 3 使用 spring.data.redis 配置） 
- HTTP 客户端 / 响应式：Spring WebFlux、Project Reactor（流式调用等场景） 
- API 文档：Knife4j、SpringDoc（OpenAPI 3）

### 准备工作
- 安装 **MySQL**，创建业务库
- 安装 **Redis**
- 安装 **MongoDB**，用于聊天记忆等
- 配置**微信小程序**
- 配置**阿里云 OSS 对象存储**，登录阿里云控制台，用于对象存储 OSS
- 配置**阿里云百炼大模型（DashScope）**
- 配置**Pinecone 向量数据库**
