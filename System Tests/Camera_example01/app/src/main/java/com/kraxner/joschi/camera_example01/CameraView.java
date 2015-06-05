package com.kraxner.joschi.camera_example01;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * Created by Joschi on 18.05.2015.
 */
public class CameraView extends SurfaceView implements  Runnable
{
    SurfaceHolder holder;
    boolean isItOK = false;
    Thread t = null;


    public CameraView(Context context) {
        super(context);

        holder = getHolder();

    }

    @Override
    public void run()
    {
       while(isItOK)
       {
           if(!holder.getSurface().isValid())
           {
               continue;
           }
           Canvas c = holder.lockCanvas();
           c.drawColor(Color.RED);
           holder.unlockCanvasAndPost(c);
       }

    }

    void pause()
    {
        isItOK = false;
        while(true) {
            try {
                t.join();
            }catch(InterruptedException e){

            }
            break;
        }
        t= null;
    }

    void resume()
    {
        isItOK = true;
        t = new Thread(this);
        t.start();
    }
}
