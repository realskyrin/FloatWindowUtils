package com.example.admin.floatwindowutils;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    Context context;
    FloatWindow floatWindow;
    View contentView;

    Switch swAutoalign;
    Switch swModely;
    Switch swMove;

    boolean isAutoAlign;
    boolean isModality;
    boolean isMoveAble;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;
        contentView = getContentView();

        findViewById(R.id.btn_show).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!FloatWindow.isAppOpsOn(MainActivity.this)) {
                    FloatWindow.openOpsSettings(MainActivity.this);
                    return;
                }
                initFloatWindow(contentView);
            }
        });

        findViewById(R.id.btn_remove).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (floatWindow != null) {
                    floatWindow.remove();
                }
            }
        });

        findViewById(R.id.btn_ops).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FloatWindow.openOpsSettings(MainActivity.this);
            }
        });

        swAutoalign = (Switch) findViewById(R.id.sw_autoalign);
        swModely = (Switch) findViewById(R.id.sw_modely);
        swMove = (Switch) findViewById(R.id.sw_move);

        swAutoalign.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                isAutoAlign = isChecked;
                initFloatWindow(contentView);
            }
        });
        swModely.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                isModality = isChecked;
                initFloatWindow(contentView);
            }
        });
        swMove.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                isMoveAble = isChecked;
                initFloatWindow(contentView);
            }
        });
    }

    /**
     * 初始化floatWindow
     *
     * @param view
     */
    private void initFloatWindow(View view) {
        if (floatWindow != null) {
            floatWindow.remove();
            floatWindow = null;
        }
        floatWindow = new FloatWindow.With(context, view)
                .setAutoAlign(isAutoAlign)
                .setModality(isModality)
                .setMoveAble(isMoveAble)
                .create();
        floatWindow.show();
    }

    /**
     * 创建一个需要悬浮的视图
     *
     * @return
     */
    private View getContentView() {
        View view = LayoutInflater.from(context).inflate(R.layout.fv_test, null);
        final View ll_menu = view.findViewById(R.id.ll_btn);
        view.findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context, "button", Toast.LENGTH_SHORT).show();
            }
        });
        view.findViewById(R.id.button2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context, "button2", Toast.LENGTH_SHORT).show();
            }
        });
        view.findViewById(R.id.button3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                floatWindow.remove();
            }
        });
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ll_menu.getVisibility() == View.VISIBLE) {
                    ll_menu.setVisibility(View.GONE);
                } else {
                    ll_menu.setVisibility(View.VISIBLE);
                }
            }
        });
        return view;
    }

    private View getBtn() {
        Button mBtn = new Button(this);
        mBtn.setText("悬浮按钮");
        mBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context, "click", Toast.LENGTH_SHORT).show();
            }
        });
        return mBtn;
    }
}
