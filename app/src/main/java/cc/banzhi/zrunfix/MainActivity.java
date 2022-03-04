package cc.banzhi.zrunfix;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Toast;


import androidx.appcompat.app.AppCompatActivity;

import cc.banzhi.runfix.RunFix;

import cc.banzhi.zrunfix.R;

public class MainActivity extends AppCompatActivity {
    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 修复前
        findViewById(R.id.tv_tip).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "出bug了", Toast.LENGTH_SHORT).show();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, stringFromJNI() + "", Toast.LENGTH_SHORT).show();
                        Log.d("RunFix", RunFix.getInstance(MainActivity.this).fixDefaultDir());
                    }
                }, 2000);
            }
        });

//        // 修复后
//        findViewById(R.id.tv_tip).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Toast.makeText(MainActivity.this, "修复了", Toast.LENGTH_SHORT).show();
//            }
//        });
    }
}