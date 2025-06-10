package client;

import chess.ChessBoard;
import chess.ChessGame;
import chess.ChessPiece;
import chess.ChessPosition;
import model.GameData;
import static ui.EscapeSequences.*;

public class GameClient
{
    private GameData gameData;
    private String whitePlayer;
    private String blackPlayer;
    private ChessGame game;

    public GameClient(GameData data)
    {
        this.gameData = data;
        this.whitePlayer = gameData.whiteUsername();
        this.blackPlayer = gameData.blackUsername();
        this.game = gameData.game();
    }

    public String printBoard()
    {
        StringBuilder output = new StringBuilder();
        ChessBoard board = game.getBoard();
        boolean isWhite = true;

        output.append(SET_BG_COLOR_BLACK + SET_TEXT_COLOR_BLUE + EMPTY + " a  b  c  d  e  f  g  h " + EMPTY + RESET_BG_COLOR + "\n");

        for(int row = 8; row > 0; row--)
        {
            output.append(SET_BG_COLOR_BLACK + SET_TEXT_COLOR_BLUE + " " + Integer.toString(row) + " ");
            for(int col = 8; col > 0; col--)
            {
                output.append(printSquare(isWhite));
                isWhite = !isWhite;
                output.append(printPiece(board.getPiece(new ChessPosition(row, col))));
            }
            isWhite = !isWhite;
            output.append(SET_BG_COLOR_BLACK + SET_TEXT_COLOR_BLUE + " " + Integer.toString(row) + " ");
            output.append(RESET_BG_COLOR + "\n" + SET_BG_COLOR_DARK_GREY);
        }

        output.append(SET_BG_COLOR_BLACK + SET_TEXT_COLOR_BLUE + EMPTY + " a  b  c  d  e  f  g  h " + EMPTY + RESET_BG_COLOR + "\n");

        return output.toString();
    }

    private String printSquare(boolean isWhite)
    {
        if(isWhite)
        {
            return SET_BG_COLOR_LIGHT_GREY;
        }
        else
        {
            return SET_BG_COLOR_DARK_GREY;
        }
    }

    private String printPiece(ChessPiece piece)
    {
        StringBuilder output = new StringBuilder();

        if(piece == null)
        {
            return EMPTY;
        }

        if(piece.getTeamColor() == ChessGame.TeamColor.WHITE)
        {
            output.append(SET_TEXT_COLOR_WHITE);
        }
        else
        {
            output.append(SET_TEXT_COLOR_BLACK);
        }

        switch(piece.getPieceType())
        {
            case KING -> output.append(BLACK_KING);
            case QUEEN -> output.append(BLACK_QUEEN);
            case BISHOP -> output.append(BLACK_BISHOP);
            case KNIGHT -> output.append(BLACK_KNIGHT);
            case ROOK ->  output.append(BLACK_ROOK);
            case PAWN -> output.append(BLACK_PAWN);
        };

        return output.toString();
    }
}
