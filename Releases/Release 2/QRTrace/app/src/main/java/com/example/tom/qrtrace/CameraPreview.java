package com.example.tom.qrtrace;

import android.content.Context;
import android.hardware.Camera;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("deprecation")
public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback
{
  private Context camera_context;
  
  private CameraView camera_draw;
  private SurfaceHolder camera_holder;
  private Camera android_camera;

  private android.view.ViewGroup.LayoutParams layout_params;
  private int h;
  private int w;
  private List<Integer> dimensions;

  private static final int DIM_WIDTH = 1;
  private static final int DIM_HEIGHT = 0;

  private static final String TAG = "QRT";

  public CameraPreview(Context context, Camera camera)
  {
    super(context);
    camera_context = context;
    android_camera = camera;

    camera_holder = getHolder();
    camera_holder.addCallback(this);
    camera_holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
  }

  public void surfaceCreated(SurfaceHolder holder)
  {
    try
    {
     if(holder != null && android_camera != null)
     {
       android_camera.setPreviewDisplay(holder);
       android_camera.startPreview();
     }
    }
    catch (IOException e)
    {
      Log.d(TAG, "Error setting camera preview: " + e.getMessage());
    }
  }

  public void surfaceDestroyed(SurfaceHolder holder)
  {

  }

  public void surfaceChanged(SurfaceHolder holder, int format, int w, int h)
  {
    if (camera_holder.getSurface() == null)
    {
      return;
    }

    try
    {
      android_camera.stopPreview();
    }
    catch (Exception e)
    {
      Log.d(TAG, "Error: tried to stop a non-existent preview: " + e.getMessage());
    }

    if (this.h == 0 && this.w == 0)
    {
      this.h = h;
      this.w = w;

      dimensions = setCameraParameters(h, w);
    }

    layout_params = this.getLayoutParams();
    layout_params.width = dimensions.get(DIM_WIDTH);
    layout_params.height = dimensions.get(DIM_HEIGHT);
    this.setLayoutParams(layout_params);

    try
    {
      android_camera.setPreviewDisplay(camera_holder);
      android_camera.startPreview();
    }
    catch(Exception e)
    {
      Log.d(TAG, "Error starting camera preview: " + e.getMessage());
    }
  }

  public void setCorrectRotation()
  {
    switch(((WindowManager)camera_context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getRotation())
    {
      case Surface.ROTATION_90:
      {
        android_camera.setDisplayOrientation(0);

        layout_params = this.getLayoutParams();
        layout_params.width = dimensions.get(DIM_HEIGHT);
        layout_params.height = dimensions.get(DIM_WIDTH);

        this.setLayoutParams(layout_params);

        if(camera_draw != null)
        {
          layout_params = camera_draw.getLayoutParams();
          layout_params.width = dimensions.get(DIM_HEIGHT);
          layout_params.height = dimensions.get(DIM_WIDTH);

          camera_draw.setLayoutParams(layout_params);
        }

        break;
      }

      case Surface.ROTATION_180:
      {
        android_camera.setDisplayOrientation(270);

        layout_params = this.getLayoutParams();
        layout_params.width = dimensions.get(DIM_WIDTH);
        layout_params.height = dimensions.get(DIM_HEIGHT);

        this.setLayoutParams(layout_params);

        if(camera_draw != null)
        {
          layout_params = camera_draw.getLayoutParams();
          layout_params.width = dimensions.get(DIM_WIDTH);
          layout_params.height = dimensions.get(DIM_HEIGHT);

          camera_draw.setLayoutParams(layout_params);
        }

        break;
      }

      case Surface.ROTATION_270:
      {
        android_camera.setDisplayOrientation(180);

        layout_params = this.getLayoutParams();
        layout_params.width = dimensions.get(DIM_HEIGHT);
        layout_params.height = dimensions.get(DIM_WIDTH);

        this.setLayoutParams(layout_params);

        if(camera_draw != null)
        {
          layout_params = camera_draw.getLayoutParams();
          layout_params.width = dimensions.get(DIM_HEIGHT);
          layout_params.height = dimensions.get(DIM_WIDTH);

          camera_draw.setLayoutParams(layout_params);
        }

        break;
      }

      default :
      {
        android_camera.setDisplayOrientation(90);

        layout_params = this.getLayoutParams();
        layout_params.width = dimensions.get(DIM_WIDTH);
        layout_params.height = dimensions.get(DIM_HEIGHT);

        this.setLayoutParams(layout_params);

        if(camera_draw != null)
        {
          layout_params = camera_draw.getLayoutParams();
          layout_params.width = dimensions.get(DIM_WIDTH);
          layout_params.height = dimensions.get(DIM_HEIGHT);

          camera_draw.setLayoutParams(layout_params);
        }
      }
    }
  }

  private List<Integer> setCameraParameters(int w, int h)
  {
    List<Integer> new_dimension = new ArrayList<>();
    Camera.Parameters parameters = android_camera.getParameters();
    Camera.Size size = getBestPreviewSize(w, h);

    parameters.setPreviewSize(size.width, size.height);
    android_camera.setParameters(parameters);

    new_dimension.add(size.width);
    new_dimension.add(size.height);

    return new_dimension;
  }

  private Camera.Size getBestPreviewSize(int width, int height)
  {
    int result_area;
    int new_area;

    Camera.Size result=null;
    Camera.Parameters p = android_camera.getParameters();

    for(Camera.Size size : p.getSupportedPreviewSizes())
    {
      if(size.width <= width && size.height <= height)
      {
        if(result == null)
        {
          result = size;
        }
        else
        {
          result_area = result.width*result.height;
          new_area = size.width*size.height;

          if(new_area > result_area)
          {
            result = size;
          }
        }
      }
    }

    return result;
  }
  
  public void setCameraView(CameraView camera_draw)
  {
	  this.camera_draw = camera_draw;
  }

  public List<Integer> getPreviewDimensions()
  {
    return dimensions;
  }
}