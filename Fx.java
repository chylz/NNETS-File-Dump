/*
 * Author: Luke Zeng
 * Date of Creation: 1/30/2024
 *
 * Description: Linear (f(x) = x) implementation of the activation function f(x).
 *              and its derivative.
 *
 * Table of Contents:
 *    public double funct(double arg)
 *    public double derivative(double arg)
 *    public String toString()
 */

public class Fx implements ActivationFunction
{
/*
 * F(x) = x function --> one implementation of activation function.
 */
   public double funct(double arg)
   {
      return arg;
   }

/*
 * dummy derivative function (won't actually train).
 */
   public double derivative(double arg)
   {
      return 1.0;
   }

/*
 * returns a string indicating that this object represents a linear activation function.
 */
   public String toString()
   {
      return "linear";
   }
} //public class Fx implements ActivationFunction

