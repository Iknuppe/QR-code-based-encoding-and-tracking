package com.example.tom.camerapreviewtest;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.os.Build;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;
import java.io.ByteArrayOutputStream;
import java.util.List;

import static com.example.tom.camerapreviewtest.convertToQRMatrix.convertToQRMatrix;
import static com.example.tom.camerapreviewtest.convertToQRMatrix.findSquares;
import static com.example.tom.camerapreviewtest.convertToQRMatrix.identifyFinderPatterns;

@SuppressWarnings("deprecation")
public class CameraPreviewTest extends ActionBarActivity
{
  private Camera android_camera;

  private TextView my_text_view;
  private CameraPreview camera_preview;
  private CameraView camera_draw;

  private static List<Square> finder_patterns;

  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);

    // Hide the status bar.
    if(Build.VERSION.SDK_INT < 16)
    {
      getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }
    else
    {
      View decorView = getWindow().getDecorView();
      int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
      decorView.setSystemUiVisibility(uiOptions);
    }

    setContentView(R.layout.activity_camera_preview_test);

    my_text_view = (TextView)findViewById(R.id.qr_text_view);
    my_text_view.setText("Number of squares: ?");

    if(checkCameraHardware(getApplicationContext()))
    {
      // Create an instance of Camera
      android_camera = getCameraInstance();

      if(android_camera != null)
      {
        android_camera.setDisplayOrientation(90);
      }

      // Create our Preview view and set it as the content of our activity.
      camera_preview = new CameraPreview(this, android_camera);
      camera_draw = new CameraView(this);
      FrameLayout camera_preview_frame = (FrameLayout)findViewById(R.id.camera_preview);
      FrameLayout.LayoutParams params2 = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.FILL_PARENT);
      params2.gravity = Gravity.CENTER;
      params2.width = 960; ////<- camera_preview.getCurrentWidth();
      params2.height = 1280; //camera_preview.getCurrentHeight();
      /*while(camera_preview.getCurrentWidth() != 0 && camera_preview.getCurrentHeight() != 0)
      {
        params2.width = camera_preview.getCurrentWidth();
        params2.height = camera_preview.getCurrentHeight();
      }*/
      System.out.println("swag: "+camera_preview.getCurrentWidth()+" "+camera_preview.getCurrentHeight());
      camera_draw.setLayoutParams(params2);
      FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.FILL_PARENT);
      params.gravity = Gravity.CENTER;
      camera_preview.setLayoutParams(params);
      camera_preview_frame.addView(camera_draw);
      camera_preview_frame.addView(camera_preview);

      //int new_width=0, new_height=0;
      //double ratio=(double)mPreviewSize.width/mPreviewSize.height;
      //mPreview.getHeight()

      /*if((double)preview.getWidth()/preview.getHeight()<ratio)
      {
        new_width=(int)(Math.round(preview.getHeight()*ratio));
        new_height=getWindowManager().getDefaultDisplay().getHeight();
      }
      else
      {
        new_width=getWindowManager().getDefaultDisplay().getHeight();
        new_height=(int)Math.round((double)new_width/ratio);
      }*/
      //preview.setLayoutParams(new FrameLayout.LayoutParams(new_width, new_height));

      android_camera.setPreviewCallback(new Camera.PreviewCallback()
      {
        @Override
        public void onPreviewFrame(byte[] camera_data, Camera android_camera)
        {
          finder_patterns = findSquares(convertByteToBitmap(camera_data, android_camera));
          my_text_view = (TextView)findViewById(R.id.qr_text_view);
          my_text_view.setText("Number of squares: "+finder_patterns.size());
        }
      });
    }
  }

  // Check if this device has a camera
  private boolean checkCameraHardware(Context context)
  {
    if(context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA))
    {
      // this device has a camera
      return true;
    }
    else
    {
      // no camera on this device
      return false;
    }
  }

  // A safe way to get an instance of the Camera object.
  public static Camera getCameraInstance()
  {
    Camera android_camera = null;
    try
    {
      //android_camera = Camera.open(0); // attempt to get a Camera instance
      android_camera = android.hardware.Camera.open(0);
    }
    catch (Exception e)
    {
      // Camera is not available (in use or does not exist)
    }
    return android_camera; // returns null if camera is unavailable
  }

  public static List<Square> getFinderPatterns()
  {
    return finder_patterns;
  }

  private static Bitmap convertByteToBitmap(byte[] camera_data, Camera android_camera)
  {
    Camera.Parameters parameters = android_camera.getParameters();
    Camera.Size size = parameters.getPreviewSize();

    YuvImage yuvImage = new YuvImage(camera_data, ImageFormat.NV21, size.width, size.height, null);

    Rect rectangle = new Rect();
    rectangle.top = 0;
    rectangle.bottom = size.height;
    rectangle.left = 0;
    rectangle.right = size.width;

    ByteArrayOutputStream baos = new ByteArrayOutputStream(); yuvImage.compressToJpeg(rectangle, 100, baos);
    byte[] imageData = baos.toByteArray();

    return BitmapFactory.decodeByteArray(imageData, 0, imageData.length);
  }

  @Override
  public void onConfigurationChanged(Configuration newConfig) {

    super.onConfigurationChanged(newConfig);
    camera_preview.setCorrectRotation();
  }
}
