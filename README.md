# 寻友-后端

## 项目简介
一个提供组队、聊天功能、帮助大家找到志同道合的伙伴的移动端网站（APP 风格）。包括用户登录注册、私聊、更新个人信息、按标签搜索用户、推荐相似用户、组队等功能。

## 在线访问网站
https://friends.codelearn.top

#### 主页：
![img.png](img/img.png)
#### 个人信息：
![img_1.png](img/img_1.png)
#### 私聊
![img_2.png](img/img_2.png)
#### 消息
![img_3.png](img/img_3.png)
#### 队伍
![img_4.png](img/img_4.png)
## 技术栈
#### 前端
- Vue 3
- Vant UI 组件库
- TypeScript
- Vite 脚手架
- Axios 请求库
#### 后端
- Java SpringBoot 2.7.x 框架
- MyBatis-Plus
- Redis 缓存
- Redisson 分布式锁
- Spring Scheduler 定时任务
- Swagger + Knife4j 接口文档
- Gson：JSON 序列化库
- 相似度匹配算法
- WebSocket 通信

## 运行项目
- 执行 sql/create_table.sql
- 修改yaml配置文件（redis，mysql）