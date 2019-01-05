package com.mk.puzzle.common;

public class StringUtils
{
    public static int countOccurrencesOf(String s, String pattern)
    {
        return (s.length() - s.replaceAll(pattern, "").length()) / pattern.length();
    }
}
