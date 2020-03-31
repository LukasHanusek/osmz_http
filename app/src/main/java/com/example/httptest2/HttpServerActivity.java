package com.example.httptest2;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Camera;
import android.os.Bundle;
import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.httptest2.httpcamera.CameraController;

public class HttpServerActivity extends Activity implements OnClickListener{

	private SocketServer s;

	public static TextView bytessend;

	public EditText maxThreads;

	private CameraController cc;

	public static Handler handler = new Handler(Looper.getMainLooper()) {
		@Override
		public void handleMessage(Message inputMessage) {
			bytessend.setText(Stats.getTotalSend() + " bytes send");
		}
	};

    @Override
    protected void onStop() {
        super.onStop();
        cc.destroy();
        s.close();
    }
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_http_server);
        
        Button btn1 = (Button)findViewById(R.id.button1);
        Button btn2 = (Button)findViewById(R.id.button2);
         
        btn1.setOnClickListener(this);
        btn2.setOnClickListener(this);

		bytessend = findViewById(R.id.bytessend);
		maxThreads = findViewById(R.id.maxThreads);
        
    }

    private static final int READ_EXTERNAL_STORAGE = 1;

	@Override
	public void onClick(View v) {
	    //set new maximum threads button
        if (v.getId() == R.id.setThreads) {
            int max = 5;
            try {
                max = Integer.valueOf(maxThreads.getText().toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
            s.setMax(max);
        }
        //start web server button
		if (v.getId() == R.id.button1) {
            int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
            if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, READ_EXTERNAL_STORAGE);
            }
            int max = 5;
            try {
                max = Integer.valueOf(maxThreads.getText().toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
			s = new SocketServer(max);
			s.start();
            cc = new CameraController();
		}
		//stop web server button
		if (v.getId() == R.id.button2) {
			s.close();
			try {
				s.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
    
}
