package com.example.httptest2;

import android.Manifest;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.httptest2.httpcamera.CameraController;

public class HttpService extends Service {

    private SocketServer s;
    private CameraController cc;
    private int maxThreads;

    private final IBinder localService = new LocalService();

    class LocalService extends Binder {
        HttpService getService() {
            return HttpService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return localService;
    }

    public void setMax(int max) {
        this.maxThreads = max;
        s.setMax(this.maxThreads);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startID) {
        this.maxThreads = 5;
        s = new SocketServer(this.maxThreads);
        s.start();
        cc = new CameraController();
        return super.onStartCommand(intent, flags, startID);
    }

    public void stopService() {
        cc.destroy();
        s.close();
    }

    @Override
    public void onDestroy() {
        cc.destroy();
        s.close();
    }


}
