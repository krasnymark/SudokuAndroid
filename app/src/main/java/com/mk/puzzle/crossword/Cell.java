package com.mk.puzzle.crossword;

import android.graphics.Point;

public class Cell
{
	private Point point = new Point();
	private char letter = 0;
	private Point direction = new Point(0, 0);
	// Copy Constructor
	public Cell(Cell aCell)
	{
		super();
		letter = aCell.getLetter();
		point = new Point(aCell.getPoint());
		direction = new Point(aCell.getDirection());
	}

	public Cell(int x, int y)
	{
		super();
		setPoint(x, y);
	}

	public Cell(Point point)
	{
		this(point.x, point.y);
	}

	public Point getPoint()
	{
		return point;
	}

	private void setPoint(int x, int y)
	{
		this.point.x = x;
		this.point.y = y;
	}

	public void setPoint(Point point)
	{
		this.point = point;
	}

	public char getLetter()
	{
		return letter;
	}

	public void setLetter(char letter)
	{
		this.letter = letter;
	}

	public Point getDirection()
	{
		return direction;
	}

	public void setDirection(int x, int y)
	{
		this.direction.x = x;
		this.direction.y = y;
	}

	public void setDirection(Point direction)
	{
		this.direction = direction;
	}

	public char getLetterChar()
	{
		return letter == 0 ? '.' : letter;
	}

	@Override
	public String toString()
	{
		// TODO Auto-generated method stub
		return "Cell(" + this.point.x + "," + this.point.y + ") = [" + getLetterChar() + "]";
	}

}
