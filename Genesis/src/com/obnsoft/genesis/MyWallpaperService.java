/*
 * Copyright (C) 2012 OBN-soft
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.obnsoft.genesis;

import android.service.wallpaper.WallpaperService;
import android.util.Log;
import android.view.SurfaceHolder;

public class MyWallpaperService extends WallpaperService {

    @Override
    public Engine onCreateEngine() {
        return new MyEngine();
    }

    public static void myLog(String msg) {
        Log.d("Genesis", msg);
    }

    /*-----------------------------------------------------------------------*/

    class MyEngine extends Engine {
        private MyRenderer  mRenderer = null;
        private MyThread    mThread = null;

        @Override
        public void onCreate(SurfaceHolder holder) {
            myLog("onCreated");
            super.onCreate(holder);

            holder.setType(SurfaceHolder.SURFACE_TYPE_GPU);
        }

        @Override
        public void onSurfaceCreated(SurfaceHolder holder) {
            myLog("onSurfaceCreated");
            super.onSurfaceCreated(holder);

            if (mRenderer != null) {
                myLog("Double renderer! Why?");
                mRenderer.onDispose();
            }
            mRenderer = new MyRenderer();
            mRenderer.onInitialize(getApplicationContext(), holder);
        }

        @Override
        public void onSurfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            myLog("onSurfaceChanged: " + width + "x" + height);
            super.onSurfaceChanged(holder, format, width, height);

            mRenderer.onSurfaceChanged(holder, format, width, height);
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            myLog("onVisibilityChanged: " + visible);
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
        public void onSurfaceDestroyed(SurfaceHolder holder) {
            myLog("onSurfaceDestroyed");
            super.onSurfaceDestroyed(holder);

            if (mThread != null) {
                mThread.pause();
                mThread = null;
            }
            if (mRenderer != null) {
                mRenderer.onDispose();
                mRenderer = null;
            }
        }

        @Override
        public void onDestroy() {
            myLog("onDestroy");
            super.onDestroy();
        }

    }

    /*-----------------------------------------------------------------------*/

    class MyThread extends Thread {

        private static final int INTERVAL = 50;

        private MyRenderer  mRenderer = null;
        private boolean     mLoop = false;

        public MyThread(MyRenderer renderer) {
            mRenderer = renderer;
        }

        @Override
        public void run() {
            long tm = System.currentTimeMillis();
            mLoop = true;

            myLog("Thread loop start");
            mRenderer.onStartDrawing();
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
            mRenderer.onFinishDrawing();
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
