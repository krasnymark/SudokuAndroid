package com.mk.puzzle.sudoku;

import android.annotation.TargetApi;

import com.mk.puzzle.common.Logger;
import com.mk.puzzle.common.PuzzleMove;
import com.mk.puzzle.common.PuzzleState;
import com.mk.puzzle.common.PuzzleGoal;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@TargetApi(24)
public class SudokuState implements PuzzleState
{
    private final static Logger logger = Logger.getLogger(SudokuState.class);
    private SudokuPuzzle puzzle;
    private SudokuBoard board;
    private Random random;

    public SudokuState(SudokuPuzzle puzzle)
    {
        super();
        this.puzzle = puzzle;
        board = new SudokuBoard();
    }

    public SudokuState(SudokuState state)
    {
        super();
        board = new SudokuBoard(state.getBoard());
    }

    @Override
    public PuzzleState clone()
    {
        return new SudokuState(this);
    }

    @Override
    public String toString()
    {
        return board.toString();
    }

    public SudokuBoard getBoard()
    {
        return board;
    }

    @Override
    public List<SudokuMove> getAvailableMoves()
    {
        random = new Random(System.currentTimeMillis());
        List<SudokuMove> availableMoves = new ArrayList<>();
        for (int x = 1; x <= board.getSize(); x++)
        for (int y = 1; y <= board.getSize(); y++)
        {
            SudokuCell cell = board.getCell(x, y);
            if (cell.getNumber() > 0) continue;
            List<SudokuMove> cellMoves = board.getCell(x, y).getAvailableMoves();
            if (cellMoves.size() == 0)
            {
                logger.debug("No moves for: " + cell);
                return cellMoves;
            }
            availableMoves.addAll(cellMoves);
        }
        return puzzle.goal == PuzzleGoal.Init
                ? availableMoves.stream().sorted((left,right) -> left.getAltKey().compareTo(right.getAltKey())).collect(Collectors.toList())
                : availableMoves.stream().sorted((left,right) -> left.getKey().compareTo(right.getKey())).collect(Collectors.toList());
    }

    @Override
    public void applyMove(PuzzleMove move)
    {
        SudokuMove sudokuMove = (SudokuMove) move;
        sudokuMove.apply();
    }

    @Override
    public void takeBack(PuzzleMove move)
    {
        SudokuMove sudokuMove = (SudokuMove) move;
        sudokuMove.takeBack();
    }

    @Override
    public boolean isSolved()
    {
        return board.getCellsWithoutNumbers().size() == 0;
//        return puzzle.goal == PuzzleGoal.Init
//                ? board.getCellsWithNumbers().size() >= puzzle.level.initialFill
//                : board.getCellsWithoutNumbers().size() == 0;
    }

    @Override
    public void reset()
    {
        board.getCells().stream().filter(cell -> !cell.isInitial()).forEach(cell -> cell.removeNumber());
    }

    public void setInitial()
    {
        board.getCells().forEach(cell -> cell.setInitial(cell.getNumber() > 0));
    }
}
