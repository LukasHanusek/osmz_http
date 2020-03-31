package com.example.httptest2;

import java.util.concurrent.atomic.AtomicLong;

public class Stats {

    private static AtomicLong received = new AtomicLong();
    private static AtomicLong send = new AtomicLong();

    public static void addReceived(long l) {
        received.addAndGet(l);
    }

    public static void addSend(long l) {
        send.addAndGet(l);
    }

    public static long getTotalReceived() {
        return received.get();
    }

    public static long getTotalSend() {
        return send.get();
    }


}
