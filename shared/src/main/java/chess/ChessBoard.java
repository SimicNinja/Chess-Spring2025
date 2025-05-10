package chess;

import java.util.Arrays;
import java.util.Objects;

/**
 * A chessboard that can hold and rearrange chess pieces.
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessBoard
{
	private ChessPiece[][] board;

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
		board[position.getRow() - 1][position.getColumn() - 1] = piece;
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
		return board[position.getRow() - 1][position.getColumn() - 1];
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
		board = new ChessPiece[8][8];
	}

	private void pawnRow(int row, ChessGame.TeamColor color)
	{
		for(int col = 0; col < 8; col++)
		{
			board[row][col] = new ChessPiece(color, ChessPiece.PieceType.PAWN);
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
					board[row][col] = new ChessPiece(color, ChessPiece.PieceType.ROOK);
					break;
				case 1:
				case 6:
					board[row][col] = new ChessPiece(color, ChessPiece.PieceType.KNIGHT);
					break;
				case 2:
				case 5:
					board[row][col] = new ChessPiece(color, ChessPiece.PieceType.BISHOP);
					break;
				case 3:
					board[row][col] = new ChessPiece(color, ChessPiece.PieceType.QUEEN);
					break;
				case 4:
					board[row][col] = new ChessPiece(color, ChessPiece.PieceType.KING);
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
		return Objects.deepEquals(board, that.board);
	}

	@Override
	public int hashCode()
	{
		return Arrays.deepHashCode(board);
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
				if(board[row][col] != null)
				{
					output.append(board[row][col].toString());
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
