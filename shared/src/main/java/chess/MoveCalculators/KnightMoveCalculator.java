package chess.MoveCalculators;

import chess.ChessBoard;
import chess.ChessMove;
import chess.ChessPosition;

import java.util.ArrayList;

public class KnightMoveCalculator extends MoveCalculator
{
	public ArrayList<ChessMove> pieceMoves(ChessBoard board, ChessPosition start)
	{
		checkMove(1, 2, board, start);
		checkMove(1, -2, board, start);
		checkMove(-1, 2, board, start);
		checkMove(-1, -2, board, start);

		checkMove(2, 1, board, start);
		checkMove(2, -1, board, start);
		checkMove(-2, 1, board, start);
		checkMove(-2, -1, board, start);

		return moves;
	}
}
