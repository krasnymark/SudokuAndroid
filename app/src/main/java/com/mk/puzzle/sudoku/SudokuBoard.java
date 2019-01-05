package com.mk.puzzle.sudoku;

import android.annotation.TargetApi;

import android.graphics.Point;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collector;
import java.util.stream.Collectors;

@TargetApi(24)
public class SudokuBoard
{
    private SudokuCell[][] cells;
    private int size = 9;
    private int size1 = size + 1;

    public SudokuBoard()
    {
        super();
        setSize(size);
        this.cells = new SudokuCell[size1][size1];
        for (int x = 1; x < size1; x++)
        for (int y = 1; y < size1; y++)
        {
            cells[x][y] = new SudokuCell(x, y);
        }
        // Teammates
        for (int x = 1; x < size1; x+=3)
        for (int y = 1; y < size1; y+=3)
        {
            List<SudokuCell> team = new ArrayList<>(9);
            for (int i = 0; i < 3; i++)
            for (int j = 0; j < 3; j++)
            {
                team.add(cells[x+i][y+j]);
            }
            addTeammates(team);
        }
        // Global
        for (int x = 1; x < size1; x++)
        {
            List<SudokuCell> team = new ArrayList<>(9);
            for (int i = 1; i < size1; i++)
            {
                team.add(cells[x][i]);
            }
            addTeammates(team);
        }
        for (int y = 1; y < size1; y++)
        {
            List<SudokuCell> team = new ArrayList<>(9);
            for (int i = 1; i < size1; i++)
            {
                team.add(cells[i][y]);
            }
            addTeammates(team);
        }
    }

    private void addTeammates(List<SudokuCell> team)
    {
        team.forEach(c1 ->
        {
            team.forEach(c2 -> c1.addTeammate(c2));
        });
    }

    // Copy Constructor
    public SudokuBoard(SudokuBoard board)
    {
        super();
        setSize(board.getSize());
        this.cells = new SudokuCell[size1][size1];
        for (int x = 1; x < size1; x++)
        for (int y = 1; y < size1; y++)
        {
            cells[x][y] = new SudokuCell(board.getCell(x,y));
        }
    }

    public int getSize()
    {
        return size;
    }

    public void setSize(int size)
    {
        this.size = size;
        size1 = size + 1;
    }

    public List<SudokuCell> getCells()
    {
        List<SudokuCell> list = new ArrayList<SudokuCell>();
        for (SudokuCell[] array : cells) {
            list.addAll(Arrays.asList(array));
        }
        return list.stream().filter(cell -> cell != null).collect(Collectors.toList());
    }

    public List<SudokuCell> getCellsWithNumbers()
    {
        return getCells().stream().filter(cell -> cell.getNumber() > 0).collect(Collectors.toList());
    }

    public List<SudokuCell> getCellsWithoutNumbers()
    {
        return getCells().stream().filter(cell -> cell.getNumber() == 0).collect(Collectors.toList());
    }

    public void setCell(Point p, int number)
    {
        setCell(p.x, p.y, number);
    }

    public SudokuCell setCell(int x, int y, int number)
    {
        SudokuCell cell = getCell(x, y);
        cell.setNumber(number);
        return cell;
    }

    public SudokuCell getCell(Point p)
    {
        return getCell(p.x, p.y);
    }

    public SudokuCell getCell(int x, int y)
    {
        return cells[x][y];
    }

    public String toCSV()
    {
        StringBuilder builder = new StringBuilder(); // "Board: size = " + size + "\n\n"
        for (int y = 1; y < size1; y++)
        {
            if (y > 1) builder.append("\n");
            for (int x = 1; x < size1; x++)
            {
                SudokuCell cell = cells[x][y];
                if (x > 1) builder.append(",");
                builder.append(cell.isInitial() ? cell.getNumber() : 0);
            }
        }
        return builder.toString();
    }

    public String toString()
    {
        StringBuilder builder = new StringBuilder(); // "Board: size = " + size + "\n\n"
        for (int y = 1; y < size1; y++)
        {
            builder.append("\n");
            for (int x = 1; x < size1; x++)
            {
                SudokuCell cell = cells[x][y];
                builder.append((cell.isInitial() ? "*" : " ") + cell.getNumber() + " ");
            }
        }
        return builder.toString();
    }
}
