package com.mk.android.sudoku;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
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

import com.mk.puzzle.common.Logger;
import com.mk.puzzle.common.Puzzle;
import com.mk.puzzle.common.PuzzleGoal;
import com.mk.puzzle.common.PuzzleSolver;
import com.mk.puzzle.sudoku.SudokuCell;
import com.mk.puzzle.sudoku.SudokuPuzzle;
import com.mk.puzzle.sudoku.SudokuState;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@TargetApi(24)
public class MainActivity extends AppCompatActivity
{
    private final Logger logger = Logger.getLogger(getClass());
    public static final int count = 9;
    protected static int id = 100;

    private final CurrentInput currentInput = new CurrentInput();
    private Map<Point,SudokuButton> btnMap = new HashMap<>(count * count);
    private SudokuPuzzle puzzle = new SudokuPuzzle();
    private Context context;
    private TableRow.LayoutParams rlp = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT);
    private int padding = 1;
    private PuzzleTimer timer;
    enum ButtonType
    {
        Default(1.05f, Color.GRAY),
        Initial(1.20f, Color.BLACK),
        Hint(1.30f, Color.argb(160, 160, 60, 60)); // R.color.colorBtnHint // TODO - Animate
        float scale;
        int color;
        ButtonType(float scale, int color)
        {
            this.scale = scale;
            this.color = color;
        }
    }

    /**
     * Need a final value object to store values in onClick methods
     */
    class CurrentInput
    {
        SudokuButton button;

        public void uncheck()
        {
            if (button != null)
            {
                button.btnView.setChecked(false); // Only one checked - aka radio
            }
        }
    }

    /**
     * Single Cell View - SudokuCell is the model
     */
    class SudokuButton
    {
        SudokuPuzzle puzzle; // Need ref to access in AsyncTask
        Point point;
        ToggleButton btnView = new ToggleButton(context); // May change the view to something else?

        SudokuButton(SudokuPuzzle puzzle, int x, int y)
        {
            this.puzzle = puzzle;
            point = new Point(x, y);
            setId(x, y, ++id);
            btnView.setId(id);
            SudokuButton self = this;
            btnView.setOnClickListener(view ->
            {
                btnMap.values().forEach(SudokuButton::unhighlight);
                if (currentInput.button != null && currentInput.button != self)
                {
                    currentInput.uncheck(); // Only one checked - aka radio
                }
                currentInput.button = self;
                if (btnView.isChecked())
                {
                    getCell().getTeammates().forEach(mate ->
                    {
                        Point p = mate.getPoint();
                        SudokuButton button = btnMap.get(p);
                        button.highlight();
                    });
                }
            });
            btnView.setOnLongClickListener(view ->
            {
                setValue(0);
                return true;
            });
            setValue(0); // x + "." + y
            btnView.setLayoutParams(rlp);
            btnView.setPadding(padding,padding,padding,padding);
            setEnabled(true);
            setButtonType(ButtonType.Default);
        }

        public SudokuCell getCell()
        {
            SudokuState state = (SudokuState) puzzle.getState();
            return state.getBoard().getCell(point);
        }

        // Delegate to view
        void setEnabled(boolean b)
        {
            btnView.setEnabled(b);
        }
        void setTextColor(int color)
        {
            btnView.setTextColor(color);
        }
        void highlight()
        {
            btnView.getBackground().setAlpha(205);
        }
        void unhighlight()
        {
            btnView.getBackground().setAlpha(255);
        }
        void highlightError()
        {
            btnView.setTextColor(Color.RED);
        }
        void setButtonType(ButtonType buttonType)
        {
            btnView.setTextColor(buttonType.color);
            btnView.setTextScaleX(buttonType.scale);
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
                if (getCell().isAvailable(number))
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

    /**
     * Input - TODO - Consider disabling "not-available" moves or popping this up as a context menu
     */
    class InputButton
    {
        int number;
        Button btn = new Button(context);

        InputButton(int number)
        {
            this.number = number;
            btn.setLayoutParams(rlp);
            btn.setText(String.valueOf(number));
            btn.setOnClickListener(view ->
            {
                if (currentInput.button != null)
                {
                    String text = String.valueOf(btn.getText());
                    int number1 = Integer.valueOf(text);
                    currentInput.button.setValue(0);
                    if (currentInput.button.getCell().isAvailable(number1))
                    {
                        // Good move!
                        currentInput.button.setButtonType(ButtonType.Default);
                    }
                    else
                    {
                        currentInput.button.highlightError();
                    }
                    currentInput.button.setValue(number1);
                    if (puzzle.isSolved())
                    {
                        timer.stop();
                        // Tell the user .. like they don't know already :)
                        puzzleSolved();
                    }
                }
            });
        }
    }

    class PuzzleTimer
    {
        long startTime = 0;
        long pauseTime = 0;
        long pauseStart = 0;
        long puzzleTime;
        boolean isPaused;
        Handler timerHandler = new Handler();
        TextView timerText = (TextView) findViewById(R.id.puzzleTime);
        TableLayout table = (TableLayout) findViewById(R.id.tableLayout);

        Runnable timerRunnable = new Runnable()
        {
            @Override
            public void run()
            {
                long currentPause = isPaused() ? System.currentTimeMillis() - pauseStart : 0;
                puzzleTime = System.currentTimeMillis() - startTime - pauseTime - currentPause;
                logger.debug("R.id.puzzleTime: " + R.id.puzzleTime + " timerText: " + timerText);
                timerText.setText(getPuzzleTime());
                timerHandler.postDelayed(this, 500);
            }
        };
        public void start()
        {
            startTime = System.currentTimeMillis();
            pauseTime = 0;
            timerHandler.postDelayed(timerRunnable, 0);
        }
        public void stop()
        {
            timerHandler.removeCallbacks(timerRunnable);
        }
        void pause()
        {
            setPaused(true);
            pauseStart = System.currentTimeMillis();
            table.setVisibility(View.INVISIBLE);
            invalidateOptionsMenu();
        }
        void unpause()
        {
            setPaused(false);
            pauseTime += System.currentTimeMillis() - pauseStart;
            table.setVisibility(View.VISIBLE);
            invalidateOptionsMenu();
        }
        boolean isPaused()
        {
            return isPaused;
        }
        private void setPaused(boolean paused)
        {
            isPaused = paused;
        }

        public String getPuzzleTime()
        {
            int seconds = (int) (puzzleTime / 1000);
            int minutes = seconds / 60;
            seconds = seconds % 60;
            return String.format(Locale.getDefault(), "%d:%02d", minutes, seconds);
        }

        String getPuzzleTimeWithMillis()
        {
            int millis = (int) (puzzleTime % 1000);
            return getPuzzleTime() + String.format(Locale.getDefault(), ".%03d", millis);
        }
    }

    abstract class PuzzleTask extends AsyncTask<Puzzle, Integer, Puzzle>
    {
        abstract PuzzleGoal getGoal();

        @Override
        protected void onPostExecute(Puzzle puzzle)
        {
            currentInput.uncheck();
            currentInput.button = null;
            SudokuState state = (SudokuState) puzzle.getState();
            switch (getGoal())
            {
                case Solve:
                {
                    timer.stop();
                    if (puzzle.isSolved())
                    {
                        puzzleSolved();
                    }
                    else
                    {
                        failedToSolve();
                    }
                    break;
                }
                case Generate:
                {
                    TextView puzzleDifficulty = findViewById(R.id.puzzleDifficulty);
                    logger.debug("R.id.puzzleDifficulty: " + R.id.puzzleDifficulty + " puzzleDifficulty: " + puzzleDifficulty);
                    puzzleDifficulty.setText(String.valueOf(puzzle.getDifficulty())); // This is bogus. TODO - Total branches while solving it
//                    timer.start(); // No need it'll continue thru reset
//                    break;
                }
                case Reset:
                {
                    timer.start();
                    break;
                }
            }
            state.getBoard().getCells().forEach(cell ->
            {
                Point p = cell.getPoint();
                SudokuButton sb = btnMap.get(p);
                if (sb != null)
                {
                    sb.unhighlight();
                    sb.setValue(cell.getNumber());
                    sb.setEnabled(true);
                    if (cell.isInitial())
                    {
                        sb.setEnabled(false);
                        sb.setButtonType(ButtonType.Initial);
                    }
                    else if (cell.isHint())
                    {
                        sb.setButtonType(ButtonType.Hint);
                    }
                    else
                    {
                        sb.setButtonType(ButtonType.Default);
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

        @Override
        PuzzleGoal getGoal()
        {
            return PuzzleGoal.Generate;
        }

        PuzzleGenerateTask(SudokuPuzzle.Level level)
        {
            this.level = level;
        }

        @Override
        protected Puzzle doInBackground(Puzzle... puzzleContext)
        {
            SudokuPuzzle puzzle = (SudokuPuzzle) puzzleContext[0];
            puzzle.setState(new SudokuState(puzzle));
            puzzle.generate(level);
            return puzzle;
        }
    }

    class PuzzleSolveTask extends PuzzleTask
    {
        @Override
        PuzzleGoal getGoal()
        {
            return PuzzleGoal.Solve;
        }

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
            timer.start();
            puzzleSolver.solveInPlace(puzzle.getState());
            return puzzle;
        }
    }

    class PuzzleHintTask extends PuzzleTask
    {
        @Override
        PuzzleGoal getGoal()
        {
            return PuzzleGoal.Hint;
        }

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
            puzzle.setGoal(PuzzleGoal.Hint);
//            timer.start();
            puzzleSolver.applyHint(puzzle.getState());
            return puzzle;
        }
    }

    class PuzzleResetTask extends PuzzleTask
    {
        @Override
        PuzzleGoal getGoal()
        {
            return PuzzleGoal.Reset;
        }

        @Override
        protected void onProgressUpdate(Integer... values)
        {
            super.onProgressUpdate(values);
        }

        @Override
        protected Puzzle doInBackground(Puzzle... puzzleContext)
        {
            SudokuPuzzle puzzle = (SudokuPuzzle) puzzleContext[0];
            puzzle.reset();
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

    @Override
    public boolean onPrepareOptionsMenu(Menu menu)
    {
        MenuItem item = menu.findItem(R.id.solve);
        if (timer.isPaused())
        {
            item.setEnabled(false);
            item.getIcon().setAlpha(130);
        }
        else
        {
            item.setEnabled(true);
            item.getIcon().setAlpha(255);
        }
        return true;
    }

    public boolean newPuzzle(MenuItem item)
    {
        try
        {
            SudokuPuzzle.Level level = SudokuPuzzle.Level.valueOf(item.toString());
            TextView puzzleLevel = findViewById(R.id.puzzleLevel);
            logger.debug("R.id.puzzleLevel: " + R.id.puzzleLevel + " puzzleLevel: " + puzzleLevel);
            puzzleLevel.setText(level.name());
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

    public boolean hintNext(MenuItem item)
    {
        try
        {
            PuzzleHintTask puzzleSolveTask = new PuzzleHintTask();
            puzzleSolveTask.execute(puzzle);
            return true;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }
    }

    public boolean resetPuzzle(MenuItem item)
    {
        try
        {
            PuzzleResetTask puzzleResetTask = new PuzzleResetTask();
            puzzleResetTask.execute(puzzle);
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

    private void puzzleSolved()
    {
        done("Solved!", AlertDialog.BUTTON_POSITIVE);
    }

    private void failedToSolve()
    {
        done("Couldn't Solve!", AlertDialog.BUTTON_NEGATIVE);
    }

    private void done(String text, int whichButton)
    {
        AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
        alertDialog.setTitle(text);
        alertDialog.setMessage("In: " + timer.getPuzzleTimeWithMillis());
        alertDialog.setButton(whichButton, "OK", (DialogInterface dialog, int which) -> dialog.dismiss());
        alertDialog.show();
        currentInput.button = null;
    }

    public void pausePuzzle(View view)
    {
        if (timer.isPaused())
            timer.unpause();
        else
            timer.pause();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;
        timer = new PuzzleTimer();

        TableLayout table = (TableLayout) findViewById(R.id.tableLayout);
        for (int y = 1; y <= 9; y++)
        {
//            LinearLayout ll = new LinearLayout(this); // Won't work :( need to redo the whole TableLayout
//            ll.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            TableRow row = new TableRow(this);
            row.setLayoutParams(rlp);
            for (int x = 1; x <= 9; x++)
            {
                SudokuButton sb = new SudokuButton(puzzle, x, y);
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
        divider.setLayoutParams(new RelativeLayout.LayoutParams(1, TableRow.LayoutParams.MATCH_PARENT)); // rlp makes all columns stretch to same width
        divider.setBackgroundColor(Color.WHITE);
        return divider;
    }
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setContentView(R.layout.activity_main);
    }}
