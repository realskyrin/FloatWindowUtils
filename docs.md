## 首先有必要简单了解如下几个类
---
### WindowManager
>负责设备与窗口进行通讯的接口，使用`Context.getSystemService(Context.WINDOW_SERVICE)`获取其实例，
`WindowManager`提供了`addView(View view, ViewGroup.LayoutParams params)`，`removeView(View view)`，
`updateViewLayout(View view, ViewGroup.LayoutParams params)`三个方法用来向设备屏幕 添加、移除以及更新
一个 view 。
### WindowManager.LayoutParams
>通过名字就可以看出来 它是`WindowManager`的一个内部类，专门用来描述 view 的属性 比如大小、透明度
、初始位置、视图层级等。
### DisplayMetrics
>该对象用来描述关于显示器的一些信息，例如其大小，密度和字体缩放。例如获取屏幕宽度`DisplayMetrics.widthPixels`
