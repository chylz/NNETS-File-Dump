import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;

/*
 * Author: Luke Zeng
 * Date of Creation: 3/8/2024
 *
 * Handles all the File IO for the network (configuration, weights, truth table)
 * Implements an interpreter for one version of LOML (Luke's Obvious Minimal Language) bytecode.
 *
 * Table of Contents:
 *    public Config(String Filename) throws IOException
 *    public boolean loadConfig()
 */


public class Config
{

/*
 * file IO vars.
 */
   public String config;        // configuration file name.
   public DataInputStream inp;  // file reader
   public boolean loaded;
   public boolean validfile;

/*
 * file parameters vars.
 */
   public double lambda;         // step size multiplier for gradient descent.
   public int numInputNodes;
   public int[] numNodes;
   public int numActLayers;
   public int numLayers;
   public int numOutNodes;       // number of output nodes.
   public int populationMethod;  // population method --> 0: randomize weights | 1: manually set weights | 2: File IO
   public boolean train;         // if true, train. if false, run.
   public double minRand;        // minimum random threshold
   public double maxRand;        // maximum random threshold.
   public double avgErrCut;      // average error cutoff (error threshold).
   public int maxIterations;     // maximum iterations before cutoff.
   public boolean printWeight;   // flag to indicate whether to print weights before running.
   public boolean printTruths;   // flag to indicate whether to print the truth table.
   public boolean runAfterTrain; // indicates whether the network should run after training.
   public boolean saveWeights;   // indicates to save weights to the file named FILENAME.
   public int numCases;          // number of cases in the truth table.
   public boolean savediff;
   public int keepAlive = 0;     // number of iterations to report results after (default value of zero).
   public int saveTime;          // how often to save weights.
   public String weightsFile;    // File for the weights of the network.
   public String outputWeights;
   public String inputs;         // File for the inputs of the network.
   public String outputs;        // File for the expected outputs of network.
   public String actfunct;       // activation function name (lowercase)
   public InputValidation valid; // stores input validation booleans (1 for each configuration parameter indicating it has been defined)


/*
 * Constructs a configuration handler with the given config file to read from and loads the configuration.
 */
   public Config(String Filename) throws IOException
   {
      config = Filename;
      inp = new DataInputStream(new FileInputStream(config));
      valid = new InputValidation();
      validfile = true;
      loaded = false;
      loadConfig();
      if(!validfile)
      {
         System.out.println("booboofile"); // REPLACE THIS WITH AN ACTUAL ERROR REPORTING.
      }

      inp.close();

   } // public Config(String Filename) throws IOException



/*
 * reads the given file in and stores the configurations for that file (after this, cannot load any more
 * configurations into this instance of the object to prevent concurrent modification).
 * returns true if loading was successful (return value doesn't really matter right now, will be used in future).
 */
   public boolean loadConfig()
   {
      boolean ret = true;
      boolean done = false;
      if(loaded) // indicates if the file was already loaded (can only load once).
      {
         ret = false;
      }
      int curCommand = 0; // dummy variable to indicate current section.

      while (!done && !loaded && validfile) // small interpreter to interpret the input file into the config params.
      {

         //System.out.println("DEBUG: hi");
         try
         {
            int stringLength; // temporary variable to read string length from commands. .
            int command = inp.readInt();
            //System.out.println("DEBUG " + command);
            switch (command) // for each case the int represents a compiled tag for each parameter in config file.
            {
               case 0: // command 0 indicates the config file has ended.
                  done = true;
                  break;
               case 1: // indicate that current section is 1 (config)
                  curCommand = command;
                  break;
               case 2: // indicate number of input nodes (DEPRECATED)
                  numInputNodes = inp.readInt();
                  valid.numInputDefined = true;
                  break;
               case 3: // command to set number of output nodes (DEPRECATED)
                  numOutNodes = inp.readInt();
                  valid.numOutDefined = true;
                  break;
               case 4: // command to set population method.
                  populationMethod = inp.readInt();
                  valid.popMethodDefined = true;
                  break;
               case 5: // command to set train method.
                  train = inp.readInt() == 1; // Syntax for compiled .bin in configuration file is 1 for train is true.
                  valid.trainDefined = true;
                  break;
               case 6: // command to set minimum random value.
                  minRand = inp.readDouble();
                  valid.minRandDefined = true;
                  break;
               case 7: // command to set minimum random value. 
                  maxRand = inp.readDouble();
                  valid.maxRandDefined = true;
                  break;
               case 8: // command to set average error cutoff. 
                  avgErrCut = inp.readDouble();
                  valid.avgErrCutDefined = true;
                  break;
               case 9: // command to set maximum iterations. 
                  maxIterations = inp.readInt();
                  valid.maxIterationsDefined = true;
                  break;
               case 10: // command to set value of printweight. 
                  printWeight = inp.readInt() == 1; // Syntax for compiled .bin in configuration file is 1 for when param is true.
                  valid.printWeightDefined = true;
                  break;
               case 11: // command to set printTruths
                  printTruths = inp.readInt() == 1; // Syntax for compiled .bin in configuration file is 1 for when param is true.
                  valid.printTruthsDefined = true;
                  break;
               case 12: // command to set runAfterTrain
                  runAfterTrain = inp.readInt() == 1; // Syntax for compiled .bin in configuration file is 1 for when param is true.
                  valid.runAfterDefined = true;
                  break;
               case 13: // command to save Weights
                  saveWeights = inp.readInt() == 1; // Syntax for compiled .bin in configuration file is 1 for when param is true.
                  valid.saveWeightDefined = true;
                  break;
               case 14: // command to set activation function
                  actfunct = ""; // length of the weights file name.
                  stringLength = inp.readInt(); // length of the activation function string name.
                  for (int index = 0; index < stringLength; index++)
                  {
                     actfunct+=inp.readChar();
                  }
                  valid.actfunctDefined = true;
                  break;
               case 15: // command to set numcases.
                  numCases = inp.readInt();
                  valid.numCasesDefined = true;
                  break;
               case 16: // command to read lambda.
                  lambda = inp.readDouble();
                  valid.lambdaDefined = true;
                  break;
               case 17: // command for number of hidden nodes.
                  numActLayers = inp.readInt();
                  numLayers = numActLayers-1; // number of layers is always number of activation layers minus 1.
                  numNodes = new int[numActLayers];

                  for (int al = 0; al < numActLayers; al++)
                  {
                     numNodes[al] = inp.readInt();
                  }

                  valid.numHiddenDefined = true;
                  break;
               case 18: // command to set keepAlive
                  keepAlive = inp.readInt();
                  break;
               case 19: // dummy command for weights section.
                  curCommand = command;
                  break;
               case 20: // command for weights section.
                  stringLength = inp.readInt(); // length of the weights file name.

                  weightsFile = "";
                  for (int index = 0; index < stringLength; index++)
                  {
                     weightsFile+=inp.readChar();
                  }

                  valid.weightsFileDefined = true;
                  break;
               case 21: // command for weights2 section.
                  stringLength = inp.readInt(); // length of the weights file name.

                  outputWeights = "";
                  for (int index = 0; index < stringLength; index++)
                  {
                     outputWeights += inp.readChar();
                  }

                  valid.weightsFile2Defined = true;
                  break;
               case 22: // dummy ocmmand for truth table files (inputs and outputs) section.
                  curCommand = command;
                  break;
               case 23: // indicates outputs file.
                  stringLength = inp.readInt(); // length of the weights file name.

                  outputs = "";
                  for (int index = 0; index < stringLength; index++)
                  {
                     outputs+=inp.readChar();
                  }

                  valid.outputsDefined = true;
                  break;
               case 24: // indicates inputs file
                  stringLength = inp.readInt(); // length of the weights file name.

                  inputs = "";
                  for (int index = 0; index < stringLength; index++)
                  {
                     inputs+=inp.readChar();
                  }

                  valid.inputsDefined = true;
                  break;
               case 25:
                  saveTime = inp.readInt();
                  break;
               case 26:
                  savediff = inp.readInt() == 1;
                  break;
            } //switch (command)
         } // try
         catch (IOException e)
         {
            e.printStackTrace(); // for DEBUG
            //System.out.println("DEBUG: hi");
            validfile = false;
         }
      } // while(!done && !loaded && validfile)


      loaded = true;
      return ret;
   } // public boolean loadConfig()

} // public class Config
