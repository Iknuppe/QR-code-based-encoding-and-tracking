
package com.example.tom.qrtrace;

import android.util.Log;
import java.util.ArrayList;
import java.util.List;

public class encodeMatrix
{
  private static final String TAG = "QRT";

  public static String encodeMatrix(int[][] qr_array)
  {
    int[] fvi = encodeFormatVersionInformation(qr_array);

    if(fvi == null)
    {
      return null;
    }

    int[][] marked_test_matrix = markStructureElements(qr_array);
    List<Integer> qr_bitstream = readQRData(marked_test_matrix, fvi);
    int mode = convertBinaryListToDecimal(qr_bitstream.subList(0, 4));

    if(!(mode == 0x1 || mode == 0x2 || mode == 0x4 || mode == 0x7 || mode == 0x8))
    {
      return null;
    }

    String character_string = null;

    switch(mode)
    {
      case 0x1:
      {
        int character_count = convertBinaryListToDecimal(qr_bitstream.subList(4, 14));
        List<Integer> qr_data = qr_bitstream.subList(14, qr_bitstream.size());

        try
        {
          character_string = encodeInNumericMode(qr_data, character_count);
        }
        catch(Exception e)
        {
          Log.d(TAG, "Error: numeric encoding failed: " + e.getMessage());
        }

        break;
      }
      case 0x2:
      {
        int character_count = convertBinaryListToDecimal(qr_bitstream.subList(4, 13));
        List<Integer> qr_data = qr_bitstream.subList(13, qr_bitstream.size());

        try
        {
          character_string = encodeInAlphanumericMode(qr_data, character_count);
        }
        catch(Exception e)
        {
          Log.d(TAG, "Error: alphanumeric encoding failed: " + e.getMessage());
        }

        break;
      }
      case 0x4:
      {
        int character_count = convertBinaryListToDecimal(qr_bitstream.subList(4, 12));
        List<Integer> qr_data = qr_bitstream.subList(12, qr_bitstream.size());

        try
        {
          character_string = encodeInByteMode(qr_data, character_count);
        }
        catch(Exception e)
        {
          Log.d(TAG, "Error: byte encoding failed: " + e.getMessage());
        }

        break;
      }
      case 0x8:
      {
        Log.d(TAG, "Kanji mode not implemented yet...");
        break;
      }
      case 0x7:
      {
        Log.d(TAG, "ECI mode not implemented yet...");
        break;
      }
      default: break;
    }

    return character_string;
  }

  private static int[] encodeFormatVersionInformation(int[][] qr_array)
  {
    int[] fvi = new int[15];
    int pos_counter = 0;
    int dimension = qr_array.length;

    for(int counter = 0 ; counter <= dimension - 1 ; counter++)
    {
      if(((counter > (dimension - 9)) || (counter < 8)) && (counter != 6))
      {
        fvi[pos_counter] = qr_array[8][counter];
        pos_counter++;
      }
    }

    for(int counter = 0 ; counter < 4 ; counter++)
    {
      if(fvi[fvi.length - counter - 1] != qr_array[counter][8])
      {
        return null;
      }
    }

    int[] fvi_mask = {1, 0, 1, 0, 1, 0, 0, 0, 0, 0, 1, 0, 0, 1, 0};

    for(int counter = 0 ; counter < fvi.length ; counter++)
    {
      fvi[counter] ^= fvi_mask[counter];
    }

    return fvi;
  }

  private static int getVersion(int[][] qr_array)
  {
    return (qr_array.length - 17)/4;
  }

  private static int[][] markStructureElements(int[][] qr_array)
  {
    int[][] marked_array = qr_array;
    int dimension = marked_array.length;
    int version = getVersion(qr_array);
    int x_value;
    int y_value;

    marked_array = markAreaOfArray(marked_array, 0, 8, 0, 8);
    marked_array = markAreaOfArray(marked_array, dimension - 8, dimension, 0, 8);
    marked_array = markAreaOfArray(marked_array, 0, 8, dimension - 8, dimension);

    marked_array = markAreaOfArray(marked_array, 0, dimension, 6, 7);
    marked_array = markAreaOfArray(marked_array, 6, 7, 0, dimension);

    marked_array = markAreaOfArray(marked_array, 0, 9, 8, 9);
    marked_array = markAreaOfArray(marked_array, dimension - 8, dimension, 8, 9);

    marked_array = markAreaOfArray(marked_array, 8, 9, 0, 9);
    marked_array = markAreaOfArray(marked_array, 8, 9, dimension - 7, dimension);

    marked_array[(4 * version) + 9][8] = 2;

    int[] alignment_list = getAlignmentPatterns(version);
    for(int counter_1 = 0 ; counter_1 < alignment_list.length ; counter_1++)
    {
      for(int counter_2 = 0 ; counter_2 < alignment_list.length ; counter_2++)
      {
        if(!((counter_1 == 0 && counter_2 == 0) || (counter_1 == (alignment_list.length - 1) && counter_2 == 0) || (counter_1 == 0 && counter_2 == (alignment_list.length - 1))))
        {
          x_value = alignment_list[counter_1];
          y_value = alignment_list[counter_2];
          marked_array = markAreaOfArray(marked_array, x_value - 2, x_value + 3, y_value - 2, y_value + 3);
        }
      }
    }

    return marked_array;
  }

