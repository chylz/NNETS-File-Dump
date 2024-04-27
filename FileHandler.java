import java.io.*;

/*
 * Author: Luke Zeng
 * Date of Creation: 2/23/2024
 *
 * Description: Handles file IO of weights and inputs and expected outputs for the neural network.
 * Contains utilities for storing and loading both truth tables and weights.
 *
 * Table of Contents:
 *    public FileHandler(String weights)
 *    public void changeFile(String weights)
 *    public void writeWeights(double[][][] w, int[] numNodes, int numLayers)
 *    public boolean loadWeights(double[][][] w, int[] numNodes, int numLayers) throws IOException
 *    public static void encodeIns(double[][] inputs, int numInputs, int numCases, String filename) throws IOException
 *    public static void encodeOuts(double[][] outputs, int numOutputs, int numCases, String filename) throws IOException
 */

public class FileHandler
{
   public String network;        // network weights file name
   public DataInputStream ninp;  // file reader
   public DataOutputStream nout; // file writer (output)



/*
 * Creates a new file handler object with the given weight file
 */
   public FileHandler(String weights)
   {
      network = weights;
   }

/*
 * changes the weights file to the new one.
 */
   public void changeFile(String weights)
   {
      network = weights;
   }

/*
 * returns sum of the given array.
 */
   public int sum(int[] numNodes)
   {
      int agg = 0;
      for (int num : numNodes)
      {
         agg+=num;
      }
      return agg;
   }
/*
 * returns the weights sum of the given array
 */
   public int sumw (int[] numNodes)
   {
      int sum = 0;
      for (int index = 0; index < numNodes.length-1; index++)
      {
         sum+=numNodes[index]*numNodes[index+1];
      }
      return sum;
   }
/*
 * Writes the network weights to the file.
 * Requires the input of the network configuration
 */
   public void writeWeights(double[][][] w, int[] numNodes, int numLayers)
   {
      try
      {
         File f = new File(network);
         f.createNewFile();
         nout = new DataOutputStream(new FileOutputStream(f)); // helps with writing to file.

         ByteArrayOutputStream bo = new ByteArrayOutputStream();
         DataOutputStream doe = new DataOutputStream(bo);

         for (int n = 0; n < numLayers; n++)
         {
            for (int k = 0; k < numNodes[n]; k++) // write w[CON2] weights to file.
            {
               for (int j = 0; j < numNodes[n+1]; j++)
               {
                  doe.writeDouble(w[n][k][j]);
               }
            }
         }

         nout.write(bo.toByteArray());
         nout.close();
      } // try
      catch (IOException e)
      {
         e.printStackTrace();
         System.out.println("Error in saving weights. Please check file permissions and presence. Program will continue to run until aborted by user");
      }


   } // public void writeWeights(double[][][] w, ...

/*
 * Loads the weights into the given weight arrays with given network configuration.
 * if the end of the reader is reached and there are still values (IE, weights file is invalid)
 * return false.
 * Will throw an error if the file is not found, handled in the main network file.
 */
   public boolean loadWeights(double[][][] w, int[] numNodes, int numLayers) throws IOException
   {
      ninp = new DataInputStream((new FileInputStream(network))); // helps with inputting from file.
      byte[] b = new byte[sumw(numNodes)*8];
      ninp.read(b);
      ninp = new DataInputStream(new ByteArrayInputStream(b));

      boolean tooshort = false; // flag to indicate the file is too short.
      boolean toolong = true;   // flag to see if the file is too long.
      byte test;

      try
      {
         for (int n = 0; n < numLayers; n++)
         {
            for (int k = 0; k < numNodes[n]; k++)
            {
               for (int j = 0; j < numNodes[n+1]; j++)
               {
                  w[n][k][j] = ninp.readDouble();
               }
            }
         }
      } // try
      catch (EOFException e)
      {
         tooshort = true;
      }

      if (!tooshort) // if return is still true, see if there are more bytes to read.
      {
         try
         {
            test = ninp.readByte(); // test to see if there is anything else left in the file
         }
         catch (EOFException e) // if nothing left in file, the file is just right.
         {
            toolong = false;
         }
      } // if (!tooshort)

      ninp.close();

      return !(tooshort || toolong); // only return true if the number of weights is just right.

   } // public void loadWeights(double[][][] w, ...

/*
 * encodes the given inputs into a binary file, given inputs, number of inputs, and number of cases.
 */
   public static void encodeIns(double[][] inputs, int numInputs, int numCases, String filename) throws IOException
   {
      DataOutputStream out = new DataOutputStream(new FileOutputStream(filename)); // file writer.

      for (int cases = 0; cases < numCases; cases++)
      {
         for (int input = 0; input < numInputs; input++)
         {
            out.writeDouble(inputs[cases][input]);
         }
      }
      out.close();
   } // public static void encodeIns (double[][] inputs, ...

/*
 * encodes the given truth table into the given binary file, specifying number of outputs and number of cases.
 * returns true if the truth table dimensions and file are valid.
 */
   public static void encodeOuts(double[][] outputs, int numOutputs, int numCases, String filename) throws IOException
   {
      DataOutputStream out = new DataOutputStream(new FileOutputStream(filename)); // file writer.


      for (int cases = 0; cases < numCases; cases++)
      {
         for (int output = 0; output < numOutputs; output++)
         {
            out.writeDouble(outputs[cases][output]);
         }
      }

      out.close();

   } // public static void encodeOuts(double[][] outputs, ...


/*
 * takes in the file for inputs and puts the values in the given input array.
 * returns true if the entire file are valid inputs.
 */
   public static boolean decodeIns(double[][] inputs, String filename,
                                   int numInputs, int numCases) throws IOException
   {
      DataInputStream in = new DataInputStream(new FileInputStream(filename)); // file reader.
      byte[] b = new byte[numInputs*numCases*Double.BYTES]; // size of input file.
      in.read(b);

      in = new DataInputStream(new ByteArrayInputStream(b));

      boolean tooshort = false;
      boolean toolong = true;
      byte test;

      try
      {
         for (int cases = 0; cases < numCases; cases++)
         {
            for (int input = 0; input < numInputs; input++)
            {
               inputs[cases][input] = in.readDouble();
            }
         }

      } // try
      catch (IOException e)
      {
         tooshort = true;
      }


      try
      {
         test = in.readByte(); // test to see if file is too long (not fitting with dimensions of the outputs)
      }
      catch (IOException e) // if input runs out of items to read, the file is not too long.
      {
         toolong = false;
      }

      in.close();
      return !(tooshort || toolong);
   } // public static void decodeIns(double[][] inputs ...

/*
 * takes in the file for truth table of for given number of outputs and # of cases.
 * returns true if the entire file is valid truth table.
 */
   public static boolean decodeOuts(double[][] outputs, String filename,
                                   int numOutputs, int numCases) throws IOException
   {
      DataInputStream in = new DataInputStream(new FileInputStream(filename)); // file reader.
      byte[] b = new byte[numOutputs*numCases*Double.BYTES];
      in.read(b);

      in = new DataInputStream(new ByteArrayInputStream(b));


      boolean tooshort = false;
      boolean toolong = true;
      byte test;

      try
      {
         for (int cases = 0; cases < numCases; cases++)
         {
            for (int output = 0; output < numOutputs; output++)
            {
               outputs[cases][output] = in.readDouble();
            }
         }

      } // try
      catch (IOException e)
      {
         tooshort = true;
      }


      try
      {
         test = in.readByte(); // test to see if file is too long (not fitting with dimensions of the outputs)
      }
      catch (IOException e) // if input runs out of items to read, the file is not too long.
      {
         toolong = false;
      }

      in.close();
      return !(tooshort || toolong);
   } // public static void decodeOuts(double[][] outputs ...


} // public class FileHandler
