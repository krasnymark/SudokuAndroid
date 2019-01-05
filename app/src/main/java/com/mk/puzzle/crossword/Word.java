package com.mk.puzzle.crossword;


public class Word
{
	private String word;
	private String clue;

	public Word(String clue, String word)
	{
		super();
		this.clue = clue;
		this.word = word;
	}

	public String getWord()
	{
		return word;
	}

	public void setWord(String word)
	{
		this.word = word;
	}

	public String getClue()
	{
		return clue;
	}

	public void setClue(String clue)
	{
		this.clue = clue;
	}

	@Override
	public String toString()
	{
		return getWord(); // + " - " + getClue();
	}

}
