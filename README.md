# 龙门阵

![Build Status](https://img.shields.io/badge/build-passing-brightgreen) ![License](https://img.shields.io/badge/license-MIT-blue)

龙门阵里话家常，  茶馆街头笑声扬。  
故事传奇传千古，  幽默讽刺韵味长。  
川渝文化显魅力，  民间智慧聚一堂。  
亲朋好友齐相聚，  龙门阵里乐陶陶。

这是 [Socket-ChatRoom](https://github.com/jayeew/Socket-ChatRoom) 的一个 `fork` 版本，由 [jayeew](https://github.com/jayeew) 创建。本 `fork` 版本旨在扩展原项目的功能，实现利用`Socket API`编制的一个能部署在`Internet`上的多人聊天系统。

> 🚀🚀🚀🚀🚀🚀🚀🚀🚀🚀🚀🎉【喜大普奔】【完结撒花】🎉🚀🚀🚀🚀🚀🚀🚀🚀🚀🚀🚀
> 
> 🚀🚀🚀🚀🚀🚀🚀🚀🚀🚀🚀🎉【喜大普奔】【完结撒花】🎉🚀🚀🚀🚀🚀🚀🚀🚀🚀🚀🚀
>
> 🚀🚀🚀🚀🚀🚀🚀🚀🚀🚀🚀🎉【喜大普奔】【完结撒花】🎉🚀🚀🚀🚀🚀🚀🚀🚀🚀🚀🚀

## 特色

**DONE**

- 私聊/群发
- 发送图片
- 发送文件
- 在线人数更新
- 客户端/服务器端多线程并发
- 聊天多选组播
- 聊天记录，即重新登陆后消息不会丢失

**DOING**

- ~~聊天记录，即重新登陆后消息不会丢失~~

## 快速开始

想要体验龙门阵的魅力？按照以下步骤快速开始：

1. **克隆仓库**：

   ```bash
   git clone git@github.com:QiuyanLiu-64bit/LongMenZhen.git
   ```
2. **运行应用**：

   ```bash
   // bin/DBTest/LongMenZhen.jar 请先测试数据库连接
   java -jar .\LongMenZhen.jar

   // bin/Server/LongMenZhen.jar 启动服务器端
   java -jar .\LongMenZhen.jar
   
   // bin/Client/LongMenZhen.jar 启动客户端（可多个）
   java -jar .\LongMenZhen.jar
   ```

## 初始适配

- [ ] `src/Client/Client.java`

    ```java
    private int port = ;// 服务器端口
    private String ip = "";
    ```
- [ ] 创建 `src/dbconfig.properties`

    ```java
    driver=com.mysql.cj.jdbc.Driver
    url=jdbc:mysql://localhost:3306/<👀👀👀👀>?useUnicode=true&characterEncoding=utf-8&useSSL=false
    user=
    password=
    ```
- [ ] 同时需要新建名为`<👀👀👀👀>`的本地`mysql`数据库，数据库内执行

   ```mysql
   CREATE TABLE info (
    username VARCHAR(255),
    userpwd VARCHAR(255)
   );
   
   -- 测试数据
   -- 插入数据
   INSERT INTO info (username, userpwd)
   VALUES
       ('10000', '10000'),
       ('10', '10'),
       ('1', '1');

   -- 展示数据
   SELECT * FROM info;
   ```

## 修改并编译

推荐使用IDEA进行项目的编译，我们提供了相应的`MF/**MF/META-INF/MANIFEST.MF`文件指明主类，方便快速找到项目入口并利用IDEA进行编译。

- [ ] 在IDEA中找到 `设置` -> `项目结构` -> `项目设置` -> `工件` -> `添加` -> `JAR` -> `空`。

- [ ] 修改 `名称`，并右键 `输出布局` 中的 `归档（蓝色的）`，选择 `重命名`，修改工件名称。然后在输出布局中创建目录 `META-INF`，选中后添加对应的 `MANIFEST.MF` 文件。随后，在输出布局中添加已提取文件，将libs下的外部库文件全部选中并添加。最后在输出布局中添加 `模块输出` 文件，点击 `应用` -> `确定`。需要新建 `Client` 和 `Server`，`DBTest` 可选。

- [ ] 右键项目中的 `img` 文件夹，选择 `将目录标记为` -> `资源根目录`。

- [ ] 最后选择 `构建` -> `构建工件` -> `所有工件`。即可构建自己的工件。

## 可执行文件入口

- Client   ============   src/Client/Login
- Server   ============   src/Server/Server
- DBTest   ============   src/DB/TestUserDB

## 贡献

我们欢迎所有形式的贡献，无论是新功能的建议，代码贡献，还是问题报告。

## 联系我们

有问题或想参与讨论？在`issue`讨论

---

© 
