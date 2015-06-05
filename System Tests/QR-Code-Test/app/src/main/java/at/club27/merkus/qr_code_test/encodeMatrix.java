/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.club27.merkus.qr_code_test;

import java.util.ArrayList;
import java.util.List;

public class encodeMatrix
{
  static String encodeMatrix(int[][] qr_array)
  {
    //Get format and version information
    int[] fvi = encodeFormatVersionInformation(qr_array);
    
    //Mark all structure elements in QR array
    int[][] marked_test_matrix = markStructureElements(qr_array);
    
    //Read raw data from QR array
    List<Integer> qr_bitstream = readQRData(marked_test_matrix, fvi);
    
    //Split raw data into mode indicator, character count and qr data
    int mode = convertBinaryListToDecimal(qr_bitstream.subList(0, 4));
    int character_count = convertBinaryListToDecimal(qr_bitstream.subList(4, 13));
    List<Integer> qr_data = qr_bitstream.subList(13, qr_bitstream.size());
    
    System.out.println("Mode: "+mode+" character count: "+character_count+" data size: "+qr_data.size());
    System.out.println("Convert to characters ...");
    
    //Create character list and select encoding method
    List<Character> character_list = new ArrayList<>();
    String character_string = new String();
    
    switch(mode)
    {
        case 0x1: //Numeric Mode
        {
            break;
        }
        case 0x2: //Alphanumeric Mode
        {
            character_string = encodeInAlphanumericMode(qr_data, character_count);
            break;
        }
        case 0x4: //Byte Mode
        {
            convertToCharList(qr_bitstream.subList(13, qr_bitstream.size()), character_list);
            break;
        }
        case 0x8: //Kanji Mode
        {
            break;
        }
        case 0x7: //ECI Mode
        {
            break;
        }
        default: break;
    }

    return character_string;
  }

  static int[] encodeFormatVersionInformation(int[][] qr_array)
  {
    //Format version information
    int[] fvi = new int[15]; 
    int pos_counter = 0;
    int dimension = qr_array.length;

    //Horizontal information:
    for(int counter = dimension - 1; counter >= 0; counter--)
    {
      if(((counter > (dimension - 9)) || (counter < 8)) && (counter != 6))
      {
        fvi[pos_counter] = qr_array[8][counter];
        pos_counter++;
      }
    }
    
    //check for match
      //vertical information:
      /*for(int counter = 0 ; counter < dimension ; counter++)
     {
     if(counter > (dimension - 7) && counter < 9 && counter != 6 && counter != (dimension - 8))
     {
     format_version_information.add(qr_array[counter][8]);
     }
     }*/
      //mischung für performance?
    
    //Mask for demasking fvi, specified in Qr specification
    int[] fvi_mask = {1, 0, 1, 0, 1, 0, 0, 0, 0, 0, 1, 0, 0, 1, 0};
    
    //Demask fvi
    for(int counter = 0; counter < fvi.length; counter++)
    {
      fvi[counter] ^= fvi_mask[counter];
    }
    
    return fvi;
  }

  static int[][] markStructureElements(int[][] qr_array)
  {
    int[][] marked_array = qr_array;
    int dimension = marked_array.length;

    //Finder patterns and sepperators:
    marked_array = markAreaOfArray(marked_array, 0, 8, 0, 8);//center square
    marked_array = markAreaOfArray(marked_array, dimension - 8, dimension, 0, 8);//x square
    marked_array = markAreaOfArray(marked_array, 0, 8, dimension - 8, dimension);//y_square

    //Timing patterns:
    marked_array = markAreaOfArray(marked_array, 0, dimension, 6, 7);//horizontal line
    marked_array = markAreaOfArray(marked_array, 6, 7, 0, dimension);//vertical line

    //Format and version information area:
    //horizontal information
    marked_array = markAreaOfArray(marked_array, 0, 9, 8, 9);
    marked_array = markAreaOfArray(marked_array, dimension - 8, dimension, 8, 9);
    //verical information
    marked_array = markAreaOfArray(marked_array, 8, 9, 0, 9);
    marked_array = markAreaOfArray(marked_array, 8, 9, dimension - 7, dimension);

    //dark module:
    marked_array[(4 * 1) + 9][8] = 2;

    //(Allignment patterns:)
    
    return marked_array;
  }

