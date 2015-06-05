package at.club27.merkus.qr_code_test;

import android.graphics.Color;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.app.ListActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ArrayAdapter;
import static at.club27.merkus.qr_code_test.convertToQRMatrix.convertToQRMatrix;
import static at.club27.merkus.qr_code_test.encodeMatrix.encodeMatrix;

//import java.awt.image.BufferedImage;
//import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
//import javax.imageio.ImageIO;



public class QrMainActivity extends ActionBarActivity {

    ArrayList<String> listItems=new ArrayList<String>();
    ArrayAdapter<String> adapter;

    ListView theFact;
    String shareFact = "Initialized";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_main);
        theFact = (ListView) findViewById(R.id.listViewError);

        adapter=new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1,
                listItems);

        theFact.setAdapter(adapter);



        listItems.add(shareFact);
        adapter.notifyDataSetChanged();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_qr_main, menu);
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
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void buttonOnClick(View v)
    {
        //BufferedImage test_image = ImageIO.read(ConvertToMatrixTest.class.getResource("simpledata.bmp"));
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;
        //options.inDensity = 0;
        Bitmap mbitmap = (Bitmap)BitmapFactory.decodeResource(getResources(),R.drawable.helloworld, options);
        listItems.add("Höhe ist "+ mbitmap.getHeight());
        listItems.add("Breite ist " + mbitmap.getWidth());
        int Pixel = mbitmap.getPixel(36,36);
        if (Pixel == Color.BLACK)
        {
            listItems.add("ist Schwarz");
        }
        else
        {
            listItems.add("nicht Schwarz");
        }
        /*int redValue = Color.red(Pixel);
        int blueValue = Color.blue(Pixel);
        int greenValue = Color.green(Pixel);
        listItems.add("Rotwert ist "+ redValue);
        listItems.add("Bluewert ist "+ blueValue);
        listItems.add("Greenwert ist "+ greenValue);
        */
        adapter.notifyDataSetChanged();;
        //BufferedImage test_image = ImageIO.read(ConvertToMatrixTest.class.getResource("encode.bmp"));
        //BufferedImage test_image = ImageIO.read(ConvertToMatrixTest.class.getResource("encode2.bmp"));
        //BufferedImage test_image = ImageIO.read(ConvertToMatrixTest.class.getResource("encode3.bmp"));
        //BufferedImage test_image = ImageIO.read(ConvertToMatrixTest.class.getResource("encodeNumv1.bmp"));
        //BufferedImage test_image = ImageIO.read(ConvertToMatrixTest.class.getResource("encodeNumv1s.bmp"));
        //BufferedImage test_image = ImageIO.read(ConvertToMatrixTest.class.getResource("encodeQRNumv2.bmp"));
        //BufferedImage test_image = ImageIO.read(ConvertToMatrixTest.class.getResource("encodeQRv3.bmp"));
        //BufferedImage test_image = ImageIO.read(ConvertToMatrixTest.class.getResource("hello-world-final.bmp"));
        //BufferedImage test_image = ImageIO.read(ConvertToMatrixTest.class.getResource("HHQRcode.bmp"));
        //BufferedImage test_image = ImageIO.read(ConvertToMatrixTest.class.getResource("livetest.bmp")); //error
        //BufferedImage test_image = ImageIO.read(ConvertToMatrixTest.class.getResource("monobmpr.bmp"));
        //BufferedImage test_image = ImageIO.read(ConvertToMatrixTest.class.getResource("monobmpr2.bmp"));
        //BufferedImage test_image = ImageIO.read(ConvertToMatrixTest.class.getResource("Qr2.bmp"));
        //BufferedImage test_image = ImageIO.read(ConvertToMatrixTest.class.getResource("QR2Content.bmp"));
        //BufferedImage test_image = ImageIO.read(ConvertToMatrixTest.class.getResource("QRCode.bmp"));

        int error_counter = 0;
        int[][] image_array;// = convertToArray(test_image);
        int[][] reference_array =
                {
                        {0,1,1,1,1,1,1,0,1,1},
                        {1,0,1,1,1,1,0,1,0,1},
                        {1,1,0,1,1,1,1,1,1,1},
                        {1,1,1,0,0,0,0,0,0,0},
                        {1,1,0,1,1,0,1,1,1,1},
                        {1,1,0,1,1,1,1,1,1,1},
                        {1,0,1,1,1,0,1,1,1,1},
                        {1,0,1,1,1,1,1,1,1,1},
                        {0,1,1,1,1,0,1,1,1,1},
                        {0,1,1,1,1,1,1,1,1,1}
                };
        System.out.println("Testing convertToArray...");

        System.out.println("Generating array...");
        int[][] qr_array = convertToQRMatrix(mbitmap);

        //Check if conversion was successful
        int error_count = 0;

        for(int y_counter = 0; y_counter < qr_array.length; y_counter++)
        {
            for(int x_counter = 0; x_counter < qr_array[0].length; x_counter++)
            {
                System.out.print(qr_array[y_counter][x_counter] + " ");
            }

            System.out.println(" ");
        }

        listItems.add("QR: " + encodeMatrix(qr_array));

        /*
        for(int y_counter = 0; y_counter < image_array.length; y_counter++)
        {
            for(int x_counter = 0; x_counter < image_array[0].length; x_counter++)
            {
                if(image_array[y_counter][x_counter] != reference_array[y_counter][x_counter])
                {
                    error_counter++;
                }
            }
        }
        */

        //System.out.println("Errors: "+error_counter);
        //ByteBuffer byteBuffer = ByteBuffer.Allocate(bitmap.ByteCount);
        //bitmap.CopyPixelsToBuffer(byteBuffer);
        //byte[] bytes = byteBuffer.ToArray<byte>();

        /*
        for(int y_counter = 0; y_counter < image_array.length; y_counter++)
        {
          for(int x_counter = 0; x_counter < image_array[0].length; x_counter++)
          {
            System.out.print(image_array[y_counter][x_counter] + " ");
          }
          System.out.println(" ");
        }
        */
    }
}
