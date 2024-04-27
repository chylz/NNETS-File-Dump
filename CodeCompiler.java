import java.io.IOException;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.util.Arrays;

/*
 * Author: Luke Zeng
 * Date of Creation: 3/12/2024
 *
 * Tool for compiling LOML (Luke's Obvious Minimal Language) scripts into binary using a list of commands from a file.
 * (assumes the bytecode assigned to each command is based on the 1-based row number of the command, with 1 command on each line).
 *
 * Command file is described by a constant at the top of the class
 * (for most projects you shouldn't have to edit this value, just change the command file).
 *
 * LOML is a non-case-sensitive language.
 *
 * Command file structure is described below:
 *    Top level commands (in brackets) are to be prefixed with a "!"
 *    Lower level commands (regular lines) are to be prefixed with a "?"
 *    A top level command will be followed by all of its subcommands in the command file(lower level commands that can be used inside a top level block).
 *    The top command will determine the behavior of the block of code following it until the next top level command.
 *    Each lower level command can be followed by any number of arguments describing the data type (string, int, double) of the arguments that command can take.
 *    User should create a compiler based on their command file to interpret LOML bytecode.
 *
 * Data Type restrictions:
 *    string must be one contiguous token (no spaces in a string)
 *    int must be an integer
 *    double must be a parsable number (can be either int or double value)
 *    intarr must be one contiguous token separated by dashes (like 5-6-7-8).
 *
 * Bytecode format/interpreting (for implementing interpreter):
 *    Strings are prefixed by their length and are written as a series of chars in binary.
 *    ints and doubles are written normally.
 *
 * LOML Examples (note: capitalization doesn't matter because LOML isn't case-sensitive) :
 *
 *    Section (top/higher level commands) header syntax:
 *       [SectionTitle]
 *
 *    Lower level command syntax:
 *       command arg arg ...
 *
 *    Example command file:
 *       !CodingStuff
 *       ?command int String
 *       ?eat int double
 *       !Section2
 *       ?go String String
 *       ?you double
 *
 *    Example script (for example command file):
 *       [CodingStuff]
 *       command 5 stuff
 *       eat 3 5.0
 *
 *       [Section2]
 *       go eat food
 *       you 2.0
 *
 *
 * Table of Contents:
 *    public CodeCompiler(String command)
 *    public static void main(String[] args)
 *    public int compile(String filename)
 *    public int compileCode(String code)
 *    public String parseLine(String s, boolean first)
 *    public int higherCommand(String cmd)
 *    public int lowerCommand(String cmd, int higher)
 *    public String getFileString(String filename) throws IOException
 *    public void writeArg(DataOutputStream da, String arg, int command, int argnum) throws IOException
 *    public String[] removeEmpty(String[] arr)
 *    public void printcline(String out)
 */

public class CodeCompiler
{
   public static final String COMMANDFILE = "Commands.txt"; // Command file.

   public String compileOutput; // Output string of the compiler (warnings, errors).
   public byte[] byteCode;      // stores the given bytecode as a byte array.
   public String[] commands;
   public String[][] commandargs; // stores command argument types (int, string, double)
   public final String CMDFILE;   // stores command file (sometimes we want to pass command file from command line).
   public int numCommands;

