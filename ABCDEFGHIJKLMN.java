import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;

/*
 * Author: Luke Zeng
 * Date of Creation: 2/7/2024
 *
 * Description: Fully connected Feed-Forward N-Layer Neural Network with Gradient Descent learning and Backpropagation Optimization.
 *              The way data is handled in this neural network is modeled after the way data is handled in
 *              a spreadsheet.
 *
 * Table of Contents:
 *    public static void main(String[] args)
 *    public static void buildNet()
 *    public static void setConfig() throws IOException
 *    public static void setHelpers()
 *    public static boolean initialize()
 *    public static void populate() throws IOException
 *    public static void train()
 *    public static void allocate()
 *    public static void run()
 *    public static boolean validate()
 *    public static void echoConfig()
 *    public static void reportResults()
 *    public static void printNetworkConfig()
 *    public static void printWeights()
 *    public static void printStatus()
 *    public static double act(double x)
 *    public static double fprime(double x)
 *    public static void recalculate(int curCase)
 *    public static void recalculateForRun()
 *    public static double theta(int j)
 *    public static void input(double[] am)
 *    public static void getOutput(double[] inp)
 *    public static void calcDeltas()
 *    public static void trainProcedure(int truthtableindex)
 *    public static double calcError()
 *    public static double caseError(int testcase)
 *    public static void populateRand()
 *    public static void populateSet(double[][] neww1kj, double[][] neww2ji)
 *    public static String dumpTruthTable()
 *    public static String dumpWeights()
 *    public static String dumpVals()
 */


public class ABCDEFGHIJKLMN
{
/******
 * Default config parameters.
 */

   public static final double DEFAULT_LAMBDA = 0.3;        // default value for lambda.
   public static final int DEFAULT_POPULATION = 0;         // default value for population method.
   public static final double DEFAULT_MAXRAND = 1.5;       // default value for maxRand
   public static final double DEFAULT_MINRAND = -1.5;      // default value for minRand.
   public static final double DEFAULT_ERR = 2E-4;          // default value for avg error cutoff.
   public static final int DEFAULT_MAXIT = 100000;         // Default value for max iterations.
   public static final String DEFAULT_CONFIG = "test.bin"; // default config file.
   public static final int FIRST = 0;                      // index of the input activation layer.
   public static final int SECOND = 1;                     // index of the first hidden activation layer.
   public static final int THIRD = 2;                      // index of the second hidden activation layer.
   public static final int CON1 = 0;                       // index of the first connectivity layer.
   public static final int CON2 = 1;                       // index of the second connectivity layer.
   public static final String DEFAULT_WEIGHTS1 = "file.txt"; // default weights1 val.
   public static final String DEFAULT_WEIGHTS2 = "file.txt"; // default weights2 val.
   public static final String SAVEFOLDER = "a\\"; // folder to save weights to (empty if root folder).
   public static final int MAXFILES = 20; // isn't a parameter because we will never change this value.

/******
 * Configuration/validation
 */
   public static Config cf;
   public static InputValidation valid; // input validation.
/******
 * Base network storage and activation function.
 */
   public static double[][][] wAll;    // stores all the weights of the network.

   public static double[][] a;         // stores all the activation values.
   public static ActivationFunction f; // activation function f(x).
   public static double[][] outputs;   // stores the outputs from the sequential run function.

/******
 * Configuration Parameters
 */
   public static double lambda;         // step size multiplier for gradient descent.
   public static int[] numNodes;        // indicates the number of nodes on each activation layer
   public static int numLayers;         // number of layers in the n-layer network.
   public static int numActLayers;      // number of activation layers in the n-layer network.
   public static int outputlayer;       // indicates the output layer of the network.
   public static int populationMethod;  // population method --> 0: randomize weights | 1: manually set weights (DEPRECATED) | 2: File IO
   public static boolean train;         // if true, train. if false, run.
   public static double minRand;        // minimum random threshold
   public static double maxRand;        // maximum random threshold.
   public static double avgErrCut;      // average error cutoff (error threshold).
   public static int maxIterations;     // maximum iterations before cutoff.
   public static boolean printWeight;   // flag to indicate whether to print weights before running.
   public static boolean printTruths;   // flag to indicate whether to print the truth table.
   public static boolean runAfterTrain; // indicates whether the network should run after training.
   public static boolean saveWeights;   // indicates to save weights to the file named FILENAME.
   public static boolean savediff;      // indicates to save to different files, default false.
   public static int saveTime;          // every how many operations to save weights.
   public static int keepAlive;         // indicates the interval to report current run status.
   public static String inFile;         // input file filename.
   public static String outFile;        // output file filename.

