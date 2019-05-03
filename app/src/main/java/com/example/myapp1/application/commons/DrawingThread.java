package com.example.myapp1.application.commons;

import android.util.Log;

import com.example.myapp1.ChartTab;
import com.example.myapp1.MainActivity;
import com.example.myapp1.TerminalTab;

import java.util.concurrent.atomic.AtomicBoolean;

public class DrawingThread implements Runnable {
    private Thread t;
    private Commons commons;
//    private MainActivity mainActivity;
    private TerminalTab terminalTab;
    private ChartTab chartTab;
    private AtomicBoolean isThreadWaiting;
    private AtomicBoolean terminateThread;

    public DrawingThread(Commons commons,TerminalTab terminalTab,ChartTab chartTab) {
        this.commons = commons;
//        this.mainActivity=mainActivity;
        this.terminalTab=terminalTab;
        this.chartTab=chartTab;
        terminateThread=new AtomicBoolean(false);
        isThreadWaiting=new AtomicBoolean(true); //początkowo wątek ma czekać
        t=new Thread(this,"Drawer");
        t.start(); //włącz wątek
    }

    public void suspend(){
        if(isThreadWaiting.get()==false)
            isThreadWaiting.set(true);
    }

    public synchronized void resume(){
        if(isThreadWaiting.get()==true) {
            isThreadWaiting.set(false);
            notify();
        }
    }

    public void terminate(){
        terminateThread.set(true);
        resume();
    }

    @Override
    public void run() {
        Log.d(MainActivity.TAG,"Thread "+t.getName()+" started!");
        int i=0;
        while(true){
            try {
                synchronized (this) {
                    while (isThreadWaiting.get()) {
                        wait();
                    }
                }
            }catch (InterruptedException ie){
                ie.printStackTrace();
            }
            if(terminateThread.get())
                break;
            //ciało wątku
            String data=commons.readAllFromBuffer();
            Log.d(MainActivity.TAG,data);
            if(!data.equals(""))
                terminalTab.printData(data);
//            chartTab.addPointToChart(i,(int)Math.sin(i));

            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        Log.d(MainActivity.TAG,"Thread "+t.getName()+" terminated!");
    }
}
