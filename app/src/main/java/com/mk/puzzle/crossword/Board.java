package com.mk.puzzle.crossword;

import android.graphics.Point;


public class Board
{
	private Cell[][] cells;
	private int size;

	// Copy Constructor
	public Board(Board aBoard)
	{
		super();
		this.size = aBoard.getSize();
		this.cells = new Cell[size][size];
		for (int i = 0; i < size; i++)
		for (int j = 0; j < size; j++)
		{
			cells[i][j] = new Cell(aBoard.getCells()[i][j]);
		}
	}

	public Board(int size)
	{
		super();
		this.size = size;
		this.cells = new Cell[size][size];
		for (int i = 0; i < size; i++)
		for (int j = 0; j < size; j++)
		{
			cells[i][j] = new Cell(i, j);
		}
	}

	public String toString()
	{
		StringBuilder builder = new StringBuilder("Board: size = " + size + "\n\n");
//		for (int y = size - 1; y >= 0; y--)
		for (int y = 0; y < size; y++)
		{
			for (int x = 0; x < size; x++)
			{
				builder.append(cells[x][y].getLetterChar() + " ");
			}
			builder.append("\n");
		}
		return builder.toString();
	}

	public boolean isPointValid(int x, int y)
	{
		return (0 <= x && x < size && 0 <= y && y < size);
	}

	public boolean isPointValid(Point point)
	{
		return isPointValid(point.x, point.y);
	}

	public Cell getCell(Point p)
	{
		return getCell(p.x, p.y);
	}

	public Cell getCell(int x, int y)
	{
		return isPointValid(x, y) ? cells[x][y] : null;
	}

	public Cell[][] getCells()
	{
		return cells;
	}

	public void setCells(Cell[][] board)
	{
		this.cells = board;
	}

	public int getSize()
	{
		return size;
	}

	public void setSize(int size)
	{
		this.size = size;
	}

}