  static int[][] markAreaOfArray(int[][] marked_array, int x_start, int x_end, int y_start, int y_end)
  {
    //Mark rectangular parts of entire array
    for(int y_counter = y_start; y_counter < y_end; y_counter++)
    {
      for(int x_counter = x_start; x_counter < x_end; x_counter++)
      {
        marked_array[y_counter][x_counter] = 2;
      }
    }

    return marked_array;
  }

  //read the data from marked array
  static List<Integer> readQRData(int[][] marked_qr_array, int[] fvi)
  {
    int dimension = marked_qr_array.length;
    int mask_number = (4 * fvi[2]) + (2 * fvi[3]) + (1 * fvi[4]);
    
    List<Integer> qr_data_raw = new ArrayList<>();
    
    System.out.println("Mask number: " + mask_number);

    //Snake reading:
    //Go from right side to left side of QR array
    for(int horizontal_counter = dimension - 1; horizontal_counter >= 0; horizontal_counter -= 2)
    {
      //Up-reading column
      for(int vertical_counter = dimension - 1; vertical_counter >= 0; vertical_counter--)
      {
        //Read first bit
        if(marked_qr_array[vertical_counter][horizontal_counter] != 2)
        {
          qr_data_raw.add(demaskBit(marked_qr_array, mask_number, horizontal_counter, vertical_counter));
        }
        
        //Read second bit
        if(marked_qr_array[vertical_counter][horizontal_counter - 1] != 2)
        {
          qr_data_raw.add(demaskBit(marked_qr_array, mask_number, horizontal_counter - 1, vertical_counter));
        }
      }

      //When top is reached, move left to next column and switch to down-reading
      horizontal_counter -= 2;

      //Consider vertical timing pattern case
      if(horizontal_counter == 6)
      {
        horizontal_counter--;
      }

      //Down-reading column
      for(int vertical_counter = 0; vertical_counter < dimension; vertical_counter++)
      {
        //Read first bit
        if(marked_qr_array[vertical_counter][horizontal_counter] != 2)
        {
           qr_data_raw.add(demaskBit(marked_qr_array, mask_number, horizontal_counter, vertical_counter));
        }

        //Read second bit
        if(marked_qr_array[vertical_counter][horizontal_counter - 1] != 2)
        {
          qr_data_raw.add(demaskBit(marked_qr_array, mask_number, horizontal_counter - 1, vertical_counter));
        }
      }
    }

    return qr_data_raw;
  }

  static int demaskBit(int[][] marked_qr_array, int mask_number, int x_pos, int y_pos)
  {
    int result;
    
    switch(mask_number)
    {
      case 0: result = (x_pos + y_pos) % 2; break;
      case 1: result = y_pos % 2; break;
      case 2: result = x_pos % 3; break;
      case 3: result = (x_pos + y_pos) % 3; break;
      case 4: result = ((int) (Math.floor((double) (y_pos / 2)) + Math.floor((double) (x_pos / 3)))) % 2; break;
      case 5: result = ((x_pos * y_pos) % 2) + ((x_pos * y_pos) % 3); break;
      case 6: result = (((x_pos * y_pos) % 2) + ((x_pos * y_pos) % 3)) % 2; break;
      case 7: result = (((x_pos + y_pos) % 2) + ((x_pos * y_pos) % 3)) % 2; break;
      default: result = -1; break;
    }
    
    if(result == 0)
    {
      //Return inverted current bit
      return (marked_qr_array[y_pos][x_pos] - 1) * (-1);
    }
    else
    {
      //Return current bit
      return marked_qr_array[y_pos][x_pos];
    }
  }

