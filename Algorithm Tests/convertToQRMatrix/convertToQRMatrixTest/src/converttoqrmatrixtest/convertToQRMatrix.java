
package converttoqrmatrixtest;

import java.util.ArrayList;
import java.util.List;
import static java.lang.Math.round;

public class convertToQRMatrix
{
  /* ------------------------------------------------------------------------ */
  private static final int ORIGIN = 0;
  private static final int X_AXIS = 1;
  private static final int Y_AXIS = 2;
  public static int[][] convertToQRMatrix(int[][] image_array)
  {
    List<Square> finder_patterns = identifyFinderPatterns(image_array);
    if(finder_patterns.size() != 3)
    {
      System.out.println("Can't see finder patterns ...");
      return null;
    }

    //Calculate length and width of one matrix segment
    double delta_x = finder_patterns.get(ORIGIN).getLength()/3;
    double delta_y = finder_patterns.get(ORIGIN).getHeight()/3;
    
    System.out.println("delta_x: " + delta_x + " delta_y: " + delta_y);

    //Determine dimensions of QR matrix
    int matrix_dimension_x = ((int)round(finder_patterns.get(X_AXIS).getCenter().crdNorm(finder_patterns.get(ORIGIN).getCenter()) / delta_x)) + 6 + 1;
    int matrix_dimension_y = ((int)round(finder_patterns.get(Y_AXIS).getCenter().crdNorm(finder_patterns.get(ORIGIN).getCenter()) / delta_y)) + 6 + 1;
    
    System.out.println("matrix_dimension_x: " + matrix_dimension_x + " (" + ((finder_patterns.get(X_AXIS).getCenter().crdNorm(finder_patterns.get(ORIGIN).getCenter()) / delta_x) + 6 + 1) + ")" + " matrix_dimension_y: " + matrix_dimension_y + " (" + ((finder_patterns.get(Y_AXIS).getCenter().crdNorm(finder_patterns.get(ORIGIN).getCenter()) / delta_y) + 6 + 1) + ")");
    
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
    int[][] qr_array = new int[matrix_dimension_y][matrix_dimension_x];

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
        
        qr_array[y_counter][x_counter] = image_array[y_pos][x_pos];
      }
    }

    return qr_array;
  }
  
  /* ------------------------------------------------------------------------ */
  public static List<Square> identifyFinderPatterns(int[][] image_array)
  {
    //Try to find all 3 finder patterns
    List<Square> finder_patterns = findSquares(image_array);
    
    if(finder_patterns.size() != 3)
    {
      //Unable to find 3 finder patterns
      return finder_patterns;
    }
    
    debugInformation(finder_patterns);
    
    //Identify index of center square and both axis
    int origin = findOrigin(finder_patterns);
    int x_axis = findXaxis(finder_patterns, origin);
    int y_axis = 3 - origin - x_axis;
    
    //Generate new square list with finding pattern center coordinates
    //This is done, because of later calculations of delta_x and delta_y
    //Could be optimised in later versions...
    List<Square> sorted_finder_patterns = new ArrayList<>();
    
    sorted_finder_patterns.add(redEdgeAlgorithm((int)finder_patterns.get(origin).getCenter().getXcrd(), (int)finder_patterns.get(origin).getCenter().getYcrd(), image_array));
    sorted_finder_patterns.add(redEdgeAlgorithm((int)finder_patterns.get(x_axis).getCenter().getXcrd(), (int)finder_patterns.get(x_axis).getCenter().getYcrd(), image_array));
    sorted_finder_patterns.add(redEdgeAlgorithm((int)finder_patterns.get(y_axis).getCenter().getXcrd(), (int)finder_patterns.get(y_axis).getCenter().getYcrd(), image_array));
    
    return sorted_finder_patterns;
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
  private static final int VALID_SQUARE = 1;
  private static final int INVALID_SQUARE = 0;
  public static int checkSquare(Square current_square, List<Square> square_list)
  { 
    if(current_square == null)
    {
      return INVALID_SQUARE;
    }

    /* add first square */
    if(square_list.isEmpty())
    {
      return VALID_SQUARE;
    }
    
    if((current_square.getAreaSize() + SIZE_TOLERANCE) < square_list.get(square_list.size() - 1).getAreaSize())
    {
      return INVALID_SQUARE;
    }
    
    //System.out.println("new Area size: "+current_square.getAreaSize());
    
    for(int counter = 0 ; counter < square_list.size() ; counter++)
    {
      if(current_square.getCenter().crdCheckEqual(square_list.get(counter).getCenter()) == 1)
      {
        square_list.remove(counter);
        return VALID_SQUARE;
      }
    }

    /* if there are already 3 large squares, remove the first square, because it must be the smallest */
    if(square_list.size() == 3)
    {
      square_list.remove(0);
    }

    return VALID_SQUARE;
  }

  /* ------------------------------------------------------------------------ */
  private static final int HORIZONTAL_SCALE_FACTOR = 10;
  private static final int VERTICAL_SCALE_FACTOR = 10;
  public static List<Square> findSquares(int[][] image_array)
  {
    System.out.println("Finding squares in " + image_array[0].length + ":" + image_array.length + "...");
    int image_dimension_length = image_array[0].length;//x
    int image_dimension_height = image_array.length;//y
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
        current_square = redEdgeAlgorithm(horizontal_counter, vertical_counter, image_array);

        /* check if current square is valid */
        if(checkSquare(current_square, square_list) == VALID_SQUARE)
        {
          square_list.add(current_square);
        }
      }
    }

    return square_list;
  }

  /* ------------------------------------------------------------------------ */
  public static Square redEdgeAlgorithm(int horizontal_counter, int vertical_counter, int[][] image_array)
  {
    List<Coordinate> edges = new ArrayList<>();

    /* check if start point is dark */
    if(image_array[vertical_counter][horizontal_counter] != 0)
    {
      return null;
    }

    edges.add(findEdgeLOU(horizontal_counter, vertical_counter, image_array)); //Left Up Down (Links Oben Unten)
    edges.add(findEdgeORL(horizontal_counter, vertical_counter, image_array)); //Up Right Left (Oben Rechts Links)
    edges.add(findEdgeULR(horizontal_counter, vertical_counter, image_array)); //Down Left Right (Unten Links Rechts)
    edges.add(findEdgeRUO(horizontal_counter, vertical_counter, image_array)); //Right Down Up (Rechts Unten Oben)

    //Check if edges form a square
    if(checkEdges(edges) == 1)
    {
      return new Square(edges);
    }
    else
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
  
  /* -------------------------------------------------------------------------*/
  public static Coordinate findEdgeLOU(int x_pos, int y_pos, int[][] image_array)
  {
    /* Links Oben Unten */
    //System.out.println("Links Oben Unten...");
    
    int x_slide = x_pos;
    int y_slide = y_pos;
    int potential_finish_flag = 0;
    int successive = 4;

    
    while(true)
    {
      if((x_slide - successive) >= 0 && image_array[y_slide][x_slide - successive] != 1)//go left
      {
        potential_finish_flag = 0;
        x_slide -= successive;
      }else if((y_slide - successive) >= 0 && image_array[y_slide - successive][x_slide] != 1) //go up
      {
        y_slide -= successive;
        if(potential_finish_flag == 1)
        {
          if(successive > 1)
          {
            successive /= 2;
            potential_finish_flag = 0;
          }
          else
          {
            break;
          }
        }
      }else if((y_slide + 1) <= (image_array.length - 1) && image_array[y_slide + 1][x_slide] != 1 && successive <= 1) //go down
      {
        potential_finish_flag = 1;
        y_slide += 1;
      }else
      {
        if(successive > 1)
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
  public static Coordinate findEdgeORL(int x_pos, int y_pos, int[][] image_array)
  {
    /* Oben Rechts Links */
    //System.out.println("Oben Rechts Links...");
    
    int x_slide = x_pos;
    int y_slide = y_pos;
    int potential_finish_flag = 0;
    int successive = 4;
    
    while(true)
    {
      if((y_slide - successive) >= 0 && image_array[y_slide - successive][x_slide] != 1) //go up
      {
        potential_finish_flag = 0;
        y_slide--;
      }else if((x_slide + successive) <= (image_array[0].length - 1) && image_array[y_slide][x_slide + successive] != 1) //go right
      {
        x_slide++;
        if(potential_finish_flag == 1)
        {
          if(successive > 1)
          {
            successive /= 2;
            potential_finish_flag = 0;
          }
          else
          {
            break;
          }
        }
      }else if((x_slide - successive) >= 0 && image_array[y_slide][x_slide - successive] != 1) //go left
      {
        potential_finish_flag = 1;
        x_slide--;
      }else
      {
        if(successive > 1)
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
  public static Coordinate findEdgeULR(int x_pos, int y_pos, int[][] image_array)
  {
    /* Unten Links Rechts */
    //System.out.println("Unten Links Rechts...");
    
    int x_slide = x_pos;
    int y_slide = y_pos;
    int potential_finish_flag = 0;
    int successive = 4;

    while(true)
    {
      if((y_slide + successive) <= (image_array.length - 1) && image_array[y_slide + successive][x_slide] != 1) //go down
      {
        potential_finish_flag = 0;
        y_slide++;
      }else if((x_slide - successive) >= 0 && image_array[y_slide][x_slide - successive] != 1) //go left
      {
        x_slide--;
        if(potential_finish_flag == 1)
        {
          if(successive > 1)
          {
            successive /= 2;
            potential_finish_flag = 0;
          }
          else
          {
            break;
          }
        }
      }else if((x_slide + successive) <= (image_array[0].length - 1) && image_array[y_slide][x_slide + successive] != 1) //go right
      {
        potential_finish_flag = 1;
        x_slide++;
      }else
      {
        if(successive > 1)
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
  public static Coordinate findEdgeRUO(int x_pos, int y_pos, int[][] image_array)
  {
    /* Rechts Unten Oben */
    //System.out.println("Rechts Unten Oben...");
    
    int x_slide = x_pos;
    int y_slide = y_pos;
    int potential_finish_flag = 0;
    int successive = 4;
    
    while(true)
    {
      if((x_slide + successive) <= (image_array[0].length - 1) && image_array[y_slide][x_slide + successive] != 1) //go right 
      {
        potential_finish_flag = 0;
        x_slide++;
      }else if((y_slide + successive) <= (image_array.length - 1) && image_array[y_slide + successive][x_slide] != 1) //go down 
      {
        y_slide++;
        if(potential_finish_flag == 1)
        {
          if(successive > 1)
          {
            successive /= 2;
            potential_finish_flag = 0;
          }
          else
          {
            break;
          }
        }
      }else if((y_slide - successive) >= 0 && image_array[y_slide - successive][x_slide] != 1) // go up 
      {
        potential_finish_flag = 1;
        y_slide--;
      }else
      {
        if(successive > 1)
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
  
  /* ------------------------------------------------------------------------ */
  public static Coordinate dynamicFindEdge(int x_pos, int y_pos, int[][] image_array, String order)
  {
    int x_slide = x_pos;
    int y_slide = y_pos;
    boolean potential_finish_flag = false;
    boolean break_flag;
    int search_length = 1;
    
    while(true)
    {
      break_flag = false;
      //System.out.println(search_length);
      for(int counter = 0 ; counter < 3 && break_flag == false ; counter++)
      {
        switch(order.charAt(counter))
        {
          case 'L':
          {
            if((x_slide - 0) > 0 && image_array[y_slide][x_slide - search_length] != 1)
            {
              x_slide -= search_length;
              break_flag = true;
            }
            break;
          }
          case 'R':
          {
            if((x_slide + 0) < (image_array[0].length - 1) && image_array[y_slide][x_slide + search_length] != 1)
            {
              x_slide += search_length;
              break_flag = true;
            }
            break;
          }
          case 'U':
          {
            if((y_slide - 0) > 0 && image_array[y_slide - search_length][x_slide] != 1)
            {
              y_slide -= search_length;
              break_flag = true;
            }
            break;
          }
          case 'D':
          {
            if((y_slide + 0) < (image_array.length - 1) && image_array[y_slide + search_length][x_slide] != 1)
            {
              y_slide += search_length;
              break_flag = true;
            }
            break;
          }
          default:
          {
            return null;
          }
        }

        if(break_flag == true)
        {
          switch(counter)
          {
            case 0: potential_finish_flag = false; break;
            case 1: 
            {
              if(potential_finish_flag == true)
              {
                return new Coordinate(x_slide, y_slide);
              }
              else
              {
                break;
              }
            }
            case 2: potential_finish_flag = true; break;
            default: break;
          }
        }
      }
      
      if(break_flag == false)
      {/*
        if(search_length > 1)
        {
          search_length /= 2;
        }
        else
        {
              */
          return new Coordinate(x_slide, y_slide);
        //}
      }
    }
  }
}
