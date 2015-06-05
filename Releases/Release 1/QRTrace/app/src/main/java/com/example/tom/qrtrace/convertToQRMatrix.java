
package com.example.tom.qrtrace;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import android.graphics.Bitmap;
import android.graphics.Color;

public class convertToQRMatrix
{
  /* ------------------------------------------------------------------------ */
  private static final int ORIGIN = 0;
  private static final int X_AXIS = 1;
  private static final int Y_AXIS = 2;
  public static int[][] convertToQRMatrix(Bitmap qr_image)
  {
    List<Square> finder_patterns = identifyFinderPatterns(qr_image);
    if(finder_patterns == null)
    {
      System.out.println("Error reading QR-Code ...");
      return null;
    }

    //Calculate length and width of one matrix segment
    double delta_x = finder_patterns.get(ORIGIN).getLength()/3;
    double delta_y = finder_patterns.get(ORIGIN).getHeight()/3;

    //Determine dimensions of QR matrix
    double matrix_dimension_x = (finder_patterns.get(X_AXIS).getCenter().crdNorm(finder_patterns.get(ORIGIN).getCenter()) / delta_x) + 6 + 1;
    double matrix_dimension_y = (finder_patterns.get(Y_AXIS).getCenter().crdNorm(finder_patterns.get(ORIGIN).getCenter()) / delta_y) + 6 + 1;

    System.out.println("delta_x: " + delta_x + " delta_y: " + delta_y);
    System.out.println("matrix_dimension_x: " + matrix_dimension_x + " matrix_dimension_y: " + matrix_dimension_y);

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

    System.out.println("delta_x: " + delta_x + " delta_y: " + delta_y);
    System.out.println("matrix_dimension_x: " + (int)matrix_dimension_x + " matrix_dimension_y: " + (int)matrix_dimension_y);
    
    if(matrix_dimension_x != matrix_dimension_y)
    {
      System.out.println("Matrix dimensions mismatch ...");
      return null;
    }
    
    //Calculate normed delta x vector
    Coordinate delta_x_vector = finder_patterns.get(X_AXIS).getCenter().crdSubtract(finder_patterns.get(ORIGIN).getCenter());
    delta_x_vector = delta_x_vector.crdMultiply(delta_x);
    delta_x_vector = delta_x_vector.crdDivide(finder_patterns.get(X_AXIS).getCenter().crdNorm(finder_patterns.get(ORIGIN).getCenter()));
    
    System.out.println("delta_x_vector: X: " + delta_x_vector.getXcrd() + " Y: " + delta_x_vector.getYcrd());

    //Calculate normed delta y vector
    Coordinate delta_y_vector = finder_patterns.get(Y_AXIS).getCenter().crdSubtract(finder_patterns.get(ORIGIN).getCenter());
    delta_y_vector = delta_y_vector.crdMultiply(delta_y);
    delta_y_vector = delta_y_vector.crdDivide(finder_patterns.get(Y_AXIS).getCenter().crdNorm(finder_patterns.get(ORIGIN).getCenter()));
    
    System.out.println("delta_y_vector: X: " + delta_y_vector.getXcrd() + " Y: " + delta_y_vector.getYcrd());

    //Calculate start offset
    Coordinate offset = (delta_x_vector.crdMultiply(-3.0)).crdAdd(delta_y_vector.crdMultiply(-3.0));
    
    System.out.println("offset: X: " + offset.getXcrd() + " Y: " + offset.getYcrd());

    int x_pos;
    int y_pos;
    Coordinate current_pos;
    int[][] qr_array = new int[(int)matrix_dimension_y][(int)matrix_dimension_x];

    //On the basis of both vectors every segment of the QR matrix can be calculated
    for(int y_counter = 0; y_counter < matrix_dimension_y; y_counter++)
    {
      for(int x_counter = 0; x_counter < matrix_dimension_x; x_counter++)
      {
        current_pos = finder_patterns.get(ORIGIN).getCenter().crdAdd(offset);
        current_pos = current_pos.crdAdd(delta_x_vector.crdMultiply((double) x_counter));
        current_pos = current_pos.crdAdd(delta_y_vector.crdMultiply((double) y_counter));
        
        x_pos = (int)current_pos.getXcrd();
        y_pos = (int)current_pos.getYcrd();

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

  /* ------------------------------------------------------------------------ */
  public static List<Square> identifyFinderPatterns(Bitmap qr_image)
  {
    //Try to find all 3 finder patterns
    List<Square> finder_patterns = findSquares(qr_image);

    //debugInformation(finder_patterns);
    System.out.println("Squares found: "+finder_patterns.size());

    if(finder_patterns.size() != 3)
    {
      //Unable to find 3 finder patterns
      System.out.println("Can't see finder patterns ...");
      return null;
    }

    //Identify index of center square and both axis
    int origin = findOrigin(finder_patterns);
    if(origin == -1)
    {
      //Unable to find origin
      System.out.println("Unable to find origin...");
      return null;
    }
    int x_axis = findXaxis(finder_patterns, origin);
    int y_axis = 3 - origin - x_axis;
    
    //Generate new square list with finding pattern center coordinates
    //This is done, because of later calculations of delta_x and delta_y
    //Could be optimised in later versions...
    List<Square> sorted_finder_patterns = new ArrayList<>();
    
    sorted_finder_patterns.add(redEdgeAlgorithm((int)finder_patterns.get(origin).getCenter().getXcrd(), (int)finder_patterns.get(origin).getCenter().getYcrd(), qr_image));
    sorted_finder_patterns.add(redEdgeAlgorithm((int)finder_patterns.get(x_axis).getCenter().getXcrd(), (int)finder_patterns.get(x_axis).getCenter().getYcrd(), qr_image));
    sorted_finder_patterns.add(redEdgeAlgorithm((int)finder_patterns.get(y_axis).getCenter().getXcrd(), (int)finder_patterns.get(y_axis).getCenter().getYcrd(), qr_image));
    
    return sorted_finder_patterns;
  }
  
  /* ------------------------------------------------------------------------ */
  private static int findOrigin(List<Square> square_list)
  {
    //Finding the center square of squares A,B and C
    double length_A_to_B = square_list.get(0).getCenter().crdNorm(square_list.get(1).getCenter());
    double length_B_to_C = square_list.get(1).getCenter().crdNorm(square_list.get(2).getCenter());
    double length_A_to_C = square_list.get(2).getCenter().crdNorm(square_list.get(0).getCenter());

    //check A:
    if(length_B_to_C > length_A_to_B && length_B_to_C > length_A_to_C)
    {
      return 0;
    }

    //check B:
    if(length_A_to_C > length_A_to_B && length_A_to_C > length_B_to_C)
    {
      return 1;
    }

    //check C:
    if(length_A_to_B > length_B_to_C && length_A_to_B > length_A_to_C)
    {
      return 2;
    }

    return -1;
  }

  /* ------------------------------------------------------------------------ */
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
  private static final double DIVISOR = 0.1;//10%
  private static final int CENTER_TOLERANCE = 15;
  private static List<Square> checkSquare(Square current_square, List<Square> square_list)
  {
    //performance??
    if(current_square == null)
    {
      return square_list;
    }

    /* add first square */
    if(square_list.isEmpty())
    {
      //System.out.println("first");
      square_list.add(current_square);
      return square_list;
    }

    double SIZE_TOLERANCE = (square_list.get(square_list.size() - 1).getAreaSize()) * DIVISOR;
    //System.out.println("size: "+current_square.getAreaSize()+" "+square_list.get(square_list.size() - 1).getAreaSize());

    if((current_square.getAreaSize() + SIZE_TOLERANCE) < square_list.get(0).getAreaSize())
    {
      //System.out.println("too small");
      return square_list;
    }
    
    //System.out.println("new Area size: "+current_square.getAreaSize());
    
    for(int counter = 0 ; counter < square_list.size() ; counter++)
    { //System.out.println("center: "+current_square.getCenter().crdNorm(square_list.get(counter).getCenter()));
      if(Math.abs(current_square.getCenter().crdNorm(square_list.get(counter).getCenter())) < CENTER_TOLERANCE)
      {
        //System.out.println("double");
        //square_list.remove(counter);
        //square_list.add(current_square);
        //square_list = sortSquares(square_list);
        return square_list;
      }
    }
    //System.out.println("OK");
    square_list.add(current_square);
    square_list = sortSquares(square_list);

    /*System.out.print("Sort: ");
    for(int counter = 0 ; counter < square_list.size() ; counter++)
    {
      System.out.print(square_list.get(counter).getAreaSize()+" ");
    }
    System.out.println();*/
    /* if there are already 3 large squares, remove the first square, because it must be the smallest */
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

  /* ------------------------------------------------------------------------ */
  //private static final int HORIZONTAL_SCALE_FACTOR = 10;
  //private static final int VERTICAL_SCALE_FACTOR = 10;
  public static List<Square> findSquares(Bitmap qr_image)
  {
    System.out.println("Finding squares in " + qr_image.getWidth() + ":" + qr_image.getHeight() + "...");
    int image_dimension_length = qr_image.getWidth();//x
    int image_dimension_height = qr_image.getHeight();//y
    int HORIZONTAL_SCALE_FACTOR;
    int VERTICAL_SCALE_FACTOR;
    Square current_square;
    List<Square> square_list = new ArrayList<>();

    if(image_dimension_length < image_dimension_height)
    {
      HORIZONTAL_SCALE_FACTOR = image_dimension_length/10;//10
      VERTICAL_SCALE_FACTOR = image_dimension_length/10;//10
    }
    else
    {
      HORIZONTAL_SCALE_FACTOR = image_dimension_height/15;//15
      VERTICAL_SCALE_FACTOR =image_dimension_height/15;//15
    }

    /* split picture into grid and use algorithm */
    for(int vertical_counter = VERTICAL_SCALE_FACTOR;
            vertical_counter <= (image_dimension_height - VERTICAL_SCALE_FACTOR);
            vertical_counter += VERTICAL_SCALE_FACTOR)
    {
      for(int horizontal_counter = HORIZONTAL_SCALE_FACTOR;
              horizontal_counter <= (image_dimension_length - HORIZONTAL_SCALE_FACTOR);
              horizontal_counter += HORIZONTAL_SCALE_FACTOR)
      {
        current_square = redEdgeAlgorithm(horizontal_counter, vertical_counter, qr_image);

        /* check if current square is valid */
        square_list = checkSquare(current_square, square_list);
      }
    }

    return square_list;
  }

  /* ------------------------------------------------------------------------ */
  private static Square redEdgeAlgorithm(int horizontal_counter, int vertical_counter, Bitmap qr_image)
  {
    List<Coordinate> edges = new ArrayList<>();
    /* check if start point is dark */
    if(!checkIfBlack(qr_image.getPixel(horizontal_counter, vertical_counter)))
    {
      return null;
    }
    //System.out.println("Search point: "+horizontal_counter+" "+vertical_counter);
    edges.add(findEdgeLOU(horizontal_counter, vertical_counter, qr_image)); //Left Up Down (Links Oben Unten)
    edges.add(findEdgeORL(horizontal_counter, vertical_counter, qr_image)); //Up Right Left (Oben Rechts Links)
    edges.add(findEdgeULR(horizontal_counter, vertical_counter, qr_image)); //Down Left Right (Unten Links Rechts)
    edges.add(findEdgeRUO(horizontal_counter, vertical_counter, qr_image)); //Right Down Up (Rechts Unten Oben)
/*
    for(int counter = 0 ; counter < edges.size() ; counter++)
    {
      System.out.println("X: "+edges.get(counter).getXcrd()+" Y: "+edges.get(counter).getYcrd());
    }
*/
    //Check if edges form a square
    if(checkEdges(edges))
    {
      //System.out.println("added square");
      return new Square(edges);
    }
    else
    {
      return null;
    }
  }
  
  /* ------------------------------------------------------------------------ */
  private static final int CHECK_TOLERANCE = 40;//40//20
  private static final int A = 0;
  private static final int B = 1;
  private static final int C = 3;
  private static final int D = 2;
  private static boolean checkEdges(List<Coordinate> edges)
  {
    /*
    Coordinate A_to_C = edges.get(C).crdSubtract(edges.get(A));
    Coordinate B_to_D = edges.get(D).crdSubtract(edges.get(B));

    double angle = A_to_C.crdCheckNormal(B_to_D);
    System.out.println("pseudo angle: "+angle);
    System.out.println("----------");

    double a_b = edges.get(A).crdNorm(edges.get(B));
    double b_c = edges.get(B).crdNorm(edges.get(C));
    double c_d = edges.get(C).crdNorm(edges.get(D));
    double d_a = edges.get(D).crdNorm(edges.get(A));
*/
    double reference = edges.get(A).crdNorm(edges.get(B));
    //double CHECK_TOLERANCE = reference/27;
    double[] cat_list = new double[3];
    cat_list[0] = edges.get(B).crdNorm(edges.get(C));
    cat_list[1] = edges.get(C).crdNorm(edges.get(D));
    cat_list[2] = edges.get(D).crdNorm(edges.get(A));

    for(int counter = 0 ; counter < cat_list.length ; counter++)
    {
      //System.out.println("check: "+reference+" "+counter+" "+Math.abs(reference - cat_list[counter]));
      if(Math.abs(reference - cat_list[counter]) > CHECK_TOLERANCE)
      {
        return false;
      }
    }

    return true;
  }

  /* ------------------------------------------------------------------------ */
  private static boolean checkIfBlack(int pixel)
  {
    return (Color.red(pixel) <= 30 && Color.blue(pixel) <= 30 && Color.green(pixel) <= 30);
  }
  
  /* ------------------------------------------------------------------------ */
  private static void debugInformation(List<Square> square_list)
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

  /* -------------------------------------------------------------------------*/
  private static final int START_SUCCESSIVE = 8;
  private static Coordinate findEdgeLOU(int x_pos, int y_pos, Bitmap qr_image)
  {
    /* Links Oben Unten */
    //System.out.println("Links Oben Unten...");
    
    int x_slide = x_pos;
    int y_slide = y_pos;
    boolean move_flag;
    boolean potential_finish_flag = false;
    int successive = START_SUCCESSIVE;
    //int image_length = qr_image.getWidth();
    int image_height = qr_image.getHeight();

    
    while(true)
    {
      move_flag = false;

      if((x_slide - successive) >= 0 && checkIfBlack(qr_image.getPixel(x_slide - successive, y_slide)))//go left
      {
        move_flag =true;
        x_slide -= successive;
        potential_finish_flag = false;
      }

      if((y_slide - successive) >= 0 && checkIfBlack(qr_image.getPixel(x_slide, y_slide - successive))) //go up
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
        if((y_slide + successive) <= (image_height - 1) && checkIfBlack(qr_image.getPixel(x_slide, y_slide + successive))) //go down
        {
          potential_finish_flag = true;
          y_slide++;
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

  /* -------------------------------------------------------------------------*/
  private static Coordinate findEdgeORL(int x_pos, int y_pos, Bitmap qr_image)
  {
    /* Oben Rechts Links */
    //System.out.println("Oben Rechts Links...");
    
    int x_slide = x_pos;
    int y_slide = y_pos;
    boolean move_flag;
    boolean potential_finish_flag = false;
    int successive = START_SUCCESSIVE;
    int image_length = qr_image.getWidth();
    //int image_height = qr_image.getHeight();
    
    while(true)
    {
      move_flag = false;

      if((y_slide - successive) >= 0 && checkIfBlack(qr_image.getPixel(x_slide, y_slide - successive))) //go up
      {
        move_flag = true;
        y_slide -= successive;
        potential_finish_flag = false;
      }

      if((x_slide + successive) <= (image_length - 1) && checkIfBlack(qr_image.getPixel(x_slide + successive, y_slide))) //go right
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
        if((x_slide - successive) >= 0 && checkIfBlack(qr_image.getPixel(x_slide - successive, y_slide))) //go left
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

  /* -------------------------------------------------------------------------*/
  private static Coordinate findEdgeULR(int x_pos, int y_pos, Bitmap qr_image)
  {
    /* Unten Links Rechts */
    //System.out.println("Unten Links Rechts...");

    int x_slide = x_pos;
    int y_slide = y_pos;
    boolean move_flag;
    boolean potential_finish_flag = false;
    int successive = START_SUCCESSIVE;
    int image_length = qr_image.getWidth();
    int image_height = qr_image.getHeight();

    while(true)
    {
      move_flag = false;

      if((y_slide + successive) <= (image_height - 1) && checkIfBlack(qr_image.getPixel(x_slide, y_slide + successive))) //go down
      {
        move_flag = true;
        y_slide += successive;
        potential_finish_flag = false;
      }

      if((x_slide - successive) >= 0 && checkIfBlack(qr_image.getPixel(x_slide - successive, y_slide))) //go left
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
        if((x_slide + successive) <= (image_length - 1) && checkIfBlack(qr_image.getPixel(x_slide + successive, y_slide))) //go right
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


  /* -------------------------------------------------------------------------*/
  private static Coordinate findEdgeRUO(int x_pos, int y_pos, Bitmap qr_image)
  {
    /* Rechts Unten Oben */
    //System.out.println("Rechts Unten Oben...");
    
    int x_slide = x_pos;
    int y_slide = y_pos;
    boolean move_flag;
    boolean potential_finish_flag = false;
    int successive = START_SUCCESSIVE;
    int image_length = qr_image.getWidth();
    int image_height = qr_image.getHeight();
    
    while(true)
    {
      move_flag = false;

      if((x_slide + successive) <= (image_length - 1) && checkIfBlack(qr_image.getPixel(x_slide + successive, y_slide))) //go right
      {
        move_flag = true;
        x_slide += successive;
        potential_finish_flag = false;
      }

      if((y_slide + successive) <= (image_height - 1) && checkIfBlack(qr_image.getPixel(x_slide, y_slide + successive))) //go down
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
        if((y_slide - successive) >= 0 && checkIfBlack(qr_image.getPixel(x_slide, y_slide - successive))) // go up
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
