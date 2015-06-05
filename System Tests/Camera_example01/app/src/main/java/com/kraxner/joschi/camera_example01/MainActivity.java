package com.kraxner.joschi.camera_example01;

import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.camera2.CameraManager;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;


public class MainActivity extends ActionBarActivity {

    private static final String TAG = "Exception";
    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;


    Button button_detect_camera;
    Button button_show_surfaceview;

    TextView textview_debug;
    CameraView camera_view;


    Camera camera;
    private CameraPreview mPreview;
    CameraManager cameraManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeView();


        //cameraManager = (CameraManager)getSystemService(Context.CAMERA_SERVICE);

        if(checkCameraHardware(getApplicationContext()))
        {
            // Create an instance of Camera
            camera = getCameraInstance();

            // Create our Preview view and set it as the content of our activity.
            mPreview = new CameraPreview(this, camera);
            FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
            preview.addView(mPreview);
        }

        camera_view = new CameraView(this);


    }

    @Override
    protected void onPause() {
        super.onPause();
        releaseCamera();    // release the camera for other applications
        camera_view.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        camera_view.resume();
    }

    private void initializeView()
    {
        textview_debug = (TextView)findViewById(R.id.textView_debug);

        button_detect_camera = (Button)findViewById(R.id.button_detect_camera);
        button_show_surfaceview = (Button)findViewById(R.id.button_show_surfaceview);

        setButtonListener();

    }

    private void setButtonListener()
    {


        button_show_surfaceview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //textview_debug.setText("Show SurfaceView");
                //setContentView(camera_view);
                camera.takePicture(null, null, mPicture);
                // Create our Preview view and set it as the content of our activity.

            }
        });

        button_detect_camera.setOnClickListener(new View.OnClickListener(){

            boolean button_detect_camera_clicked = true;
            @Override
            public void onClick(View v) {

                if(checkCameraHardware(getApplicationContext()))
                {
                    if(button_detect_camera_clicked)
                    {
                        textview_debug.setText("TORCH ON");
                        Camera.Parameters parameters = camera.getParameters();
                        parameters.setFlashMode(Parameters.FLASH_MODE_TORCH);

                        camera.setParameters(parameters);
                        camera.startPreview();
                        button_detect_camera_clicked = false;
                    }
                    else
                    {
                        textview_debug.setText("TORCH OFF");
                        Camera.Parameters parameters = camera.getParameters();
                        parameters.setFlashMode(Parameters.FLASH_MODE_OFF);

                        camera.setParameters(parameters);
                        camera.startPreview();
                        button_detect_camera_clicked = true;
                    }
                }
            }
        });
    }

    /** Check if this device has a camera */
    private boolean checkCameraHardware(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
            // this device has a camera
            return true;
        } else {
            // no camera on this device
            return false;
        }
    }

    /** A safe way to get an instance of the Camera object. */
    public static Camera getCameraInstance(){
        Camera c = null;
        try {
            c = Camera.open(0); // attempt to get a Camera instance
        }
        catch (Exception e){
            // Camera is not available (in use or does not exist)
        }
        return c; // returns null if camera is unavailable
    }

    private void releaseCamera()
    {
        if(camera != null)
        {
            camera.release();
            camera = null;
        }
    }

    private Camera.PictureCallback mPicture = new Camera.PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {

            File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
            textview_debug.setText(pictureFile.getPath());


            if (pictureFile == null){
                Log.d(TAG, "Error creating media file, check storage permissions:");
                return;
            }

            try {
                FileOutputStream fos = new FileOutputStream(pictureFile);
                fos.write(data);
                fos.close();
            } catch (FileNotFoundException e) {
                Log.d(TAG, "File not found: " + e.getMessage());
            } catch (IOException e) {
                Log.d(TAG, "Error accessing file: " + e.getMessage());
            }

        }
    };

    /** Create a File for saving an image or video */
    private static File getOutputMediaFile(int type){
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "MyCameraApp");
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                Log.d("MyCameraApp", "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE){
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "KRAXNER_IMG_"+ timeStamp + ".jpg");
        } else if(type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "VID_"+ timeStamp + ".mp4");
        } else {
            return null;
        }

        return mediaFile;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            textview_debug.setText("ACTION_SETTINGS");
            return true;
        }
        else if(id == R.id.action_refresh) {
            textview_debug.setText("ACTION_REFRESH");
        }

        return super.onOptionsItemSelected(item);
    }
}
