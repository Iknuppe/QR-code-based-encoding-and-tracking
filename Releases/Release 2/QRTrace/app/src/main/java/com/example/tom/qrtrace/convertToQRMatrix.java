
package com.example.tom.qrtrace;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;

public class convertToQRMatrix
{
  private static final int ORIGIN = 0;
  private static final int X_AXIS = 1;
  private static final int Y_AXIS = 2;

  private static final String TAG = "QRT";

  public static int[][] convertToQRMatrix(List<Square> finder_patterns, Bitmap qr_image)
  {
    if(finder_patterns == null)
    {
      Log.d(TAG, "Error reading QR-Code ...");
      return null;
    }

    double delta_x = finder_patterns.get(ORIGIN).getLength()/7;
    double delta_y = finder_patterns.get(ORIGIN).getHeight()/7;

    double matrix_dimension_x = (finder_patterns.get(X_AXIS).getCenter().crdNorm(finder_patterns.get(ORIGIN).getCenter()) / delta_x) + 6 + 1;
    double matrix_dimension_y = (finder_patterns.get(Y_AXIS).getCenter().crdNorm(finder_patterns.get(ORIGIN).getCenter()) / delta_y) + 6 + 1;

    for(int counter = 21 ; counter <= 177 ; counter += 4)
    {
      if(Math.abs(matrix_dimension_x - counter) < 2)
      {
        delta_x *= (matrix_dimension_x / counter);
        delta_y *= (matrix_dimension_y / counter);
        matrix_dimension_x = counter;
        matrix_dimension_y = counter;
        break;
      }
    }
    
    if(matrix_dimension_x != matrix_dimension_y)
    {
      Log.d(TAG, "Matrix dimensions mismatch ...");
      return null;
    }

    Coordinate delta_x_vector = finder_patterns.get(X_AXIS).getCenter().crdSubtract(finder_patterns.get(ORIGIN).getCenter());
    delta_x_vector = delta_x_vector.crdMultiply(delta_x);
    delta_x_vector = delta_x_vector.crdDivide(finder_patterns.get(X_AXIS).getCenter().crdNorm(finder_patterns.get(ORIGIN).getCenter()));

    Coordinate delta_y_vector = finder_patterns.get(Y_AXIS).getCenter().crdSubtract(finder_patterns.get(ORIGIN).getCenter());
    delta_y_vector = delta_y_vector.crdMultiply(delta_y);
    delta_y_vector = delta_y_vector.crdDivide(finder_patterns.get(Y_AXIS).getCenter().crdNorm(finder_patterns.get(ORIGIN).getCenter()));

    Coordinate offset = (delta_x_vector.crdMultiply(-3.0)).crdAdd(delta_y_vector.crdMultiply(-3.0));

    int x_pos;
    int y_pos;
    Coordinate current_pos;
    int qr_image_width = qr_image.getWidth();
    int qr_image_height = qr_image.getHeight();
    int[][] qr_array = new int[(int)matrix_dimension_y][(int)matrix_dimension_x];

    for(int y_counter = 0; y_counter < matrix_dimension_y; y_counter++)
    {
      for(int x_counter = 0; x_counter < matrix_dimension_x; x_counter++)
      {
        current_pos = finder_patterns.get(ORIGIN).getCenter().crdAdd(offset);
        current_pos = current_pos.crdAdd(delta_x_vector.crdMultiply((double) x_counter));
        current_pos = current_pos.crdAdd(delta_y_vector.crdMultiply((double) y_counter));
        
        x_pos = (int)current_pos.getXcrd();
        y_pos = (int)current_pos.getYcrd();

        if(x_pos < 0 || x_pos >= qr_image_width || y_pos < 0 || y_pos >= qr_image_height)
        {
          return null;
        }

        if(checkIfBlack(qr_image.getPixel(x_pos, y_pos)))
        {
          qr_array[y_counter][x_counter] = 1;
        }
        else
        {
          qr_array[y_counter][x_counter] = 0;
        }
      }
    }

    return qr_array;
  }

