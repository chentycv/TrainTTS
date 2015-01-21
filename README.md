Train TTS
===========
该项目实现了从串口实时读取数据，并存入本地SQLite数据库，同时满足一定条件触发TTS响应的简单流程。

安装
-----------
* 先安装preinstall目录下面的两个apk
* 到[系统设置] - [辅助功能] - [文字转语音(TTS)输出] - 选择[讯飞语音合成]
* 点击[讯飞语音合成]右侧设置按钮 - [语言] - [中文(中国)] 

特性
-----------
* 实现串口数据的读取
* 实现SQLite数据接口
* 实现Android TTS的调用

参考项目
------------
* [SQLite 笔记](https://hackpad.com/SQLite-vOapONJo3de)
* [Android TTS](http://www.tutorialspoint.com/android/android_text_to_speech.htm)
* [android-serialport-api](https://code.google.com/p/android-serialport-api/)