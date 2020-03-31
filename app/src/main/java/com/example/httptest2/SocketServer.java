package com.example.httptest2;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Semaphore;

import android.util.Log;

public class SocketServer extends Thread {
	
	private ServerSocket serverSocket;
	public final int port = 12345;
	boolean bRunning;

	//limit max thread using semaphore
    private Semaphore semaphore;

    public SocketServer(int maxThreads) {
        this.semaphore = new Semaphore(maxThreads);
    }

    public void setMax(int max) {
        this.semaphore.release(this.semaphore.getQueueLength());
        this.semaphore = new Semaphore(max);
    }
	
	public void run() {
        try {
        	Log.d("SERVER", "Creating Socket");
            serverSocket = new ServerSocket(port);
            bRunning = true;
            while (bRunning) {
                Socket s = serverSocket.accept();
                semaphore.acquire();
                ClientThread ct = new ClientThread(s);
                ct.start();
            }
        } catch (IOException | InterruptedException e) {
            if (serverSocket != null && serverSocket.isClosed()) {
				Log.d("SERVER", "Normal exit");
			} else {
            	Log.d("SERVER", "Error");
            	e.printStackTrace();
            }
        } finally {
        	serverSocket = null;
        	bRunning = false;
        }
    }

    public void close() {
        try {
            serverSocket.close();
        } catch (IOException e) {
            Log.d("SERVER", "Error, probably interrupted in accept(), see log");
            e.printStackTrace();
        }
        bRunning = false;
    }

}