  public static List<Square> identifyFinderPatterns(List<Square> finder_patterns)
  {
    if(finder_patterns.size() != 3)
    {
      return null;
    }

    int origin = findOrigin(finder_patterns);

    if(origin == -1)
    {
      return null;
    }

    int x_axis = findXaxis(finder_patterns, origin);
    int y_axis = 3 - origin - x_axis;

    List<Square> sorted_finder_patterns = new ArrayList<>();

    sorted_finder_patterns.add(finder_patterns.get(origin));
    sorted_finder_patterns.add(finder_patterns.get(y_axis));
    sorted_finder_patterns.add(finder_patterns.get(x_axis));

    return sorted_finder_patterns;
  }

  private static int findOrigin(List<Square> square_list)
  {
    double length_A_to_B = square_list.get(0).getCenter().crdNorm(square_list.get(1).getCenter());
    double length_B_to_C = square_list.get(1).getCenter().crdNorm(square_list.get(2).getCenter());
    double length_A_to_C = square_list.get(2).getCenter().crdNorm(square_list.get(0).getCenter());

    if(length_B_to_C > length_A_to_B && length_B_to_C > length_A_to_C)
    {
      return 0;
    }

    if(length_A_to_C > length_A_to_B && length_A_to_C > length_B_to_C)
    {
      return 1;
    }

    if(length_A_to_B > length_B_to_C && length_A_to_B > length_A_to_C)
    {
      return 2;
    }

    return -1;
  }

  private static int findXaxis(List<Square> square_list, int origin)
  {
    int first;
    int second;

    switch(origin)
    {
      case 0:
      {
        first = 1;
        second = 2;
        break;
      }
      case 1:
      {
        first = 0;
        second = 2;
        break;
      }
      case 2:
      {
        first = 0;
        second = 1;
        break;
      }
      default:
      {
        return -1;
      }
    }

    Coordinate start_to_first = square_list.get(first).getCenter().crdSubtract(square_list.get(origin).getCenter());
    start_to_first = start_to_first.crdRotateNeg90();

    Coordinate start_to_second = square_list.get(second).getCenter().crdSubtract(square_list.get(origin).getCenter());

    if(start_to_first.crdCheckEqual(start_to_second) == 1)
    {
      return second;
    }else
    {
      return first;
    }
  }

  private static final double DIVISOR = 0.1;
  private static final int CENTER_TOLERANCE = 15;

  private static List<Square> checkSquare(Square current_square, List<Square> square_list)
  {
    if(current_square == null)
    {
      return square_list;
    }

    if(square_list.isEmpty())
    {
      square_list.add(current_square);
      return square_list;
    }

    double SIZE_TOLERANCE = (square_list.get(square_list.size() - 1).getAreaSize()) * DIVISOR;

    if((current_square.getAreaSize() + SIZE_TOLERANCE) < square_list.get(0).getAreaSize())
    {
      return square_list;
    }

    
    for(int counter = 0 ; counter < square_list.size() ; counter++)
    {
      if(Math.abs(current_square.getCenter().crdNorm(square_list.get(counter).getCenter())) < CENTER_TOLERANCE)
      {
        return square_list;
      }
    }

    square_list.add(current_square);
    square_list = sortSquares(square_list);

    if(square_list.size() > 3)
    {
      square_list.remove(0);
    }

    return square_list;
  }

  private static List<Square> sortSquares(List<Square> square_list)
  {
    Collections.sort(square_list, new Comparator<Square>()
    {
      @Override
      public int compare(Square square_1, Square square_2)
      {
        return Double.compare(square_1.getAreaSize(), square_2.getAreaSize());
      }
    });

    return square_list;
  }