  static void convertToCharList(List<Integer> int_list, List<Character> char_list)
  {
    int sum = 0;
    int j = 0;
    
    for(int i = 1; i < int_list.size()+1; i++) //start at i = 1, because i modulo 0 is 0 
    {
      sum += int_list.get(i-1) * (int)Math.pow(2, j);
      j++;  
      
      if(i % 8 == 0)
      {
        char_list.add((char)sum);
        sum = 0;
        j = 0; 
      }
    }
    
    char_list.add((char)sum);
  }
    
  static String encodeInAlphanumericMode(List<Integer> qr_data_list, int character_count)
  {
      int current_data_block;
      int first_number;
      int second_number;
      
      StringBuilder character_list = new StringBuilder(character_count);
      
      if(character_count % 2 == 0)  //Even number of characters
      {
        for(int counter = 0 ; counter < (character_count/2)*11 ; counter += 11)
        {
          current_data_block = convertBinaryListToDecimal(qr_data_list.subList(counter, counter + 11));
          second_number = current_data_block % 45;
          first_number = (current_data_block - second_number) / 45;
          character_list.append(convertToAlphanumericLetter(first_number));
          character_list.append(convertToAlphanumericLetter(second_number));
        }
      }
      else  //Odd number of characters
      {
        for(int counter = 0 ; counter < ((character_count - 1)/2)*11 ; counter += 11)
        {
          current_data_block = convertBinaryListToDecimal(qr_data_list.subList(counter, counter + 11));
          second_number = current_data_block % 45;
          first_number = (current_data_block - second_number) / 45;
          character_list.append(convertToAlphanumericLetter(first_number));
          character_list.append(convertToAlphanumericLetter(second_number));
        }
      
        //Get last letter
        character_list.append(convertToAlphanumericLetter(convertBinaryListToDecimal(qr_data_list.subList(((character_count - 1)/2)*11,(((character_count - 1)/2)*11) + 6))));
      }

      return character_list.toString();
  }
    
  static int convertBinaryListToDecimal(List<Integer> binary_list)
  {
        int result = 0;
        int list_size = binary_list.size() - 1;
        
        for(int counter = 0 ; counter < binary_list.size() ; counter++)
        {
            result += binary_list.get(list_size - counter)*((int)Math.pow(2, counter));
        }

        return result;
  }
    /*
    static int convertBinaryArrayToDecimal(int[] binary_array)
    {
        int result = 0;
        
        for(int counter = 0 ; counter < binary_array.length ; counter++)
        {
            result += binary_array[counter]*((int)Math.pow(2, counter));
        }
        
        return  result;
    }
    */
    
    static char convertToAlphanumericLetter(int number)
    {
      switch(number)
      {
        case 0: return '0';
        case 1: return '1';
        case 2: return '2';
        case 3: return '3';
        case 4: return '4';
        case 5: return '5';
        case 6: return '6';
        case 7: return '7';
        case 8: return '8';
        case 9: return '9';
        case 10: return 'A';
        case 11: return 'B';
        case 12: return 'C';
        case 13: return 'D';
        case 14: return 'E';
        case 15: return 'F';
        case 16: return 'G';
        case 17: return 'H';
        case 18: return 'I';
        case 19: return 'J';
        case 20: return 'K';
        case 21: return 'L';
        case 22: return 'M';
        case 23: return 'N';
        case 24: return 'O';
        case 25: return 'P';
        case 26: return 'Q';
        case 27: return 'R';
        case 28: return 'S';
        case 29: return 'T';
        case 30: return 'U';
        case 31: return 'V';
        case 32: return 'W';
        case 33: return 'X';
        case 34: return 'Y';
        case 35: return 'Z';
        case 36: return ' ';
        case 37: return '$';
        case 38: return '%';
        case 39: return '*';
        case 40: return '+';
        case 41: return '-';
        case 42: return '.';
        case 43: return '/';
        case 44: return ':';
        default: return (char)(-1);
      }
    }
}