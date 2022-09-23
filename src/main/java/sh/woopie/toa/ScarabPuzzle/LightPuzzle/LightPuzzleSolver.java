package sh.woopie.toa.ScarabPuzzle.LightPuzzle;

import org.apache.commons.math3.fraction.Fraction;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.DecompositionSolver;
import org.apache.commons.math3.linear.LUDecomposition;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;

public class LightPuzzleSolver
{
	public static final long[] denominators = {0, 0, 0, 0, 0, 0, 0, 0};

	public static final double[][] coeffMatrix = {
		{1, 1, 0, 1, 0, 0, 0, 0}, // a11
		{1, 1, 1, 0, 0, 0, 0, 0}, // a12
		{0, 1, 1, 0, 1, 0, 0, 0}, // a13
		{1, 0, 0, 1, 0, 1, 0, 0}, // a21
		{0, 0, 1, 0, 1, 0, 0, 1}, // a23
		{0, 0, 0, 1, 0, 1, 1, 0}, // a31
		{0, 0, 0, 0, 0, 1, 1, 1}, // a32
		{0, 0, 0, 0, 1, 0, 1, 1}  // a33
	};

	public double[] solve(double[] state)
	{
		RealMatrix coefficients = new Array2DRowRealMatrix(coeffMatrix, false);
		DecompositionSolver solver = new LUDecomposition(coefficients).getSolver();

		RealVector constants = new ArrayRealVector(state, false);
		RealVector solved = solver.solve((constants));

		double[] solution = solved.toArray();

		for(int i = 0; i < solution.length; i++) {
			denominators[i] = new Fraction(solution[i]).getDenominator();
		}

		// Get the greatest common divisor from all denominators
		long gcd = gcdArr();

		// Rebuild our solution and determine which tiles need to be flipped, odd ones.
		for(int i = 0; i < solution.length; i++)
		{
			solution[i] = Math.rint(solution[i] * gcd % 2);
			solution[i] = Math.abs(solution[i]);
		}

		return solution;
	}

	private static long gcdArr()
	{
		long result = denominators[0];

		for (int i = 1; i < denominators.length; i++)
		{
			result = gcd(denominators[i], result);
		}

		return result;
	}

	private static long gcd(long a, long b)
	{
		if (a == 0)
		{
			return b;
		}
		return gcd(b % a, a);
	}
}