  public static List<Square> findSquares(Bitmap qr_image)
  {
    int image_dimension_length = qr_image.getWidth();
    int image_dimension_height = qr_image.getHeight();
    int HORIZONTAL_SCALE_FACTOR;
    int VERTICAL_SCALE_FACTOR;
    Square current_square;
    List<Square> square_list = new ArrayList<>();

    if(image_dimension_length < image_dimension_height)
    {
      HORIZONTAL_SCALE_FACTOR = image_dimension_length/20;
      VERTICAL_SCALE_FACTOR = image_dimension_length/20;
    }
    else
    {
      HORIZONTAL_SCALE_FACTOR = image_dimension_height/30;
      VERTICAL_SCALE_FACTOR =image_dimension_height/30;
    }

    for(int vertical_counter = VERTICAL_SCALE_FACTOR;
            vertical_counter <= (image_dimension_height - VERTICAL_SCALE_FACTOR);
            vertical_counter += VERTICAL_SCALE_FACTOR)
    {
      for(int horizontal_counter = HORIZONTAL_SCALE_FACTOR;
              horizontal_counter <= (image_dimension_length - HORIZONTAL_SCALE_FACTOR);
              horizontal_counter += HORIZONTAL_SCALE_FACTOR)
      {
        current_square = redNetAlgorithm(horizontal_counter, vertical_counter, qr_image);
        square_list = checkSquare(current_square, square_list);
      }
    }

    return square_list;
  }

  private static Square redNetAlgorithm(int horizontal_counter, int vertical_counter, Bitmap qr_image)
  {
    List<Coordinate> edges = new ArrayList<>();

    if(!checkIfBlack(qr_image.getPixel(horizontal_counter, vertical_counter)))
    {
      return null;
    }

    edges.add(findEdgeLOU(horizontal_counter, vertical_counter, qr_image));
    edges.add(findEdgeORL(horizontal_counter, vertical_counter, qr_image));
    edges.add(findEdgeULR(horizontal_counter, vertical_counter, qr_image));
    edges.add(findEdgeRUO(horizontal_counter, vertical_counter, qr_image));

    if(checkEdges(edges, qr_image))
    {
      return new Square(edges);
    }
    else
    {
      return null;
    }
  }

  private static int assignCheckTolerance(Bitmap qr_image)
  {
    switch(qr_image.getWidth())
    {
      case 320: return 4;
      case 480: return 4;
      case 640: return 5;
      case 800: return 6;
      case 1024: return 6;
      case 1280: return 8;
      case 1600: return 9;
      case 1920: return 10;
      case 2560: return 12;
      default: return 8;
    }
  }

  private static final int A = 0;
  private static final int B = 1;
  private static final int C = 3;
  private static final int D = 2;

  private static boolean checkEdges(List<Coordinate> edges, Bitmap qr_image)
  {
    int CHECK_TOLERANCE = assignCheckTolerance(qr_image);

    double reference = edges.get(A).crdNorm(edges.get(B));
    double[] cat_list = new double[3];

    cat_list[0] = edges.get(B).crdNorm(edges.get(C));
    cat_list[1] = edges.get(C).crdNorm(edges.get(D));
    cat_list[2] = edges.get(D).crdNorm(edges.get(A));

    for(int counter = 0 ; counter < cat_list.length ; counter++)
    {
      if(Math.abs(reference - cat_list[counter]) > CHECK_TOLERANCE)
      {
        return false;
      }
    }

    return true;
  }

  private static boolean checkIfBlack(int pixel)
  {
    return (Color.red(pixel) <= 50 && Color.blue(pixel) <= 50 && Color.green(pixel) <= 50);
  }

  public static void debugInformation(List<Square> square_list)
  {
    for(int counter = 0; counter < square_list.size(); counter++)
    {
      System.out.println("Square " + (counter + 1) + ": ("
              + square_list.get(counter).getLength() + " * "
              + square_list.get(counter).getHeight() + " = "
              + square_list.get(counter).getAreaSize() + ") @ center X: "
              + square_list.get(counter).getCenter().getXcrd() + " Y: "
              + square_list.get(counter).getCenter().getYcrd());
      for(int counter2 = 0; counter2 < 4; counter2++)
      {
        System.out.println("  Edge " + (counter2 + 1) + ":"
                + " X: " + square_list.get(counter).getEdge(counter2).getXcrd()
                + " Y: " + square_list.get(counter).getEdge(counter2).getYcrd());
      }
    }
  }

