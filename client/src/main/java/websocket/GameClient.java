package websocket;

import chess.ChessBoard;
import chess.ChessGame;
import chess.ChessPiece;
import chess.ChessPosition;
import com.google.gson.Gson;
import model.GameData;
import serverfacade.ResponseException;
import websocket.messages.ServerMessage;
import javax.websocket.*;
import java.net.URI;

import static ui.EscapeSequences.*;

public class GameClient extends Endpoint implements ServerMessageObserver
{
    private final GameData gameData;
    private final ChessGame game;
    private Session session;

    public GameClient(GameData data) throws ResponseException
    {
        this.gameData = data;
        this.game = gameData.game();

        try
        {
            URI uri = new URI("ws://localhost:8080");
            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            this.session = container.connectToServer(this, uri);
        }
        catch(Exception e)
        {
            throw new ResponseException(500, "Error: Could not establish websocket connection.");
        }

        this.session.addMessageHandler(new MessageHandler.Whole<String>()
        {
            @Override
            public void onMessage(String message)
            {
                ServerMessage serverMsg = new Gson().fromJson(message, ServerMessage.class);
                GameClient.this.notify(serverMsg);
            }
        });
    }

    @Override
    public void onOpen(Session session, EndpointConfig endpointConfig)
    {

    }

    public void send(String msg) throws ResponseException
    {
        try
        {
            this.session.getBasicRemote().sendText(msg);
        }
        catch(Exception e)
        {
            throw new ResponseException(500, "Error: Unable to contact server.");
        }
    }

    @Override
    public void notify(ServerMessage message)
    {
        switch(message.getServerMessageType())
        {
            case NOTIFICATION -> displayNotification();
            case ERROR -> displayError();
            case LOAD_GAME -> loadGame();
        }
    }

    public void displayNotification()
    {}

    public void displayError()
    {}

    public void loadGame()
    {}

    public String printBoard(boolean whitePerspective)
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
