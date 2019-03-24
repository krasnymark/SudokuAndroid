package com.mk.puzzle.sudoku;

import android.annotation.TargetApi;

import com.mk.puzzle.common.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

@TargetApi(24)
public class SudokuPuzzle implements Puzzle
{
    private final static Logger logger = Logger.getLogger(SudokuPuzzle.class);
    private SudokuState state;
    private String fileName;
    private int targetCount;
    private PuzzleSolver solver = new PuzzleSolver();
    protected PuzzleGoal goal = PuzzleGoal.Solve;
    protected Level level;
    public enum Level // TODO - Move Level up ?
    {
        Beginner(40)
    ,   Novice(35)
    ,   Intermediate(30)
    ,   Advanced(25)
    ,   Master(20);
        int initialFill;

        Level(int initialFill)
        {
            this.initialFill = initialFill;
        }
    }

    public SudokuPuzzle()
    {
        this.state = new SudokuState(this);
    }

    @Override
    public void load()
    {
        try
        {
            state = new SudokuState(this);
            SudokuBoard board = ((SudokuState) getState()).getBoard();
            Scanner scanner = new Scanner(new File(getFileName()));
            List<String> lines = new ArrayList<String>();
            int y = 1;
            while (scanner.hasNext())
            {
                String line = scanner.nextLine();
                String[] cells = line.split(",");
                for (int x = 0; x < cells.length; x++)
                {
                    int number = cells[x].trim().length() == 0 ? 0 : Integer.valueOf(cells[x]);
                    SudokuCell cell = board.setCell(x + 1, y, number);
                    if (number > 0) cell.setInitial(true);
                }
                y++;
            }
            logger.info(state);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void generate()
    {
        Arrays.stream(Level.values()).sorted(Comparator.reverseOrder()).forEach(level ->
        {
            int count = 0;
            int start = 0;
            while (count++ <= start + targetCount)
            {
                if (new File(getFileName(level, count)).exists())
                {
                    start = count;
                    continue;
                }
                if (generate(level))
                {
                    store(state, level, count);
                    logger.info("generated " + level + " # " + count + state);
                }
            }
        });
    }

    public boolean generate(Level level)
    {
        logger.info("Initializing level: " + level);
        state = new SudokuState(this);
        if (init(state, level) && solve(state, level))
        {
            reduceSolutionToPuzzle(state, level);
            logger.info("generated " + level + state);
            return true;
        }
        return false;
    }

    private void reduceSolutionToPuzzle(SudokuState state, Level level)
    {
        // 1. Remove some cells from STate, keep just enough for the "Level" to solve.
        // 2. Estimate difficulty
        Random r = new Random(System.currentTimeMillis());
        state.getBoard().getCells().stream().filter(cell -> r.nextInt(81) > level.initialFill).forEach(cell -> cell.removeNumber());
        state.setInitial();
    }

    private void store(SudokuState state, Level level, int count)
    {
        String fileName = getFileName(level, count);
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(fileName)))
        {
            bw.write(state.getBoard().toCSV());
        }
        catch (IOException e)
        {
            logger.error("Problem saving: " + fileName + " Error: " + e);
        }
    }

    private String getFileName(Level level, int count)
    {
        return "data/generated/Sudoku-" + level + "-" + count + ".csv";
    }

    private boolean init(SudokuState state, Level level)
    {
        this.level = level;
        setGoal(PuzzleGoal.Init);
        Stack<PuzzleMove> moves = new Stack<>();
        if (solver.solveInPlace(state, moves))
        {
            moves.forEach(move -> ((SudokuMove)move).getCell().setInitial(true));
            logger.info("Initialized level: " + level + state);
            return true;
        }
        return false;
    }

    private boolean solve(SudokuState state, Level level)
    {
        setGoal(PuzzleGoal.Solve);
        Stack<PuzzleMove> moves = new Stack<>();
        if (solver.solveInPlace(state, moves))
        {
            logger.info("Solved level: " + level + state);
            return true;
        }
        return false;
    }

    @Override
    public boolean solve()
    {
        setGoal(PuzzleGoal.Solve);
        if (solver.solveInPlace(state))
        {
            logger.info("Solved level: " + level + state);
            return true;
        }
        return false;
    }

    @Override
    public void reset()
    {
        getState().reset();
    }

    @Override
    public int getDifficulty()
    {
        return state.getBoard().getCellsWithoutNumbers().stream().mapToInt(cell -> cell.getAvailableCount()).map(aCount -> (int) Math.pow(10, aCount - 1)).sum();
    }

    @Override
    public PuzzleState getState()
    {
        return state;
    }

    @Override
    public PuzzleMethod getMethod()
    {
        return PuzzleMethod.InPlace;
    }

    public String getFileName()
    {
        return fileName;
    }

    public void setFileName(String fileName)
    {
        this.fileName = fileName;
    }

    public void setSolver(PuzzleSolver solver)
    {
        this.solver = solver;
    }

    @Override
    public void setGoal(PuzzleGoal goal)
    {
        this.goal = goal;
    }

    @Override
    public PuzzleGoal getGoal()
    {
        return goal;
    }

    public Level getLevel()
    {
        return level;
    }

    public void setLevel(Level level)
    {
        this.level = level;
    }

    public void setTargetCount(int targetCount)
    {
        this.targetCount = targetCount;
    }
}
