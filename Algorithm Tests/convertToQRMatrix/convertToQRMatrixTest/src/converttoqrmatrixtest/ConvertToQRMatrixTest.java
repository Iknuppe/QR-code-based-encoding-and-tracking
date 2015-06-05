
package converttoqrmatrixtest;

import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;

import static converttoqrmatrixtest.convertToQRMatrix.convertToQRMatrix;
import static converttoqrmatrixtest.convertToMatrix.convertToMatrix;

public class ConvertToQRMatrixTest
{
  public static void main(String[] args) throws IOException
  {
      System.out.println("Testcases for convertToQRMatrix:");
      
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
      
      if(thirdTest())
      {
        System.out.println("Test successful.");
      }
      else
      {
        System.out.println("Test failed.");
      }
      
      if(fourthTest())
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
    int error_count = 0;
  
    System.out.println("1: Testing reading of hello-world-final");
    
    BufferedImage test_image = ImageIO.read(ConvertToQRMatrixTest.class.getResource("hello-world-final.bmp"));
    
    int[][] correct_qr_array = {
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
    
    int[][] image_array = convertToMatrix(test_image);
    int[][] qr_array = convertToQRMatrix(image_array);
    
    for(int y_counter = 0; y_counter < correct_qr_array.length; y_counter++)
    {
      for(int x_counter = 0; x_counter < correct_qr_array[0].length; x_counter++)
      {
        if(qr_array[y_counter][x_counter] != correct_qr_array[y_counter][x_counter])
        {
          error_count++;
        }
        
        System.out.print(qr_array[y_counter][x_counter] + " ");
      }
      
      System.out.println(" ");
    }
        
    if(error_count == 0)
    {
      System.out.println("No errors occured.");
      return true;
    }
    else
    {
      System.out.println("Error count: " + error_count + " occured.");
      return false;
    }
  }
  
  private static boolean secondTest() throws IOException
  {
    int error_count = 0;
    
    System.out.println("2: Testing conversion of HHQRcode.bmp");
    
    BufferedImage test_image = ImageIO.read(ConvertToQRMatrixTest.class.getResource("HHQRcode.bmp"));
    
    System.out.println("  Start convertion from bitmap to matrix.");
    int[][] image_array = convertToMatrix(test_image);
    int[][] qr_array = convertToQRMatrix(image_array);
    
    for(int y_counter = 0; y_counter < qr_array.length; y_counter++)
    {
      for(int x_counter = 0; x_counter < qr_array[0].length; x_counter++)
      {
        System.out.print(qr_array[y_counter][x_counter] + " ");
      }
      
      System.out.println(" ");
    }
        
    if(error_count == 0)
    {
      System.out.println("No errors occured.");
      return true;
    }
    else
    {
      System.out.println("Error count: " + error_count + " occured.");
      return false;
    }
  }
  
  private static boolean thirdTest() throws IOException
  {
    int error_count = 0;
    
    System.out.println("3: Testing conversion of monobmpr2.bmp");
    
    BufferedImage test_image = ImageIO.read(ConvertToQRMatrixTest.class.getResource("monobmpr2.bmp"));
    
    System.out.println("  Start convertion from bitmap to matrix.");
    int[][] image_array = convertToMatrix(test_image);
    int[][] qr_array = convertToQRMatrix(image_array);
    
    for(int y_counter = 0; y_counter < qr_array.length; y_counter++)
    {
      for(int x_counter = 0; x_counter < qr_array[0].length; x_counter++)
      {
        System.out.print(qr_array[y_counter][x_counter] + " ");
      }
      
      System.out.println(" ");
    }
        
    if(error_count == 0)
    {
      System.out.println("No errors occured.");
      return true;
    }
    else
    {
      System.out.println("Error count: " + error_count + " occured.");
      return false;
    }
  }
  
  private static boolean fourthTest() throws IOException
  {
    int error_count = 0;
    
    System.out.println("4: Testing conversion of livetest.bmp");
    
    BufferedImage test_image = ImageIO.read(ConvertToQRMatrixTest.class.getResource("livetest.bmp"));
    
    System.out.println("  Start convertion from bitmap to matrix.");
    int[][] image_array = convertToMatrix(test_image);
    int[][] qr_array = convertToQRMatrix(image_array);
    
    for(int y_counter = 0; y_counter < qr_array.length; y_counter++)
    {
      for(int x_counter = 0; x_counter < qr_array[0].length; x_counter++)
      {
        System.out.print(qr_array[y_counter][x_counter] + " ");
      }
      
      System.out.println(" ");
    }
        
    if(error_count == 0)
    {
      System.out.println("No errors occured.");
      return true;
    }
    else
    {
      System.out.println("Error count: " + error_count + " occured.");
      return false;
    }
  }
}
