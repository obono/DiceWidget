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

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLUtils;
import android.service.wallpaper.WallpaperService;
import android.util.Log;
import android.view.SurfaceHolder;

public class MyWallpaperService extends WallpaperService {

    @Override
    public Engine onCreateEngine( )
    {
        return new GLEngine( );
    }

    public void log( String msg )
    {
        Log.d( "GLES_SAMPLE", msg );
    }

    public class GLEngineRender
    {

        int vertexBufferObject = 0;

        int textureID          = 0;
        int textureHeight      = 0;
        int textureWidth       = 0;

        int windowWidth        = 0;
        int windowHeight       = 0;

        float degree = 0;

        public int getWindowWidth( )
        {
            return windowWidth;
        }

        public int getWindowHeight( )
        {
            return windowHeight;
        }

        public void onSurfaceChanged( GL10 gl10, int width, int height )
        {
            windowWidth = width;
            windowHeight = height;

            // ! 事前に転送済みにしておく
            {
                float vertices[] =
                {
                        // !  x       y    z     u       v
                        -0.5f,
                        0.5f,
                        0.0f,
                        0.0f,
                        0.0f, // !< 左上
                        -0.5f,
                        -0.5f,
                        0.0f,
                        0.0f,
                        1.0f, // !< 左下
                        0.5f,
                        0.5f,
                        0.0f,
                        1.0f,
                        0.0f, // !< 右上
                        0.5f,
                        -0.5f,
                        0.0f,
                        1.0f,
                        1.0f, // !< 右下
                };

                ByteBuffer bb = ByteBuffer.allocateDirect( vertices.length * 4 );
                bb.order( ByteOrder.nativeOrder( ) );
                FloatBuffer vbo = bb.asFloatBuffer( );
                vbo.put( vertices );
                vbo.position( 0 );

                gl10.glEnableClientState( GL10.GL_TEXTURE_COORD_ARRAY );
                gl10.glEnableClientState( GL10.GL_VERTEX_ARRAY );

                GL11 gl11 = ( GL11 ) gl10;
                //! 頂点オブジェクト作成
                {
                    int[] buffers = new int[ 1 ];
                    gl11.glGenBuffers( 1, buffers, 0 );
                    vertexBufferObject = buffers[ 0 ];
                    gl11.glBindBuffer( GL11.GL_ARRAY_BUFFER, vertexBufferObject );
                    gl11.glBufferData( GL11.GL_ARRAY_BUFFER, vbo.capacity( ) * 4, vbo, GL11.GL_STATIC_DRAW );
                }
                {
                    gl11.glVertexPointer( 3, GL10.GL_FLOAT, 4 * 5, 0 );
                    gl11.glTexCoordPointer( 2, GL10.GL_FLOAT, 4 * 5, 4 * 3 );
                }
                gl11.glBindBuffer( GL11.GL_ARRAY_BUFFER, 0 );
            }
            //! texture
            /*
            */
            {
                Bitmap bitmap = BitmapFactory.decodeResource( getResources( ), R.drawable.image_512 );

                gl10.glEnable( GL10.GL_TEXTURE_2D );
                int[] buffer = new int[ 1 ];
                gl10.glGenTextures( 1, buffer, 0 );
                textureID = buffer[ 0 ];
                textureWidth = bitmap.getWidth( );
                textureHeight = bitmap.getHeight( );

                gl10.glBindTexture( GL10.GL_TEXTURE_2D, textureID );
                GLUtils.texImage2D( GL10.GL_TEXTURE_2D, 0, bitmap, 0 );
                gl10.glTexParameterf( GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_NEAREST );
                gl10.glTexParameterf( GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_NEAREST );

                // ! bitmapを破棄
                bitmap.recycle( );
            }
        }

        /**
         * 四角形を描画する。
         *
         * @param gl10
         * @param x
         * @param y
         * @param w
         * @param h
         */
        public void drawQuad( GL10 gl10, int x, int y, int w, int h )
        {
            // ! 描画位置を行列で操作する
            float sizeX = ( float ) w / ( float ) getWindowWidth( ) * 2;
            float sizeY = ( float ) h / ( float ) getWindowHeight( ) * 2;
            float sx = ( float ) x / ( float ) getWindowWidth( ) * 2;
            float sy = ( float ) y / ( float ) getWindowHeight( ) * 2;

            gl10.glLoadIdentity( );
            degree += 1f;
            gl10.glRotatef(degree, 0.0f, 0.0f, 1.0f);
            gl10.glTranslatef( -1.0f + sizeX / 2.0f + sx, 1.0f - sizeY / 2.0f - sy, 0.0f );
            gl10.glScalef( sizeX, sizeY, 1.0f );
            gl10.glDrawArrays( GL10.GL_TRIANGLE_STRIP, 0, 4 );
        }

