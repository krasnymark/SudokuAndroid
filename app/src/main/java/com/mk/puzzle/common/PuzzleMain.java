package com.mk.puzzle.common;


/**
 * @author MK
 *
 */
public class PuzzleMain
{
	private PuzzleSolver solver;

	/**
	 * @param args - Spring context
	 */
	public static void main(String[] args)
	{
		if (args.length > 0)
		{
			PuzzleSolver solver = new PuzzleSolver();
			solver.start();
		}
	}

	public void setSolver(PuzzleSolver solver)
	{
		this.solver = solver;
	}
}
