手把手教你开发一个可公网访问的网站
（所有用到的工具和域名和服务器成本为0）
布哥说明：本文专为行外人、零基础新手打造，全程使用免费工具，不需要懂高深技术，手把手教你把本地项目部署成全球可访问的网站，告别 localhost。整套方案布哥从零到上线花了三个小时，你顺利的话应该一两个小时就可以完成，过程中如果遇到任何问题可问 AI 解决。
首先给大家看看终点：
✅ 成品演示地址：https://dagouzhuang.onrender.com/

一、注册 GitHub，创建代码仓库
1. 打开 GitHub 官网注册账号：https://github.com/
2. 新建公开仓库，用来存放你的项目所有代码。
（仓库创建页面配置：公开仓库、无需勾选 README、无需 gitignore、无需证书）

二、安装 TortoiseGit，拉取代码到本地
1. 电脑安装 TortoiseGit 工具。
2. 在电脑空白文件夹右键，将 GitHub 远程仓库克隆到本地，得到本地代码文件夹。

三、安装免费 AI 编程工具 TRAE
打开官网安装 TRAE AI 编程工具，全程免费、零基础可用：https://www.trae.cn/
唯一真正免费、全自动开发、自动写代码、自动生成部署文件的 AI 工具。
四、AI 全自动开发前后端项目
1. 用 TRAE 打开你刚刚拉取的本地代码文件夹。
2. 直接输入需求（复制即可）：
帮我开发一个吃狗庄的贪吃蛇游戏，功能可以根据股票交易术语设计，前后端在一个工程，前端VUE，后端springboot，用户访问时根据IP自动创建用户信息，有积分，和排行榜
3.等待 AI 自动开发，自动生成完整前后端代码。
4.
五、本地启动测试（确认项目能跑）
1. 启动后端（终端执行）
Plain Text
cd backend
mvn spring-boot:run
2. 启动前端（新开终端执行）
Plain Text
cd frontend
npm install
npm run dev
3. 浏览器访问：http://localhost:3000，即可正常游玩项目，可以看到程序已经启动起来了，不过此时你本地通过localhost可以访问，但互联网访问不了，要实现给互联网访问也很简单，后面布哥会告诉你如何部署到远程服务器，告别他人无情的嘲讽XD
六、让 AI 自动生成 Docker 部署文件
在 TRAE 输入指令：我用docker部署，给我创建好相关文件
AI 会自动一次性生成全部部署文件：
•Dockerfile 多阶段构建文件
•docker-compose.yml 配置文件
•跨域、静态资源配置类
•适配云端部署的 yml 配置
七、提交全部代码到 GitHub
将项目代码 + AI 生成的全部部署文件，统一提交并推送至 GitHub 远程仓库，保证代码完整。

八、Render 免费云服务器部署上线
1. 打开 Render 官网，GitHub 账号直接登录：https://render.com/

2.新建 Web Service（免费版足够个人使用）

3. 绑定你的 GitHub 项目仓库
4. docker配置：

•Root Directory：留空
•Dockerfile Path：./Dockerfile
•Build Context：.
5.点击 Deploy Web Service，开始自动云端构建部署。

6.等待 3–5 分钟，部署报错基本都是 Docker 配置问题，把报错发给 TRAE 自动修复即可。

九、成功公网访问
部署完成后，Render 自动生成免费公网域名，全世界均可直接访问。

✅ 成品演示地址：https://dagouzhuang.onrender.com/ 
注：render免费版的网站如果长期无人访问会跳转到官网，需要等15秒后再次访问

随便做的游戏，只是演示用，很多BUG，不用在意，过程中如果有问题可以关注微博：顺势独行的布哥 https://weibo.com/u/6242589419
