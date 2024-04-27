import java.io.*;


/* Author: Luke Zeng
 * Date of Creation: 4/24/2024
 * converts a bin format of a bmp file to an array and performs operations using pelArray, then converts back to bin.
 */

public class ToIntArray
{
   public static int[][] pels;
   public static int W;
   public static int H;
   public static String file;
   public static String output;


   public static void main(String[] args) throws IOException
   {
      W = Integer.parseInt(args[0]);
      H = Integer.parseInt(args[1]);
      file = args[2];
      pels = new int[H][W];
      output = "modified" + file;
      convertBin();
      PelArray p = new PelArray(pels);
      p = p.grayScaleImage();

      p = p.onesComplimentImage();
      p = p.flipHorizontal();

      int xc = p.getXcom();
      int yc = p.getYcom();


      System.out.println("COM: " + xc + " " + yc);

      p = p.crop(xc-1000, yc-1000, xc+1000, yc+1300);
      p = p.scale(100, 100);
      p = p.forceMin(0x00dddddd , 0);
      //p = p.forceMax(0x00b3b3b3, 1);

      outputBin(p);
   }

   public static void outputBin(PelArray pa) throws IOException
   {
      File f = new File(output);
      ByteArrayOutputStream b = new ByteArrayOutputStream();
      DataOutputStream out = new DataOutputStream(b);
      System.out.println(pa.arrayOfPels.length);
      for (int[] arr : pa.arrayOfPels)
      {
         for (int pel : arr)
         {
            out.writeByte((byte)((pel) & 0x00FF));
         }
      }
      out = new DataOutputStream(new FileOutputStream(f));
      out.write(b.toByteArray());
   }
   public static void convertBin() throws IOException
   {
      DataInputStream in = new DataInputStream(new FileInputStream(file));
      byte[] bin = new byte[W*H*3];
      in.read(bin);
      in = new DataInputStream(new ByteArrayInputStream(bin));

      for (int iRow = 0; iRow < H; iRow++)
      {
         for (int iCol = 0; iCol < W; iCol++)
         {
            pels[H - 1 - iRow][iCol] = in.readByte() | in.readByte()<<8 | in.readByte()<<16;
            //System.out.println(pels[H - 1 - iRow][iCol]);
         }
      }
   }
}