   public int line;    // the current line the compiler is on when scanning (used for error reporting)
   private boolean cp; // indicates if command file is present.


/*
 * compiles the given files on command line.
 */
   public static void main(String[] args)
   {
      if (args.length >= 1)
      {
         CodeCompiler cc = new CodeCompiler(COMMANDFILE);

         for (int index = 0; index < args.length; index++)
         {
            cc.compile(args[index]);
         }

         System.out.println(cc.compileOutput); // print compile output of the compiler (all at the same time)
      } // if (args.length >= 1)
      else
      {
         System.out.println("not enough args to compile, please give at least one code file");
      }
   } // public static void main(String[] args)

/*
 * Creates a new Code Compiler with the given command file.
 */
   public CodeCompiler(String command)
   {
      CMDFILE = command;
      compileOutput = "";
      cp = true;                    // flag for if the command file is present or not.
      String[] commandlines = null; // stores the lines of the command file.
      String[] curline;             // current line split up into space separated tokens.
      int numargs;

      try
      {
         commandlines = removeEmpty(getFileString(CMDFILE).split("\n")); // splits the line-separated and lowercased command lines into array.
         numCommands = commandlines.length;
      }
      catch (IOException e)
      {
         printcline("Command file not found. Please give a valid command file");
         cp = false;
      }

      commands = new String[numCommands];      // stores command keywords.
      commandargs = new String[numCommands][]; // stores argument types for each command.

      if (cp) // only initialize if command file is present and valid.
      {
         for (int comman = 0; comman < numCommands; comman++) // loop through each line of commands, parse out the command and its args
         {
            curline = removeEmpty(commandlines[comman].split(" "));
            numargs = curline.length;

            commandargs[comman] = new String[numargs-1];
            if (numargs > 0 && curline[0].charAt(0) == '?') // only split parse if the first token on the line is a lower level command
            {
               commands[comman] = curline[0];

               for (int arg = 0; arg < numargs-1; arg++) // loop over numargs-1 because one of the arguments on the line is the command
               {
                  commandargs[comman][arg] = curline[arg+1];
               }
            } // if (numargs > 0 && curline[0].charAt(0) == '?')

            else if (numargs>0)
            {
               commands[comman] = curline[0];
            }

         } // for (int comman = 0; comman < numCommands; comman++)
      } // if (cp)

   } // public CodeCompiler(String command)

/*
 * Compiles the given file name using the LOML (Luke's Obvious Minimal Language) standard.
 * file format should be "*.loml".
 * returns the exit code of compilation (0 is successful, 1 is compile error)
 */
   public int compile(String filename)
   {
      int exit = 0;
      if (cp) // only compile if command file is present.
      {
         DataOutputStream out;
         String code = "";          // initializes code to an empty string before trying to fill it with the contents of a file.
         String outputfile = "";    // output file name (declared as empty first, will be appended to later).
         boolean canCompile = true; // flag to see if the current code can be compiled (basically if the file exists and has correct extension).

         String[] filen = removeEmpty(filename.split("\\.")); // gets tokens of the filename separated by periods to parse extension and filename.

         if (filen.length < 2) // if the number of tokens separated by periods (filename.extension) is less than 2, not a valid file.
         {
            canCompile = false;
            printcline("invalid file given, please input a .loml file");
            exit = 1; // exit code 1 means the code did not compile properly.
         }
         else
         {
            if (!filen[1].equals("loml")) // check file extension to make sure extension is .loml.
            {
               canCompile = false;
               printcline("Invalid file extension " + filen[1] + " expected: .loml");
               exit = 1; // exit code 1 means the code did not compile properly.
            }
            else // if input file is valid, set output file and create (if necessary) output file with name filename.bin.
            {
               outputfile = filen[0] + ".bin"; // make the same file name but with .bin extension.

               File compiled = new File(outputfile);
               try
               {
                  compiled.createNewFile();
               }
               catch (IOException e)
               {
                  printcline("error in output file creaton");
                  exit = 1; // exit code 1 means the code did not compile or save properly.
               }
            } // else
         } // else




         try
         {
            if (canCompile) // if we can compile, set code to the given
            {
               code = getFileString(filename);
            }
         }
         catch (IOException e)
         {
            printcline("code file not found, please give a valid file name");
            canCompile = false;
         }

         if (canCompile) // only compile if all pre compile checks passed.
         {
            exit = compileCode(code); // compile the code (string form) into binary array byteCode.
            try
            {
               out = new DataOutputStream(new FileOutputStream(outputfile));
               out.write(byteCode); // write compiled code to the file.
               out.close();
            }
            catch (IOException e)
            {
               printcline("Error in output file creation or output writing. ");
            }
         } // if (canCompile)


         printcline("Code compiled with exit code " + exit); // print exit code to compile output

      } // if (cp)
      else // if command file is not present, there is a problem with compilation (obviously).
      {
         exit = 1; // exit code 1 means command file not present.
      }

      return exit; // exit codes: 0 means successful, 1 means there was a compile problem (currently in development)

   } // public int compile(String filename)



/*
 * Compiles the given string using according to the command file. .
 * Returns an exit code depending on compilation results (0 is successful, 1 is compilation error).
 */
   public int compileCode(String code)
   {
      int exit = 0;
      if (cp)
      {
         ByteArrayOutputStream bytes = new ByteArrayOutputStream();
         DataOutputStream ds = new DataOutputStream(bytes);

         int numLines;
         String[] sections = removeEmpty(code.split("\\["));

         int numsec = sections.length; // number of "sections" in the code ([section title])
         String[] lines;               // lines in a "section".
         String[] expr;                // expressions on a line.
         int curSection;               // command type of the current section (higher level command)
         int curCommand;               // current command type (on each line) (lower level command).
         int numExp;

         line = 0;                     // current line of the compiler.

         for (int section = 0; section < numsec; section++) // loop through each section.
         {
            lines = removeEmpty(sections[section].split("\n"));
            numLines = lines.length;
            curSection = higherCommand(parseLine(lines[0], true)); // determine command type of higher command.


            if (curSection == -1)
            {
               continue; // don't add section if section header is not recognized.
            }

            try
            {
               ds.writeInt(curSection); // indicate a section has started by prefixing each section with a section command.
            }
            catch (IOException e)
            {
               printcline("You broke the compiler"); // this shouldn't IOException because we are writing to a bytearray output stream
            }

/*
 * loop through each line in a section (starting from index 1 on zero index because we are skipping the section header).
 */
            for (int linesec = 1; linesec < numLines; linesec++)
            { // current line in the current section (starting from line 1 because section header is already processed)
               line++;
               expr = removeEmpty(parseLine(lines[linesec], false).split(" ")); // first parse line to remove any comments from line
               if (expr.length == 0) // don't process line if empty.
               {
                  continue;
               }

               curCommand = lowerCommand(expr[0], curSection);

               if (curCommand == -1) // curCommand -1 indicates that command was not found.
               {
                  printcline("unrecognized command on line " + line);
                  continue; // don't add command if unrecognized.
               }

               numExp = expr.length;

               try
               {
                  ds.writeInt(curCommand); // write current command to the compiled bytecode.
               }
               catch (IOException e)
               {
                  printcline("You broke the compiler"); // this shouldn't be called because we are writing to a bytearray
               }

/*
 * check if number of arguments matches expected number (subtract 1 on length of the expression to exclude the first command
 * (to single out number of args))
 * subtract 1 from curCommand because curCommand is 1-based indexing whereas commandargs is 0-based.
 */
               if (commandargs[curCommand-1].length != expr.length-1)
               {
                  continue;
               }

               for (int exp = 1; exp < numExp; exp++) // start from second token (index 1) because first token is command (we ignore that)
               {
                  try
                  {
                     writeArg(ds, expr[exp], curCommand, exp); // write the current argument of the current command to the compiled code.
                  }
                  catch (IOException e)
                  {
                     printcline("this shouldn't be happening. You broke the compiler");
                  }
               } // for (int exp = 1; exp < numExp; exp++)

               try // sections are opened and closed by a section flag.
               {
                  ds.writeInt(curSection);
               }
               catch (IOException e)
               {
                  printcline("You broke the compiler"); // this shouldn't IOException because we are writing to a bytearray output stream
               }

            } // for (int linesec = 1; linesec < numLines; linesec++)

            line++; // increment line number for error reporting.
         } // for (int section = 0; section < numsec; section++)

         try
         {
            ds.writeInt(0); // ending command, signal to interpreter that the program has ended.
         }
         catch (IOException e)
         {
            printcline("this should not be happening, contact developer with compile output and code");
         }

         byteCode = bytes.toByteArray();
      } // if (cp)

      return exit; // return exit code, indicating how the code exited.
   } // public int compileCode(String code)

/*
 * parses the given line (String) into just the expression (ignoring comments).
 * the parameter first just means if the line is the first line in a section (the one in brackets).
 */
   public String parseLine(String s, boolean first)
   {
      int index = 0;
      int endReason = 0;    // reason for termination of parse (was the end of the expression a newline (1), a bracket (2) or a pound (3))
      boolean done = false; // flag to finish parsing.

      while (!done) //keep parsing the expression until a closing bracket or newline or a pound.
      {

         if (index >= s.length()) // if end of line, simply return the whole line.
         {
            endReason = 1;
            done = true;
         }
         else if (s.charAt(index) == '#') // '#' is the character for comments (IE, ignore everything after the #)
         {
            endReason = 3;
            done = true;
         }
         else if (s.charAt(index) == ']') // ']' is the character which ends a top level command (IE, ignore everything after the ']'.
         {
            endReason = 2;
            done = true;
         }
         else
         {
            index++;
         }
      } // while(!done)

      if (endReason == 2 && !first) // report an error to compile output if an extra close bracket is found not closing the first line.
      {
         printcline("Unrecognized ']' on line " + line);
      }
      if (endReason !=2 && first) // report an error to compile output if first line doesn't have a close bracket.
      {
         printcline("Expected ']' after expression on line " + line);
      }

      return s.substring(0, index);
   } // public String parseLine(String s, boolean first)

/*
 * interprets the higher-level command type of the given string in LOML to compile into LOML bytecode on the given line for error reporting.
 */
   public int higherCommand(String cmd)
   {
      int type = -1; // an impossible command value to detect if the given command is not found.

      for (int command = 1; command <= numCommands; command++)
      {
/*
 * removes the front character to compare the current command with the given command and ensures the given command is a valid
 * top level command.
 * Ensures the first character in a command (index 0) is a !, indicating a top level command.
 *
 * command is 1-indexed and commands is 0-indexed, so we subtract 1 from command.
 */
         if (commands[command-1].charAt(0) == '!' && cmd.equals(commands[command-1].substring(1))) // substring(1) removes first character
         {
            type = command;
         }
      } // for (int command = 1; command <= numCommands; command++)

      if (type == -1) // if command not found, report it to compiel output.
      {
         printcline("Top level Command " + cmd + " on line " + line + " unrecognized");
      }

      return type;
   } // public int higherCommand(String cmd)

/*
 * interprets the bytecode representations of lower level commands given the current higher level command
 * and command string.
 */
   public int lowerCommand(String cmd, int higher)
   {
      int type = -1;          // an impossible command value to detect if the given command is not found. (this case is -1).
      int command = higher+1; // start looking for lower level command starting from the next command after a higher level command

/*
 * starts at the higher level command, trying to find the lower level command.
 * Ends if another section (higher level command) is found (command not found in current section).
 */
      while (command <= numCommands && commands[command-1].charAt(0) == '?')
      {
/*
 * removes the front character (? or !) to compare the current command (which has hierarchy indicators
 * with the given command and ensures the given command is a valid top level command.
 * command is 1-indexed and command is 0-indexed, so we subtract 1 from command to match indexing.
 */
         if (cmd.equals(commands[command-1].substring(1)))
         {
            type = command;
         }
         command++;
      } // while (command <= numCommands && commands[command-1].charAt(0) == '?')

      if (type == -1) // if command not found, report it to compile output.
      {
         printcline("Lower level Command " + cmd + " on line " + line + " unrecognized");
      }

      return type;
   } // public int lowerCommand(String cmd, int higher)

/*
 * returns the contents of a file as a lowercase string.
 */
   public String getFileString(String filename) throws IOException
   {
      BufferedReader reader = new BufferedReader(new FileReader(filename));
      StringBuilder sb = new StringBuilder();

      String curline = reader.readLine();

      while (curline != null) // reads the whole file as one string, until reading the read line returns null (end of file)
      {
         sb.append(curline + "\n"); // Add current line to code string with newline to keep with syntax.
         curline = reader.readLine();
      }

      return sb.toString().toLowerCase(); // returns lowercase version of the string.
   } // public String getFileString(String filename)

/*
 * writes the given argument of the given command to the given output stream with the correct data typing.
 */
   public void writeArg(DataOutputStream da, String arg, int command, int argnum) throws IOException
   {
/*
 * commands, argnum are 1 indexed, commandargs, argnum are zero indexed. Thus, we shift the latter two by 1.
 */
      String curarg = commandargs[command-1][argnum-1];

      try // determines data type of the argument expected, print a compile error if not matched.
      {
         if (curarg.equals("double")) // write argument as a double if arg is double type
         {
            da.writeDouble(Double.parseDouble(arg));
         }
         else if (curarg.equals("int")) // write argument as a int if arg is int type
         {
            da.writeInt(Integer.parseInt(arg));
         }
         else if (curarg.equals("string")) // write argument as a string if arg is string type.
         {
            int strlength = arg.length();
            da.writeInt(strlength); // write string length to indicate length of string to interpreter.

            da.writeChars(arg);     // write whole string.
         }
         else if (curarg.equals("intarr"))
         {
            String[] elements = removeEmpty(arg.split("-"));
            int numElements = elements.length;

            da.writeInt(numElements);
            for (int index = 0; index < numElements; index++)
            {
               da.writeInt(Integer.parseInt(elements[index]));
            }
         }
         else
         {
            printcline("unrecognized data type for command " + commands[command-1] + " try changing commands file to one of three" +
                            " recognized data types (double, int, string).");
         }
      } // try
      catch (NumberFormatException e)
      {
         printcline ("data type mismatch for command " + commands[command-1] + " expected " + commandargs[command-1][argnum]);
      }
   } // public void writeArg(DataOutputStream da, String arg, int command, int argnum) throws IOException

/*
 * returns a copy of an array with all empty strings removed. (helps clean up string split command)
 * Also moves all nonempty strings to the front of the given array.
 */
   public String[] removeEmpty(String[] arr)
   {
      int index = 0;
      int length = arr.length;
      for (int ind=0; ind < length; ind++)
      {
         if (arr[ind].length() != 0) // if current index isn't empty string, push it to the front.
         {
            arr[index++] = arr[ind];
         }
      }

      return Arrays.copyOf(arr, index); // return a copy of the array up until the end of the nonempty strings.
   } // public String[] removeEmpty(String[] arr)

/*
 * prints the given string to the compile output followed by line break.
 */
   public void printcline(String out)
   {
      compileOutput += out + "\n";
   }
} // public class CodeCompiler
