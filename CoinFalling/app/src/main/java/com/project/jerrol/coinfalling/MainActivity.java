package com.project.jerrol.coinfalling;

import android.opengl.GLSurfaceView;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;

import com.project.jerrol.coinfalling.interfaces.CoinListener;
import com.project.jerrol.coinfalling.opengl.CoinGL;

public class MainActivity extends AppCompatActivity implements CoinListener {

    CoinGL glSurface;

    EditText editText;
    Button button;

    RelativeLayout layout;
    RelativeLayout.LayoutParams glParams;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Turn off the window's title bar
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        super.onCreate(savedInstanceState);

        // Fullscreen mode
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        //Create an Instance with this Activity
        glSurface = new CoinGL(this, 0);

        // Set our view.
        setContentView(R.layout.activity_main);

        // Retrieve our Relative layout from our main layout we just set to our view.
        layout = (RelativeLayout) findViewById(R.id.main_layout);

        // Attach our surfaceview to our relative layout from our main layout.
        glParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        layout.addView(glSurface, glParams);

        editText = findViewById(R.id.editText);
        button = findViewById(R.id.button);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                glSurface.onResumeGL(Integer.parseInt(editText.getText().toString()));
            }
        });
    }

    public void updateCoin() {

    }

    @Override
    protected void onResume() {
        super.onResume();
        glSurface.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        glSurface.onPause();
    }

    @Override
    public void onCoinFallingComplete(boolean isComplete) {
        Log.i("MainActivity", "Destroy render");
        glSurface.onPauseGL();
    }

}
