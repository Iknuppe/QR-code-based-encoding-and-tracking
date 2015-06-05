/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.club27.merkus.qr_code_test;

import android.graphics.Bitmap;
import android.graphics.Color;

import java.util.ArrayList;
import java.util.List;
import static java.lang.Math.round;

public class convertToQRMatrix
{
  /* ------------------------------------------------------------------------ */
  public static int[][] convertToQRMatrix(Bitmap qr_image)
  {
    System.out.println("Start convertToQRMatrix algorithm test...");
    
    //Try to find all 3 finder patterns
    List<Square> pre_square_list = findSquares(qr_image);
    
    if(pre_square_list.size() != 3)
    {
      System.out.println("Can't see finder patterns ...");
      //return error;
    }
    
    debugInformation(pre_square_list);

    System.out.println("Identifying Squares...");
    
    //Identify index of center square and both axis
    int origin = findOrigin(pre_square_list);
    int x_axis = findXaxis(pre_square_list, origin);
    int y_axis = 3 - origin - x_axis;
    
    System.out.println("Origin: Square " + (origin + 1) + ", x_axis: Square " + (x_axis + 1) + ", y_axis: Square " + (y_axis + 1));
    
    //Generate new square list with finding pattern center coordinates
    //This is done, because of later calculations of delta_x and delta_y
    //Could be optimised in later versions...
    List<Square> square_list = new ArrayList<>();
    square_list.add(redEdgeAlgorithm((int)pre_square_list.get(origin).getCenter().getXcrd(), (int)pre_square_list.get(origin).getCenter().getYcrd(), qr_image));
    square_list.add(redEdgeAlgorithm((int)pre_square_list.get(x_axis).getCenter().getXcrd(), (int)pre_square_list.get(x_axis).getCenter().getYcrd(), qr_image));
    square_list.add(redEdgeAlgorithm((int)pre_square_list.get(y_axis).getCenter().getXcrd(), (int)pre_square_list.get(y_axis).getCenter().getYcrd(), qr_image));
    
    //Re-define indices
    origin = 0;
    x_axis = 1;
    y_axis = 2;
    
    //Calculate length and width of one matrix segment
    double delta_x = square_list.get(origin).getLength()/3;
    double delta_y = square_list.get(origin).getHeight()/3;
    
    System.out.println("delta_x: " + delta_x + " delta_y: " + delta_y);

    //Determine dimensions of QR matrix
    int matrix_dimension_x = ((int)round(square_list.get(x_axis).getCenter().crdNorm(square_list.get(origin).getCenter()) / delta_x)) + 6 + 1;
    int matrix_dimension_y = ((int)round(square_list.get(y_axis).getCenter().crdNorm(square_list.get(origin).getCenter()) / delta_y)) + 6 + 1;
    
    System.out.println("matrix_dimension_x: " + matrix_dimension_x + " (" + ((square_list.get(x_axis).getCenter().crdNorm(square_list.get(origin).getCenter()) / delta_x) + 6 + 1) + ")" + " matrix_dimension_y: " + matrix_dimension_y + " (" + ((square_list.get(y_axis).getCenter().crdNorm(square_list.get(origin).getCenter()) / delta_y) + 6 + 1) + ")");
    
    if(matrix_dimension_x != matrix_dimension_y)
    {
      System.out.println("Matrix dimensions mismatch ...");
      //return error;
    }
    
    //Calculate normed delta x vector
    Coordinate delta_x_vector = square_list.get(x_axis).getCenter().crdSubtract(square_list.get(origin).getCenter());
    delta_x_vector = delta_x_vector.crdMultiply(delta_x);
    delta_x_vector = delta_x_vector.crdDivide(square_list.get(x_axis).getCenter().crdNorm(square_list.get(origin).getCenter()));
    
    System.out.println("delta_x_vector: X: " + delta_x_vector.getXcrd() + " Y: " + delta_x_vector.getYcrd());

    //Calculate normed delta y vector
    Coordinate delta_y_vector = square_list.get(y_axis).getCenter().crdSubtract(square_list.get(origin).getCenter());
    delta_y_vector = delta_y_vector.crdMultiply(delta_y);
    delta_y_vector = delta_y_vector.crdDivide(square_list.get(y_axis).getCenter().crdNorm(square_list.get(origin).getCenter()));
    
    System.out.println("delta_y_vector: X: " + delta_y_vector.getXcrd() + " Y: " + delta_y_vector.getYcrd());

    //Calculate start offset
    Coordinate offset = (delta_x_vector.crdMultiply(-3.0)).crdAdd(delta_y_vector.crdMultiply(-3.0));
    
    System.out.println("offset: X: " + offset.getXcrd() + " Y: " + offset.getYcrd());

    int x_pos;
    int y_pos;
    Coordinate current_pos;
    int[][] qr_array = new int[matrix_dimension_y][matrix_dimension_x];

    //On the basis of both vectors every segment of the QR matrix can be calculated
    for(int y_counter = 0; y_counter < matrix_dimension_y; y_counter++)
    {
      for(int x_counter = 0; x_counter < matrix_dimension_x; x_counter++)
      {
        current_pos = square_list.get(origin).getCenter().crdAdd(offset);
        current_pos = current_pos.crdAdd(delta_x_vector.crdMultiply((double) x_counter));
        current_pos = current_pos.crdAdd(delta_y_vector.crdMultiply((double) y_counter));
        
        x_pos = (int)current_pos.getXcrd();
        y_pos = (int)current_pos.getYcrd();

        if(qr_image.getPixel(x_pos, y_pos) == Color.BLACK)
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
  
  /* ------------------------------------------------------------------------ */
  private static final int LENGTH_TOLERANCE = 1;
  public static int findOrigin(List<Square> square_list)
  {
    //Finding the center square of squares A,B and C
    double length_A_to_B = square_list.get(0).getCenter().crdNorm(square_list.get(1).getCenter());
    double length_B_to_C = square_list.get(1).getCenter().crdNorm(square_list.get(2).getCenter());
    double length_A_to_C = square_list.get(2).getCenter().crdNorm(square_list.get(0).getCenter());

    //check A:
    if((length_A_to_B - LENGTH_TOLERANCE) < (length_A_to_C + LENGTH_TOLERANCE) &&
       (length_A_to_B + LENGTH_TOLERANCE) > (length_A_to_C - LENGTH_TOLERANCE))
    {
      return 0;
    }

    //check B:
    if((length_A_to_B - LENGTH_TOLERANCE) < (length_B_to_C + LENGTH_TOLERANCE) &&
       (length_A_to_B + LENGTH_TOLERANCE) > (length_B_to_C - LENGTH_TOLERANCE))
    {
      return 1;
    }

    //check C:
    if((length_B_to_C - LENGTH_TOLERANCE) < (length_A_to_C + LENGTH_TOLERANCE) &&
       (length_B_to_C + LENGTH_TOLERANCE) > (length_A_to_C - LENGTH_TOLERANCE))
    {
      return 2;
    }

    return -1;
  }

  /* ------------------------------------------------------------------------ */
  public static int findXaxis(List<Square> square_list, int origin)
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

    /* calculate vector from start point to first point */
    Coordinate start_to_first = square_list.get(first).getCenter().crdSubtract(square_list.get(origin).getCenter());
    start_to_first = start_to_first.crdRotateNeg90();

    /* calculate vector from start point to second point */
    Coordinate start_to_second = square_list.get(second).getCenter().crdSubtract(square_list.get(origin).getCenter());

    if(start_to_first.crdCheckEqual(start_to_second) == 1)
    {
      //first
      return second;
    }else
    {
      //second
      return first;
    }
  }
  
  /* ------------------------------------------------------------------------ */
  private static final int SIZE_TOLERANCE = 2;
  public static int checkSquare(Square current_square, List<Square> square_list)
  { 
    if(current_square == null)
    {
      return 0;
    }

    /* add first square */
    if(square_list.isEmpty())
    {
      return 1;
    }
    /*
    for(int counter = 0 ; counter < square_list.size() ; counter++)
    {
      if((current_square.getAreaSize() + SIZE_TOLERANCE) < square_list.get(counter).getAreaSize() ||
          current_square.getCenter().crdCheckEqual(square_list.get(counter).getCenter()) == 1)
      {
        return 0;
      }
    }
    */
    
    if((current_square.getAreaSize() + SIZE_TOLERANCE) < square_list.get(square_list.size() - 1).getAreaSize())
    {
      return 0;
    }
    
    //System.out.println("new Area size: "+current_square.getAreaSize());
    
    for(int counter = 0 ; counter < square_list.size() ; counter++)
    {
      if(current_square.getCenter().crdCheckEqual(square_list.get(counter).getCenter()) == 1)
      {
        square_list.remove(counter);
        return 1;
      }
    }

    /* if there are already 3 large squares, remove the first square, because it must be the smallest */
    if(square_list.size() == 3)
    {
      square_list.remove(0);
    }

    return 1;
  }

  /* ------------------------------------------------------------------------ */
  private static final int HORIZONTAL_SCALE_FACTOR = 10;
  private static final int VERTICAL_SCALE_FACTOR = 10;
  public static List<Square> findSquares(Bitmap qr_image)
  {
    System.out.println("Finding squares in " + qr_image.getWidth() + ":" + qr_image.getHeight() + "...");
    int image_dimension_length = qr_image.getWidth();//x
    int image_dimension_height = qr_image.getHeight();//y
    Square current_square;
    List<Square> square_list = new ArrayList<>();

    /* split picture into grid and use algorithm */
    for(int vertical_counter = 0;
            vertical_counter <= (image_dimension_height - VERTICAL_SCALE_FACTOR);
            vertical_counter += VERTICAL_SCALE_FACTOR)
    {
      for(int horizontal_counter = 0;
              horizontal_counter <= (image_dimension_length - HORIZONTAL_SCALE_FACTOR);
              horizontal_counter += HORIZONTAL_SCALE_FACTOR)
      {
        //System.out.println("testing X: " + horizontal_counter + " Y: " + vertical_counter);
        //System.out.println("Wert: " + image_array[vertical_counter][horizontal_counter]);
        current_square = redEdgeAlgorithm(horizontal_counter, vertical_counter, qr_image);

        /* check if current square is valid */
        if(checkSquare(current_square, square_list) == 1)
        {
          square_list.add(current_square);
        }
      }
    }

    /*
    if(square_list.size() == 3)
    {
      return square_list;
    }
    else
    {
      return null;
    }
    */
    return square_list;
  }

  /* ------------------------------------------------------------------------ */
  public static Square redEdgeAlgorithm(int horizontal_counter, int vertical_counter, Bitmap qr_image)
  {
    int x_slide = horizontal_counter;
    int y_slide = vertical_counter;
    int image_length = qr_image.getWidth();
    int image_height = qr_image.getHeight();
    int potential_finish_flag;
    List<Coordinate> edges = new ArrayList<>();

    /* check if start point is dark */
    if(qr_image.getPixel(x_slide, y_slide) == Color.WHITE)
    {
      return null;
    }

    //System.out.println("testing X: " + horizontal_counter + " Y: " + vertical_counter);
    /* -----------------------------------------------------------------------*/
    /* Links Oben Unten */
    //System.out.println("Links Oben Unten...");
    x_slide = horizontal_counter;
    y_slide = vertical_counter;
    potential_finish_flag = 0;

    while(true)
    {
      if(x_slide > 0 && (qr_image.getPixel(x_slide - 1, y_slide) == Color.BLACK))//go left
      {
        potential_finish_flag = 0;
        x_slide--;
      }else if(y_slide > 0 && (qr_image.getPixel(x_slide, y_slide - 1) == Color.BLACK)) //go up
      {
        y_slide--;
        if(potential_finish_flag == 1)
        {
          break;
        }
      }else if(y_slide < (image_height - 1) && (qr_image.getPixel(x_slide, y_slide + 1) == Color.BLACK)) //go down
      {
        potential_finish_flag = 1;
        y_slide++;
      }else
      {
        break;
      }
    }

    edges.add(new Coordinate(x_slide, y_slide));

    /* -----------------------------------------------------------------------*/
    /* Oben Rechts Links */
    //System.out.println("Oben Rechts Links...");
    x_slide = horizontal_counter;
    y_slide = vertical_counter;
    potential_finish_flag = 0;
    //auf eof/grenzen prüfen
    
    while(true)
    {
      if(y_slide > 0 && (qr_image.getPixel(x_slide, y_slide - 1) == Color.BLACK)) //go up
      {
        potential_finish_flag = 0;
        y_slide--;
      }else if(x_slide < (image_length - 1) && (qr_image.getPixel(x_slide + 1, y_slide) == Color.BLACK)) //go right
      {
        x_slide++;
        if(potential_finish_flag == 1)
        {
          break;
        }
      }else if(x_slide > 0 && (qr_image.getPixel(x_slide - 1, y_slide) == Color.BLACK)) //go left
      {
        potential_finish_flag = 1;
        x_slide--;
      }else
      {
        break;
      }
    }

    edges.add(new Coordinate(x_slide, y_slide));

    /* -----------------------------------------------------------------------*/
    /* Unten Links Rechts */
    //System.out.println("Unten Links Rechts...");
    x_slide = horizontal_counter;
    y_slide = vertical_counter;
    potential_finish_flag = 0;

    while(true)
    {
      if(y_slide < (image_height - 1) && (qr_image.getPixel(x_slide, y_slide + 1) == Color.BLACK)) //go down
      {
        potential_finish_flag = 0;
        y_slide++;
      }else if(x_slide > 0 && (qr_image.getPixel(x_slide - 1, y_slide) == Color.BLACK)) //go left
      {
        x_slide--;
        if(potential_finish_flag == 1)
        {
          break;
        }
      }else if(x_slide < (image_length - 1) && (qr_image.getPixel(x_slide + 1, y_slide) == Color.BLACK)) //go right
      {
        potential_finish_flag = 1;
        x_slide++;
      }else
      {
        break;
      }
    }

    edges.add(new Coordinate(x_slide, y_slide));

    /* -----------------------------------------------------------------------*/
    /* Rechts Unten Oben */
    //System.out.println("Rechts Unten Oben...");
    x_slide = horizontal_counter;
    y_slide = vertical_counter;
    potential_finish_flag = 0;
    
    while(true)
    {//System.out.println("xslide: "+x_slide+" yslide: "+y_slide);
      if(x_slide < (image_length - 1) && (qr_image.getPixel(x_slide + 1, y_slide) == Color.BLACK)) //go right
      {
        potential_finish_flag = 0;
        x_slide++;
      }else if(y_slide < (image_height - 1) && (qr_image.getPixel(x_slide, y_slide + 1) == Color.BLACK)) //go down
      {
        y_slide++;
        if(potential_finish_flag == 1)
        {
          break;
        }
      }else if(y_slide > 0 && (qr_image.getPixel(x_slide, y_slide - 1) == Color.BLACK)) // go up
      {
        potential_finish_flag = 1;
        y_slide--;
      }else
      {
        break;
      }
    }

    edges.add(new Coordinate(x_slide, y_slide));

    /* -----------------------------------------------------------------------*/
    //System.out.println("Check all 4 edges...");
    if(checkEdges(edges) == 1)
    {
      return new Square(edges);
    }else
    {
      return null;
    }
  }

  /* ------------------------------------------------------------------------ */
  private static final int CATHETUS_TOLERANCE = 5;
  public static int checkEdges(List<Coordinate> edges)
  {//überflüssige tests
    Coordinate A_to_B = edges.get(1).crdSubtract(edges.get(0));
    A_to_B = A_to_B.crdRotatePos90();
    Coordinate A_to_D = edges.get(2).crdSubtract(edges.get(0));
    if(A_to_B.crdCheckEqual(A_to_D) == 0)
    {
      return 0;
    }

    Coordinate C_to_D = edges.get(2).crdSubtract(edges.get(3));
    C_to_D = C_to_D.crdRotatePos90();
    Coordinate C_to_B = edges.get(1).crdSubtract(edges.get(3));
    if(C_to_D.crdCheckEqual(C_to_B) == 0)
    {
      return 0;
    }

    //edges.get(1).crdSubtract(edges.get(3));
    //edges.get(2).crdSubtract(edges.get(3));
    double first_cathetus = edges.get(0).crdNorm(edges.get(3));
    double second_cathetus = edges.get(1).crdNorm(edges.get(2));

    if((first_cathetus + CATHETUS_TOLERANCE) > (second_cathetus - CATHETUS_TOLERANCE)
            && (first_cathetus - CATHETUS_TOLERANCE) < (second_cathetus + CATHETUS_TOLERANCE))
    {
      return 1;
    }
    else
    {
      return 0;
    }
  }
  
  /* ------------------------------------------------------------------------ */
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
}
