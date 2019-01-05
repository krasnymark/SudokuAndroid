package com.mk.puzzle.crossword;

import com.mk.puzzle.common.Logger;
import com.mk.puzzle.common.Puzzle;
import com.mk.puzzle.common.PuzzleMethod;
import com.mk.puzzle.common.PuzzleMove;
import com.mk.puzzle.common.PuzzleSolver;
import com.mk.puzzle.common.PuzzleState;
import com.mk.puzzle.common.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.TreeMap;

/**
 * @author MK
 *
 */
public class CrosswordPuzzle implements Puzzle
{
	private final static Logger logger = Logger.getLogger(CrosswordPuzzle.class);
	private final static String[] DELIMITERS = {"\t", "|", "~", "^", "!", "#", ";", ":", ",", "="};
	private String fileName;
	private List <Word> words = new ArrayList<>();
	private CrosswordState state;
	private int timeLimit = 10;
	private double maxRatio = 1.25;
	private double sumRatio = 1.25;
	private boolean shuffle = true;
	private boolean center;
	private Random rand = new Random();

	@Override
	public void load()
	{
		try
		{
			Scanner scanner = new Scanner(new File(getFileName()));
			List <String> lines = new ArrayList<>();
			while (scanner.hasNext())
			{
				lines.add(scanner.nextLine());
			}
			String delimiter = testForDelimiter(lines);
			for (String line : lines)
			{
				parseLine(line, delimiter);
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	private static String testForDelimiter(List <String> lines)
	{
		for (String delimiter : DELIMITERS)
		{
			int count = StringUtils.countOccurrencesOf(lines.get(0), delimiter);
			if (count == 0) continue;
			for (String line : lines)
			{
				if (StringUtils.countOccurrencesOf(line, delimiter) !=  count)
				{
					count = 0;
					break;
				}
			}
			if (count > 0) return delimiter;
		}
		return "";
	}

	@Override
	public boolean keepTrying()
	{
		return true;
	}

	@Override
	public void init()
	{
		logger.debug("CrosswordPuzzle.init");
		logger.debug("Words: " + words);
		int maxLength = 0;
		int sumLength = 0;
		for (Word word : words)
		{
			int length = word.getWord().length();
			maxLength = Math.max(maxLength, length);
			sumLength += length;
		}
		int size;
		if (state == null)
		{
			size = (int) Math.max(maxLength * maxRatio, Math.sqrt(sumLength * sumRatio));
		}
		else
		{
			logger.debug("Failed the time limit - resizing!");
			size = state.getBoard().getSize() + 1;
		}
		state = new CrosswordState(size);
		state.setWordsToGo(isShuffle() ? shuffle(words) : words);
		state.setCenter(center);
		logger.debug("Count: " + words.size() + " Longest: " + maxLength + " Total: " + sumLength + " Square: " + size);
	}

	private List<Word> shuffle(List<Word> in)
	{
		List<Word> out = new ArrayList<>();
		Map<String, Word> sortedWords = new TreeMap<>(new Comparator<String>()
		{
			public int compare(String s1, String s2)
			{
				return s1.substring(rand.nextInt(s1.length()-1)).compareTo(s2.substring(rand.nextInt(s2.length()-1)));
			}
		});
		for (Word word : in)
		{
			sortedWords.put(word.getClue(), word);
		}
		out.addAll(sortedWords.values());
		return out;
	}

	private void parseLine(String line, String delimiter)
	{
		Scanner scanner = new Scanner(line);
		scanner.useDelimiter(delimiter);
		String word = scanner.next();
		String clue = scanner.next();
		words.add(new Word(word.trim(), clue.trim()));
	}

	public void setState(PuzzleState state)
	{
		this.state = (CrosswordState) state;
	}

	public PuzzleState getState()
	{
		return state;
	}

	public List<PuzzleMove> getAvailableMoves()
	{
		// TODO Auto-generated method stub
		logger.debug("getAvailableMoves");
		return state.getAvailableMoves();
	}

	private String getFileName()
	{
		return fileName;
	}

	public void setFileName(String fileName)
	{
		this.fileName = fileName;
	}

	public boolean isSolved()
	{
		return state.isSolved();
	}

	@Override
	public int getTimeLimit()
	{
		return timeLimit;
	}

    @Override
    public PuzzleMethod getMethod()
    {
        return PuzzleMethod.Clone;
    }

    public void setTimeLimit(int timeLimit)
	{
		this.timeLimit = timeLimit;
	}

	public double getMaxRatio()
	{
		return this.maxRatio;
	}

	public void setMaxRatio(double maxRatio)
	{
		this.maxRatio = maxRatio;
	}

	public double getSumRatio()
	{
		return this.sumRatio;
	}

	public void setSumRatio(double sumRatio)
	{
		this.sumRatio = sumRatio;
	}

	private boolean isShuffle()
	{
		return this.shuffle;
	}

	public void setShuffle(boolean shuffle)
	{
		this.shuffle = shuffle;
	}

	public boolean isCenter()
	{
		return center;
	}

	public void setCenter(boolean center)
	{
		this.center = center;
	}

}
