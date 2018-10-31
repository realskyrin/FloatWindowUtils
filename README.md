# A useful class that makes it easier to create floating windows

### How to use
```java
View contentView = LayoutInflater.from(context).inflate(R.layout.fv_test,null);
    
FloatWindow floatWindow = new FloatWindow.With(this, layout)
            .setModality(false)
            .setMoveAble(true)
            .setAutoAlign(true)
            .setAlpha(0.5f)
            .setWidth(WindowManager.LayoutParams.WRAP_CONTENT)
            .setHeight(WindowManager.LayoutParams.MATCH_PARENT)
            .create();
// Displayed on the screen
floatWindow.show();
// Remove from the screen
floatWindow.remove();
```
### Example
![](example.gif)

### [READ MORE](http://www.jianshu.com/p/a23cfb8f2e5f)
