package com.example.httptest2;

import android.os.Environment;
import android.util.Log;

import com.example.httptest2.httpcamera.CameraController;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

public class ReplyWriter {

    private static String storagePath = Environment.getExternalStorageDirectory().getPath() + "/WebServer/";
    private byte data;

    public static void writeReply(Socket s, File f) throws IOException {
        //404
        if (!f.exists()) {
            write404(f, s);
        } else {
            //text files
            if (f.getName().endsWith(".html") || f.getName().endsWith(".htm") || f.getName().endsWith(".txt")) {
                writeText(f, s);
                //image file
            } else if (f.getName().endsWith(".jpg") || f.getName().endsWith(".png") || f.getName().endsWith(".jpeg")){
                writeBinary(f, s);
            }
        }
    }

    public static void writeSnapshot(Socket s) throws IOException {
        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
        BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
        //write head
        out.write("HTTP/1.1 200 OK");
        out.newLine();
        out.write("Content-Length: " + CameraController.safeData.length);
        out.newLine();
        out.write("Content-Type: image/jpeg");
        out.newLine();
        out.newLine();
        out.flush();

        //stats
        Stats.addSend(CameraController.safeData.length);
        HttpServerActivity.handler.sendEmptyMessage(1); //push update to UI thread

        //write binary data
        s.getOutputStream().write(CameraController.safeData, 0, CameraController.safeData.length);

        //flush and close - must be here or response wont be complete
        s.getOutputStream().flush();
        s.getOutputStream().close();
        in.close();
        s.close();
    }

    public static void writeStream(Socket s) throws IOException {
        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
        BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
        //write head
        out.write("HTTP/1.1 200 OK");
        out.newLine();
        out.write("Content-Type: multipart/x-mixed-replace; boundary=\"OSMZ_boundary\"");
        out.flush();


        while(s.isBound() && s.isConnected()) {
            byte[] data = CameraController.safeData;

            out.newLine();
            out.newLine();
            out.write("--OSMZ_boundary");
            out.newLine();
            out.write("Content-Type: image/jpeg");
            out.newLine();
            out.write("Content-Length: " + data.length);
            out.newLine();
            out.newLine();
            out.flush();
            //write binary data
            s.getOutputStream().write(data, 0, data.length);
            s.getOutputStream().flush();
            out.flush();

            //stats
            Stats.addSend(CameraController.safeData.length);
            HttpServerActivity.handler.sendEmptyMessage(1); //push update to UI thread

            //sleep = 4 FPS
            try {
                Thread.sleep(250);
            } catch (Exception e) { }
        }


        //flush and close the stream
        try {
            s.getOutputStream().close();
            s.getInputStream().close();
            s.close();
        } catch (Exception e) {
            //dont care
        }
    }



    private static void writeBinary(File f, Socket s) throws IOException {
        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
        BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
        FileInputStream fis = new FileInputStream(f.getAbsolutePath());
        long len = getLenghtInBytes(f);
        //write head
        out.write("HTTP/1.1 200 OK");
        out.newLine();
        out.write("Content-Length: " + len);
        out.newLine();
        out.write("Content-Type: image/jpeg");
        out.newLine();
        out.newLine();
        out.flush();
        Log.d("SERVER", "Length: " + len);

        //stats
        Stats.addSend(len);
        HttpServerActivity.handler.sendEmptyMessage(1); //push update to UI thread

        //write binary file
        byte[] buffer = new byte[ 4096 ];
        int bytesRead;
        while ((bytesRead = fis.read(buffer)) != -1 ) {
            s.getOutputStream().write(buffer, 0, bytesRead);
        }

        //flush and close - must be here or response wont be complete
        s.getOutputStream().flush();
        s.getOutputStream().close();
        in.close();
        s.close();
    }

    public static void processCmd(Socket s, String cmd) throws IOException {
        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
        BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));

        Log.d("cmd", "processing: " + cmd);

        String cmds[] = cmd.split("%20");

        ProcessBuilder pb = new ProcessBuilder(cmds);
        Process process = pb.start();
        BufferedReader bis = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line = bis.readLine();
        StringBuilder output = new StringBuilder();
        while (line != null) {
            Log.d("cmd", line);
            output.append(line);
            output.append("<br>");
            line = bis.readLine();
        }
        try {
            process.destroy();
        } catch (Exception e) {
            e.printStackTrace();
        }

        int len = output.toString().length();
        out.write("HTTP/1.1 200 OK");
        out.newLine();
        out.write("Content-Length: " + len);
        out.newLine();
        out.write("Content-Type: text/html; charset=utf-8");
        out.newLine();
        out.newLine();
        out.write(output.toString(), 0, len);

        //stats
        Stats.addSend(len);
        HttpServerActivity.handler.sendEmptyMessage(1); //push update to UI thread

        out.close();
        in.close();
    }

    private static void writeText(File f, Socket s) throws IOException {
        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
        BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
        FileInputStream fis = new FileInputStream(f.getAbsolutePath());
        byte content[] = new byte[(int) f.length()];
        fis.read(content);
        String result = new String(content);
        int len = result.length();
        out.write("HTTP/1.1 200 OK");
        out.newLine();
        out.write("Content-Length: " + len);
        out.newLine();
        out.write("Content-Type: text/html; charset=utf-8");
        out.newLine();
        out.newLine();
        out.write(result, 0, len);

        //stats
        Stats.addSend(len);
        HttpServerActivity.handler.sendEmptyMessage(1); //push update to UI thread

        out.close();
        in.close();
    }

    private static int getLenghtInBytes(File f) throws IOException {
        FileInputStream fis = new FileInputStream(f.getAbsolutePath());
        BufferedInputStream bis = new BufferedInputStream( fis );
        int total = 0;
        byte[] buffer = new byte[ 8192 ];
        int bytesRead;
        while ((bytesRead = bis.read(buffer)) != -1 ) {
            total += bytesRead;
        }
        fis.close();
        bis.close();
        return total;
    }


    private static void write404(File f, Socket s) throws IOException {
        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
        BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
        f = new File(storagePath + "404.html");
        FileInputStream fis = new FileInputStream(f.getAbsolutePath());
        byte content[] = new byte[(int) f.length()];
        fis.read(content);
        String result = new String(content);
        int len = result.length();

        out.write("HTTP/1.1 404 Not Found");
        out.newLine();
        out.write("Content-Length: " + len);
        out.newLine();
        out.write("Content-Type: text/html; charset=utf-8");
        out.newLine();
        out.newLine();
        out.write(result, 0, len);

        //stats
        Stats.addSend(len);
        HttpServerActivity.handler.sendEmptyMessage(1); //push update to UI thread

        out.close();
        in.close();
    }


}
