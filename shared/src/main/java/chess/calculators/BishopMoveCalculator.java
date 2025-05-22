package chess.calculators;

import chess.*;

import java.util.ArrayList;

public class BishopMoveCalculator extends MoveCalculator
{
	public ArrayList<ChessMove> pieceMoves(ChessBoard board, ChessPosition start)
	{
		checkDirection(1, 1, board, start);
		checkDirection(1, -1,board, start);
		checkDirection(-1, 1, board, start);
		checkDirection(-1, -1, board, start);

		return moves;
	}
}
