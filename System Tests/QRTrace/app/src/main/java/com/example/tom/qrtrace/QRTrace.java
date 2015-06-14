package com.example.tom.qrtrace;

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
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;
import java.io.ByteArrayOutputStream;
import java.util.List;

import static com.example.tom.qrtrace.convertToQRMatrix.convertToQRMatrix;
import static com.example.tom.qrtrace.convertToQRMatrix.findSquares;
import static com.example.tom.qrtrace.convertToQRMatrix.identifyFinderPatterns;
import static com.example.tom.qrtrace.encodeMatrix.encodeMatrix;
import static com.example.tom.qrtrace.convertToQRMatrix.debugInformation;

@SuppressWarnings("deprecation")
public class QRTrace extends ActionBarActivity
{
  private Camera android_camera;

  private TextView qr_content;
  private TextView qr_information;
  private CameraPreview camera_preview;
  private CameraView camera_draw;

  private static List<Square> finder_patterns;

  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);

    ActionBar actionBar = getSupportActionBar();
    actionBar.hide();

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

    setContentView(R.layout.qr_trace_layout_main);

    qr_content = (TextView)findViewById(R.id.qr_content_view);
    qr_content.setText("Text: ?");
    qr_information = (TextView)findViewById(R.id.qr_information_view);
    qr_information.setText("Number of squares: ?");

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
      params2.width = 960;//camera_preview.getCurrentWidth();
      params2.height = 1280;//camera_preview.getCurrentHeight();
      camera_draw.setLayoutParams(params2);

      FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.FILL_PARENT);
      params.gravity = Gravity.CENTER;
      camera_preview.setLayoutParams(params);
      camera_preview_frame.addView(camera_draw);
      camera_preview_frame.addView(camera_preview);

      android_camera.setPreviewCallback(new Camera.PreviewCallback()
      {
        @Override
        public void onPreviewFrame(byte[] camera_data, Camera android_camera)
        {
          //System.out.println("============= NEW SCAN =============");
          //finder_patterns = findSquares(convertByteToBitmap(camera_data, android_camera));
          Bitmap test_image = convertByteToBitmap(camera_data, android_camera);
          String teststring = "";
          int[][] qr_int = null;
          finder_patterns = findSquares(test_image);
          int num_squares = finder_patterns.size();
          finder_patterns = identifyFinderPatterns(finder_patterns, test_image);
          if(finder_patterns != null)
          {
            //debugInformation(finder_patterns);
            try
            {
              qr_int = convertToQRMatrix(finder_patterns, test_image);
            }
            catch(Exception e)
            {

            }

            if(qr_int != null)
            {
              teststring = encodeMatrix(qr_int);
              if(!(teststring == "fail" || teststring == "format" || teststring == "mode" || teststring == "Byte" || teststring == "Kanji" || teststring == "ECI" || teststring == "AlphaFail"))
              {
                qr_content = (TextView)findViewById(R.id.qr_content_view);
                qr_content.setText("Text: "+teststring);
              }
            }
          }
          qr_information = (TextView)findViewById(R.id.qr_information_view);
          qr_information.setText("Number of squares: "+num_squares+" "+teststring);
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
  public void onConfigurationChanged(Configuration newConfig)
  {
    System.out.println("Config Changed");
    super.onConfigurationChanged(newConfig);
    camera_preview.setCorrectRotation(this);
  }

  public void setViewProps(int w, int h)
  {
    FrameLayout.LayoutParams params2 = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.FILL_PARENT);
    params2.gravity = Gravity.CENTER;
    params2.width = w;
    params2.height = h;
    camera_draw.setLayoutParams(params2);
  }
}
