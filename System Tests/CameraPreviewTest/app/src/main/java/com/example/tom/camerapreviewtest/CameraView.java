package com.example.tom.camerapreviewtest;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;

import java.util.List;

@SuppressWarnings("deprecation")
public class CameraView extends SurfaceView implements  Runnable, SurfaceHolder.Callback
{
  Context view_context;
  Thread view_thread = null;
  SurfaceHolder surface_holder;
  volatile boolean running = false;
  List<Square> Finderpatterns;
  private android.view.ViewGroup.LayoutParams lp;

  private Paint test_paint = new Paint(Paint.ANTI_ALIAS_FLAG);

    public CameraView(Context context)
    {
      super(context);
      view_context = context;
      getHolder().addCallback(this);
      surface_holder = getHolder();
      surface_holder.setFormat(PixelFormat.TRANSPARENT);
      running = true;
      view_thread = new Thread(this);
      view_thread.start();
    }

    @Override
    public void run()
    {
      int x_crd = 0;
      int y_crd = 0;

      test_paint.setStyle(Paint.Style.STROKE);
      test_paint.setStrokeWidth(20);
      test_paint.setColor(Color.RED);

      while(running)
      {
        if(surface_holder.getSurface().isValid())
        {
          Finderpatterns = CameraPreviewTest.getFinderPatterns();

          Canvas view_canvas = surface_holder.lockCanvas();
          view_canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

          view_canvas.drawPoint(0, 0, test_paint);
          //view_canvas.drawPoint(60, 256, test_paint);
          test_paint.setColor(Color.BLUE);
          view_canvas.drawPoint(view_canvas.getWidth(), 0, test_paint);
          test_paint.setColor(Color.GREEN);
          view_canvas.drawPoint(0, view_canvas.getHeight(), test_paint);
          //view_canvas.drawPoint(view_canvas.getWidth(), view_canvas.getHeight(), test_paint);

          test_paint.setColor(Color.RED);

          if(Finderpatterns != null)
          {
            for(int counter = 0 ; counter < Finderpatterns.size() ; counter++)
            {
              switch(((WindowManager)view_context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getRotation())
              {
                case Surface.ROTATION_90:
                  x_crd = (int)Finderpatterns.get(counter).getCenter().getXcrd();
                  y_crd = (int)Finderpatterns.get(counter).getCenter().getYcrd();
                  break;
                case Surface.ROTATION_180:
                  //x_crd = ;
                  //y_crd = ;
                  break;
                case Surface.ROTATION_270:
                  x_crd = view_canvas.getWidth() - (int)Finderpatterns.get(counter).getCenter().getXcrd();
                  y_crd = view_canvas.getHeight() - (int)Finderpatterns.get(counter).getCenter().getYcrd();
                  break;
                default:
                  x_crd = view_canvas.getWidth() - (int)Finderpatterns.get(counter).getCenter().getYcrd();
                  y_crd = (int)Finderpatterns.get(counter).getCenter().getXcrd();

              }
              view_canvas.drawPoint(x_crd, y_crd, test_paint);
            }
          }

          surface_holder.unlockCanvasAndPost(view_canvas);
        }
      }
    }

    public void onPauseCameraView()
    {
      boolean retry = true;
      running = false;

      while(retry)
      {
        try
        {
          view_thread.join();
          retry = false;
        }
        catch(InterruptedException e)
        {

        }
        break;
      }

      view_thread= null;
    }

    public void onResumeCameraView()
    {
      running = true;
      view_thread = new Thread(this);
      view_thread.start();
    }

  @Override
  public void surfaceCreated(SurfaceHolder holder)
  {

  }

  @Override
  public void surfaceChanged(SurfaceHolder holder, int format, int width, int height)
  {

  }

  @Override
  public void surfaceDestroyed(SurfaceHolder holder)
  {

  }
}