        /**
         *
         * @param gl10
         * @param x
         * @param y
         * @param w
         * @param h
         */
        private void setTextureArea( GL10 gl10, int x, int y, int w, int h )
        {
            float tw = ( float ) w / ( float ) textureWidth;
            float th = ( float ) h / ( float ) textureHeight;
            float tx = ( float ) x / ( float ) textureWidth;
            float ty = ( float ) y / ( float ) textureHeight;

            gl10.glMatrixMode( GL10.GL_TEXTURE );
            gl10.glLoadIdentity( );
            gl10.glTranslatef( tx, ty, 0.0f );
            gl10.glScalef( tw, th, 1.0f );
            gl10.glMatrixMode( GL10.GL_MODELVIEW );
        }

        public void onDrawFrame( GL10 gl10 )
        {
            gl10.glClearColor( 0.5f, 0.5f, 0.5f, 1 );
            gl10.glClear( GL10.GL_COLOR_BUFFER_BIT );

            setTextureArea( gl10, 0, 0, 512, 512 );
            drawQuad( gl10, 0, 0, 256, 256 );
        }
    }

    public class GLEngineSurface extends Thread
    {
        public GLEngineSurface( SurfaceHolder holder )
        {
            this.holder = holder;
        }

        private boolean       destroy    = false;
        private boolean       pause      = false;

        /**
         * 描画対象のholder。
         */
        private SurfaceHolder holder;

        /**
         * EGLインターフェース。
         */
        private EGL10         egl;

        /**
         * GLコンテキスト。
         */
        private EGLContext    eglContext = null;
        /**
         * ディスプレイ。
         */
        private EGLDisplay    eglDisplay = null;
        /**
         * サーフェイス。
         */
        private EGLSurface    eglSurface = null;

        /**
         * コンフィグ情報。
         */
        private EGLConfig     eglConfig  = null;

        /**
         * GL用インターフェース。
         */
        protected GL10        gl10       = null;

        /**
         * 描画先サーフェイスの幅・高さ
         */
        private int           windowWidth = -1, windowHeight = -1;

        /**
         * GLの開始処理を行う。
         */
        private void initialize( )
        {
            egl = ( EGL10 ) EGLContext.getEGL( );

            //! 描画先ディスプレイ確保
            eglDisplay = egl.eglGetDisplay( EGL10.EGL_DEFAULT_DISPLAY );

            //! EGL初期化。
            //! ここでGLESのバージョンを取得できるが、ES1.0が必ず帰ってくるようである。
            {
                int[] version =
                {
                        -1, -1
                };
                if( !egl.eglInitialize( eglDisplay, version ) )
                {
                    log( "!eglInitialize" );
                    return;
                }
            }

            //! コンフィグ取得
            {
                EGLConfig[] configs = new EGLConfig[ 1 ];
                int[] num = new int[ 1 ];

                //! この配列でGLの性能を指定する。
                //! ディスプレイの色深度、Z深度もここで指定するが、
                //! 基本的に2D描画する場合はデフォルトのままでも特に問題ない。
                //! specに対応していない値を入れても初期化が失敗する。
                int[] spec =
                {
                    EGL10.EGL_NONE
                //! 終端にはEGL_NONEを入れる
                };
                if( !egl.eglChooseConfig( eglDisplay, spec, configs, 1, num ) )
                {
                    log( "!eglChooseConfig" );
                    return;
                }

                eglConfig = configs[ 0 ];
            }

            //! レンダリングコンテキスト作成
            {
                //レンダリングコンテキスト作成
                eglContext = egl.eglCreateContext( eglDisplay, eglConfig, EGL10.EGL_NO_CONTEXT, null );
                if( eglContext == EGL10.EGL_NO_CONTEXT )
                {
                    log( "glContext == EGL10.EGL_NO_CONTEXT" );
                    return;
                }
            }
            //! 描画先サーフェイスを作成する
            {
                //! SurfaceHolderに結びつける
                egl.eglMakeCurrent( eglDisplay, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_CONTEXT );
                eglSurface = egl.eglCreateWindowSurface( eglDisplay, eglConfig, holder, null );

                if( eglSurface == EGL10.EGL_NO_SURFACE )
                {
                    log( "glSurface == EGL10.EGL_NO_SURFACE" );
                    return;
                }
            }

            //! GLESインターフェース取得
            {
                gl10 = ( GL10 ) eglContext.getGL( );
            }

            //! サーフェイスとコンテキストを結びつける
            {
                if( !egl.eglMakeCurrent( eglDisplay, eglSurface, eglSurface, eglContext ) )
                {
                    log( "!eglMakeCurrent" );
                    return;
                }
            }
        }

