package com.mk.puzzle.sudoku;

import android.annotation.TargetApi;

import com.mk.puzzle.common.PuzzleMove;
import com.mk.puzzle.common.PuzzleState;

import java.util.Objects;
import java.util.Random;

@TargetApi(24)
public class SudokuMove implements PuzzleMove
{
    private SudokuCell cell;
    private int number;
    private Random r = new Random();
    private int[] rMap = new int[10];

    public SudokuMove(SudokuCell cell, int number)
    {
        this.cell = cell;
        this.number = number;
        rMap[number] = r.nextInt(100);
    }

    public SudokuCell getCell()
    {
        return cell;
    }

    public void setCell(SudokuCell cell)
    {
        this.cell = cell;
    }

    public int getNumber()
    {
        return number;
    }

    public void setNumber(int number)
    {
        this.number = number;
    }

    public void apply()
    {
        cell.setNumber(number);
    }

    public void takeBack()
    {
        cell.removeNumber();
    }

    @Override
    public String getKey()
    {
        return "" + cell.getAvailableCount() * 1000 + cell.getPoint().x * 100 + cell.getPoint().y * 10 + number;
    }

    @Override
    public String getAltKey()
    {
        return "" + cell.getAvailableCount() * 1000 + cell.getPoint().y * 100 + cell.getPoint().x * 10 + rMap[number];
    }

    @Override
    public boolean isDeadEnd(PuzzleState state)
    {
        // There was Only one move or we are up to the last available
        return cell.getAvailableCount() == 1 || this.equals(cell.getAvailableMoves().get(cell.getAvailableCount() - 1));
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (!(o instanceof SudokuMove)) return false;
        SudokuMove move = (SudokuMove) o;
        return number == move.number &&
                Objects.equals(cell, move.cell);
    }

    @Override
    public int hashCode()
    {

        return Objects.hash(cell, number);
    }

    @Override
    public String toString()
    {
        return number + "->" + cell;
    }
}
