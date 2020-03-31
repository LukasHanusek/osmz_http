package com.example.httptest2;

import android.os.Environment;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.concurrent.Semaphore;

public class ClientThread extends Thread {

    private Socket s;
    private Semaphore semaphore;

    public ClientThread(Socket s, Semaphore se) {
        this.s = s;
        this.semaphore = se;
    }

    @Override
    public void run() {
        try {
            Log.d("SERVER", "Socket Accepted in thread"); //je vidět v Logcat debugu

            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
            BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));

            String tmp = in.readLine();
            while (tmp != null && !tmp.isEmpty()) {
                if (tmp.startsWith("GET")) {
                    processGet(tmp, s);
                    break;
                } else {
                    tmp = in.readLine();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            semaphore.release();
            try {
                s.getInputStream().close();
            } catch (Exception e) {}
            try {
                s.getOutputStream().close();
            } catch (Exception e) {}
            try {
                s.close();
            } catch (Exception e) {}
        }
    }

    private String storagePath = Environment.getExternalStorageDirectory().getPath() + "/WebServer/";

    /**
     * Aby to fungovalo je třeba nejdříve zapnout http server v android emulatoru kliknout na tlačítko start
     *
     * aby se to dalo otevřít v prohlížeči je třeba otevřít port je na to bat script v AndroidWorkspace složce zde na pc
     * @param request
     */
    public void processGet(String request, Socket s) {
        String uri = request.split("\\s+")[1];
        File f = null;
        try {

            //special camera url
            if (uri.equals("/camera/snapshot")) {
                ReplyWriter.writeSnapshot(s);
                return;
            }
            if (uri.equals("/camera/stream")) {
                ReplyWriter.writeStream(s);
                return;
            }

            //cmd processor
            if (uri.startsWith("/cgi-bin/")) {
                ReplyWriter.processCmd(s, uri.split("/cgi-bin/")[1]);
                return;
            }

            //special index url
            if (uri.equals("/")) {
                f = new File(storagePath + "index.html");

            //path to file url
            } else {
                f = new File(storagePath + uri);
            }


            ReplyWriter.writeReply(s, f);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
