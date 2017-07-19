# AQI_and_Temp_Monitor
Used to collect and analyze AQI and temperature values to provide a better precision. (一个具备多种数据分析显示的空气质量指数和气温采集APP)<br>

## 项目性质
不再维护和更新，此项目还有对应的51单片机采集设备C源码和Aliyun ECS服务器的PHP源码，在此不再提供。实有需要的话请通过我的profile里的联系方式与我取得联系。<br>
项目是我的毕业设计作品，整个设计耗时半个月，测试过近500次，暂时没有发现BUG。若有疑问可提交issues，我会尽量给出解决办法。<br>

## 关于Lab功能
labActivity.java是该项目的创新点。其可以将用户采集的数据备份至云端，并生成当日地区大数据报告，精准度随着使用人数的提升而升高。

## 版权
项目中com.leyuwei.XXX均为原创，在使用其中源码时，请在应用中标明作者和源码链接（[link](https://github.com/leyuwei/AQI_and_Temp_Monitor)）<br>

## 致谢
项目中使用到了Github诸多优秀的开源项目，具体如下：
* HelloCharts（[link](https://github.com/lecho/hellocharts-android)）图表控件
* Material_CalendarView（[link](https://github.com/prolificinteractive/material-calendarview)）Material风格的日历控件
* ormlite（[link](https://github.com/j256/ormlite-android)）SQLite数据库辅助工具
* android-async-http（[link](https://github.com/loopj/android-async-http)）异步http访问工具<br>
它们中的许多已经不再更新，可能没法适配Android N、Android O版本。请尽量将target选在6.0左右。
