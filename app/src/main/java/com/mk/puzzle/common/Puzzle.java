package com.mk.puzzle.common;


/**
 * @author MK
 */
public interface Puzzle
{
    PuzzleState getState();
    default int getDifficulty() {return 0;};
    default void load() {};
    default void init() {};
    default int getTimeLimit() {return 10;};
    PuzzleMethod getMethod();
    default void setState(PuzzleState state) {};
    default void setGoal(PuzzleGoal goal) { }
    default PuzzleGoal getGoal() {return PuzzleGoal.Solve;}
    default void generate() {}
    default boolean keepTrying() {return false;}
}
