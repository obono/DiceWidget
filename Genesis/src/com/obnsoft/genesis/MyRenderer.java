package com.obnsoft.genesis;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.egl.EGLSurface;
import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLUtils;
import android.util.Log;
import android.view.SurfaceHolder;

public class MyRenderer {

    private Context         mContext;

    private EGL10           mEGL;
    private EGLContext      mEGLContext = null;
    private EGLDisplay      mEGLDisplay = null;
    private EGLSurface      mEGLSurface = null;
    private EGLConfig       mEGLConfig  = null;
    private GL10            mGL10       = null;
    private int             mWindowWidth = -1;
    private int             mWindowHeight = -1;

    int mVertexBufferObject = 0;
    int mTextureID          = 0;
    int mTextureHeight      = 0;
    int mTextureWidth       = 0;
    float mDegree = 0;

    public MyRenderer(Context context) {
        mContext = context;
    }

    public void onInitialize() {
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
    }

    private void initializeObject(GL10 gl10) {
        myLog("initializeObject");

        float vertices[] = {
            -0.5f,  0.5f, 0.0f, 0.0f, 0.0f,
            -0.5f, -0.5f, 0.0f, 0.0f, 1.0f,
             0.5f,  0.5f, 0.0f, 1.0f, 0.0f,
             0.5f, -0.5f, 0.0f, 1.0f, 1.0f,
        };

        ByteBuffer bb = ByteBuffer.allocateDirect(vertices.length * 4);
        bb.order(ByteOrder.nativeOrder());
        FloatBuffer vbo = bb.asFloatBuffer();
        vbo.put(vertices);
        vbo.position(0);

        gl10.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
        gl10.glEnableClientState(GL10.GL_VERTEX_ARRAY);

        /*  Vertex  */
        GL11 gl11 = (GL11) gl10;
        int[] buffers = new int[1];
        gl11.glGenBuffers(1, buffers, 0);
        mVertexBufferObject = buffers[0];
        gl11.glBindBuffer(GL11.GL_ARRAY_BUFFER, mVertexBufferObject);
        gl11.glBufferData(GL11.GL_ARRAY_BUFFER, vbo.capacity() * 4, vbo, GL11.GL_STATIC_DRAW);
        gl11.glVertexPointer(3, GL10.GL_FLOAT, 4 * 5, 0);
        gl11.glTexCoordPointer(2, GL10.GL_FLOAT, 4 * 5, 4 * 3);
        gl11.glBindBuffer(GL11.GL_ARRAY_BUFFER, 0);

        /*  Texture  */
        Bitmap bitmap = BitmapFactory.decodeResource(
                mContext.getResources(), R.drawable.image_512);
        gl10.glEnable(GL10.GL_TEXTURE_2D);
        //int[] buffers = new int[1];
        gl10.glGenTextures(1, buffers, 0);
        mTextureID = buffers[0];
        mTextureWidth = bitmap.getWidth();
        mTextureHeight = bitmap.getHeight();

        gl10.glBindTexture(GL10.GL_TEXTURE_2D, mTextureID);
        GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bitmap, 0);
        gl10.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_NEAREST);
        gl10.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_NEAREST);
        bitmap.recycle();
    }

    private void drawFrame(GL10 gl10) {
        mDegree += 4f;

        gl10.glViewport(0, 0, mWindowWidth, mWindowHeight);
        gl10.glClearColor(0.25f, 0.0f, 0.5f, 1);
        gl10.glClear(GL10.GL_COLOR_BUFFER_BIT);
        setTextureArea(gl10, 0, 0, 512, 512);
        drawQuad(gl10, mWindowWidth / 2 - 128, mWindowHeight / 2 - 128, 256, 256);
        mEGL.eglSwapBuffers(mEGLDisplay, mEGLSurface);
    }

    private void setTextureArea(GL10 gl10, int x, int y, int w, int h) {
        float tw = (float) w / (float) mTextureWidth;
        float th = (float) h / (float) mTextureHeight;
        float tx = (float) x / (float) mTextureWidth;
        float ty = (float) y / (float) mTextureHeight;

        gl10.glMatrixMode(GL10.GL_TEXTURE);
        gl10.glLoadIdentity();
        gl10.glTranslatef(tx, ty, 0.0f);
        gl10.glScalef(tw, th, 1.0f);
        gl10.glMatrixMode(GL10.GL_MODELVIEW);
    }

    private void drawQuad(GL10 gl10, int x, int y, int w, int h) {
        float sizeX = (float) w / (float) mWindowWidth * 2;
        float sizeY = (float) h / (float) mWindowHeight * 2;
        float sx = (float) x / (float) mWindowWidth * 2;
        float sy = (float) y / (float) mWindowHeight * 2;

        gl10.glLoadIdentity();
        gl10.glRotatef(mDegree, 0.0f, 0.0f, 1.0f);
        gl10.glTranslatef(-1.0f + sizeX / 2.0f + sx, 1.0f - sizeY / 2.0f - sy, 0.0f);
        gl10.glScalef(sizeX, sizeY, 1.0f );
        gl10.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, 4);
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
        Log.d("Genesis", msg);
    }

}
