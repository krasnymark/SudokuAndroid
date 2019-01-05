package com.mk.puzzle.crossword;

import android.graphics.Point;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import com.mk.puzzle.common.PuzzleMove;
import com.mk.puzzle.common.PuzzleState;

public class CrosswordState implements PuzzleState, Cloneable
{
	private Board board;
	private List <Word> wordsDone = new ArrayList<>();
	private List <Word> wordsToGo = new ArrayList<>();
	private List <Cell> potentialCrossings = new ArrayList<Cell>();
	private Point[] directions = {new Point(1,0), new Point(0,1)};
	private boolean center = true; // Start trying from the center of the board

	public CrosswordState(int size)
	{
		super();
		board = new Board(size);
	}

	// Copy Constructor
	public CrosswordState(CrosswordState state)
	{
		super();
		board = new Board(state.getBoard());
		wordsDone = new ArrayList<>(state.getWordsDone());
		wordsToGo = new ArrayList<>(state.getWordsToGo());
		potentialCrossings = new ArrayList<>();
		for (Cell cell : state.getPotentialCrossings())
		{
			potentialCrossings.add(new Cell(cell));
		}
	}

	public Board getBoard()
	{
		return board;
	}

	public void setBoard(Board board)
	{
		this.board = board;
	}

	public List<PuzzleMove> getAvailableMoves()
	{
		// TODO - Move from center to the edges.
		final int size = board.getSize();
		List<PuzzleMove> moves = new ArrayList<PuzzleMove>();
		for (Word word : wordsToGo)
		{
			char[] chars = word.getWord().toCharArray();
			int length = chars.length;
			if (wordsDone.isEmpty())
			{
				for (int k = 0; k < directions.length; k++)
				for (int i = 0; i < board.getSize() - (length - 1) * directions[k].x; i++)
				for (int j = 0; j < board.getSize() - (length - 1) * directions[k].y; j++)
				{
					Cell cell = board.getCells()[i][j];
					moves.add(new CrosswordMove(word, cell.getPoint(), directions[k]));
				}
				if (isCenter())
				{
					Set<PuzzleMove> sortedMoves = new TreeSet<PuzzleMove>(new Comparator<PuzzleMove>()
					{

						public int compare(PuzzleMove m1, PuzzleMove m2)
						{
							CrosswordMove move1 = (CrosswordMove) m1;
							CrosswordMove move2 = (CrosswordMove) m2;
							int boardCenter = size / 2;
							int l1 = move1.getWord().getWord().length() / 2;
							int l2 = move2.getWord().getWord().length() / 2;
							Point c1 = new Point(move1.getStart().x + l1 * move1.getDirection().x, move1.getStart().y + l1
									* move1.getDirection().y);
							Point c2 = new Point(move2.getStart().x + l2 * move2.getDirection().x, move2.getStart().y + l2
									* move2.getDirection().y);
							int d1 = Math.abs(boardCenter - c1.x) + Math.abs(boardCenter - c1.y);
							int d2 = Math.abs(boardCenter - c2.x) + Math.abs(boardCenter - c2.y);
							return d1 - d2;
						}

					});
					sortedMoves.addAll(moves);
					moves.clear();
					moves.addAll(sortedMoves);
				}
			}
			else
			{
				for (Cell potentialCrossing : potentialCrossings)
				{
					for (int i = 0; i < length; i++)
					{
						if (chars[i] == potentialCrossing.getLetter())
						{
							// TODO - Try to intersect in the orthogonal direction
							Point oDirection = potentialCrossing.getDirection();
							Point direction = getOrthogonalDirection(oDirection);
							Point start = new Point(potentialCrossing.getPoint());
							translate(start, -i * direction.x, -i * direction.y);
							// Start should be inside
							if (!isPointValid(start)) break;
							Point point = new Point(start);
							// Should not begin after letter
							translate(point, direction.x, -direction.y);
							if (isPointValid(point) && board.getCells()[point.x][point.y].getLetter() != 0) break;
							// Go to end
							translate(point,direction.x * length, direction.y * length);
							// End should be inside
							if (!isPointValid(point)) break;
							// Should not end before letter
							translate(point, direction.x, direction.y);
							if (isPointValid(point) && board.getCells()[point.x][point.y].getLetter() != 0) break;
							point = new Point(start);
							boolean isMoveValid = true;
							for (int j = 0; j < length; j++)
							{
								Cell cell = board.getCells()[point.x][point.y];
								if (cell.getLetter() == 0)
								{
									Cell neighbor = board.getCell(point.x - oDirection.x, point.y - oDirection.y);
									isMoveValid = neighbor == null || neighbor.getLetter() == 0;
									if (!isMoveValid) break;
									neighbor = board.getCell(point.x + oDirection.x, point.y + oDirection.y);
									isMoveValid = neighbor == null || neighbor.getLetter() == 0;
									if (!isMoveValid) break;
								}
								else if (cell.getLetter() != chars[j] || cell.getDirection().equals(direction))
								{
									isMoveValid = false;
									break;
								}
								translate(point, direction.x, direction.y);
							}
							if (isMoveValid) moves.add(new CrosswordMove(word, start, direction));
						}
					}
				}
			}
		}
		return moves;
	}

	private void translate(Point p, int x, int y)
	{
		p.set(p.x + x, p.y + y);
	}

	public boolean isPointValid(int x, int y)
	{
		return board.isPointValid(x, y);
	}

	public boolean isPointValid(Point point)
	{
		return board.isPointValid(point);
	}

	public Point getOrthogonalDirection(Point direction)
	{
		return new Point(direction.y, direction.x);
	}

	public void applyMove(PuzzleMove move)
	{
		CrosswordMove aMove = (CrosswordMove) move;
		Word aWord = aMove.getWord();
		String word = aWord.getWord();
		Point start = aMove.getStart();
		Point direction = aMove.getDirection();
		for (int i = 0; i < word.length(); i++)
		{
			Cell cell = board.getCells()[start.x + direction.x * i][start.y + direction.y * i];
			cell.setLetter(word.charAt(i));
			cell.setDirection(cell.getDirection().x + direction.x, cell.getDirection().y + direction.y);
			if ((cell.getDirection().x + cell.getDirection().y) == 1) potentialCrossings.add(cell);
		}
		wordsDone.add(aWord);
		wordsToGo.remove(aWord);
	}

	@Override
	public void takeBack(PuzzleMove move)
	{
//		takeBack is tricky :(- clone state.
	}

	public CrosswordState clone() // Copy
	{
		return new CrosswordState(this);
	}

	public boolean isSolved()
	{
		return wordsToGo.isEmpty();
	}

	@Override
	public String toString()
	{
		// TODO Auto-generated method stub
		return board.toString() + "\nDone: " + wordsDone + "\nTogo: " + wordsToGo + "\n";
	}

	public List<Word> getWordsToGo()
	{
		return wordsToGo;
	}

	public void setWordsToGo(List<Word> wordsToGo)
	{
		this.wordsToGo = wordsToGo;
	}

	public List<Word> getWordsDone()
	{
		return wordsDone;
	}

	public void setWordsDone(List<Word> wordsDone)
	{
		this.wordsDone = wordsDone;
	}

	public List<Cell> getPotentialCrossings()
	{
		return potentialCrossings;
	}

	public void setPotentialCrossings(List<Cell> potentialCrossings)
	{
		this.potentialCrossings = potentialCrossings;
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
