package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame
{
    private TeamColor teamToMove;
    private ChessBoard board;
    private boolean gameOver = false;

    public ChessGame()
    {
        teamToMove = TeamColor.WHITE;
        board = new ChessBoard();
        board.resetBoard();
    }

    public ChessGame(ChessBoard board)
    {
        teamToMove = TeamColor.WHITE;
        this.board = board;
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard()
    {
        return this.board;
    }

    /**
     * Sets this game's chessboard with a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board)
    {
        this.board = board;
    }

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn()
    {
        return teamToMove;
    }

    /**
     * Set's which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team)
    {
        teamToMove = team;
    }

    public boolean isGameOver()
    {
        return gameOver;
    }

    public void setGameOver()
    {
        this.gameOver = true;
    }

    /**
     * Gets a valid moves for a piece at the given location
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */
    public Collection<ChessMove> validMoves(ChessPosition startPosition)
    {
        ChessPiece piece = board.getPiece(startPosition);
        Collection<ChessMove> validMoves = new ArrayList<>();

        if(piece == null)
        {
            return null;
        }

        // For legibility & debugging purposes. pieceMoves is a list of moves that a specific piece could make regarding
        // its own movement rules. validMoves contains the list of moves that account for chess rules (such as check).
        Collection<ChessMove> pieceMoves = piece.pieceMoves(board, startPosition);

        for(ChessMove move : pieceMoves)
        {
            if(legalMove(move, board))
            {
                validMoves.add(move);
            }
        }

        return validMoves;
    }

    /**
     * Checks whether the provided move places your king in check.
     *
     * @param move  Chess move that is being checked.
     * @param board The relevant chess board object.
     * @return Whether the specified move places the king (of player who made the move) in check.
     */
    public boolean legalMove(ChessMove move, ChessBoard board)
    {
        ChessBoard testBoard = new ChessBoard(board);
        ChessPiece piece = testBoard.getPiece(move.getStartPosition());
        testBoard.movePiece(move);

        return !inCheck(piece.getTeamColor(), testBoard);
    }

    /**
     * Makes a move in a chess game
     *
     * @param move chess move to perform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException
    {
        ChessPiece piece = this.board.getPiece(move.getStartPosition());
        Collection<ChessMove> validMoves = validMoves(move.getStartPosition());

        if(piece != null && validMoves.contains(move) && piece.getTeamColor() == getTeamTurn() && !gameOver)
        {
            this.board.movePiece(move);
            setTeamTurn(otherTeam(getTeamTurn()));
        }
        else
        {
            throw new InvalidMoveException("That move is illegal.");
        }
    }

    /**
     * Helper & readability function for inCheck()
     *
     * @param team The current team
     * @return The opposite team to the given parameter.
     */
    public TeamColor otherTeam(TeamColor team)
    {
        if(team == TeamColor.BLACK)
        {
            return TeamColor.WHITE;
        }
        else
        {
            return TeamColor.BLACK;
        }
    }

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor)
    {
        return isInCheck(teamColor) && noTeamMoves(teamColor);
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor)
    {
        return !isInCheck(teamColor) && noTeamMoves(teamColor);
    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor)
    {
        return inCheck(teamColor, this.board);
    }

    /**
     * Encapsulation of isInCheck() logic so the board state can be a parameter.
     *
     * @return Whether the given team on the specified board is in check.
     */
    private boolean inCheck(TeamColor teamColor, ChessBoard board)
    {
        for(ChessPieceAndPosition piece : board.getTeamPieces(otherTeam(teamColor)))
        {
            for(ChessMove move : piece.piece().pieceCaptures(board, piece.position))
            {
                if(board.getPiece(move.getEndPosition()).getPieceType() == ChessPiece.PieceType.KING)
                {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean noTeamMoves(TeamColor color)
    {
        for(ChessPieceAndPosition piece : board.getTeamPieces(color))
        {
            if(!validMoves(piece.position).isEmpty())
            {
                return false;
            }
        }
        return true;
    }

	public record ChessPieceAndPosition(ChessPiece piece, ChessPosition position) {}

    /**
     * Enum identifying the 2 possible teams in a chess game
     */
    public enum TeamColor
    {
        WHITE, BLACK
    }

    @Override
    public boolean equals(Object o)
    {
        if(o == null || getClass() != o.getClass())
        {
            return false;
        }
        ChessGame chessGame = (ChessGame) o;
        return teamToMove == chessGame.teamToMove && Objects.equals(board, chessGame.board);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(teamToMove, board);
    }
}