  private static final int START_SUCCESSIVE = 8;

  private static Coordinate findEdgeLOU(int x_pos, int y_pos, Bitmap qr_image)
  {
    int x_slide = x_pos;
    int y_slide = y_pos;
    int image_height = qr_image.getHeight();
    int successive = START_SUCCESSIVE;
    int color_change_count = 0;

    boolean move_flag;
    boolean potential_finish_flag = false;
    boolean current_pixel;
    boolean color_flag = true;

    while(color_change_count < 2)
    {
      if((x_slide - successive) >= 0)
      {
        x_slide -= successive;
      }
      else
      {
        break;
      }

      if((y_slide - successive) >= 0)
      {
        y_slide -= successive;
      }
      else
      {
        break;
      }

      current_pixel = checkIfBlack(qr_image.getPixel(x_slide, y_slide));

      if(color_flag != current_pixel)
      {
        color_change_count++;
        color_flag = current_pixel;
      }
    }

    while(true)
    {
      move_flag = false;

      if((x_slide - successive) >= 0 && checkIfBlack(qr_image.getPixel(x_slide - successive, y_slide)))
      {
        move_flag =true;
        x_slide -= successive;
        potential_finish_flag = false;
      }

      if((y_slide - successive) >= 0 && checkIfBlack(qr_image.getPixel(x_slide, y_slide - successive)))
      {
        move_flag =true;
        y_slide -= successive;

        if(potential_finish_flag)
        {
          if(successive > 1)
          {
            successive /= 2;
            potential_finish_flag = false;
          }
          else
          {
            break;
          }
        }
      }

      if(!move_flag)
      {
        if((y_slide + successive) <= (image_height - 1) && checkIfBlack(qr_image.getPixel(x_slide, y_slide + successive)))
        {
          potential_finish_flag = true;
          y_slide += successive;
        }
        else if(successive > 1)
        {
          successive /= 2;
        }
        else
        {
          break;
        }
      }
    }

    return new Coordinate(x_slide, y_slide);
  }

  private static Coordinate findEdgeORL(int x_pos, int y_pos, Bitmap qr_image)
  {
    int x_slide = x_pos;
    int y_slide = y_pos;
    boolean move_flag;
    boolean potential_finish_flag = false;
    int successive = START_SUCCESSIVE;
    int image_length = qr_image.getWidth();

    int color_change_count = 0;
    boolean current_pixel;
    boolean color_flag = true;

    while(color_change_count < 2)
    {
      if((y_slide - successive) >= 0)
      {
        y_slide -= successive;
      }
      else
      {
        break;
      }

      if((x_slide + successive) <= (image_length - 1))
      {
        x_slide += successive;
      }
      else
      {
        break;
      }

      current_pixel = checkIfBlack(qr_image.getPixel(x_slide, y_slide));

      if(color_flag != current_pixel)
      {
        color_change_count++;
        color_flag = current_pixel;
      }
    }

    while(true)
    {
      move_flag = false;

      if((y_slide - successive) >= 0 && checkIfBlack(qr_image.getPixel(x_slide, y_slide - successive)))
      {
        move_flag = true;
        y_slide -= successive;
        potential_finish_flag = false;
      }

      if((x_slide + successive) <= (image_length - 1) && checkIfBlack(qr_image.getPixel(x_slide + successive, y_slide)))
      {
        move_flag = true;
        x_slide += successive;

        if(potential_finish_flag)
        {
          if(successive > 1)
          {
            successive /= 2;
            potential_finish_flag = false;
          }
          else
          {
            break;
          }
        }
      }

      if(!move_flag)
      {
        if((x_slide - successive) >= 0 && checkIfBlack(qr_image.getPixel(x_slide - successive, y_slide)))
        {
          potential_finish_flag = true;
          x_slide -= successive;
        }
        else if(successive > 1)
        {
          successive /= 2;
        }
        else
        {
          break;
        }
      }
    }

     return new Coordinate(x_slide, y_slide);
  }

