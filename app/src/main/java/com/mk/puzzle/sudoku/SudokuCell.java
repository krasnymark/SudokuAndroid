package com.mk.puzzle.sudoku;

import com.mk.puzzle.common.PuzzleMove;

import android.annotation.TargetApi;
import android.graphics.Point;
import java.util.*;
import java.util.List;

/**
 *
 */
@TargetApi(24)
public class SudokuCell
{
    private Point point; // 1,1 -> 9,9 - Cell coordinates
    private int number = 0; // 1 -> 9 - Cell value
    private int size = 9; // Default .. forever
    private boolean isInitial; // = read-only
    private int[] available; // Numbers available for cell
    private Set<SudokuCell> teammates = new TreeSet<>(new Comparator<SudokuCell>() // Cells on the same row/column + in the same small 3*3 square
    {
        @Override
        public int compare(SudokuCell o1, SudokuCell o2)
        {
            return (o1.getPoint().x - o2.getPoint().x) * 10 + (o1.getPoint().y - o2.getPoint().y);
        }
    });

    public SudokuCell()
    {
        super();
        available = new int[size + 1];
        Arrays.fill(available, 1);
    }

    public SudokuCell(Point point)
    {
        this();
        this.point = point;
    }

    public SudokuCell(int x, int y)
    {
        this(new Point(x, y));
    }

    public SudokuCell(Point point, int number, boolean isInitial, int[] available, Set<SudokuCell> teammates)
    {
        this(point);
        setNumber(number);
        setInitial(isInitial);
        setAvailable(available);
        setTeammates(teammates);
    }

    // Copy Constructor
    public SudokuCell(SudokuCell cell)
    {
        this(cell.point, cell.number, cell.isInitial, cell.available, cell.teammates);
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (!(o instanceof SudokuCell)) return false;
        SudokuCell cell = (SudokuCell) o;
        return number == cell.number &&
                Objects.equals(point, cell.point);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(point, number);
    }

    @Override
    public String toString()
    {
        return "(" + point.x + "," + point.y + ")" + (number > 0 ? "=" + number : "") + "a:" + getAvailableCount();
    }

    public boolean isInitial()
    {
        return isInitial;
    }

    public void setInitial(boolean initial)
    {
        isInitial = initial;
    }

    private void setAvailable(int[] available)
    {
        this.available = available;
    }

    public Set<SudokuCell> getTeammates()
    {
        return teammates;
    }

    private void setTeammates(Set<SudokuCell> teammates)
    {
        this.teammates = teammates;
    }

    public int getAvailableCount()
    {
        int count = 0;
        for (int i = 1; i <= size; i++)
        {
            if (available[i] > 0) count++;
        }
        return count;
    }
    public List<SudokuMove> getAvailableMoves()
    {
        List<SudokuMove> availableMoves = new ArrayList<>();
        if (number == 0)
        for (int i = 1; i <= size; i++)
        {
            if (available[i] > 0)
                availableMoves.add(new SudokuMove(this, i));
        }
        return availableMoves;
    }

    public boolean isAvailable(int number)
    {
        return available[number] > 0;
    }

    private void setAvailable(int number)
    {
        this.available[number]++;
    }

    private void setNotAvailable(int number)
    {
        this.available[number]--;
    }

    public void setNumber(int number)
    {
        if (this.number == 0)
        {
            this.number = number;
            teammates.forEach(teammate -> teammate.setNotAvailable(number));
        }
    }

    public int removeNumber()
    {
        int number = this.number;
        if (this.number > 0)
        {
            this.number = 0;
            teammates.forEach(teammate -> teammate.setAvailable(number));
        }
        return number;
    }

    public int getNumber()
    {
        return number;
    }

    private boolean hasTeammate(SudokuCell cell)
    {
        return teammates.contains(cell);
    }

    public void addTeammate(SudokuCell cell)
    {
        if (this == cell)
            return;
        teammates.add(cell);
        if (!cell.hasTeammate(this))
            cell.addTeammate(this);
    }

    public void setTeammates()
    {
        this.teammates = teammates;
    }

    public Point getPoint()
    {
        return point;
    }
}
