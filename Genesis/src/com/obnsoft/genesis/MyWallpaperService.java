package com.obnsoft.genesis;

import android.service.wallpaper.WallpaperService;
import android.util.Log;
import android.view.SurfaceHolder;

public class MyWallpaperService extends WallpaperService {

    @Override
    public Engine onCreateEngine() {
        return new MyEngine();
    }

    private void log(String msg) {
        Log.d("Genesis", msg);
    }

    /*-----------------------------------------------------------------------*/

    public class MyEngine extends Engine {
        private MyEngineSurface mSurface = null;

        @Override
        public void onCreate(SurfaceHolder surfaceHolder) {
            super.onCreate(surfaceHolder);
            surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_GPU);
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            log("onVisibilityChanged : " + visible);
            super.onVisibilityChanged(visible);
            if (visible) {
                mSurface.onResume();
            } else {
                mSurface.onPause();
            }
        }

        @Override
        public void onSurfaceCreated(SurfaceHolder holder) {
            super.onSurfaceCreated(holder);
            mSurface = new MyEngineSurface(getSurfaceHolder(), getResources());
            mSurface.start();
        }

        @Override
        public void onSurfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            log("onSurfaceChanged: " + width + "x" + height);
            super.onSurfaceChanged(holder, format, width, height);
            mSurface.onSurfaceChanged(width, height);
        }

        @Override
        public void onSurfaceDestroyed(SurfaceHolder holder) {
            log("onSurfaceDestroyed");
            super.onSurfaceDestroyed(holder);
            if (mSurface != null) {
                mSurface.onDestroy();
                mSurface = null;
            }
        }
    }

}
