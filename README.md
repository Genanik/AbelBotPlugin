# 大火Bot  
基于Abel框架的机器人  
此项目处于活动状态，欢迎在issue里提出建议或反馈bug！
  
准备将大火升级至 [Mirai-Console](https://github.com/mamoe/mirai-console) 1.0

**一切开发旨在学习，请勿用于非法用途**  

[![Issues](https://img.shields.io/github/issues/Genanik/AbelBotPlugin.svg?style=popout)](https://github.com/Genanik/AbelBotPlugin)
![Stars](https://img.shields.io/github/stars/Genanik/AbelBotPlugin)

## 支持以下功能  
* 复读
* 镜像复读图片
* 名人"名言"
* GIF倒放
* 图片缩放
* 红茶提醒
* 报时 (待完善)
* ~~繁体转简体~~  

PS: 图片处理相关使用JNA和Golang实现，可以在 [这个仓库](https://github.com/Genanik/Horizontal-flip-of-picture) 里查看相关代码

## 特色实现  
* 接管群内指令，实现bot管理员/用户指令的分离  
* 不同模块统一管理

## 相关说明  
Genanik的私人机器人，因此本仓库仅用于学习交流  

### 历史
一期大火采用 [qqbot](https://github.com/pandolia/qqbot) ，主要为群友提供文本翻译服务，命名为"翻译机"，代码乱的一批。随着smartQQ的关闭而停止服务  
二期大火采用 [Mirai-Core](https://github.com/mamoe/mirai) ，主要为群友提供翻译以及沙雕功能，源码较乱，可在此仓库中找到原始commit  
三期大火，也就是现在这个版本，采用 [Mirai-Console](https://github.com/mamoe/mirai-console) 相较于前两个时期较为完善
   
### 未来
估计以后会再转回[Mirai-Core](https://github.com/mamoe/mirai)?  
  
### Abel和大火有什么关系?
Mirai-Console的指令系统太反人类了（  
于是自己实现了个 [AbelFramework](https://github.com/Genanik/AbelFramework) ，将不同的功能进行统一管理  
所以二期大火(指使用Mirai-Core的)也叫Abel机器人
