package client;

import chess.ChessBoard;
import chess.ChessGame;
import chess.ChessPiece;
import chess.ChessPosition;
import model.GameData;

import static ui.EscapeSequences.*;

public class GameClient
{
    private final String blackPlayer;
    private final GameData gameData;
    private final String whitePlayer;
    private final ChessGame game;

    public GameClient(GameData data)
    {
        this.gameData = data;
        this.whitePlayer = gameData.whiteUsername();
        this.blackPlayer = gameData.blackUsername();
        this.game = gameData.game();
    }

    public String printBoard(String username)
    {
        if(username.equals(blackPlayer))
        {
            return printBoard(false);
        }
        else
        {
            return printBoard(true);
        }
    }

    private String printBoard(boolean whitePerspective)
    {
        StringBuilder output = new StringBuilder();
        ChessBoard board = game.getBoard();
        boolean isWhite = true;

        // Set up column labels
        String[] cols = whitePerspective
                ? new String[]{"a", "b", "c", "d", "e", "f", "g", "h"}
                : new String[]{"h", "g", "f", "e", "d", "c", "b", "a"};

        // Print top labels
        output.append(SET_BG_COLOR_BLACK + SET_TEXT_COLOR_BLUE + EMPTY);
        for(String col : cols)
        {
            output.append(" " + col + " ");
        }
        output.append(EMPTY + RESET_BG_COLOR + "\n");

        // Row iteration direction
        int rowStart = whitePerspective ? 8 : 1;
        int rowEnd = whitePerspective ? 0 : 9;
        int rowStep = whitePerspective ? -1 : 1;

        for(int row = rowStart; row != rowEnd; row += rowStep)
        {
            output.append(SET_BG_COLOR_BLACK + SET_TEXT_COLOR_BLUE + " " + row + " ");
            for(int i = 0; i < 8; i++)
            {
                int col = whitePerspective ? i + 1 : 8 - i;
                output.append(printSquare(isWhite));
                isWhite = !isWhite;
                output.append(printPiece(board.getPiece(new ChessPosition(row, col))));
            }
            isWhite = !isWhite;
            output.append(SET_BG_COLOR_BLACK + SET_TEXT_COLOR_BLUE + " " + row + " ");
            output.append(RESET_BG_COLOR + "\n" + SET_BG_COLOR_DARK_GREY);
        }

        // Print bottom labels
        output.append(SET_BG_COLOR_BLACK + SET_TEXT_COLOR_BLUE + EMPTY);
        for(String col : cols)
        {
            output.append(" " + col + " ");
        }
        output.append(EMPTY + RESET_BG_COLOR + "\n");

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
            case ROOK -> output.append(BLACK_ROOK);
            case PAWN -> output.append(BLACK_PAWN);
        }

        return output.toString();
    }
}
