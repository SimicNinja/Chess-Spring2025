package chess;

import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import chess.ChessGame.ChessPieceAndPosition;

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

	public ChessBoard(ChessBoard original)
	{
		this.boardState = new ChessPiece[8][8];

		for(int row = 0; row < 8; row++)
		{
			for(int col = 0; col < 8; col++)
			{
				ChessPiece piece = original.boardState[row][col];

				if(piece != null)
				{
					this.boardState[row][col] = new ChessPiece(piece.getTeamColor(), piece.getPieceType());
				}
			}
		}
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

	private class ChessPieceIterator implements Iterator<ChessPieceAndPosition>
	{
		private final ChessGame.TeamColor targetTeam;
		private int row = 0;
		private int col = 0;
		private boolean hasNext = false;

		public ChessPieceIterator(ChessGame.TeamColor color)
		{
			targetTeam = color;
			findNext();
		}

		@Override
		public boolean hasNext()
		{
			return hasNext;
		}

		@Override
		public ChessPieceAndPosition next()
		{
			if(!hasNext())
			{
				throw new NoSuchElementException("No more chess pieces");
			}

			ChessPiece current = boardState[row][col];
			ChessPosition position = new ChessPosition(row + 1, col + 1);

			col++;
			if(col > 7)
			{
				col = 0;
				row++;
			}
			findNext();

			return new ChessPieceAndPosition(current, position);
		}

		private void findNext()
		{
			hasNext = false;
			while(row < 8)
			{
				while(col < 8)
				{
					ChessPiece piece = boardState[row][col];

					if(piece != null && piece.getTeamColor() == targetTeam)
					{
						hasNext = true;
						return;
					}

					col++;
				}

				col = 0;
				row++;
			}
		}
	}

	public boolean occupied(ChessPosition position)
	{
		return boardState[position.getRow() - 1][position.getColumn() - 1] != null;
	}

	public boolean containsAlly(ChessPosition position, ChessPiece piece)
	{
		return this.occupied(position) && getPiece(position).getTeamColor() == piece.getTeamColor();
	}

	/**
	 * Checks is an enemy is located at the designate positon
	 *
	 * @param position The position that may contain an enemy
	 * @param piece    The piece you are moving
	 * @return True if enemy piece is at position, otherwise false
	 */
	public boolean containsEnemy(ChessPosition position, ChessPiece piece)
	{
		return this.occupied(position) && getPiece(position).getTeamColor() != piece.getTeamColor();
	}

	public boolean inStartingPosition(ChessPiece testPiece, ChessPosition position)
	{
		ChessBoard startingBoard = new ChessBoard();
		startingBoard.resetBoard();

		for(ChessPieceAndPosition piecePair : startingBoard.getTeamPieces(testPiece.getTeamColor()))
		{
			if(piecePair.piece().getPieceType() == testPiece.getPieceType() && position.equals(piecePair.position()))
			{
				return true;
			}
		}

		return false;
	}
	
	/**
	 * Applies ChessMove to the board. Assumes that the move is valid as logic is found in makeMove() in ChessGame.
	 */
	public void movePiece(ChessMove move)
	{
		//Readability variables
		ChessPiece piece = getPiece(move.getStartPosition());
		ChessGame.TeamColor color = piece.getTeamColor();
		ChessPosition start = move.getStartPosition();
		ChessPosition end = move.getEndPosition();

		switch(move.getPromotionPiece())
		{
			case null:
				this.addPiece(end, new ChessPiece(color, piece.getPieceType(), true));
				break;
			case QUEEN:
				this.addPiece(end, new ChessPiece(color, ChessPiece.PieceType.QUEEN, true));
				break;
			case KNIGHT:
				this.addPiece(end, new ChessPiece(color, ChessPiece.PieceType.KNIGHT, true));
				break;
			case BISHOP:
				this.addPiece(end, new ChessPiece(color, ChessPiece.PieceType.BISHOP, true));
				break;
			case ROOK:
				this.addPiece(end, new ChessPiece(color, ChessPiece.PieceType.ROOK, true));
				break;
			case KING, PAWN:
				break;
		}


		if(piece.getPieceType() == ChessPiece.PieceType.KING && abs(start.getColumn() - end.getColumn()) > 1)
		{
			moveRook(end, color);
		}
		else if(piece.getPieceType() == ChessPiece.PieceType.PAWN)
		{
			//En Passant Move Implementation
		}

		this.removePiece(move.getStartPosition());
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

	/**
	 * Removes piece at the given position.
	 * @param position Location for piece to be removed.
	 */
	private void removePiece(ChessPosition position)
	{
		boardState[position.getRow() - 1][position.getColumn() - 1] = null;
	}

	/**
	 * Logic for castling; assumes move is valid.
	 * @param kingEnd End location of the castling king.
	 * @param kingColor Team color of the castling king.
	 */
	private void moveRook(ChessPosition kingEnd, ChessGame.TeamColor kingColor)
	{
		if(kingEnd.getColumn() == 3)
		{
			if(kingColor == ChessGame.TeamColor.WHITE)
			{
				removePiece(new ChessPosition(1, 1));
			}
			else
			{
				removePiece(new ChessPosition(8, 1));
			}
			this.addPiece(kingEnd.offset(0, 1), new ChessPiece(kingColor, ChessPiece.PieceType.ROOK, true));
		}
		else if(kingEnd.getColumn() == 7)
		{
			if(kingColor == ChessGame.TeamColor.WHITE)
			{
				removePiece(new ChessPosition(1, 8));
			}
			else
			{
				removePiece(new ChessPosition(8, 8));
			}
			this.addPiece(kingEnd.offset(0, -1), new ChessPiece(kingColor, ChessPiece.PieceType.ROOK, true));
		}
	}
}
