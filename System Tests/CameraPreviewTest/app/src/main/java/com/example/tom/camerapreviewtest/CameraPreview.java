package com.example.tom.camerapreviewtest;

import android.content.Context;
import android.hardware.Camera;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;
import java.io.IOException;
import java.util.ArrayList;

@SuppressWarnings("deprecation")
public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback
{
  Context camera_context;

  private static final String TAG = "Penis";
  private SurfaceHolder camera_holder;
  private Camera android_camera;
  private android.view.ViewGroup.LayoutParams lp;
  private int h;
  private int w;
  private ArrayList<Integer> size;

  public CameraPreview(Context context, Camera camera)
  {
    super(context);
    camera_context = context;
    android_camera = camera;

    // Install a SurfaceHolder.Callback so we get notified when the
    // underlying surface is created and destroyed.
    camera_holder = getHolder();
    camera_holder.addCallback(this);
    // deprecated setting, but required on Android versions prior to 3.0
    camera_holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
  }

  public void surfaceCreated(SurfaceHolder holder)
  {
    // The Surface has been created, now tell the camera where to draw the preview.

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
    // empty. Take care of releasing the Camera preview in your activity.
    //mHolder.removeCallback(this);
    //mCamera.release();
  }

  public void surfaceChanged(SurfaceHolder holder, int format, int w, int h)
  {
    Log.d(TAG, "surfaceChanged");
    if (camera_holder.getSurface() == null)
    {
      // preview surface does not exist
      return;
    }

    // stop preview before making changes
    try
    {
      android_camera.stopPreview();
    }
    catch (Exception e)
    {
      // ignore: tried to stop a non-existent preview
    }

    // set preview size and make any resize, rotate or
    // reformatting changes here
    //Camera.Parameters parameters = android_camera.getParameters();
    //Camera.Size size = getBestPreviewSize(h, w);
    //parameters.setPreviewSize(size.width, size.height);
    //android_camera.setParameters(parameters);
    if (this.h == 0 && this.w == 0) {
      this.h = h;
      this.w = w;//<-
      size = setCameraParameters(h, w);
      Log.d(TAG, String.valueOf(this.h));
      Log.d(TAG, String.valueOf(this.w));
    }

    lp = this.getLayoutParams();
    lp.width = size.get(1);//960; // required width
    lp.height = size.get(0);//1280; // required height
    this.setLayoutParams(lp);
    //setCorrectRotation();

    // start preview with new settings
    try
    {
      android_camera.setPreviewDisplay(camera_holder);
      android_camera.startPreview();
    }
    catch (Exception e)
    {
      Log.d(TAG, "Error starting camera preview: " + e.getMessage());
    }
  }

  public void setCorrectRotation()
  {
    Log.d(TAG, "ROTATION!");
    switch (((WindowManager)camera_context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getRotation())
    {
      case Surface.ROTATION_90:
        Log.d(TAG, "Landscape");
        android_camera.setDisplayOrientation(0);
        lp = this.getLayoutParams();
        lp.width = size.get(0); // required width
        lp.height = size.get(1); // required height
        this.setLayoutParams(lp);
        break;
      case Surface.ROTATION_180:
        //Log.d(TAG, "Reverse Portrait");
        android_camera.setDisplayOrientation(270);
        lp = this.getLayoutParams();
        lp.width = size.get(1); // required width
        lp.height = size.get(0); // required height
        this.setLayoutParams(lp);
        break;
      case Surface.ROTATION_270:
        //Log.d(TAG, "Reverse Landscape");
        android_camera.setDisplayOrientation(180);
        lp = this.getLayoutParams();
        lp.width = size.get(0); // required width
        lp.height = size.get(1); // required height
        this.setLayoutParams(lp);
        break;
      default :
        //Log.d(TAG, "Portrait");
        android_camera.setDisplayOrientation(90);
        lp = this.getLayoutParams();
        lp.width = size.get(1); // required width
        lp.height = size.get(0); // required height
        this.setLayoutParams(lp);
    }
  }

  private ArrayList<Integer> setCameraParameters(int w, int h) {
    ArrayList<Integer> returnargs = new ArrayList<>();
    Camera.Parameters parameters = android_camera.getParameters();
    Camera.Size size = getBestPreviewSize(w, h);
    parameters.setPreviewSize(size.width, size.height);
    android_camera.setParameters(parameters);
    returnargs.add(size.width);
    returnargs.add(size.height);
    return returnargs;
  }

  private Camera.Size getBestPreviewSize(int width, int height)
  {
    Camera.Size result=null;
    Camera.Parameters p = android_camera.getParameters();
    for (Camera.Size size : p.getSupportedPreviewSizes()) {
      //Log.d(TAG, String.valueOf(size.width));
      //Log.d(TAG, String.valueOf(size.height));

      if (size.width<=width && size.height<=height) {
        if (result==null) {
          result=size;
        } else {
          int resultArea=result.width*result.height;
          int newArea=size.width*size.height;

          if (newArea>resultArea) {
            result=size;
          }
        }
      }
    }
    return result;
  }

  public int getCurrentHeight()
  {
    return h;
  }

  public int getCurrentWidth()
  {
    return w;
  }
}