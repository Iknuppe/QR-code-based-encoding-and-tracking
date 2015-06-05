
package converttomatrixtest;

import java.io.IOException;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

import static converttomatrixtest.convertToMatrix.convertToMatrix;

public class ConvertToMatrixTest
{
    public static void main(String[] args) throws IOException
    {
      System.out.println("Testcases for convertToMatrix:");
      
      if(firstTest())
      {
        System.out.println("Test successful.");
      }
      else
      {
        System.out.println("Test failed.");
      }
      
      if(secondTest())
      {
        System.out.println("Test successful.");
      }
      else
      {
        System.out.println("Test failed.");
      }
      
      System.out.println("Finished all testcases.");
    }
    
    private static boolean firstTest() throws IOException
    {
      int error_counter = 0;
      
      System.out.println("1: Testing image interpretation");
      
      System.out.println("  Loading simpledata.bmp");
      BufferedImage test_image = ImageIO.read(ConvertToMatrixTest.class.getResource("simpledata.bmp"));
      
      int[][] correct_interpretation = 
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
      
      System.out.println("  Start convertion from bitmap to matrix.");
      int[][] image_array = convertToMatrix(test_image);
      
      System.out.println("  Check interpretation:");
      for(int y_counter = 0; y_counter < correct_interpretation.length; y_counter++)
      {
        for(int x_counter = 0; x_counter < correct_interpretation[0].length; x_counter++)
        {
          if(image_array[y_counter][x_counter] != correct_interpretation[y_counter][x_counter])
          {
            error_counter++;
          }
        }
      }
      
      System.out.println("Errors: "+error_counter);
      
      if(error_counter == 0)
      {
        return true;
      }
      else
      {
        return false;
      }
    }
    
    private static boolean secondTest() throws IOException
    {
      int error_counter = 0;
      
      System.out.println("2: Testing conversion of QR code");
      
      System.out.println("  Loading hello-world-final.bmp");
      BufferedImage test_image = ImageIO.read(ConvertToMatrixTest.class.getResource("hello-world-final.bmp"));
      
      int[][] correct_interpretation = 
      {
        {0,0,0,0,0,0,0,1,1,1,1,0,1,1,0,0,0,0,0,0,0},
        {0,1,1,1,1,1,0,1,0,0,1,1,0,1,0,1,1,1,1,1,0},
        {0,1,0,0,0,1,0,1,1,0,1,0,0,1,0,1,0,0,0,1,0},
        {0,1,0,0,0,1,0,1,0,0,0,0,0,1,0,1,0,0,0,1,0},
        {0,1,0,0,0,1,0,1,0,0,1,0,1,1,0,1,0,0,0,1,0},
        {0,1,1,1,1,1,0,1,1,0,1,1,0,1,0,1,1,1,1,1,0},
        {0,0,0,0,0,0,0,1,0,1,0,1,0,1,0,0,0,0,0,0,0},
        {1,1,1,1,1,1,1,1,0,0,1,0,0,1,1,1,1,1,1,1,1},
        {0,0,1,0,0,0,0,1,0,0,1,1,0,0,0,1,0,0,1,0,1},
        {0,1,0,0,0,0,1,0,1,1,1,1,0,0,0,0,1,0,0,0,1},
        {1,1,0,1,0,1,0,0,1,1,1,0,1,1,0,0,1,1,1,1,1},
        {0,1,0,0,1,0,1,1,1,0,1,0,0,1,1,1,0,0,1,1,1},
        {0,0,1,0,0,0,0,0,0,0,0,1,0,0,0,1,0,0,0,0,0},
        {1,1,1,1,1,1,1,1,0,1,1,1,0,1,1,0,1,0,1,1,1},
        {0,0,0,0,0,0,0,1,1,0,0,1,1,0,0,1,1,0,0,0,0},
        {0,1,1,1,1,1,0,1,0,1,0,1,1,0,1,1,0,1,0,0,0},
        {0,1,0,0,0,1,0,1,0,0,1,0,1,1,0,1,1,1,0,0,0},
        {0,1,0,0,0,1,0,1,0,1,0,0,0,1,1,1,0,1,0,1,1},
        {0,1,0,0,0,1,0,1,1,0,1,1,1,1,0,1,1,1,1,0,0},
        {0,1,1,1,1,1,0,1,0,0,0,1,1,0,0,0,1,1,0,0,1},
        {0,0,0,0,0,0,0,1,1,0,1,0,1,1,1,1,1,1,1,0,1}
      };
      
      System.out.println("  Start convertion from bitmap to matrix.");
      int[][] image_array = convertToMatrix(test_image);
      
      System.out.println("  Check interpretation:");
      for(int y_counter = 0; y_counter < correct_interpretation.length; y_counter++)
      {
        for(int x_counter = 0; x_counter < correct_interpretation[0].length; x_counter++)
        {
          if(image_array[y_counter][x_counter] != correct_interpretation[y_counter][x_counter])
          {
            error_counter++;
          }
        }
      }
      
      System.out.println("Errors: "+error_counter);
      
      if(error_counter == 0)
      {
        return true;
      }
      else
      {
        return false;
      }
    }
}