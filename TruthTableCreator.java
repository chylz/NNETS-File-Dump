import java.io.*;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

/*
 * Author: Luke Zeng
 * Date of Creation: 3/15/2024
 *
 * Tool which helps convert truth table (both inputs and outputs) into binary input and output files.
 *
 * Table of Contents:
 *    public static void main(String[] args)
 *    public static void createTruthTable(String file, String newFile1, String newFile2)
 */

public class TruthTableCreator
{
/*
 * converts a given text file with inputs and outputs into 2 binary files, one with the inputs and one with outputs.
 */
   public static void main(String[] args)
   {
      if (args.length<3)
      {
         System.out.println("not enough command line arguments to make a truth table. Please add arguments in the form of " +
                            "java TruthTableCreator <truthtable> <outputfile>");
      }
      else
      {
/*
 * write truth table from first argument to the inputs file specified by second arg and outputs file on 3rd arg.
 */
         createTruthTable(args[0], args[1], args[2]);
      }
   }

/*
 * converts a space and line-separated truth table (both inputs and outputs) from the given file with numcases, inputs, and outputs on the top line
 * followed by all the inputs (doubles) row by row, followed by all the outputs (doubles) row by row.
 *
 * writes the inputs to file newFile1, writes outputs to newFile2
 */
   public static void createTruthTable(String file, String newFile1, String newFile2)
   {
/*
 * set default values for inputs, outputs, numCases, numOutputs, numINputs to prevent compiler from complaining.
 */
      double[][] inputs = null;
      double[][] outputs = null;
      int numCases = 0;
      int numOutputs = 0;
      int numInputs = 0;

      File newf;  // file to contain the encoded inputs.
      File newf2; // second file to contain endcoded truth table.

      boolean passed = true; // flag to see if the truth table made it past first round of checks.


      try
      {
         BufferedReader br = new BufferedReader(new FileReader(file));

         StringTokenizer st = new StringTokenizer(br.readLine());
         numCases = Integer.parseInt(st.nextToken());
         numInputs = Integer.parseInt(st.nextToken());
         numOutputs = Integer.parseInt(st.nextToken());

         inputs = new double[numCases][numInputs];
         outputs = new double[numCases][numOutputs];

         for (int cases = 0; cases < numCases; cases++)
         {
            st = new StringTokenizer(br.readLine()); // tokenize every line of the input.
            for (int input = 0; input < numInputs; input++)
            {
               inputs[cases][input] = Double.parseDouble(st.nextToken()); // read to temp array.
            }
         }

         for (int cases = 0; cases < numCases; cases++)
         {
            st = new StringTokenizer(br.readLine()); //tokenize every line of the output.
            for (int output = 0; output < numOutputs; output++)
            {
               outputs[cases][output] = Double.parseDouble(st.nextToken()); // read to temp array.
            }
         }

      } // try
      catch (FileNotFoundException e)
      {
         System.out.println("truth table file not found, please give a valid truth table file");
         passed = false;
      }
      catch (NoSuchElementException e)
      {
         System.out.println("Truth table invalid (ran out of columns in a row), please check its validity");
         passed = false;
      }
      catch (IOException e)
      {
         System.out.println("An error has occurred with Truth Table conversion. Please check that numInputs matches number of input rows, etc. ");
         passed = false;
      }

      if (passed)
      {
         try
         {
            newf = new File(newFile1);
            newf2 = new File(newFile2);

            newf.createNewFile();  // create new file to store inputs
            newf2.createNewFile(); // create new file to store outputs

            FileHandler.encodeIns(inputs, numInputs, numCases, newFile1);   // encodes inputs into binary file.
            FileHandler.encodeOuts(outputs, numOutputs, numCases, newFile2); // encodes outputs into binary file
         } // try
         catch (IOException e)
         {
            System.out.println("An error has occurred with saving the truth table. Please check permissions");
         }
      } // if (passed)

   } // public static void createTruthTable(String file, String newFile1, String newFile2)
} // public class TruthTableCreator