  private static Coordinate findEdgeULR(int x_pos, int y_pos, Bitmap qr_image)
  {
    int x_slide = x_pos;
    int y_slide = y_pos;
    boolean move_flag;
    boolean potential_finish_flag = false;
    int successive = START_SUCCESSIVE;
    int image_length = qr_image.getWidth();
    int image_height = qr_image.getHeight();

    int color_change_count = 0;
    boolean current_pixel;
    boolean color_flag = true;

    while(color_change_count < 2)
    {
      if((y_slide + successive) <= (image_height - 1))
      {
        y_slide += successive;
      }
      else
      {
        break;
      }

      if((x_slide - successive) >= 0)
      {
        x_slide -= successive;
      }
      else
      {
        break;
      }

      current_pixel = checkIfBlack(qr_image.getPixel(x_slide, y_slide));

      if(color_flag != current_pixel)
      {
        color_change_count++;
        color_flag = current_pixel;
      }
    }

    while(true)
    {
      move_flag = false;

      if((y_slide + successive) <= (image_height - 1) && checkIfBlack(qr_image.getPixel(x_slide, y_slide + successive)))
      {
        move_flag = true;
        y_slide += successive;
        potential_finish_flag = false;
      }

      if((x_slide - successive) >= 0 && checkIfBlack(qr_image.getPixel(x_slide - successive, y_slide)))
      {
        move_flag = true;
        x_slide -= successive;

        if(potential_finish_flag)
        {
          if(successive > 1)
          {
            successive /= 2;
            potential_finish_flag = false;
          }
          else
          {
            break;
          }
        }
      }

      if(!move_flag)
      {
        if((x_slide + successive) <= (image_length - 1) && checkIfBlack(qr_image.getPixel(x_slide + successive, y_slide)))
        {
          potential_finish_flag = true;
          x_slide += successive;
        }
        else if(successive > 1)
        {
          successive /= 2;
        }
        else
        {
          break;
        }
      }
    }

    return new Coordinate(x_slide, y_slide);
  }

  private static Coordinate findEdgeRUO(int x_pos, int y_pos, Bitmap qr_image)
  {
    int x_slide = x_pos;
    int y_slide = y_pos;
    boolean move_flag;
    boolean potential_finish_flag = false;
    int successive = START_SUCCESSIVE;
    int image_length = qr_image.getWidth();
    int image_height = qr_image.getHeight();

    int color_change_count = 0;
    boolean current_pixel;
    boolean color_flag = true;

    while(color_change_count < 2)
    {
      if((x_slide + successive) <= (image_length - 1))
      {
        x_slide += successive;
      }
      else
      {
        break;
      }

      if((y_slide + successive) <= (image_height - 1))
      {
        y_slide += successive;
      }
      else
      {
        break;
      }

      current_pixel = checkIfBlack(qr_image.getPixel(x_slide, y_slide));

      if(color_flag != current_pixel)
      {
        color_change_count++;
        color_flag = current_pixel;
      }
    }

    while(true)
    {
      move_flag = false;

      if((x_slide + successive) <= (image_length - 1) && checkIfBlack(qr_image.getPixel(x_slide + successive, y_slide)))
      {
        move_flag = true;
        x_slide += successive;
        potential_finish_flag = false;
      }

      if((y_slide + successive) <= (image_height - 1) && checkIfBlack(qr_image.getPixel(x_slide, y_slide + successive)))
      {
        move_flag = true;
        y_slide += successive;

        if(potential_finish_flag)
        {
          if(successive > 1)
          {
            successive /= 2;
            potential_finish_flag = false;
          }
          else
          {
            break;
          }
        }
      }

      if(!move_flag)
      {
        if((y_slide - successive) >= 0 && checkIfBlack(qr_image.getPixel(x_slide, y_slide - successive)))
        {
          potential_finish_flag = true;
          y_slide -= successive;
        }
        else if(successive > 1)
        {
          successive /= 2;
        }
        else
        {
          break;
        }
      }
    }

    return new Coordinate(x_slide, y_slide);
  }
}
