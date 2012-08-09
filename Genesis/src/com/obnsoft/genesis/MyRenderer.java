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

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.Random;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.egl.EGLSurface;
import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;
import javax.microedition.khronos.opengles.GL11ExtensionPack;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.opengl.GLU;
import android.preference.PreferenceManager;
import android.view.SurfaceHolder;

public class MyRenderer implements OnSharedPreferenceChangeListener {

    private static final int BYTES_FLOAT = 4;

    private Context             mContext;
    private SharedPreferences   mPrefs;
    
    private EGL10       mEGL        = null;
    private EGLContext  mEGLContext = null;
    private EGLDisplay  mEGLDisplay = null;
    private EGLSurface  mEGLSurface = null;
    private EGLConfig   mEGLConfig  = null;
    private GL10        mGL10       = null;

    private int     mWindowWidth    = -1;
    private int     mWindowHeight   = -1;
    private int     mBufferIndex    = 0;

    private int     mLevel;
    private float   mAlpha;
    private float   mScale;
    private boolean mInvert;

    private float   mRotX;
    private float   mRotY;
    private float   mRotZ;
    private float   mRotR;
    private long    mStartTime;

    public MyRenderer(Context context) {
        mContext = context;
    }

    public void onInitialize() {
        mPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        mPrefs.registerOnSharedPreferenceChangeListener(this);
        initializeGL();
    }

    public void onSurfaceChanged(int width, int height) {
        mWindowWidth = width;
        mWindowHeight = height;
    }

    public void onStartDrawing(SurfaceHolder holder) {
        initializeSurface(holder);
        initializeObject(mGL10);
    }

    public void onDrawFrame() {
        drawFrame(mGL10);
    }

    public void onFinishDrawing() {
        finalizeSurface();
    }

    public void onDispose() {
        finalizeGL();
        mPrefs.unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
        initializeObject(mGL10);
    }

    /*-----------------------------------------------------------------------*/

    private void initializeGL() {
        myLog("initializeGL");

        /*  Initialize  */
        mEGL = (EGL10) EGLContext.getEGL();
        mEGLDisplay = mEGL.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY);
        int[] version = { -1, -1 };
        if (!mEGL.eglInitialize(mEGLDisplay, version)) {
            myLog("!eglInitialize");
            return;
        }

        /*  Get configuration  */
        EGLConfig[] configs = new EGLConfig[1];
        int[] num = new int[1];
        int[] spec = { EGL10.EGL_NONE };
        if (!mEGL.eglChooseConfig(mEGLDisplay, spec, configs, 1, num)) {
            myLog("!eglChooseConfig");
            return;
        }
        mEGLConfig = configs[0];

        /*  Create rendering context  */
        mEGLContext = mEGL.eglCreateContext(mEGLDisplay, mEGLConfig, EGL10.EGL_NO_CONTEXT, null);
        if (mEGLContext == EGL10.EGL_NO_CONTEXT) {
            myLog("glContext == EGL10.EGL_NO_CONTEXT");
            return;
        }

