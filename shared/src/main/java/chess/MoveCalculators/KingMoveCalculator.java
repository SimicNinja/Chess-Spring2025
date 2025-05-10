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

		return moves;
	}
}
