package colorphone.acb.com.libweather;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import colorphone.acb.com.libweather.base.BaseAppCompatActivity;

/**
 * Created by zqs on 2019/4/9.
 */
public class WeatherVideoActivity extends BaseAppCompatActivity {

    private ImageView ivSetting;
    private ImageView ivClose;
    private ImageView ivCallCccept;

    public static void start(Context context) {
        Intent intent = new Intent(context, WeatherVideoActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather_video);
        initView();
    }

    private void initView() {
        ivSetting = findViewById(R.id.iv_setting);
        ivClose = findViewById(R.id.iv_close);
        ivCallCccept = findViewById(R.id.iv_call_accept);
        ivSetting.setOnClickListener(onClickListener);
        ivClose.setOnClickListener(onClickListener);
        ivCallCccept.setOnClickListener(onClickListener);
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v == ivSetting) {
                popSetting(v);
            } else if (v == ivClose) {
                WeatherVideoActivity.this.finish();
            } else if (v == ivCallCccept) {

            }
        }
    };

    private void popSetting(View v) {

    }
}