   public static String weights1 = DEFAULT_WEIGHTS1;       // first weights file
   public static String weights2 = DEFAULT_WEIGHTS2;       // second weights file

   public static FileHandler fh;        // handles the files for the network.

   public static boolean hitThreshold = false; // flag for program termination due to hitting error threshold. (default false)
   public static boolean outofOps = false;     // flag for program termination due to running out of operations. (default false)
   public static double errorReached;          // stores error reached at the end of training.



/******
 * Truth Table Arrays
 */
   public static double[][] givenInputs;     // truth table inputs.
   public static double[][] expectedOutputs; // truth table outputs.
   public static int numCases;               // number of rows (cases) in the truth table.

/******
 * Training Arrays/Variables
 * Lowercase greek letter spellings (like psi) represent the lowercase greek letters.
 * Uppercase greek letter spellings (like Theta) represent the uppercase greek letters.
 */
   public static double[][] psi;        // psi arrays.
   public static double[][] Theta;      // stores theta values for the network.
   public static int iterationsReached; // the amount of iterations reached during training.

/******
 * network frontend flags/miscellaneous
 */
   public static int exit = 0;   // default exit value is 0 (flag to determine if initialization is valid).
   public static long starttime; // start time of train
   public static long endtime;   // end time of train
   public static Date date;      // date object for runtime tracking.

/*
 * main function: initializes and runs/train the network based on passed control file to command line
 * (will use default if no configuration file is provided).
 */
   public static void main(String[] args)
   {
      if (args.length > 0) // set configuration file from command line arguments.
      {
         try
         {
            for (String arg : args)
            {
               cf = new Config(arg); // load configuration file specified on command line.

               System.out.println("Configuration file: " + arg); //

               buildNet();
            }
         }
         catch (IOException e) // catch any IO issues in the configuration file.
         {
            System.out.println("Cannot find configuration file, or configuration file is of a corrupted format. Will use default (test.bin)");
            try
            {
               cf = new Config(DEFAULT_CONFIG); // Load the default configuration instead of the non-functional provided configuration.
            }
            catch (IOException ie)
            {
               System.out.println("Add the default config file into your directory, dummy");
            }
         } // catch (IOException e)

      } // if (args.length > 0)
      else // if no config file is provided, use default config file.
      {
         System.out.println("No configuration file provided. Will use default (test.bin)");
         try
         {
            cf = new Config(DEFAULT_CONFIG); // Load the default configuration.

            System.out.println("running network on configuration file " + DEFAULT_CONFIG);

            buildNet(); // run the program on the loaded configuration.
         }
         catch (IOException ie)
         {
            System.out.println("Add the default config file into your directory, dummy");
         }
      } // else


   } // public static void main(String[] args)



/*
 * initializes and runs the program according to the current loaded configuration.
 */
   public static void buildNet()
   {
      boolean canstart; // Now don't you say? indicates that training/running can start (if it wasn't clear already)
      canstart = initialize();

      if (canstart)
      {
         date = new Date(); // record start time.
         starttime = date.getTime();

         if (train)          // procedure if training
         {
            printStatus();   // prints a small status message letting user know that the program is training.

            train();         // trains the network.

            if (runAfterTrain)
            {
               run();        // runs the network after training to get the F values for each test case to compare with truth table.
            }
         } // if (train)
         else                // procedure if running
         {
            if (printWeight) // if option is selected, print weights.
            {
               printWeights();
            }

            printStatus();   // prints a short status message which indicates that the program is running.

            run();           // runs the network for all cases and stores the outputs.
         } // else

         date = new Date();  // record end time.
         endtime = date.getTime();

         reportResults();    // print the result of the train/run.

         if (saveWeights) // saves the weights to the output file if option is selected.
         {
            fh.writeWeights(wAll, numNodes, numLayers);
         }
      } // if (canstart)
   } // public static void buildNet()


/******
 * Driver methods
 */

/*
 * sets the configuration parameters of the network.
 * If no values are provided for any configuration
 */
   public static void setConfig() throws IOException
   {
      String actfunct; // temporary string to store the activation function configuration.

      numActLayers = cf.numActLayers;
      numLayers = cf.numLayers;

      numNodes = new int[numActLayers]; // array to store # of nodes on each activation layer

      maxIterations = cf.maxIterations;         // Maximum amount of iterations when training.
      avgErrCut = cf.avgErrCut;                 // Error to stop at when training.
      minRand = cf.minRand;                     // Minimum value for random population of weights.
      maxRand = cf.maxRand;                     // Maximum value for random population of weights.
      lambda = cf.lambda;                       // Lambada factor --> multiplier used to adjust step size in training
      populationMethod = cf.populationMethod;   // 0 is random, 1 is manual populate (DEPRECATED), 2 is populate from file.
      train = cf.train;                         // Training flag --> if set to true the network will train, if false the network will run.
      numCases = cf.numCases;                   // Reflects the number of cases put into the input and output of truth table.
      printWeight = cf.printWeight;             // Flag that determines whether to print weights at the beginning of running.
      printTruths = cf.printTruths;             // Flag that determines whether to print truth table at the end of running/training.
      runAfterTrain = cf.runAfterTrain;         // Should the network run and print run results after training (default is yes).
      saveWeights = cf.saveWeights;             // set to true to save weights to file (only salient if train).
      inFile = cf.inputs;                       // set file name of the inputs.
      outFile = cf.outputs;                     // file name of the truth table.
      keepAlive = cf.keepAlive;                 // every x iterations, report error.
      actfunct = cf.actfunct;                   // set activation function.
      saveTime = cf.saveTime;                   // when to save weights (every x operations)
      weights1 = cf.weightsFile;                // input file weights.
      weights2 = cf.outputWeights;              // output file weights
      savediff = cf.savediff;                   // save differnt files.

      for (int n = 0; n < numActLayers; n++)
      {
         numNodes[n] = cf.numNodes[n];
      }

      valid = cf.valid; // sets validation parameters of config to current validation parameters.

      if ((saveWeights && train) || populationMethod == 2) // if training & saving weights or populating weights from file
      {
         fh = new FileHandler(""); // handles the files for the project.
      }

      switch (actfunct)
      {
         case "sigmoid":
            f = new Sigmoid();
            break;
         case "linear":
            f = new Fx();
         case "tangent":
            f = new HypTan();
      } // switch (actfunct)
   } // public static void setConfig() throws IOException

/*
 * sets helper variables
 */
   public static void setHelpers()
   {
      outputlayer = numActLayers-1;
   }

/*
 * runs allocate, populate, setconfig, and echoconfig, not necessarily in that order (makes main method less cluttered).
 */
   public static boolean initialize()
   {
      try
      {
         setConfig(); // set configurations.
         setHelpers();

         if(validate()) // only keep initializing if validation passes
         {
            echoConfig();
            allocate();
            populate(); // may change exit code to 1 (initialization fails) if file IO for weights or truth tables fails.
         }
      } // try
      catch (FileNotFoundException fe)
      {
         System.out.println("Given config file is not found. Please give a valid configuration file");
         exit = 1; // exit code 1 means initialize doesn't complete.
      }
      catch (IOException ie)
      {
         System.out.println("Something is wrong with the configuration file or the weights files or the truth table. Please fix");
         exit = 1; // exit code 1 means intialize doesn't complete
      }

      return exit == 0; // initialize only passes if exit code is 0 (any exit code other than zero means validate didn't pass).
   } // public boolean initialize()

/*
 * populates the arrays of the network based on the configuration parameters.
 */
   public static void populate() throws IOException
   {
      switch (populationMethod)                 // populates the weights of the network.
      {
         case 0:                                // random population
            populateRand();
            break;
         case 2:                                            // Populate from file.
            fh.changeFile(weights1);
            if (!fh.loadWeights(wAll, numNodes, numLayers)) // loads weights from file, return true if valid weights, false if invalid.
            {
               System.out.println("CONFIG: weights file does not match with given config parameters. Populating random");
               populateRand();
            }
            break;

      } // switch (populationMethod)

/*
 * Load the inputs/expected outputs, checking if dimensions are valid.
 */
      if (!FileHandler.decodeIns(givenInputs, inFile, numNodes[FIRST], numCases)) // returns false if input file is invalid.
      {
         System.out.println("CONFIG: input dimensions don't match up with given parameters. Aborting");
         exit = 1; // exit code 1 is a flag that indicates that initialize didn't complete
      }

      boolean outsPres = false;
      if(train || (!train && printTruths))
         outsPres = FileHandler.decodeOuts(expectedOutputs, outFile, numNodes[outputlayer], numCases); // indicate that output file is valid

      if (train && !outsPres)
      {
         System.out.println("CONFIG: expected output dimensions don't match up with given parameters and you are training. Aborting");
         exit = 1; // exit code 1 is a flag that indicates that initialize didn't complete
      }

      if (!train && printTruths && !outsPres) // if running and printing truths and output file is invalid, indicate so.
      {
         System.out.println("CONFIG: Truth table file invalid or not matching config. Will not print truth table this run.");
         printTruths = false;
      }

   } // public static void populate() throws IOException



/*
 * trains the network with gradient descent learning with backpropagation optimization,
 * stores the amount of iterations the network took to train in a static field.
 * Will terminate if iterations exceeds maximum iterations defined in configuration parameters or if
 * an error threshold defined in configuration parameters is reached.
 *
 * an iteration consists of numCases cycles of delta w calculations and applications
 * (essentially, calculating and updating delta w for each training case is one iteration)
 *
 * Will save weights to the output file if option is selected.
 */
   public static void train()
   {
      boolean done = false;  // flag to indicate that training has finished.
      int cases;             // loop iterator for each case in training.
      int curiteration = 0;  // current iteration of the training.
      double curerror = 0.0; // current error of the network.

      if (saveWeights)
         fh.changeFile(weights2);
      while (!done)
      {
         curerror = 0.0; // reset error to 0.

         for (cases = 0; cases < numCases; cases++)
         {
            trainProcedure(cases); // sets inputs, runs network, calculates delta W, and applies delta W for a given case.

            recalculateForRun();   // reevaluate the network to calculate error.

            curerror += caseError(cases); // calculate error for each case and accumulate error
         }
         curerror /= (double) numCases; // take average of the error.



         if (curerror <= avgErrCut)
         {
            hitThreshold = true; // mark that training terminated for hitting error threshold.
            done = true;         // flag to indicate that training has finished.
         }

         if (++curiteration >= maxIterations)
         {
            outofOps = true; // mark that training terminated not from hitting the error threshold
            done = true;     // flag to indicate that training has finished.
         }

         if (keepAlive > 0 && (curiteration % keepAlive) == 0)
         {
            System.out.print("TRAINING: current iteration: " + curiteration + ", error: " + curerror);

            System.out.println();


         }

         if(saveWeights && saveTime>0 && (curiteration % saveTime == 0))
         {

            if (savediff)
               fh.changeFile(SAVEFOLDER + ((curiteration/saveTime) % MAXFILES) +"-"+weights2);
            System.out.print("SAVE: current iteration: " + curiteration + " - Saving weights to file " + fh.network + " ... ");
            fh.writeWeights(wAll, numNodes, numLayers);
            System.out.print("done!");
            System.out.println();
         }

      } // while (!done)

      iterationsReached = curiteration; // stores the iterations reached for result reporting.
      errorReached = curerror;          // stores the error reached at the end of training for result reporting.
   } // public static void train()



/*
 * allocates the major network arrays for the network.
 */
   public static void allocate()
   {
      givenInputs = new double[numCases][numNodes[FIRST]];


      if (train) // only allocate training-exclusive arrays if training.
      {
         psi = new double[numActLayers][];
         Theta = new double[numActLayers-1][]; // Theta sub i values aren't needed, thus, don't allocate (numactlayer-1).

         for (int alpha = SECOND; alpha < numActLayers-1; alpha++) // Theta arrays only needed for second to second to last activation layer.
         {
            Theta[alpha] = new double[numNodes[alpha]];
         }

         for (int alpha = THIRD; alpha < numActLayers; alpha++) // psi is only needed on the third and through last activation layers.
         {
            psi[alpha] = new double[numNodes[alpha]];
         }

         expectedOutputs = new double[numCases][numNodes[outputlayer]];
      } // if (train)
      else if (printTruths) // if not training, only allocate truth table if printing truth table.
      {
         expectedOutputs = new double[numCases][numNodes[outputlayer]];
      }

      if ((train && runAfterTrain) || !train)           // only allocate run-exclusive arrays if running or running after training.
      {
         outputs = new double[numCases][numNodes[outputlayer]]; // stores the outputs after running.
      }
      wAll = new double[numLayers][][];                        // stores all of the network weights.

      for (int n = 0; n < numLayers; n++)
      {
         wAll[n] = new double[numNodes[n]][numNodes[n+1]];
      }

      a = new double[numActLayers][]; // stores activation layer values.

      for (int alpha = 0; alpha < numActLayers; alpha++)
      {
         a[alpha] = new double[numNodes[alpha]];
      }
   } // public static void allocate()

/*
 * runs the network on all cases and stores the result.
 */
   public static void run()
   {
      for (int cases = 0; cases < numCases; cases++) // runs all the test cases from the truth table.
      {
         getOutput(givenInputs[cases]);        // runs the network with the inputs from the given truth table case.
         for (int i = 0; i < numNodes[outputlayer]; i++) // stores the outputs of the run for this truth table case.
         {
            outputs[cases][i] = a[outputlayer][i];
         }
      }
   } // public static void run()

/*
 * validates that the configuration parameters are all defined and
 * reports if not all params are present (sets default vals if not present).
 */
   public static boolean validate()
   {
      boolean ret = true;
      if (!valid.lambdaDefined)
      {
         System.out.println("Config: lambda not defined, setting lambda to 0.3");
         lambda = DEFAULT_LAMBDA; // default lambda value.
      }


      if (!valid.numHiddenDefined)
      {
         System.out.println("Config: Network configuration not defined, aborting.");
         ret = false;
      }
/*
 * set population method to default if undefined or the value is invalid not equal to 0 or 2 (random and file respectively)
 */
      if (!valid.popMethodDefined || !(populationMethod == 0 || populationMethod == 2))
      {
         System.out.println("Config: The population method you specified is missing or isn't valid. Will use random values.");
         populationMethod = DEFAULT_POPULATION; // default population method.
      }

      if (!valid.trainDefined)
      {
         System.out.println("Config: have not specified whether to train or run, will train");
         train = true;
      }

      if (!valid.minRandDefined)
      {
         System.out.println("Config: minRand not defined, will set to -1.5");
         minRand = DEFAULT_MINRAND; // default minRand.
      }

      if (!valid.maxRandDefined)
      {
         System.out.println("Config: maxRand not defined, will set to 1.5");
         minRand = DEFAULT_MAXRAND; // default maxRand
      }

      if (minRand>maxRand)
      {
         System.out.println("Config: minRand is larger than maxRand, swapping min and max random vals");
         double temp = minRand;
         minRand = maxRand;
         maxRand = temp;
      }

      if (!valid.avgErrCutDefined)
      {
         System.out.println("Config: average error cutoff not defined, setting to 2E-4");
         avgErrCut = DEFAULT_ERR; // default average error cutoff.
      }

      if (!valid.maxIterationsDefined)
      {
         System.out.println("Config: Max iterations not defined, setting to 100000");
         maxIterations = DEFAULT_MAXIT; // default maxIterations value.
      }

      if (!valid.printWeightDefined)
      {
         System.out.println("Config: Did not specify whether to print weights, will not print");
         printWeight = false; // don't print weights.
      }

      if (!valid.printTruthsDefined)
      {
         System.out.println("Config: Did not specify whether to print truth table, will print");
         printTruths = true;
      }

      if (!valid.runAfterDefined && train)
      {
         System.out.println("Config: Did not specify whether to run after train, will run after train");
         runAfterTrain = true;
      }

      if (!valid.saveWeightDefined)
      {
         System.out.println("Config: Did not specify whether to save weights, not saving them.");
         saveWeights = false;
      }

      if (!valid.numCasesDefined)
      {
         System.out.println("Config: numCases not defined, aborting");
         ret = false;
      }

      if (!valid.actfunctDefined)
      {
         System.out.println("Config: activation function not defined; you get a sigmoid.");
         f = new Sigmoid();
      }

      if (!valid.inputsDefined)
      {
         System.out.println("Config: input file not defined, aborting.");
         ret = false;

      }

      if (!valid.outputsDefined && train) // if outputs are not defined and we are training, abort.
      {
         System.out.println("Config: truth table not defined for training  aborting.");
         ret = false;
      }
      else if (!valid.outputsDefined && printTruths) // if outputs are not defined and we are printing truth table, don't print truth table.
      {
         System.out.println("Config: truth table not defined and you are printing truth table. No longer will print truth table");
         printTruths = false;
      }

      if (!valid.weightsFileDefined && populationMethod == 2) // if reading weights from file and weights aren't present, random
      {
         System.out.println("Config: weights file not present with population set to file. Randomly populating.");
         populationMethod = DEFAULT_POPULATION;
      }

      return ret;
   } //public static boolean validate()

/*
 * prints all the configuration parameters to the terminal.
 */
   public static void echoConfig()
   {
      printNetworkConfig(); // prints network configuration (N-Layer).

      System.out.println(); // newline for clarity.

      System.out.println("Configuration Parameters");
      System.out.println("number of input nodes: " + numNodes[FIRST]);
      System.out.println("number of output nodes: " + numNodes[outputlayer]);
      System.out.println("number of cases: " + numCases);
      System.out.println("population method: " + populationMethod);

      if (populationMethod == 2) // population method = 2 means load from file (indicate which file weights loaded from).
      {
         System.out.println("loading weights from " + weights1);
      }

      if (populationMethod == 0) // print random range if populating randomly (populationMethod = 0)
      {
         System.out.println("minRand: " + minRand);
         System.out.println("maxRand: " + maxRand);
      }

      System.out.println("activation function: " + f.toString()); // prints the activation function's type.
      System.out.println("inputs file: " + inFile);               // echoes the inputs file.

      if (printTruths || train)
      {
         System.out.println("Truth table file: " + outFile); // echoes the truth table file if training or if printing truths.
      }

      if (train) // training-exclusive parameters.
      {
         System.out.println();
         System.out.println("Training Exclusive Parameters: ");
         System.out.println("average error cutoff: " + avgErrCut);
         System.out.println("maximum iterations: " + maxIterations);
         System.out.println("lambda: " + lambda);
         System.out.println("keepAlive: " + keepAlive);
         System.out.println("save weights: " + saveWeights);
         System.out.println("save interval: " + saveTime);
         System.out.println("Save to different files: " + savediff);
         if (saveWeights && !savediff) // print out message if saving weights where to save weights to.
         {
            System.out.println("Will save weights to " + weights2);
         }
      } // if (train)

      System.out.println(); // newline for clarity.

      if (printWeight)
      {
         System.out.println("Will print weights after run/train");
      }

      if (printTruths)
      {
         System.out.println("Will print truth table after run/train");
      }
   } // public static void echoConfig()



/******
 * Methods for printing information to the console. 
 */

/*
 * If training, reports the results of training, including reason for termination, iterations reached, error reached,
 * final weights, and the results of the subsequent run after train.
 * If running, reports the results of the run.
 * If option is selected, print truth table.
 */
   public static void reportResults()
   {
      if (train)
      {
         System.out.println("Training Terminated");
         System.out.print("Reason(s) for termination: ");
         if (hitThreshold) // if the training termination happened because the error went below the error threshold, report it.
         {
            System.out.print("Error Threshold Reached");
         }
         if (outofOps) // otherwise, report that the maximum amount of iterations has been reached.
         {
            if (hitThreshold) // add a comma if both reasons for termination were satisfied.
            {
               System.out.println(", ");
            }
            System.out.print("Max Iterations Reached");
         }
         System.out.println();

         System.out.println("Iterations Reached: " + iterationsReached); // output the number of iterations training took.

         System.out.println("Error Reached: " + errorReached);           // output the error reached at the end of training.

         System.out.println();                                           // newline for formatting purposes


         if (printWeight)
         {
            System.out.println("Final Weights: ");
            System.out.println(dumpWeights());                           // print out weights in a nice format, if specified.
         }
      } // if (train)

      System.out.println("Time Elapsed: " + (endtime-starttime) + " ms"); // print time elapsed.

      if (saveWeights && train) // indicate weights have been saved to given file.
      {
         System.out.println(); // newline for fomratting.
         System.out.println("Weights saved to file " + cf.weightsFile);
      }

      if (printTruths) // print out truth table if option is turned on.
      {
         System.out.println();
         System.out.println("Truth Table (format: | In ... In | Out ... Out |)");
         System.out.println(dumpTruthTable());
      }

      if (runAfterTrain || (!train)) // only report run results if program is running after training or running.
      {
         System.out.println("Results from Running:");
         for (int cases = 0; cases < numCases; cases++)
         {
            System.out.print("Case " + (cases + 1) + ": ");
            for (int i = 0; i < numNodes[outputlayer]; i++)
            {
               System.out.print(outputs[cases][i] + " ");
            }
            System.out.println();
         }
      } // if (runAfterTrain || (!train))

   } // public static void reportResults()

/*
 * prints the configuration (NLayer) of the network in a pretty-printing format.
 */
   public static void printNetworkConfig()
   {
      System.out.print("Network Configuration: ");
      System.out.print(numNodes[FIRST]);
      for (int alpha = 1; alpha < numActLayers; alpha++)
      {
         System.out.print("-" + numNodes[alpha]);
      }
   }

/*
 * prints the weights of the network to the console. 
 */
   public static void printWeights()
   {
      System.out.println("Weights: ");
      System.out.println(dumpWeights()); // prints a pretty-printing representation of the weights.
   }
   
/*
 * prints a small status message that indicates to the user that the program is running or training
 * (as opposed to the program freezing)
 */
   public static void printStatus()
   {
      System.out.println(); // whitespace for clarity
      if (train) // if training print a training status message.
      {
         System.out.println("Training ...");
      }
      else // if running print a running status message.
      {
         System.out.println("Running ...");
      }
      System.out.println(); // whitespace for clarity
   } // public static void printStatus()


/******
 * Sigmoid function wrappers (I am doing this for convenience and clarity).
 */

/*
 * wrapper for activation function --> f(x)
 */
   public static double act(double x)
   {
      return f.funct(x);
   }

/*
 * wrapper for derivative of activation function --> f'(x)
 */
   public static double fprime(double x)
   {
      return f.derivative(x); // (f(x)*(1-f(x)) --> f prime with a sigmoid function.
   }



/******
 * network helper functions.
 */

/*
 * runs the network and stores the resulting activations based on the stored input.
 * Stores the theta sub j and theta sub k values for the hidden activation layer and
 * calculates psi sub i values and stores them. (used for train).
 * takes the current test case (to reference truth table to calculate psis).
 */
   public static void recalculate(int curCase)
   {
      double thetasubi;

      for (int alpha = SECOND; alpha < numActLayers-1; alpha++) // iterate from 2nd act layer to second to last act layer (first doesn't need to recalculate b/c it is input!)
      {
         for (int j = 0; j < numNodes[alpha]; j++) // updates each node in the 2nd hidden layer.
         {
            Theta[alpha][j] = theta(alpha, j);

            a[alpha][j] = act(Theta[alpha][j]);
         }
      }

      for (int i = 0; i < numNodes[outputlayer]; i++) // updates each node in the output layer and stores psis for output layer
      {
         thetasubi = theta(outputlayer, i);

         a[outputlayer][i] = act(thetasubi);
         psi[outputlayer][i] = (expectedOutputs[curCase][i] - a[outputlayer][i]) * fprime(thetasubi);
      }
   } // public static void recalculate(int curCase)

/*
 * runs the network and stores the resulting activations based on the stored input.
 */
   public static void recalculateForRun()
   {
      for (int alpha = SECOND; alpha < numActLayers; alpha++) // iterate from 2nd act layer to last act layer (first doesn't need to recalculate b/c it is input!)
      {
         for (int j = 0; j < numNodes[alpha]; j++) // updates each node in the 1st hidden layer.
         {
            a[alpha][j] = act(theta(alpha, j));
         }
      }
   } // public static void recalculateForRun()



/*
 * calculates the dot product (theta) for node j on activation layer alpha.
 */
   public static double theta(int alpha, int j)
   {
      double sum = 0.0;
      for (int k = 0; k < numNodes[alpha-1]; k++) // calculates the dot product of the input activations and the edges leading to node j.
      {
         sum += a[alpha-1][k] * wAll[alpha-1][k][j]; // takes the nodes from the previous activation layer when calculating theta (weights indexed accordingly).
      }
      return sum;
   } // public static double theta(int alpha, int j)


/*
 * takes in a set of inputs and sets the current stored input to the given input.
 */
   public static void input(double[] am)
   {
      for (int m = 0; m < numNodes[FIRST]; m++) // set input activation layer to the given inputs.
      {
         a[FIRST][m] = am[m];
      }
   } // public static void input(double[] am)



/*
 * runs the network on a given set of input activations.
 */
   public static void getOutput(double[] inp)
   {
      input(inp);    // inputs the given set of values.
      recalculateForRun(); // runs the network.
   }




/******
 * Training Helper functions
 */

/*
 * calculates the delta w values for the network and applies them.
 */
   public static void calcDeltas()
   {
      double Omega;
      double Psik;

      for (int alpha = outputlayer-1;  alpha >= THIRD; alpha--) // loop from second to last act layer to third act layer.
      {
         for (int k = 0; k < numNodes[alpha]; k++)
         {
            Omega = 0.0;
            for (int j = 0; j < numNodes[alpha+1]; j++)
            {
               Omega += psi[alpha+1][j] * wAll[alpha][k][j];

               wAll[alpha][k][j] += lambda * a[alpha][k] * psi[alpha+1][j];
            }

            psi[alpha][k] = Omega * fprime(Theta[alpha][k]);
         } // for (int j = 0; j < numNodes[alpha]; j++)
      } // for (int alpha = numLayers-1;  al >= 2; al--)


      for (int k = 0; k < numNodes[SECOND]; k++)
      {
         Omega = 0.0;
         for (int j = 0; j < numNodes[THIRD]; j++)
         {
            Omega += psi[THIRD][j] * wAll[CON2][k][j];

            wAll[CON2][k][j] += lambda * a[SECOND][k] * psi[THIRD][j];
         }

         Psik = Omega * fprime(Theta[SECOND][k]);

         for (int m = 0; m < numNodes[FIRST]; m++)
         {
            wAll[CON1][m][k] += lambda * a[FIRST][m] * Psik;
         }
      } // for (int k = 0; k < numNodes[SECOND]; k++)
   } // public static void calcDeltas()



/*
 * Calculates and updates delta w for one training cycle based on the given truth table index.
 * List of operations in one training cycle, sequentially:
 *    Input truth table values
 *    run the network on the given truth table index.
 *    calculate and apply delta ws.
 */
   public static void trainProcedure(int truthtableindex)
   {
      input(givenInputs[truthtableindex]);
      recalculate(truthtableindex); // runs the network.

      calcDeltas();
   } // public static void trainProcedure(int truthtableindex)



/*
 * calculates the average error bound of the network through all the test cases. (Deprecated)
 */
   public static double calcError()
   {
      double omegai;
      double ret = 0.0;
      for (int cases = 0; cases < numCases; cases++) // run network for all the cases and calculate the error for each one
      {
         input(givenInputs[cases]);                  // input the truth table inputs for the current index.
         recalculateForRun();                        // run the network
         for (int i = 0; i < numNodes[outputlayer]; i++)       // loop over outputs and calculate error.
         {
            omegai = expectedOutputs[cases][i] - a[outputlayer][i]; // calculate difference between expected output for this case and actual output.
            ret += 0.5 * omegai * omegai;              // add error for this output to total error.
         }
      }
      ret /= (double) numCases; // Divide by numCases to get average error. (note: cast to double b/c numCases is an int)

      return ret;
   } // public static double calcError()

/*
 * calculates the error for the given test case.
 */
   public static double caseError(int testcase)
   {
      double omegai;
      double ret = 0.0;

      for (int i = 0; i < numNodes[outputlayer]; i++) // loop over outputs and calculate error for this test case.
      {
         omegai = expectedOutputs[testcase][i] - a[outputlayer][i]; // calculate difference between expected output for this case and actual output.
         ret += 0.5 * omegai * omegai;                              // add error for this output to total error.
      }
      return ret;
   } // public static double caseError(int testcase)



/******
 * network population helper functions
 */

/*
 * populates the weights of the network with random values within the range of the configuration parameters.
 */
   public static void populateRand()
   {
      double range = maxRand-minRand; // range size of the random number to help calculate range.
      double offset = minRand;        // offset of the random number to help calculate range.

      for (int n = 0; n < numLayers; n++)
      {
         for (int k = 0; k < numNodes[n]; k++) // populate weights from act layer alpha = n to alpha = n+1.
         {
            for (int j = 0; j < numNodes[n+1]; j++)
            {
               wAll[n][k][j] = Math.random() * range + offset; // random number between minRand and maxRand.
            }
         }
      }
   } // public static void populateRand()




/******
 * Pretty printing string functions
 */

/*
 * returns a pretty-printing string of the truth table. 
 */
   public static String dumpTruthTable()
   {
      String ret = "";

      for (int cases = 0; cases < numCases; cases++)
      {
         ret += "| ";
         for (int input = 0; input < numNodes[FIRST]; input++)
         {
            ret += givenInputs[cases][input] + " "; // adds input to return string.
         }

         ret += "| ";
         for (int output = 0; output < numNodes[outputlayer] ; output++)
         {
            ret += expectedOutputs[cases][output] + " ";
         }
         ret += "| --> Case " + (cases+1) + "\n"; // adds output and case number label.
      } // for (int cases = 0; cases < numCases; cases++)
      
      return ret;
   } // public static String dumpTruthTable()



/*
 * returns all the edge weights as a pretty printing string.
 */
   public static String dumpWeights()
   {
      String ret = "";

      for (int n = 0; n < numLayers; n++)
      {
         ret += "----------------------------------------------------------------------"; // divider characters for aesthetic
         ret += "\n";

         for (int k = 0; k < numNodes[n]; k++) // formats w1kj weights in an easily digestible format.
         {
            ret += "| ";
            for (int j = 0; j < numNodes[n+1]; j++)
            {
               ret += "w"+ (n+1) + k + j + ": " +  wAll[CON2][k][j] + " | ";
            }
            ret += "\n";
         }
      } // for (int n = 0; n < numLayers; n++)

      ret += "----------------------------------------------------------------------"; // divider characters for aesthetic
      ret += "\n";

      return ret;
   } // public static String dumpWeights()



/*
 * returns all the activation values as a string. Used for DEBUG.
 */
   public static String dumpVals()
   {
      String ret = "";

      for (int alpha = 0; alpha < numActLayers; alpha++)
      {
         for (int j = 0; j < numNodes[alpha]; j++)
         {
            ret += a[alpha][j] + " ";
         }
         ret += "\n";
      }

      return ret;
   } // public static String dumpVals()

} // public class ABCDEFGHIJKLMN