        /**
         * GLの終了処理を行う。
         */
        private void dispose( )
        {
            //サーフェイス破棄
            if( eglSurface != null )
            {
                //レンダリングコンテキストとの結びつけは解除

                /**
                 * デフォルトに戻す動作をしないと復旧できない
                 */
                egl.eglMakeCurrent( eglDisplay, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_CONTEXT );
                egl.eglDestroySurface( eglDisplay, eglSurface );
                eglSurface = null;
            }

            //レンダリングコンテキスト破棄
            if( eglContext != null )
            {
                egl.eglDestroyContext( eglDisplay, eglContext );
                eglContext = null;
            }

            //ディスプレイコネクション破棄
            if( eglDisplay != null )
            {
                egl.eglTerminate( eglDisplay );
                eglDisplay = null;
            }
        }

        @Override
        public void run( )
        {
            GLEngineRender render = new GLEngineRender( );
            initialize( );
            render.onSurfaceChanged( gl10, windowWidth, windowHeight );
            while ( !destroy )
            {
                if( !pause )
                {
                    render.windowHeight = windowHeight;
                    render.windowWidth = windowWidth;
                    gl10.glViewport( 0, 0, windowWidth, windowHeight );
                    render.onDrawFrame( gl10 );
                    egl.eglSwapBuffers( eglDisplay, eglSurface );
                }
                else
                {
                    try
                    {
                        Thread.sleep( 100 );
                    }
                    catch( Exception e )
                    {

                    }
                }
            }
            dispose( );
        }

        public void onPause( )
        {
            pause = true;
        }

        public void onResume( )
        {
            pause = false;
        }

        /**
         * スレッドを止めて処理を終了する。
         */
        public void onDestroy( )
        {
            synchronized( this )
            {
                //終了要求を出す
                destroy = true;
            }

            try
            {
                //スレッド終了を待つ
                join( );
            }
            catch( InterruptedException ex )
            {
                Thread.currentThread( ).interrupt( );
            }
        }

    }

    /**
     *
     */
    public class GLEngine extends Engine
    {
        private GLEngineSurface gl = null;

        /**
         * サーフェイスが作成された。
         *
         * @param surfaceHolder
         */
        @Override
        public void onCreate( SurfaceHolder surfaceHolder )
        {
            super.onCreate( surfaceHolder );
            surfaceHolder.setType( SurfaceHolder.SURFACE_TYPE_GPU );

        }

        /**
         * 可視・不可視が変更された。
         *
         * @param visible
         */
        @Override
        public void onVisibilityChanged( boolean visible )
        {
            log( "onVisibilityChanged : " + visible );
            super.onVisibilityChanged( visible );

            if( visible )
            {
                gl.onResume( );
            }
            else
            {
                gl.onPause( );
            }
        }

        @Override
        public void onSurfaceCreated( SurfaceHolder holder )
        {
            super.onSurfaceCreated( holder );
            gl = new GLEngineSurface( getSurfaceHolder( ) );
            gl.start( );
        }

        @Override
        public void onSurfaceChanged( SurfaceHolder holder, int format, int width, int height )
        {
            log( "onSurfaceChanged" );
            log( "" + width + " x " + height );
            super.onSurfaceChanged( holder, format, width, height );

            gl.windowWidth = width;
            gl.windowHeight = height;
        }

        @Override
        public void onSurfaceDestroyed( SurfaceHolder holder )
        {
            log( "onSurfaceDestroyed" );
            super.onSurfaceDestroyed( holder );

            if( gl != null )
            {
                gl.onDestroy( );
                gl = null;
            }
        }
    }

}