  private static int[][] markAreaOfArray(int[][] marked_array, int x_start, int x_end, int y_start, int y_end)
  {
    for(int y_counter = y_start; y_counter < y_end; y_counter++)
    {
      for(int x_counter = x_start; x_counter < x_end; x_counter++)
      {
        marked_array[y_counter][x_counter] = 2;
      }
    }

    return marked_array;
  }

  private static List<Integer> readQRData(int[][] marked_qr_array, int[] fvi)
  {
    int dimension = marked_qr_array.length;
    int mask_number = (4 * fvi[2]) + (2 * fvi[3]) + (1 * fvi[4]);
    int[][] test_qr_matrix = marked_qr_array;
    List<Integer> qr_data_raw = new ArrayList<>();

    for(int horizontal_counter = dimension - 1; horizontal_counter >= 0; horizontal_counter -= 2)
    {
      for(int vertical_counter = dimension - 1; vertical_counter >= 0; vertical_counter--)
      {
        if(marked_qr_array[vertical_counter][horizontal_counter] != 2)
        {
          qr_data_raw.add(demaskBit(marked_qr_array, mask_number, horizontal_counter, vertical_counter));
          test_qr_matrix[vertical_counter][horizontal_counter] = demaskBit(marked_qr_array, mask_number, horizontal_counter, vertical_counter);
        }

        if(marked_qr_array[vertical_counter][horizontal_counter - 1] != 2)
        {
          qr_data_raw.add(demaskBit(marked_qr_array, mask_number, horizontal_counter - 1, vertical_counter));
          test_qr_matrix[vertical_counter][horizontal_counter] = demaskBit(marked_qr_array, mask_number, horizontal_counter - 1, vertical_counter);
        }
      }

      horizontal_counter -= 2;

      if(horizontal_counter == 6)
      {
        horizontal_counter--;
      }

      for(int vertical_counter = 0; vertical_counter < dimension; vertical_counter++)
      {
        if(marked_qr_array[vertical_counter][horizontal_counter] != 2)
        {
          qr_data_raw.add(demaskBit(marked_qr_array, mask_number, horizontal_counter, vertical_counter));
          test_qr_matrix[vertical_counter][horizontal_counter] = demaskBit(marked_qr_array, mask_number, horizontal_counter, vertical_counter);
        }

        if(marked_qr_array[vertical_counter][horizontal_counter - 1] != 2)
        {
          qr_data_raw.add(demaskBit(marked_qr_array, mask_number, horizontal_counter - 1, vertical_counter));
          test_qr_matrix[vertical_counter][horizontal_counter] = demaskBit(marked_qr_array, mask_number, horizontal_counter - 1, vertical_counter);
        }
      }
    }

    return qr_data_raw;
  }

