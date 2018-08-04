package com.example.admin.floatwindowutils;

import android.animation.ValueAnimator;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.lang.reflect.Method;

/**
 * Created by skyrin on 2017/3/16.
 */

public class FloatWindowUtils {

    private WindowManager.LayoutParams mLayoutParams;
    private WindowManager mWindowManager;
    private DisplayMetrics mDisplayMetrics;

    //view 相对于屏幕触摸点的偏移量(一般仅减去Y轴状态栏高度)
    int offsetX;
    int offsetY;
    //触摸点相对于view左上角的坐标
    float downX;
    float downY;
    //触摸点相对于屏幕左上角的坐标
    float rowX;
    float rowY;
    //悬浮窗显示标记
    boolean isShowing;
    //拖动最小偏移量
    private static final int MINIMUM_OFFSET = 5;

    private Context mContext;
    //是否自动贴边
    private boolean autoAlign;
    //是否模态窗口
    private boolean modality;
    //是否可拖动
    private boolean moveAble;
    //内部定义的View，专门处理事件拦截的父View
    private FloatView floatView;
    //外部传进来的需要悬浮的View
    private View contentView;

    public FloatWindowUtils(Builder builder) {
        this.mContext = builder.context;
        this.autoAlign = builder.autoAlign;
        this.modality = builder.modality;
        this.contentView = builder.contentView;
        this.moveAble = builder.moveAble;

        initWindowManager();
        initLayoutParams();
        initFloatView();
    }

    private void initWindowManager() {
        mWindowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        //获取一个DisplayMetrics对象，该对象用来描述关于显示器的一些信息，例如其大小，密度和字体缩放。
        mDisplayMetrics = new DisplayMetrics();
        mWindowManager.getDefaultDisplay().getMetrics(mDisplayMetrics);
    }

    private void initFloatView() {
        floatView = new FloatView(mContext);
        if (moveAble) {
            floatView.setOnTouchListener(new WindowTouchListener());
        }
    }

