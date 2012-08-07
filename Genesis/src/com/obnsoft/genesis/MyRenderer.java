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
import android.util.Log;
import android.view.SurfaceHolder;

public class MyRenderer {

    static private final int BYTES_FLOAT = 4;

    static final float QUAD_VERTICES[] = {
        -0.05f,  0.05f, 0.00f,     -0.05f, -0.05f, 0.00f,
         0.05f,  0.05f, 0.00f,      0.05f, -0.05f, 0.00f,
    };

    private Context         mContext;

    private EGL10           mEGL;
    private EGLContext      mEGLContext = null;
    private EGLDisplay      mEGLDisplay = null;
    private EGLSurface      mEGLSurface = null;
    private EGLConfig       mEGLConfig  = null;
    private GL10            mGL10       = null;
    private int             mWindowWidth = -1;
    private int             mWindowHeight = -1;

    private float mDegX = 0;
    private float mDegY = 0;
    private float mDegZ = 0;
    private float mDegR = 0;

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

        /*  Manage array  */
        ByteBuffer bb = ByteBuffer.allocateDirect(QUAD_VERTICES.length * BYTES_FLOAT);
        bb.order(ByteOrder.nativeOrder());
        FloatBuffer vbo = bb.asFloatBuffer();
        vbo.put(QUAD_VERTICES);
        vbo.position(0);
        gl10.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
        gl10.glEnableClientState(GL10.GL_VERTEX_ARRAY);

        /*  Vertex  */
        GL11 gl11 = (GL11) gl10;
        int[] buffers = new int[1];
        gl11.glGenBuffers(1, buffers, 0);
        gl11.glBindBuffer(GL11.GL_ARRAY_BUFFER, buffers[0]);
        gl11.glBufferData(GL11.GL_ARRAY_BUFFER,
                vbo.capacity() * BYTES_FLOAT, vbo, GL11.GL_STATIC_DRAW);
        gl11.glVertexPointer(3, GL10.GL_FLOAT, BYTES_FLOAT * 3, 0);
        gl11.glBindBuffer(GL11.GL_ARRAY_BUFFER, 0);
    }

    private void drawFrame(GL10 gl10) {
        mDegX += 4f;
        mDegY += 3f;
        mDegZ += 2f;
        mDegR += 1f;

        gl10.glViewport(0, 0, mWindowWidth, mWindowHeight);
        gl10.glClearColor(0f, 0f, 0f, 1f);
        gl10.glClear(GL10.GL_COLOR_BUFFER_BIT);
        //gl10.glClear(GL10.GL_ALPHA_BITS);
        //gl10.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE);

        int LEVEL = 5;
        for (int i = -LEVEL; i <= LEVEL; i++) {
            int jRange = LEVEL - Math.abs(i);
            for (int j = -jRange; j <= jRange; j++) {
                int kRange = jRange - Math.abs(j);
                for (int k = -kRange; k <= kRange; k++) {
                    drawQuad(gl10,
                            (float) i / (float) LEVEL,
                            (float) j / (float) LEVEL,
                            (float) k / (float) LEVEL,
                            (float) Math.cos(Math.toRadians(mDegR)));
                }
            }
        }
        mEGL.eglSwapBuffers(mEGLDisplay, mEGLSurface);
    }

    private void drawQuad(GL10 gl10, float x, float y, float z, float r) {
        gl10.glLoadIdentity();
        gl10.glEnable(GL10.GL_ALPHA);
        gl10.glRotatef(mDegX, 1.0f, 0.0f, 0.0f);
        gl10.glRotatef(mDegY, 0.0f, 1.0f, 0.0f);
        gl10.glRotatef(mDegZ, 0.0f, 0.0f, 1.0f);
        gl10.glColor4f((x + 1f) / 4f, (y + 1f) / 4f, (z + 1f) / 4f, 0.25f);
        gl10.glTranslatef(x * r, y * r, z * r);
        //gl10.glScalef(sizeX, sizeY, 1.0f );
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
