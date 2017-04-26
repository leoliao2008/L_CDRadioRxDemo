package com.skycaster.skc_cdradiorx.utils;

import android.os.AsyncTask;
import android.os.SystemClock;

/**
 * Created by 廖华凯 on 2016/12/16.
 */
public class TimingUtils {

    private static boolean isTiming;

    public static synchronized void startTiming(final TimeUpdater timeUpdater){
        if(isTiming){
            return;
        }
       new AsyncTask<Void, Long, Void>() {

            @Override
            protected void onPreExecute() {
                isTiming = true;
            }

            @Override
            protected Void doInBackground(Void... params) {
                long time = 0;
                while (isTiming) {
                    SystemClock.sleep(1000);
                    time += 1;
                    publishProgress(time);
                }
                return null;
            }

            @Override
            protected void onProgressUpdate(Long... values) {
                timeUpdater.onUpdate(values[0]);
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public interface TimeUpdater{
        void onUpdate(long s);
    }

    public static synchronized void stopTiming(){
        isTiming=false;
    }
}
