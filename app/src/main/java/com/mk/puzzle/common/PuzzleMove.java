package com.mk.puzzle.common;

public interface PuzzleMove
{
    String getKey();
    default String getAltKey() { return getKey(); }
    default void setHint(boolean isHint) { };
    boolean isDeadEnd(PuzzleState state);
}
