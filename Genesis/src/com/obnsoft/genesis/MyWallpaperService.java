package com.obnsoft.genesis;

import android.service.wallpaper.WallpaperService;
import android.util.Log;
import android.view.SurfaceHolder;

public class MyWallpaperService extends WallpaperService {

    @Override
    public Engine onCreateEngine() {
        return new MyEngine();
    }

    private void myLog(String msg) {
        Log.d("Genesis", msg);
    }

    /*-----------------------------------------------------------------------*/

    class MyEngine extends Engine {
        private MyRenderer  mRenderer = new MyRenderer(getApplicationContext(), this);
        private MyThread    mThread = null;

        @Override
        public void onCreate(SurfaceHolder surfaceHolder) {
            super.onCreate(surfaceHolder);
            surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_GPU);
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            myLog("onVisibilityChanged : " + visible);
            super.onVisibilityChanged(visible);
            if (visible) {
                mThread = new MyThread(mRenderer);
                mThread.start();
            } else {
                mThread.pause();
                mThread = null;
            }
        }

        @Override
        public void onSurfaceCreated(SurfaceHolder holder) {
            myLog("onSurfaceCreated");
            super.onSurfaceCreated(holder);
        }

        @Override
        public void onSurfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            myLog("onSurfaceChanged: " + width + "x" + height);
            super.onSurfaceChanged(holder, format, width, height);

            mRenderer.onSurfaceChanged(width, height);
        }

        @Override
        public void onSurfaceDestroyed(SurfaceHolder holder) {
            myLog("onSurfaceDestroyed");
            super.onSurfaceDestroyed(holder);

            if (mThread != null) {
                mThread.pause();
                mThread = null;
            }
        }

        @Override
        public void onDestroy() {
            myLog("onDestroy");
            super.onDestroy();

            if (mThread != null) {
                mThread.pause();
                mThread = null;
            }
            if (mRenderer != null) {
                mRenderer = null;
            }
        }

    }

    /*-----------------------------------------------------------------------*/

    class MyThread extends Thread {

        private static final int INTERVAL = 100;

        private MyRenderer      mRenderer = null;
        private boolean         mLoop = false;

        public MyThread(MyRenderer renderer) {
            mRenderer = renderer;
        }

        @Override
        public void run() {
            long tm = System.currentTimeMillis();
            mLoop = true;

            myLog("Thread loop start");
            mRenderer.onInitialize();
            while (mLoop) {
                mRenderer.onDrawFrame();

                tm += INTERVAL;
                long wait = tm - System.currentTimeMillis();
                if (wait > 0) {
                    try {
                        Thread.sleep(wait);
                    } catch(InterruptedException e) {
                        e.printStackTrace();
                    }
                } else {
                    yield();
                    tm -= wait;
                }
            }
            mRenderer.onDispose();
            myLog("Thread loop end");
        }

        public void pause() {
            synchronized(this) {
                mLoop = false;
            }
            try {
                join();
            } catch(InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }
    }

}
