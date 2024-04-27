/*
 * Author: Luke Zeng
 * Date of Creation: 1/30/2024
 *
 * Description: Hyperbolic Tangent implementation of the activation function f(x).
 *              and its derivative.
 *
 * Table of Contents:
 *    public double funct(double arg)
 *    public double derivative(double arg)
 *    public String toString()
 */

public class HypTan implements ActivationFunction
{
/*
 * Hyperbolic Tangent function --> one implementation of activation function (represents f(x)).
 */
   public double funct(double arg)
   {
      double s = arg >= 0.0 ? 1.0 : -1.0;
      double pow = Math.exp(-s * 2.0 * arg);
      return s * (1.0 - pow) / (1.0 + pow); // hyperbolic tangent function.
   }

/*
 * Returns f'(x), the derivative of f(x)
 */
   public double derivative(double arg)
   {
      double farg = this.funct(arg);
      return 1.0 - farg * farg; // sigmoid derivative.
   }

/*
 * returns a string indicating that this object represents a hyperbolic tangent activation function.
 */
   public String toString()
   {
      return "tangent";
   }
} // public class HypTan implements ActivationFunction
