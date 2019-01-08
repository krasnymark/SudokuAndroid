package com.mk.puzzle.common;


/**
 * @author MK
 */
public interface Puzzle
{
    PuzzleState getState();
    default boolean isSolved() {return getState().isSolved();};
    default boolean keepTrying() {return false;}
    default int getDifficulty() {return 0;};
    default int getTimeLimit() {return 10;};
    default void load() {};
    default void init() {};
    default void generate() {}
    default void setState(PuzzleState state) {};
    default void setGoal(PuzzleGoal goal) { }
    default PuzzleGoal getGoal() {return PuzzleGoal.Solve;}
    default PuzzleMethod getMethod() {return PuzzleMethod.InPlace;};
}
