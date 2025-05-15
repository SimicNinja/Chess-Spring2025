package chess;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Objects;

import static java.lang.Math.abs;

/**
 * A chessboard that can hold and rearrange chess pieces.
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessBoard
{
	private ChessPiece[][] boardState;

	public ChessBoard()
	{
		clearBoard();
	}

	/**
	 * Adds a chess piece to the chessboard
	 *
	 * @param position where to add the piece to
	 * @param piece    the piece to add
	 */
	public void addPiece(ChessPosition position, ChessPiece piece)
	{
		boardState[position.getRow() - 1][position.getColumn() - 1] = piece;
	}

	/**
	 * Gets a chess piece on the chessboard
	 *
	 * @param position The position to get the piece from
	 * @return Either the piece at the position, or null if no piece is at that
	 * position
	 */
	public ChessPiece getPiece(ChessPosition position)
	{
		return boardState[position.getRow() - 1][position.getColumn() - 1];
	}

	/**
	 * Implemented to make for each loop usable in ChessGame
	 *
	 * @param color The color of the team you are looking for
	 * @return An iterator used to find all the pieces on the board of a given team/color
	 */
	public Iterable<ChessGame.ChessPieceAndPosition> getTeamPieces(ChessGame.TeamColor color)
	{
		return () -> new ChessPieceIterator(color);
	}

	private class ChessPieceIterator implements Iterator<ChessGame.ChessPieceAndPosition>
	{
		public ChessPieceIterator(ChessGame.TeamColor color) {
		}

		@Override
		public boolean hasNext() {
			return false;
		}

		@Override
		public ChessGame.ChessPieceAndPosition next() {
			return null;
		}
	}
	
	/**
	 * Applies ChessMove to the board. Assumes that the move is valid as logic is found in makeMove() in ChessGame.
	 */
	public void movePiece(ChessMove move)
	{
		throw new RuntimeException("Not Implemented");
	}

	/**
	 * Sets the board to the default starting board
	 * (How the game of chess normally starts)
	 */
	public void resetBoard()
	{
		clearBoard();
		powerRow(0, ChessGame.TeamColor.WHITE);
		pawnRow(1, ChessGame.TeamColor.WHITE);
		pawnRow(6, ChessGame.TeamColor.BLACK);
		powerRow(7, ChessGame.TeamColor.BLACK);
	}

	private void clearBoard()
	{
		boardState = new ChessPiece[8][8];
	}

	private void pawnRow(int row, ChessGame.TeamColor color)
	{
		for(int col = 0; col < 8; col++)
		{
			boardState[row][col] = new ChessPiece(color, ChessPiece.PieceType.PAWN);
		}
	}

	private void powerRow(int row, ChessGame.TeamColor color)
	{
		for(int col = 0; col < 8; col++)
		{
			switch (col)
			{
				case 0:
				case 7:
					boardState[row][col] = new ChessPiece(color, ChessPiece.PieceType.ROOK);
					break;
				case 1:
				case 6:
					boardState[row][col] = new ChessPiece(color, ChessPiece.PieceType.KNIGHT);
					break;
				case 2:
				case 5:
					boardState[row][col] = new ChessPiece(color, ChessPiece.PieceType.BISHOP);
					break;
				case 3:
					boardState[row][col] = new ChessPiece(color, ChessPiece.PieceType.QUEEN);
					break;
				case 4:
					boardState[row][col] = new ChessPiece(color, ChessPiece.PieceType.KING);
					break;
			}
		}
	}

	@Override
	public boolean equals(Object o)
	{
		if (o == null || getClass() != o.getClass())
		{
			return false;
		}
		ChessBoard that = (ChessBoard) o;
		return Objects.deepEquals(boardState, that.boardState);
	}

	@Override
	public int hashCode()
	{
		return Arrays.deepHashCode(boardState);
	}

	@Override
	public String toString()
	{
		StringBuilder output = new StringBuilder();

		for(int row = 7; row > -1; row--)
		{
			for(int col = 0; col < 8; col++)
			{
				output.append("|");
				if(boardState[row][col] != null)
				{
					output.append(boardState[row][col].toString());
				}
				else
				{
					output.append(" ");
				}
			}
			output.append("|\n");
		}

		return String.valueOf(output);
	}
}
