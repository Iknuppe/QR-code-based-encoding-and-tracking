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
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.Toast;
import java.io.ByteArrayOutputStream;
import java.util.List;

import static com.example.tom.qrtrace.convertToQRMatrix.convertToQRMatrix;
import static com.example.tom.qrtrace.convertToQRMatrix.findSquares;
import static com.example.tom.qrtrace.convertToQRMatrix.identifyFinderPatterns;
import static com.example.tom.qrtrace.encodeMatrix.encodeMatrix;

@SuppressWarnings("deprecation")
public class QRTrace extends ActionBarActivity
{
  private Camera android_camera;

  private CameraPreview camera_preview;
  private CameraView camera_draw;

  private static List<Square> finder_patterns;

  private ActionBar action_bar;
  private Toast qr_content_toast;

  private static final String TAG = "QRT";

  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);

    action_bar = getSupportActionBar();
    action_bar.hide();

    if(Build.VERSION.SDK_INT < 16)
    {
      getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }
    else
    {
      View decor_view = getWindow().getDecorView();
      int ui_options = View.SYSTEM_UI_FLAG_FULLSCREEN;
      decor_view.setSystemUiVisibility(ui_options);
    }

    setContentView(R.layout.qr_trace_layout_main);

    if(checkCameraHardware(getApplicationContext()))
    {
      android_camera = getCameraInstance();

      if(android_camera != null)
      {
        android_camera.setDisplayOrientation(90);
      }

      Camera.Parameters camera_params = android_camera.getParameters();
      camera_params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
      android_camera.setParameters(camera_params);

      camera_preview = new CameraPreview(this, android_camera);
      camera_draw = new CameraView(this);

      FrameLayout camera_preview_frame = (FrameLayout)findViewById(R.id.camera_preview);

      FrameLayout.LayoutParams camera_draw_params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.FILL_PARENT);
      camera_draw_params.gravity = Gravity.CENTER;
      camera_draw_params.width = 960;
      camera_draw_params.height = 1280;
      camera_draw.setLayoutParams(camera_draw_params);

      FrameLayout.LayoutParams camera_preview_params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.FILL_PARENT);
      camera_preview_params.gravity = Gravity.CENTER;
      camera_preview.setLayoutParams(camera_preview_params);

      camera_preview_frame.addView(camera_draw);
      camera_preview_frame.addView(camera_preview);

      camera_preview.setCameraView(camera_draw);

      android_camera.setPreviewCallback(new Camera.PreviewCallback()
      {
        @Override
        public void onPreviewFrame(byte[] camera_data, Camera android_camera)
        {
          int num_squares;
          int[][] qr_int = null;
          String qr_content = null;
          int toast_duration = Toast.LENGTH_SHORT;
          Bitmap qr_image = convertByteToBitmap(camera_data, android_camera);

          finder_patterns = findSquares(qr_image);
          num_squares = finder_patterns.size();

          finder_patterns = identifyFinderPatterns(finder_patterns);

          if(finder_patterns != null)
          {
            qr_int = convertToQRMatrix(finder_patterns, qr_image);
          }

          if(qr_int != null)
          {
            qr_content = encodeMatrix(qr_int);
          }

          if(qr_content != null && qr_content != "")
          {
            try
            {
              qr_content_toast.setText(qr_content);
            }
            catch(Exception e)
            {
              qr_content_toast = Toast.makeText(getApplicationContext(), qr_content, toast_duration);
            }

            qr_content_toast.show();
          }
        }
      });
    }
  }

  private boolean checkCameraHardware(Context context)
  {
    if(context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA))
    {
      return true;
    }
    else
    {
      return false;
    }
  }

  public static Camera getCameraInstance()
  {
    Camera android_camera = null;

    try
    {
      android_camera = android.hardware.Camera.open(0);
    }
    catch (Exception e)
    {
      Log.d(TAG, "Camera is not available (in use or does not exist):" + e.getMessage());
    }

    return android_camera;
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
    super.onConfigurationChanged(newConfig);
    camera_preview.setCorrectRotation();
  }
}
