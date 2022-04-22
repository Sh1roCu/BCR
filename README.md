# BCR
用于公主连结和BILIBILI部分功能的Mirai-Console插件<br>
~我超，写完才看见已经有过监听了~
***
* 注意：<br>
      1.使用的Mirai-Console版本为`2.10.1`，其他版本请自行尝试。<br>
      2.使用[MCL](https://github.com/iTXTech/mirai-console-loader)启动Mirai-Console<br>
      3.使用了[Google-Gson](https://github.com/google/gson) 2.8.6和[Apache-HttpClient](https://github.com/apache/httpcomponents-client) 5.5.2-alpha1第三方库<br>
      4.公主连结相关api使用[bigfun](https://www.bigfun.cn)和[calendar-updater-action](https://github.com/pcrbot/calendar-updater-action)
***
* 使用：<br>
  * 输入命令执行的功能：<br>
        ~由于嫌麻烦~没有设置command，~也没有弄个congfig替换命令文本~，直接在聊天环境中发送相关指令即可：<br><br>
        BCR相关(公主连结ReDive简中服)：<br>
          `/rank` 查询rank表 ~同样由于懒，暂时直接发送万用表链接~<br>
          `/活动` 查询活动月历<br>
          `/排名` 查询最近一次公会战的排名<br>
          `/出刀信息` 查询最近一次公会战的成员出刀数据<br>
          `/往期排名` 查询开服以来公会战每期的排名信息<br>
          `/boss` 查询当前boss的状态(周目,血量)<br>
          ...<br><br>
        bilibili相关：<br>
          `/uid UP主的UID` 查询UP主的信息<br>
           ...<br>
  * 不需要输入命令执行的功能：<br>
        bilibili：`监听任意UP主的动态并发送到指定群里`<br>
        &emsp;&emsp;&emsp;&emsp;具体配置看[Config](https://github.com/Sh1roCu/BCR#Config)：<br>
***
* Config：<br>
        &emsp;1.在`(mcl所在目录)\data\ao.sh1rocu.mirai.bcr\UPerFaceCache`中保存了查询过的UP主的头像<br>
        &emsp;2.在`(mcl所在目录)\config\ao.sh1rocu.mirai.bcr\`中有两个`手动`配置文件：<br>
        &emsp;&emsp;1) `"ClanBattle.json"`：保存每个群所使用的bigfun登录后的cookie<br>
        &emsp;&emsp;&emsp;&emsp;&emsp;example：<br>
        &emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;`{`<br>
        &emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;`"群号": "bigfun登录后的cookie",`<br>
        &emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;`"example": "xxxx",`<br>
        &emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;`"123": "abcd"`<br>
        &emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;`}`<br>
        &emsp;&emsp;2) `"DynamicWatcher.json"`：保存每个UP主的UID在哪些群开启动态监听<br>
        &emsp;&emsp;&emsp;&emsp;&emsp;example：<br>
        &emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;`{`<br>
        &emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;`"UID": {`<br>
        &emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;`"群号": "on(开启)/off(关闭)",`<br>
        &emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;`"123": "on",`<br>
        &emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;`},`<br>
        &emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;`"UID": {`<br>
        &emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;`"群号": "on(开启)/off(关闭)",`<br>
        &emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;`"456": "on",`<br>
        &emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;`},`<br>
        &emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;`}`<br>
***
* TODO(~大概也许可能应该~)：<br>
- [ ] 1.自动报刀(bigfun的api在网页端延迟很久，~应该没什么用了~)<br>
- [ ] 2.指令发送噼哩噼哩视频链接提取封面(有网页端提取，~应该也没什么用~)<br>
        ...<br>
        ...<br>
