# 一个悬浮创建工具类
---
### 使用
```java
        floatWindow = new FloatWindowUtils.Builder(context,view)
                .setAutoAlign(isAutoAlign) //是否自动贴边
                .setModality(isModality) //是否模态窗口
                .setMoveAble(isMoveAble) //是否可拖动
                .create();
        floatWindow.show();
```
