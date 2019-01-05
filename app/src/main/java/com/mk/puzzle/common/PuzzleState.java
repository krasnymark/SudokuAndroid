package com.mk.puzzle.common;

import java.util.List;

public interface PuzzleState
{
	List<? extends PuzzleMove> getAvailableMoves();
	void applyMove(PuzzleMove move);
	void takeBack(PuzzleMove move);
	boolean isSolved();
	PuzzleState clone();
}
