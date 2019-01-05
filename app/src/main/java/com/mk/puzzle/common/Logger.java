package com.mk.puzzle.common;

import java.io.OutputStream;
import java.io.PrintStream;

public class Logger
{
    private String context;
    private PrintStream out = System.out;

    public Logger(String context)
    {
        this.context = context;
    }

    public Logger(Class clazz)
    {
        this.context = clazz.getName();
    }

    public static Logger getLogger(Class clazz)
    {
        return new Logger(clazz);
    }

    public void error(Object text)
    {
        out.println(text);
    }

    public void info(Object text)
    {
        out.println(text);
    }

    public void debug(Object text)
    {
        out.println(text);
    }
}
