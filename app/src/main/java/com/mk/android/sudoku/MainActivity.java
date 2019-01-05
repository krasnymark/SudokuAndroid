package com.mk.android.sudoku;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.mk.puzzle.common.Puzzle;
import com.mk.puzzle.common.PuzzleGoal;
import com.mk.puzzle.common.PuzzleSolver;
import com.mk.puzzle.sudoku.SudokuCell;
import com.mk.puzzle.sudoku.SudokuPuzzle;
import com.mk.puzzle.sudoku.SudokuState;

import java.util.HashMap;
import java.util.Map;

@TargetApi(24)
public class MainActivity extends AppCompatActivity
{
    public static int count = 9;

    private final CurrentInput currentInput = new CurrentInput();
    private Map<Point,SudokuButton> btnMap = new HashMap<>(count * count);
    private SudokuPuzzle puzzle = new SudokuPuzzle();
    private Context context;
    private TableRow.LayoutParams rlp = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT);
    private int padding = 1;
    protected static int id = 100;

    /**
     * Need a final value object to store values in onClick methods
     */
    class CurrentInput
    {
        SudokuButton button;
    }

    /**
     * Single Cell View
     */
    class SudokuButton
    {
        Point point;
        ToggleButton btnView = new ToggleButton(context); // May change the view to something else?

        public SudokuButton(int x, int y)
        {
            point = new Point(x, y);
            setId(x, y, ++id);
            btnView.setId(id);
            SudokuButton self = this;
            btnView.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    if (currentInput.button != null && currentInput.button != self)
                    {
                        currentInput.button.btnView.setChecked(false); // Only one checked - aka radio
                        btnMap.values().forEach(btn -> btn.unhighlight());
                    }
                    currentInput.button = self;
                    getCell().getTeammates().forEach(mate ->
                    {
                        Point p = mate.getPoint();
                        SudokuButton button = btnMap.get(p);
                        button.highlight();
                    });
                }
            });
            btnView.setOnLongClickListener(new View.OnLongClickListener()
            {
                @Override
                public boolean onLongClick(View v)
                {
                    setValue(0);
                    return true;
                }
            });
            setValue(0); // x + "." + y
            btnView.setLayoutParams(rlp);
            btnView.setPadding(padding,padding,padding,padding);
            btnView.getBackground();
            setNormalColor();
        }

        public SudokuCell getCell()
        {
            SudokuState state = (SudokuState) puzzle.getState();
            return state.getBoard().getCell(point);
        }

        // Delegate to view
        public void setEnabled(boolean b)
        {
            btnView.setEnabled(b);
        }
        public void setTextColor(int color)
        {
            btnView.setTextColor(color);
        }
        public void highlight()
        {
//            btnView.setBackgroundColor(Color.CYAN);
        }
        public void unhighlight()
        {
//            btnView.setBackgroundColor(Color.YELLOW);
        }
        public void highlightError()
        {
            btnView.setTextColor(Color.RED);
        }
        public void setNormalColor()
        {
            btnView.setTextColor(Color.GRAY);
        }

        public void setValue(int number)
        {
            if (number == 0)
            {
                getCell().removeNumber();
                setValue("");
            }
            else
            {
                getCell().setNumber(number);
                setValue(String.valueOf(number));
            }
        }
        public void setValue(String text)
        {
            btnView.setText(text);
            btnView.setTextOn(text);
            btnView.setTextOff(text);
        }

        private void setId(int x, int y, int id)
        {
            setId(new Point(x, y), id);
        }
        private void setId(Point p, int id)
        {
            btnMap.put(p, this);
        }
    }

    class InputButton
    {
        int number;
        Button btn = new Button(context);

        public InputButton(int number)
        {
            this.number = number;
            btn.setLayoutParams(rlp);
            btn.setText("" + number);
            btn.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    String text = String.valueOf(btn.getText());
                    int number = Integer.valueOf(text);
                    currentInput.button.setValue(0);
                    if (currentInput.button.getCell().getAvailableMoves().stream().anyMatch(move -> move.getNumber() == number))
                    {
                        currentInput.button.setNormalColor();
                    }
                    else
                    {
                        currentInput.button.highlightError();
                    }
                    currentInput.button.setValue(number);
                }
            });
        }
    }

    abstract class PuzzleTask extends AsyncTask<Puzzle, Integer, Puzzle>
    {

        @Override
        protected void onPostExecute(Puzzle puzzle)
        {
            SudokuState state = (SudokuState) puzzle.getState();
            if (state.getBoard().getCellsWithoutNumbers().size() > 0)
            {
                TextView puzzleDifficulty = findViewById(R.id.puzzleDifficulty);
                puzzleDifficulty.setText("Difficulty: " + puzzle.getDifficulty()); // This is bogus. TODO - Total branches while solving it
            }
            state.getBoard().getCells().forEach(cell ->
            {
                Point p = cell.getPoint();
                SudokuButton sb = btnMap.get(p);
                if (sb != null)
                {
                    sb.setValue(cell.getNumber());
                    if (cell.isInitial())
                    {
                        sb.setEnabled(false);
                        sb.setTextColor(Color.BLACK);
                    }
                }
                else
                {
                    System.err.println("Cannot find view for: " + p); //  + "=>" + getId(p)
                }
            });
        }
    }

    class PuzzleGenerateTask extends PuzzleTask
    {
        SudokuPuzzle.Level level;

        public PuzzleGenerateTask(SudokuPuzzle.Level level)
        {
            this.level = level;
        }

        @Override
        protected Puzzle doInBackground(Puzzle... puzzleContext)
        {
            SudokuPuzzle puzzle = new SudokuPuzzle(); // (SudokuPuzzle) puzzleContext[0];
            puzzle.generate(level);
            return puzzle;
        }
    }

    class PuzzleSolveTask extends PuzzleTask
    {
        @Override
        protected void onProgressUpdate(Integer... values)
        {
            super.onProgressUpdate(values);
        }

        @Override
        protected Puzzle doInBackground(Puzzle... puzzleContext)
        {
            SudokuPuzzle puzzle = (SudokuPuzzle) puzzleContext[0];
            PuzzleSolver puzzleSolver = new PuzzleSolver(puzzle);
            puzzle.setGoal(PuzzleGoal.Solve);
            puzzleSolver.solveInPlace(puzzle.getState());
            return puzzle;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.sudoku_menu, menu);
        return true;
    }

    public boolean newPuzzle(MenuItem item)
    {
        try
        {
            SudokuPuzzle.Level level = SudokuPuzzle.Level.valueOf(item.toString());
            TextView puzzleLevel = findViewById(R.id.puzzleLevel);
            puzzleLevel.setText("Level: " + level.name());
            PuzzleGenerateTask puzzleGenerateTask = new PuzzleGenerateTask(level);
            puzzleGenerateTask.execute(puzzle);
            return true;
        }
        catch (IllegalArgumentException e)
        {
            e.printStackTrace();
            return false;
        }
    }

    public boolean solvePuzzle(MenuItem item)
    {
        try
        {
            PuzzleSolveTask puzzleSolveTask = new PuzzleSolveTask();
            puzzleSolveTask.execute(puzzle);
            return true;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }
    }

    public boolean aboutPuzzle(MenuItem item)
    {
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;

        TableLayout table = (TableLayout) findViewById(R.id.tableLayout);
        for (int y = 1; y <= 9; y++)
        {
            TableRow row = new TableRow(this);
            row.setLayoutParams(rlp);
            for (int x = 1; x <= 9; x++)
            {
                SudokuButton sb = new SudokuButton(x, y);
                row.addView(sb.btnView);
                if (x == 3 || x == 6)
                {
                    row.addView(vDivider()); // v-divider - not working :(
                }
            }
            table.addView(row);
            if (y == 3 || y == 6)
            {
                table.addView(hDivider()); // h-divider
            }
        }
        TableLayout iTable = (TableLayout) findViewById(R.id.inputLayout);
        TableRow row = new TableRow(this);
        row.setLayoutParams(rlp);
        for (int x = 1; x <= 9; x++)
        {
            InputButton input = new InputButton(x);
            row.addView(input.btn);
        }
        iTable.addView(row);
    }

    @NonNull
    private View hDivider()
    {
        View divider = new View(this);
        divider.setLayoutParams(new RelativeLayout.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, 1));
        divider.setBackgroundColor(Color.WHITE);
        return divider;
    }

    @NonNull
    private View vDivider()
    {
        View divider = new View(this);
        divider.setLayoutParams(new RelativeLayout.LayoutParams(1, TableRow.LayoutParams.MATCH_PARENT));
        divider.setBackgroundColor(Color.WHITE);
        return divider;
    }
}
