import java.io.*;
/*
 * aggregates and compresses all of the given bin representations of bmp files into one big bin file scaled from zero to one.
 */


public class Aggregate
{
   public static final String OUTPUTFILE = "tests.bin";
   public static final String OUTPUTIMGS = "imgs.bin";
/*
 * aggregates given input img bin files into one output bin file.
 */
   public static void main(String[] args) throws IOException
   {
      DataInputStream b;
      DataInputStream da;
      byte[] bytes = new byte[100*100];
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      DataOutputStream dab = new DataOutputStream(out);
      File f = new File(OUTPUTFILE);
      DataOutputStream realout = new DataOutputStream(new FileOutputStream(f));

      File fa = new File(OUTPUTIMGS);
      ByteArrayOutputStream ba = new ByteArrayOutputStream();
      DataOutputStream outs = new DataOutputStream(ba);

      for (String s : args)
      {
         System.out.println("Aggregating " + s);
         da = new DataInputStream(new FileInputStream(s));
         da.read(bytes);
         da.close();



         b = new DataInputStream(new ByteArrayInputStream(bytes));
         int rows = 0;
         byte temp;
         for (int row = 0; row < 100; row+=1)
         {
            rows++;
            for (int col = 0; col < 100; col+=1)
            {
               temp = (byte)b.readUnsignedByte();
               //System.out.println(temp);
               dab.writeDouble((double)temp/(255.0));
               outs.writeByte(temp);
            }
         }





      }
      System.out.println("Writing to " + OUTPUTFILE);
      outs = new DataOutputStream(new FileOutputStream(OUTPUTIMGS));
      outs.write(ba.toByteArray());

      realout.write(out.toByteArray());

      outs.close();
      out.close();
      ba.close();
      dab.close();


   }
}