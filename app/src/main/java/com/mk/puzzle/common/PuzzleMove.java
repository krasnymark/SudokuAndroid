package com.mk.puzzle.common;

public interface PuzzleMove
{
    String getKey();
    default String getAltKey() { return getKey(); }
    boolean isDeadEnd(PuzzleState state);
}
