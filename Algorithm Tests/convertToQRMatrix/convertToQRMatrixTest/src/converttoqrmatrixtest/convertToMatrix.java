
package converttoqrmatrixtest;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;

public class convertToMatrix
{
  public static int[][] convertToMatrix(BufferedImage image)
  {
    if(image == null || image.getWidth() == 0 || image.getHeight() == 0)
    {
      return null;
    }

    final byte[] pixels = ((DataBufferByte) image.getRaster()
            .getDataBuffer()).getData();

    final int width = image.getWidth();
    final int height = image.getHeight();

    int[][] result = new int[height][width];

    boolean done = false;
    boolean alreadyWentToNextByte = false;
    int byteIndex = 0;
    int row = 0;
    int col = 0;
    int numBits = 0;
    byte currentByte = pixels[byteIndex];
    while(!done)
    {
      alreadyWentToNextByte = false;

      result[row][col] = (currentByte & 0x80) >> 7;
      currentByte = (byte) (((int) currentByte) << 1);
      numBits++;

      if((row == height - 1) && (col == width - 1))
      {
        done = true;
      }else
      {
        col++;

        if(numBits == 8)
        {
          currentByte = pixels[++byteIndex];
          numBits = 0;
          alreadyWentToNextByte = true;
        }

        if(col == width)
        {
          row++;
          col = 0;

          if(!alreadyWentToNextByte)
          {
            currentByte = pixels[++byteIndex];
            numBits = 0;
          }
        }
      }
    }
    return result;
  }
}