  private static int demaskBit(int[][] marked_qr_array, int mask_number, int x_pos, int y_pos)
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
      return (marked_qr_array[y_pos][x_pos] - 1) * (-1);
    }
    else
    {
      return marked_qr_array[y_pos][x_pos];
    }
  }

  private static String encodeInNumericMode(List<Integer> qr_data_list, int character_count)
  {
    int index = 0;
    int rest = character_count % 3;
    String numbers = "";
    String helpstring = "";

    character_count = ((character_count - (character_count % 3))/3)*10 + 1;

    while(index != character_count)
    {
      helpstring = helpstring + (qr_data_list.get(index++)).toString();

      if(((index % 10) == 0) && (index != 0))
      {
        if(Integer.parseInt(helpstring, 2) < 99)
        {
          if(Integer.parseInt(helpstring, 2) < 9)
          {
            numbers += "0" + "0" + Integer.parseInt(helpstring, 2);
          }
          else
          {
            numbers += "0" + Integer.parseInt(helpstring, 2);
          }
        }
        else
        {
          numbers += Integer.parseInt(helpstring, 2);
        }

        helpstring = "";
      }
    }

    switch(rest)
    {
      case 0: break;
      case 1:
      {
        for(int counter = 1 ; counter < 4 ; counter++)
        {
          helpstring += (qr_data_list.get(index++)).toString();
        }

        numbers += Integer.parseInt(helpstring, 2);

        break;
      }
      case 2:
      {
        for(int counter = 1 ; counter < 7 ; counter++)
        {
          helpstring += (qr_data_list.get(index++)).toString();
        }

        if(Integer.parseInt(helpstring, 2) <= 9)
        {
          numbers += "0" + Integer.parseInt(helpstring, 2);
        }
        else
        {
          numbers += Integer.parseInt(helpstring, 2);
        }

        break;
      }
      default:break;
    }

    return numbers;
  }

  private static String encodeInAlphanumericMode(List<Integer> qr_data_list, int character_count)
  {
    int current_data_block;
    int first_number;
    int second_number;

    if((character_count/2)*11 > qr_data_list.size())
    {
      return null;
    }

    StringBuilder character_list = new StringBuilder(character_count);

    if(character_count % 2 == 0)
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
    else
    {
      for(int counter = 0 ; counter < ((character_count - 1)/2)*11 ; counter += 11)
      {
        current_data_block = convertBinaryListToDecimal(qr_data_list.subList(counter, counter + 11));
        second_number = current_data_block % 45;
        first_number = (current_data_block - second_number) / 45;
        character_list.append(convertToAlphanumericLetter(first_number));
        character_list.append(convertToAlphanumericLetter(second_number));
      }

      character_list.append(convertToAlphanumericLetter(convertBinaryListToDecimal(qr_data_list.subList(((character_count - 1)/2)*11,(((character_count - 1)/2)*11) + 6))));
    }

    return character_list.toString();
  }

  private static String encodeInByteMode(List<Integer> qr_data_list, int character_count) throws Exception
  {
    byte[] byte_data = new byte[character_count];

    for(int counter = 0 ; counter < character_count ; counter++)
    {
      byte_data[counter] = (byte)convertBinaryListToDecimal(qr_data_list.subList(8*counter, 8*(counter + 1)));
    }

    return new String(byte_data, "ISO-8859-1");
  }

  private static int convertBinaryListToDecimal(List<Integer> binary_list)
  {
    int result = 0;
    int list_size = binary_list.size() - 1;

    for(int counter = 0 ; counter < binary_list.size() ; counter++)
    {
      result += binary_list.get(list_size - counter)*((int)Math.pow(2, counter));
    }

    return result;
  }

  private static char convertToAlphanumericLetter(int number)
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

  private static int[] getAlignmentPatterns(int version)
  {
    switch(version)
    {
      case 1: return new int[]{6};
      case 2: return new int[]{6,18};
      case 3: return new int[]{6,22};
      case 4: return new int[]{6,26};
      case 5: return new int[]{6,30};
      case 6: return new int[]{6,34};
      case 7: return new int[]{6,22,38};
      case 8: return new int[]{6,24,42};
      case 9: return new int[]{6,26,46};
      case 10: return new int[]{6,28,50};
      case 11: return new int[]{6,30,54};
      case 12: return new int[]{6,32,58};
      case 13: return new int[]{6,34,62};
      case 14: return new int[]{6,26,46,66};
      case 15: return new int[]{6,26,48,70};
      case 16: return new int[]{6,26,50,74};
      case 17: return new int[]{6,30,54,78};
      case 18: return new int[]{6,30,56,82};
      case 19: return new int[]{6,30,58,86};
      case 20: return new int[]{6,34,62,90};
      case 21: return new int[]{6,28,50,72,94};
      case 22: return new int[]{6,26,50,74,98};
      case 23: return new int[]{6,30,54,78,102};
      case 24: return new int[]{6,28,54,80,106};
      case 25: return new int[]{6,32,58,84,110};
      case 26: return new int[]{6,30,58,86,114};
      case 27: return new int[]{6,34,62,90,118};
      case 28: return new int[]{6,26,50,74,98,122};
      case 29: return new int[]{6,30,54,78,102,126};
      case 30: return new int[]{6,26,52,78,104,130};
      case 31: return new int[]{6,30,56,82,108,134};
      case 32: return new int[]{6,34,60,86,112,138};
      case 33: return new int[]{6,30,58,86,114,142};
      case 34: return new int[]{6,34,62,90,118,146};
      case 35: return new int[]{6,30,54,78,102,126,150};
      case 36: return new int[]{6,24,50,76,102,128,154};
      case 37: return new int[]{6,28,54,80,106,132,158};
      case 38: return new int[]{6,32,58,84,110,136,162};
      case 39: return new int[]{6,26,54,82,110,138,166};
      case 40: return new int[]{6,30,58,86,114,142,170};
      default: return new int[]{};
    }
  }
}