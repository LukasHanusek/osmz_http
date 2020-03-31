package com.example.httptest2.httpcamera;

import android.hardware.Camera;
import android.util.Log;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Semaphore;

public class CameraController {

    private Camera c;
    public static byte[] safeData; //older picture safe to get
    public static byte[] unsafeData; //newest picture (might be null)
    public boolean running = true;

    private Semaphore cameraSemaphore = new Semaphore(1);
    private Thread t;

    public CameraController() {
        init();
        //repeat task
        t = new Thread() {
            public void run() {
                while(running) {
                    try {
                        //Log.d("camera", "try camera...");
                        cameraSemaphore.acquire();
                        c.startPreview();
                        //Log.d("camera", "taking picture...");
                        c.takePicture(null, null, mPicture);
                        Thread.sleep(250L); //timeout 100ms
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        t.start();
    }

    private void init(){
        Log.d("camera", "initializing camera...");
        try {
            c = Camera.open();
            c.release();
            c = Camera.open();
            c.stopPreview();

            Camera.Parameters parameters = c.getParameters();
            parameters.setPictureSize(1920, 1080);
            parameters.setJpegQuality(100);
            parameters.setVideoStabilization(false);
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
            c.setParameters(parameters);
            //c.setPreviewCallback(previewCallback);
            Log.d("camera", "camera initialized!");
        } catch (Exception e){
            e.printStackTrace();
            Log.d("camera", "camera not available!");
        }
    }

    public void destroy() {
        running = false;
        try {t.interrupt();} catch (Exception e){}
        c.release();
    }

    private Camera.PictureCallback mPicture = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            //Log.d("camera", "picture taken!");
            safeData = unsafeData;
            unsafeData = data;
            cameraSemaphore.release();
        }
    };

    /*private Camera.PreviewCallback previewCallback = new Camera.PreviewCallback() {
        @Override
        public void onPreviewFrame(byte[] bytes, Camera camera) {
            cameraSemaphore.release(); //release semaphore for another cycle
        }
    };*/

}
