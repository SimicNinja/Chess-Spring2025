package chess.MoveCalculators;

import chess.*;

import java.util.ArrayList;

public class KingMoveCalculator extends MoveCalculator
{
	public ArrayList<ChessMove> pieceMoves(ChessBoard board, ChessPosition start)
	{
		checkMove(0, 1, board, start);
		checkMove(1, 0, board, start);
		checkMove(0, -1, board, start);
		checkMove(-1, 0, board, start);

		checkMove(1, 1, board, start);
		checkMove(1, -1, board, start);
		checkMove(-1, 1, board, start);
		checkMove(-1, -1, board, start);

		ChessPiece king = board.getPiece(start);

		if(!king.getHasMoved() && board.inStartingPosition(king, start))
		{
			//Variables for readability; 1 & 8 for what the starting column should be.
			ChessPiece rook1 = board.getPiece(start.offset(0, -4));
			ChessPiece rook8 = board.getPiece(start.offset(0, 3));

			if(eligibleRook(rook1, king.getTeamColor()) && isPathClear(board, start, -1) && isPathSafe(board, start, -1))
			{
				moves.add(new ChessMove(start, start.offset(0, -2), null));
			}
			if(eligibleRook(rook8, king.getTeamColor()) && isPathClear(board, start, 1) && isPathSafe(board, start, 1))
			{
				moves.add(new ChessMove(start, start.offset(0, 2), null));
			}
		}

		return moves;
	}

	private boolean eligibleRook(ChessPiece rook, ChessGame.TeamColor color)
	{
		return rook != null && rook.getPieceType() == ChessPiece.PieceType.ROOK && !rook.getHasMoved() && rook.getTeamColor() == color;
	}

	private boolean isPathClear(ChessBoard board, ChessPosition kingStart, int colDirection)
	{
		ChessPosition temp = kingStart.offset(0, colDirection);

		while(checkBounds(temp.getRow(), temp.getColumn()))
		{
			if(board.occupied(temp) && board.getPiece(temp).getPieceType() != ChessPiece.PieceType.ROOK)
			{
				return false;
			}

			try
			{
				temp = temp.offset(0, colDirection);
			}
			catch(Exception ignored)
			{
				break;
			}
		}

		return true;
	}

	private boolean isPathSafe(ChessBoard board, ChessPosition kingStart, int colDirection)
	{
		ChessPiece king = board.getPiece(kingStart);
		ChessGame.TeamColor color = king.getTeamColor();
		ChessGame game = new ChessGame(board);
		ChessMove move1 = new ChessMove(kingStart, kingStart.offset(0, colDirection), null);
		ChessMove move2 = new ChessMove(kingStart, kingStart.offset(0, colDirection * 2), null);

		return !game.isInCheck(color) && game.legalMove(move1, board) && game.legalMove(move2, board);
	}
}
