package chess.MoveCalculators;

import chess.*;

import java.util.ArrayList;

public class PawnMoveCalculator extends MoveCalculator
{
	private ChessGame.TeamColor color;
	private int teamDirection;

	public ArrayList<ChessMove> pieceMoves(ChessBoard board, ChessPosition start)
	{
		ChessPiece piece = board.getPiece(start);
		color = piece.getTeamColor();

		if(color == ChessGame.TeamColor.WHITE)
		{
			teamDirection = 1;
		}
		else
		{
			teamDirection = -1;
		}

		checkMove(1, 0, board, start);
		checkMove(1, 1, board, start);
		checkMove(1, -1, board, start);
		checkMove(2, 0, board, start);

		return moves;
	}

	@Override
	protected void checkMove(int rowOffset, int colOffset, ChessBoard board, ChessPosition start)
	{
		int row = start.getRow() + (rowOffset * teamDirection);
		int col = start.getColumn() + colOffset;

		if(checkBounds(row, col))
		{
			//Forward w/o capture
			if(positionOpen(row, col, board))
			{
				//DoubleMove
				if(rowOffset == 2 && positionOpen(row - (teamDirection), col, board))
				{
					//White
					if(color == ChessGame.TeamColor.WHITE && start.getRow() == 2)
					{
						makeMove(row, col, start);
					}
					//Black
					else if(color == ChessGame.TeamColor.BLACK && start.getRow() == 7)
					{
						makeMove(row, col, start);
					}
				}
				//Straight Forward
				else if(rowOffset == 1 && colOffset == 0)
				{
					makeMove(row, col, start);
				}
			}
			//Diagonals w/ capture
			else if(positionContainsEnemy(row, col, board, color) && colOffset != 0)
			{
				makeMove(row, col, start);
			}
		}
	}

	private void makeMove(int row, int col, ChessPosition start)
	{
		ChessPosition end = new ChessPosition(row, col);

		if(row == 8 && color == ChessGame.TeamColor.WHITE || row == 1 && color == ChessGame.TeamColor.BLACK)
		{
			moves.add(new ChessMove(start, end, ChessPiece.PieceType.QUEEN));
			moves.add(new ChessMove(start, end, ChessPiece.PieceType.BISHOP));
			moves.add(new ChessMove(start, end, ChessPiece.PieceType.KNIGHT));
			moves.add(new ChessMove(start, end, ChessPiece.PieceType.ROOK));
		}
		else
		{
			moves.add(new ChessMove(start, end, null));
		}
	}
}
