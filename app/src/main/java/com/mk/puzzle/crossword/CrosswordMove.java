package com.mk.puzzle.crossword;

import android.graphics.Point;

import com.mk.puzzle.common.PuzzleSolver;
import com.mk.puzzle.common.PuzzleMove;
import com.mk.puzzle.common.PuzzleState;

public class CrosswordMove implements PuzzleMove
{
	private Word word;
	private Point start = new Point(-1, -1);
	private Point direction = new Point(0, 0);

	public CrosswordMove(Word word, Point start, Point direction)
	{
		super();
		this.word = word;
		this.start = start;
		this.direction = direction;
	}

	public Word getWord()
	{
		return word;
	}

	public void setWord(Word word)
	{
		this.word = word;
	}

	public Point getStart()
	{
		return start;
	}

	public void setStart(Point start)
	{
		this.start = start;
	}

	public Point getDirection()
	{
		return direction;
	}

	public void setDirection(Point direction)
	{
		this.direction = direction;
	}

	public String getKey()
	{
		return PuzzleSolver.formatter.format(start.y) + PuzzleSolver.formatter.format(start.x) + direction.y + direction.x;
	}

	@Override
	public boolean isDeadEnd(PuzzleState puzzleState)
	{
		CrosswordState state = (CrosswordState) puzzleState;
		return state.getWordsToGo().size() * 10 > (state.getWordsToGo().size() + state.getWordsDone().size()); // More than 10% left
	}

	@Override
	public String toString()
	{
		// TODO Auto-generated method stub
		return "Move: " + word + "->[" + start.x + "," + start.y + "]/[" + direction.x + "," + direction.y + "]";
	}

}
