package com.hande.goochao.utils;

import java.util.concurrent.TimeUnit;

/**
 * Created by yanshen on 2015/11/26.
 */
public class SetTimeout extends Thread {

    private int counter;

    private TimeUnit timeUnit;

    private int interval;

    private SetTimeoutHandler handler;

    public SetTimeout(int counter, TimeUnit timeUnit, int interval) {
        this.counter = counter;
        this.timeUnit = timeUnit;
        this.interval = interval;
    }

    public SetTimeout setHandler(SetTimeoutHandler handler) {
        this.handler = handler;
        return this;
    }

    @Override
    public void run() {
        int current = 0;
        while (current < counter) {

            try {
                timeUnit.sleep(interval);
                if (handler != null) {
                    handler.handler(current);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            current++;
        }
    }

    public interface SetTimeoutHandler {
        /**
         * 当前次数
         * @param current
         */
        void handler(int current);
    }
}
