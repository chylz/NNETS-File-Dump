/*
 * Author: Luke Zeng
 * Date of Creation: 1/30/2024
 *
 * Description: Sigmoid implementation of the activation function f(x).
 *              and its derivative.
 *
 * Table of Contents:
 *    public double funct(double arg)
 *    public double derivative(double arg)
 *    public String toString()
 */

public class Sigmoid implements ActivationFunction
{
/*
 * Sigmoid function --> one implementation of activation function (represents f(x)).
 */
   public double funct(double arg)
   {
      return 1.0/(1.0 + Math.exp(-arg));//sigmoid function
   }

/*
 * Returns f'(x), the derivative of f(x)
 */
   public double derivative(double arg)
   {
      double farg = this.funct(arg);
      return farg * (1.0-farg); // sigmoid derivative.
   }

/*
 * returns a string indicating that this object represents a sigmoid activation function.
 */
   public String toString()
   {
      return "sigmoid";
   }
} //public class Sigmoid implements ActivationFunction