        /*  Get GLES interface  */
        mGL10 = (GL10) mEGLContext.getGL();
    }

    private void initializeSurface(SurfaceHolder holder) {
        myLog("initializeSurface");

        /*  Create surface  */
        mEGL.eglMakeCurrent(mEGLDisplay, EGL10.EGL_NO_SURFACE,
                EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_CONTEXT);
        mEGLSurface = mEGL.eglCreateWindowSurface(
                mEGLDisplay, mEGLConfig, holder, null);
        if (mEGLSurface == EGL10.EGL_NO_SURFACE) {
            myLog("glSurface == EGL10.EGL_NO_SURFACE");
            return;
        }

        /*  Attach surface to context  */
        if (!mEGL.eglMakeCurrent(mEGLDisplay, mEGLSurface, mEGLSurface, mEGLContext)) {
            myLog("!eglMakeCurrent");
            return;
        }

        /*  View port  */
        GL10 gl10 = mGL10;
        gl10.glViewport(0, 0, mWindowWidth, mWindowHeight);
        gl10.glMatrixMode(GL10.GL_PROJECTION);
        gl10.glLoadIdentity();
        GLU.gluPerspective(gl10, 45f, (float) mWindowWidth / (float) mWindowHeight, 1f, 100f);
        GLU.gluLookAt(gl10, 0f, 0f, -32f, 0f, 0f, 0f, 0f, 1f, 0f);
        gl10.glMatrixMode(GL10.GL_MODELVIEW);

        /*  Get buffer index  */
        gl10.glEnableClientState(GL10.GL_VERTEX_ARRAY);
        //gl10.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
        int[] buf = new int[1];
        ((GL11) gl10).glGenBuffers(1, buf, 0);
        mBufferIndex = buf[0];

        /*  Other settings  */
        gl10.glEnable(GL10.GL_BLEND);
        gl10.glEnable(GL10.GL_ALPHA);
        if (mInvert) {
            GL11ExtensionPack gl11ep = (GL11ExtensionPack) mGL10;
            gl11ep.glBlendEquation(GL11ExtensionPack.GL_FUNC_REVERSE_SUBTRACT);
        }
        gl10.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE);
    }

    private void initializeObject(GL10 gl10) {
        myLog("initializeObject");

        /*  Parameters  */
        applyPrefs(mPrefs);
        Random random = new Random();
        mRotX = random.nextFloat() * 64f - 32f;
        mRotY = random.nextFloat() * 64f - 32f;
        mRotZ = random.nextFloat() * 64f - 32f;
        mRotR = random.nextFloat() * 64f - 32f;
        mStartTime = System.currentTimeMillis();

        /*  Define vertices of quadrangle  */
        float v = (6f / mLevel) * mScale;
        float ary[] = {-v, v, 0f,   -v, -v, 0f,   v, v, 0f,   v, -v, 0f};
        ByteBuffer bb = ByteBuffer.allocateDirect(ary.length * BYTES_FLOAT);
        bb.order(ByteOrder.nativeOrder());
        FloatBuffer vbo = bb.asFloatBuffer();
        vbo.put(ary);
        vbo.position(0);

        /*  Assign vertices array  */
        GL11 gl11 = (GL11) gl10;
        gl11.glBindBuffer(GL11.GL_ARRAY_BUFFER, mBufferIndex);
        gl11.glBufferData(GL11.GL_ARRAY_BUFFER,
                vbo.capacity() * BYTES_FLOAT, vbo, GL11.GL_STATIC_DRAW);
        gl11.glVertexPointer(3, GL10.GL_FLOAT, 0/*GL10.GL_FLOAT * 3*/, 0);
        gl11.glBindBuffer(GL11.GL_ARRAY_BUFFER, 0);
    }

    private void applyPrefs(SharedPreferences prefs) {
        mLevel = Integer.parseInt(prefs.getString("level", "3"));
        mAlpha = Float.parseFloat(prefs.getString("alpha", "0.2f"));
        mScale = Float.parseFloat(prefs.getString("scale", "1f"));
        mInvert = Boolean.parseBoolean(prefs.getString("invert", "false"));
    }

    private void drawFrame(GL10 gl10) {
        float tick = (float) ((System.currentTimeMillis() - mStartTime) / 1000.0);

        /*  Clear  */
        if (mInvert) {
            gl10.glClearColor(1f, 1f, 1f, 1f);
        } else {
            gl10.glClearColor(0f, 0f, 0f, 1f);
        }
        gl10.glClear(GL10.GL_COLOR_BUFFER_BIT);

        /*  Rotate  */
        gl10.glLoadIdentity();
        gl10.glRotatef(mRotX * tick, 1.0f, 0.0f, 0.0f);
        gl10.glRotatef(mRotY * tick, 0.0f, 1.0f, 0.0f);
        gl10.glRotatef(mRotZ * tick, 0.0f, 0.0f, 1.0f);

        /*  Draw quadrangles  */
        for (int i = -mLevel; i <= mLevel; i++) {
            int jRange = mLevel - Math.abs(i);
            for (int j = -jRange; j <= jRange; j++) {
                int kRange = jRange - Math.abs(j);
                for (int k = -kRange; k <= kRange; k++) {
                    drawQuad(gl10,
                            (float) i / (float) mLevel,
                            (float) j / (float) mLevel,
                            (float) k / (float) mLevel,
                            (float) Math.cos(Math.toRadians(mRotR * tick)) * 48f);
                }
            }
        }

        /*  Update surface  */
        mEGL.eglSwapBuffers(mEGLDisplay, mEGLSurface);
    }

    private void drawQuad(GL10 gl10, float x, float y, float z, float r) {
        gl10.glPushMatrix();
        gl10.glColor4f((x + 1f) / 2f, (y + 1f) / 2f, (z + 1f) / 2f, mAlpha);
        gl10.glTranslatef(x * r, y * r, z * r);
        //gl10.glScalef(mScale, mScale, 1f);
        gl10.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, 4);
        gl10.glPopMatrix();
    }

    private void finalizeSurface() {
        myLog("finalizeSurface");

        if (mEGLSurface != null) {
            mEGL.eglMakeCurrent(mEGLDisplay, EGL10.EGL_NO_SURFACE,
                    EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_CONTEXT);
            mEGL.eglDestroySurface(mEGLDisplay, mEGLSurface);
            mEGLSurface = null;
        }
    }

    private void finalizeGL() {
        myLog("finalizeGL");

        if (mEGLSurface != null) {
            mEGL.eglMakeCurrent(mEGLDisplay, EGL10.EGL_NO_SURFACE,
                    EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_CONTEXT);
            mEGL.eglDestroySurface(mEGLDisplay, mEGLSurface);
            mEGLSurface = null;
        }

        if (mEGLContext != null) {
            mEGL.eglDestroyContext(mEGLDisplay, mEGLContext);
            mEGLContext = null;
        }

        if (mEGLDisplay != null) {
            mEGL.eglTerminate(mEGLDisplay);
            mEGLDisplay = null;
        }
    }

    private void myLog(String msg) {
        MyWallpaperService.myLog(msg);
    }

}
