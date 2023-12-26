# 谷歌 Zxing 生成二维码示例

> 我的代码在本地运行的好好的，部署到服务器上就不行了？

## 1 彻底解决QRCode图片中中文乱码问题

通过加载加载 `classpath` 下的字体屏蔽服务器间的字体差异，大部`ECS`
厂商都有严格系统的习惯！所以我们使用字体外挂的方式整合到项目中进行使用，本次使用[小米开源字体-MiSans-Medium.ttf](https://hyperos.mi.com/font/zh/)
做演示

代码见：`com.example.qrcode.config.QRCodeExtraConfig`

## 2 解决服务器环境下QRCode图片中中文的锯齿感强烈

代码：`com.example.qrcode.core.QRCodeGenerator`

## 3 项目启动环境

| 环境名         | 版本       | 备注 |
|-------------|----------|----|
| JDK         | >=  21   |    |
| Spring-Boot | >= 3.x.x |    |
|             |          |    |

