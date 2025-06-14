package chess;

import java.util.Objects;

/**
 * Represents a single square position on a chess board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPosition
{
	private final int row, col;

	public ChessPosition(int row, int col)
	{
		checkBounds(row, "row");
		checkBounds(col, "column");
		this.row = row;
		this.col = col;
	}

	/**
	 * @return which row this position is in
	 * 1 codes for the bottom row
	 */
	public int getRow()
	{
		return row;
	}

	/**
	 * @return which column this position is in
	 * 1 codes for the left row
	 */
	public int getColumn()
	{
		return col;
	}

	private void checkBounds(int index, String type)
	{
		if(index < 1 || index > 8)
		{
			throw new RuntimeException("Invalid " + type + ": " + index + " you must be between 1-8 (inclusive)");
		}
	}

	/**
	 *
	 * @param rowOffset Integer value to add to class instance private row variable.
	 * @param colOffset Integer value to add to class instance private column variable.
	 * @return New ChessPosition that reflects the integer modifications to the row & column.
	 */
	public ChessPosition offset(int rowOffset, int colOffset)
	{
		return new ChessPosition(this.row + rowOffset, this.col + colOffset);
	}

	@Override
	public boolean equals(Object o)
	{
		if (o == null || getClass() != o.getClass())
		{
			return false;
		}
		ChessPosition that = (ChessPosition) o;
		return row == that.row && col == that.col;
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(row, col);
	}

	@Override
	public String toString()
	{
		char column = (char) (64 + col);

		return column + Integer.toString(row);
	}
}
