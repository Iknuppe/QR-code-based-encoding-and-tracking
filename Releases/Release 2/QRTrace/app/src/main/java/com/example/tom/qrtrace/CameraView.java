package com.example.tom.qrtrace;

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
  private Context view_context;

  private SurfaceHolder surface_holder;

  private Thread view_thread = null;
  volatile boolean running = false;

  private static final String TAG = "QRT";

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
    int x_crd;
    int y_crd;

    Coordinate current_position;
    List<Square> finder_patterns;

    Paint trace_paint = new Paint(Paint.ANTI_ALIAS_FLAG);

    trace_paint.setStyle(Paint.Style.STROKE);
    trace_paint.setStrokeWidth(20);
    trace_paint.setColor(Color.RED);

    while(running)
    {
      if(surface_holder.getSurface().isValid())
      {
        Canvas view_canvas = surface_holder.lockCanvas();

        if(view_canvas != null)
        {
          view_canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

          finder_patterns = QRTrace.getFinderPatterns();

          if(finder_patterns != null)
          {
            for(int counter = 0; counter < finder_patterns.size(); counter++)
            {
              x_crd = (int) finder_patterns.get(counter).getCenter().getXcrd();
              y_crd = (int) finder_patterns.get(counter).getCenter().getYcrd();
              current_position = coordinateCorrection(x_crd, y_crd, view_canvas.getWidth(), view_canvas.getHeight());
              x_crd = (int) current_position.getXcrd();
              y_crd = (int) current_position.getYcrd();
              view_canvas.drawPoint(x_crd, y_crd, trace_paint);
            }
          }

          surface_holder.unlockCanvasAndPost(view_canvas);
        }
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

  private Coordinate coordinateCorrection(int x_crd, int y_crd, int width, int height)
  {
    int x_pos;
    int y_pos;

    switch(((WindowManager)view_context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getRotation())
    {
      case Surface.ROTATION_90:
        x_pos = x_crd;
        y_pos = y_crd;
        break;
      case Surface.ROTATION_180:
        x_pos = x_crd;
        y_pos = y_crd;
        break;
      case Surface.ROTATION_270:
        x_pos = width - x_crd;
        y_pos = height - y_crd;
        break;
      default:
        x_pos = width - y_crd;
        y_pos = x_crd;
        break;
    }

    return new Coordinate(x_pos, y_pos);
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
