# Google Zxing 二维码生成

本项目使用 Google 的 Zxing 库生成二维码图片，有效解决了在服务器环境中二维码图片中汉字乱码和马赛克的问题。

## 1. 完全解决 QRCode 图片中的中文乱码问题

通过加载 `classpath` 下的字体，我们可以消除服务器间的字体差异。大部分 `ECS`
厂商都有删减操作系统组件的习惯，因此我们采用字体外挂的方式将其整合到项目中。本项目使用 [小米开源字体-MiSans-Medium.ttf](https://hyperos.mi.com/font/zh/)
进行演示。

相关代码请参见：`com.example.qrcode.config.QRCodeExtraConfig`

## 2. 解决服务器环境下 QRCode 图片中的中文锯齿感

相关代码请参见：`com.example.qrcode.core.QRCodeGenerator`

## 3. 项目启动环境

| 环境       | 版本       | 备注 |
|-------------|----------|----|
| JDK         | >=  1.8  |    |
| Spring-Boot | >= 2.3.x |    |