    private void initLayoutParams() {
        mLayoutParams = new WindowManager.LayoutParams();
        mLayoutParams.flags = WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
                | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        if (modality) {
            mLayoutParams.flags &= ~WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
            mLayoutParams.flags &= ~WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        }
        mLayoutParams.height = LinearLayout.LayoutParams.WRAP_CONTENT;
        mLayoutParams.width = LinearLayout.LayoutParams.WRAP_CONTENT;
        mLayoutParams.gravity = Gravity.LEFT | Gravity.TOP;
        mLayoutParams.format = PixelFormat.RGBA_8888;
        //此处mLayoutParams.type不建议使用TYPE_TOAST，因为在一些三方ROM中会出现拖动异常的问题，虽然它不需要权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mLayoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        }else {
            mLayoutParams.type =  WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        }
        //悬浮窗背景明暗度0~1，数值越大背景越暗，只有在flags设置了WindowManager.LayoutParams.FLAG_DIM_BEHIND 这个属性才会生效
        mLayoutParams.dimAmount = 0.0f;
        //悬浮窗透明度0~1，数值越大越不透明
        mLayoutParams.alpha = 0.8f;
        offsetX = 0;
        offsetY = getStatusBarHeight(mContext);
        //设置初始位置
        mLayoutParams.x = mDisplayMetrics.widthPixels - offsetX;
        mLayoutParams.y = mDisplayMetrics.widthPixels*3/4 - offsetY;
    }

    /**
     * 将窗体添加到屏幕上
     */
    public void show() {
        if (!isAppOps(mContext)){
//            openOpsSettings(mContext);
//            Toast.makeText(mContext,"需要授权应用悬浮权限",Toast.LENGTH_SHORT).show();
            return;
        }
        if (!isShowing()) {
            mWindowManager.addView(floatView, mLayoutParams);
            isShowing = true;
        }
    }

    /**
     * 悬浮窗是否正在显示
     *
     * @return true if it's showing.
     */
    private boolean isShowing() {
        if (floatView != null && floatView.getVisibility() == View.VISIBLE) {
            return isShowing;
        }
        return false;
    }

    /**
     * 打开悬浮窗设置页
     * 部分第三方ROM无法直接跳转可使用{@link #openAppSettings(Context)}跳到应用详情页
     *
     * @param context
     * @return true if it's open successful.
     */
    public static boolean openOpsSettings(Context context){
        try {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + context.getPackageName()));
            context.startActivity(intent);
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * 打开应用详情页
     * @param context
     * @return true if it's open success.
     */
    public static boolean openAppSettings(Context context){
        try {
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            Uri uri = Uri.fromParts("package", context.getPackageName(), null);
            intent.setData(uri);
            context.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * 判断 悬浮窗口权限是否打开
     * 由于android未提供直接跳转到悬浮窗设置页的api，此方法使用反射去查找相关函数进行跳转
     * 部分第三方ROM可能不适用
     * @param context
     * @return true 允许  false禁止
     */
    public static boolean isAppOps(Context context) {
        try {
            Object object = context.getSystemService(Context.APP_OPS_SERVICE);
            if (object == null) {
                return false;
            }
            Class localClass = object.getClass();
            Class[] arrayOfClass = new Class[3];
            arrayOfClass[0] = Integer.TYPE;
            arrayOfClass[1] = Integer.TYPE;
            arrayOfClass[2] = String.class;
            Method method = localClass.getMethod("checkOp", arrayOfClass);
            if (method == null) {
                return false;
            }
            Object[] arrayOfObject1 = new Object[3];
            arrayOfObject1[0] = Integer.valueOf(24);
            arrayOfObject1[1] = Integer.valueOf(Binder.getCallingUid());
            arrayOfObject1[2] = context.getPackageName();
            int m = ((Integer) method.invoke(object, arrayOfObject1)).intValue();
            return m == AppOpsManager.MODE_ALLOWED;
        } catch (Exception ex) {

        }
        return false;
    }

    /**
     * 移除悬浮窗
     */
    public void remove() {
        if (isShowing()) {
            floatView.removeView(contentView);
            mWindowManager.removeView(floatView);
            isShowing = false;
        }
    }

    /**
     * 用于获取系统状态栏的高度。
     *
     * @return 返回状态栏高度的像素值。
     */
    public static int getStatusBarHeight(Context ctx) {
        int Identifier = ctx.getResources().getIdentifier("status_bar_height",
                "dimen", "android");
        if (Identifier > 0) {
            return ctx.getResources().getDimensionPixelSize(Identifier);
        }
        return 0;
    }

    class FloatView extends LinearLayout{

        //记录按下位置
        int interceptX=0;
        int interceptY=0;

        public FloatView(Context context) {
            super(context);
            //这里由于一个ViewGroup不能add一个已经有Parent的contentView,所以需要先判断contentView是否有Parent
            //如果有则需要将contentView先移除
            if (contentView.getParent()!=null&&contentView.getParent() instanceof ViewGroup){
                ((ViewGroup) contentView.getParent()).removeView(contentView);
            }
            addView(contentView);
        }

        /**
         * 解决点击与拖动冲突的关键代码
         * @param ev
         * @return
         */
        @Override
        public boolean onInterceptTouchEvent(MotionEvent ev) {
            //此回调如果返回true则表示拦截TouchEvent由自己处理，false表示不拦截TouchEvent分发出去由子view处理
            //解决方案：如果是拖动父View则返回true调用自己的onTouch改变位置，是点击则返回false去响应子view的点击事件
            boolean isIntercept = false;
            switch (ev.getAction()){
                case MotionEvent.ACTION_DOWN:
                    interceptX = (int) ev.getX();
                    interceptY = (int) ev.getY();
                    downX = ev.getX();
                    downY = ev.getY();
                    isIntercept = false;
                    break;
                case MotionEvent.ACTION_MOVE:
                    //在一些dpi较高的设备上点击view很容易触发 ACTION_MOVE，所以此处做一个过滤
                    if (Math.abs(ev.getX()-interceptX)>MINIMUM_OFFSET&&Math.abs(ev.getY()-interceptY)>MINIMUM_OFFSET){
                        isIntercept = true;
                    }else {
                        isIntercept = false;
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    isIntercept = false;
                    break;
                default:
                    break;
            }
            return isIntercept;
        }
    }

    class WindowTouchListener implements View.OnTouchListener {

        @Override
        public boolean onTouch(View v, MotionEvent event) {

            //获取触摸点相对于屏幕左上角的坐标
            rowX = event.getRawX();
            rowY = event.getRawY() - getStatusBarHeight(mContext);

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    actionDown(event);
                    break;
                case MotionEvent.ACTION_MOVE:
                    actionMove(event);
                    break;
                case MotionEvent.ACTION_UP:
                    actionUp(event);
                    break;
                case MotionEvent.ACTION_OUTSIDE:
                    actionOutSide(event);
                    break;
                default:
                    break;
            }
            return false;
        }

        /**
         * 手指点击窗口外的事件
         *
         * @param event
         */
        private void actionOutSide(MotionEvent event) {
            //由于我们在layoutParams中添加了FLAG_WATCH_OUTSIDE_TOUCH标记，那么点击悬浮窗之外时此事件就会被响应
            //这里可以用来扩展点击悬浮窗外部响应事件
        }

        /**
         * 手指抬起事件
         *
         * @param event
         */
        private void actionUp(MotionEvent event) {
            if (autoAlign) {
                autoAlign();
            }
        }

        /**
         * 拖动事件
         *
         * @param event
         */
        private void actionMove(MotionEvent event) {
            //拖动事件下一直计算坐标 然后更新悬浮窗位置
            updateLocation((rowX - downX),(rowY - downY));
        }

        /**
         * 更新位置
         */
        private void updateLocation(float x, float y) {
            mLayoutParams.x = (int) x;
            mLayoutParams.y = (int) y;
            mWindowManager.updateViewLayout(floatView, mLayoutParams);
        }

        /**
         * 手指按下事件
         *
         * @param event
         */
        private void actionDown(MotionEvent event) {
//            downX = event.getX();
//            downY = event.getY();
        }

        /**
         * 自动贴边
         */
        private void autoAlign() {
            float fromX = mLayoutParams.x;

            if (rowX <= mDisplayMetrics.widthPixels / 2) {
                mLayoutParams.x = 0;
            } else {
                mLayoutParams.x = mDisplayMetrics.widthPixels;
            }

            //这里使用ValueAnimator来平滑计算起始X坐标到结束X坐标之间的值，并更新悬浮窗位置
            ValueAnimator animator = ValueAnimator.ofFloat(fromX, mLayoutParams.x);
            animator.setDuration(300);
            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    //这里会返回fromX ~ mLayoutParams.x之间经过计算的过渡值
                    float toX = (float) animation.getAnimatedValue();
                    //我们直接使用这个值来更新悬浮窗位置
                    updateLocation(toX, mLayoutParams.y);
                }
            });
            animator.start();
        }
    }

    public static class Builder {
        private Context context;
        private boolean autoAlign;
        private boolean modality;
        private View contentView;
        private boolean moveAble;

        /**
         * @param context 上下文环境
         * @param contentView  需要悬浮的视图
         */
        public Builder(Context context, @NonNull View contentView) {
            this.context = context.getApplicationContext();
            this.contentView = contentView;
        }

        /**
         * 是否自动贴边
         * @param autoAlign
         * @return
         */
        public Builder setAutoAlign(boolean autoAlign) {
            this.autoAlign = autoAlign;
            return this;
        }

        /**
         * 是否模态窗口（事件是否可穿透当前窗口）
         * @param modality
         * @return
         */
        public Builder setModality(boolean modality) {
            this.modality = modality;
            return this;
        }

        /**
         * 是否可拖动
         * @param moveAble
         * @return
         */
        public Builder setMoveAble(boolean moveAble) {
            this.moveAble = moveAble;
            return this;
        }

        public FloatWindowUtils create() {
            return new FloatWindowUtils(this);
        }
    }
}
