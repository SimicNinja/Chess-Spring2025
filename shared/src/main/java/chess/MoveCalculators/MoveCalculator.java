package chess.MoveCalculators;

import chess.*;

import java.util.ArrayList;

public abstract class MoveCalculator
{
	protected ArrayList<ChessMove> moves = new ArrayList<>();
	private boolean captureFlag = false;

	public abstract ArrayList<ChessMove> pieceMoves(ChessBoard board, ChessPosition start);

	protected void checkDirection(int rowOffset, int colOffset, ChessBoard board, ChessPosition start)
	{
		captureFlag = false;

		ChessPiece piece = board.getPiece(start);
		ChessGame.TeamColor color = piece.getTeamColor();

		int row = start.getRow();
		int col = start.getColumn();

		row += rowOffset;
		col += colOffset;

		while(!captureFlag && validPieceMove(row, col, board, color))
		{
			ChessPosition end = new ChessPosition(row, col);

			moves.add(new ChessMove(start, end, null));

			row += rowOffset;
			col += colOffset;
		}
	}

	protected void checkMove(int rowOffset, int colOffset, ChessBoard board, ChessPosition start)
	{
		ChessPiece piece = board.getPiece(start);
		ChessGame.TeamColor color = piece.getTeamColor();

		int row = start.getRow() + rowOffset;
		int col = start.getColumn() + colOffset;

		if(validPieceMove(row, col, board, color))
		{
			ChessPosition end = new ChessPosition(row, col);

			moves.add(new ChessMove(start, end, null));
		}
	}

	protected boolean validPieceMove(int row, int col, ChessBoard board, ChessGame.TeamColor color)
	{
		if(checkBounds(row, col))
		{
			return positionOpen(row, col, board) || positionContainsEnemy(row, col, board, color);
		}
		return false;
	}

	protected boolean checkBounds(int row, int col)
	{
		return row >= 1 && row <= 8 && col >= 1 && col <= 8;
	}

	protected boolean positionOpen(int row, int col, ChessBoard board)
	{
		ChessPosition position = new ChessPosition(row, col);

		return board.getPiece(position) == null;
	}

	protected boolean positionContainsEnemy(int row, int col, ChessBoard board, ChessGame.TeamColor color)
	{
		ChessPosition position = new ChessPosition(row, col);
		ChessPiece piece = board.getPiece(position);
		ChessGame.TeamColor enemyColor = piece.getTeamColor();

		captureFlag = true;

		return color != enemyColor;
	}
}
