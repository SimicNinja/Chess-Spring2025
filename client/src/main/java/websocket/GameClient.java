package websocket;

import chess.ChessBoard;
import chess.ChessGame;
import chess.ChessPiece;
import chess.ChessPosition;
import com.google.gson.Gson;
import model.GameData;
import serverfacade.ResponseException;
import websocket.commands.UserGameCommand;
import websocket.messages.*;
import javax.websocket.*;
import java.io.IOException;
import java.net.URI;
import java.util.Arrays;

import static ui.EscapeSequences.*;
import static websocket.commands.UserGameCommand.CommandType.*;

public class GameClient extends Endpoint implements ServerMessageObserver
{
    private final GameData gameData;
    private final ChessGame game;
    private boolean whitePlayer;
    private Session session;

    public GameClient(GameData data) throws ResponseException
    {
        this.gameData = data;
        this.game = gameData.game();

        try
        {
            URI uri = new URI("ws://localhost:8080/ws");
            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            this.session = container.connectToServer(this, uri);
        }
        catch(Exception e)
        {
            throw new ResponseException(500, e.getMessage());
//            throw new ResponseException(500, "Error: Could not establish websocket connection.");
        }

        this.session.addMessageHandler(new MessageHandler.Whole<String>()
        {
            @Override
            public void onMessage(String message)
            {
                GameClient.this.notify(message);
            }
        });
    }

    @Override
    public void onOpen(Session session, EndpointConfig endpointConfig) {}

    @Override
    public void notify(String message)
    {
        ServerMessage serverMessage = new Gson().fromJson(message, ServerMessage.class);

        switch(serverMessage.getServerMessageType())
        {
            case NOTIFICATION -> displayNotification();
            case ERROR -> displayError((ServerErrorMessage) serverMessage);
            case LOAD_GAME ->
            {
                LoadGame msg = new Gson().fromJson(message, LoadGame.class);
                loadGame(msg);
            }
        }
    }

    public void sendCommand(UserGameCommand command) throws ResponseException
    {
        try
        {
            this.session.getBasicRemote().sendText(new Gson().toJson(command));
        }
        catch(Exception e)
        {
            throw new ResponseException(500, "Error: Unable to contact server.");
        }
    }

    public void displayNotification()
    {}

    public void displayError(ServerErrorMessage error)
    {
        System.out.println(("Error: " + error.getErrorMessage()));
    }

    public void loadGame(LoadGame load)
    {
        System.out.println("\n" + printBoard(whitePlayer));
    }

    public String eval(String input) throws ResponseException
    {
        var tokens = input.split(" ");
        var cmd = (tokens.length > 0) ? tokens[0] : "help";
        var params = Arrays.copyOfRange(tokens, 1, tokens.length);
        return switch(cmd)
        {
            case "redraw" -> redraw(params);
            case "highlight" -> highlight(params);
            case "move" -> makeMove(params);
            case "resign" -> resign(params);
            case "leave" -> "leave";
            case "help" -> help();
            default ->
            {
                System.out.print(SET_TEXT_COLOR_RED + "Error: Not a valid command.\n");
                yield help();
            }
        };
    }

    public String joinGame(String authToken, String username, ChessGame.TeamColor color) throws ResponseException
    {
        try
        {
            UserGameCommand join = new UserGameCommand(CONNECT, authToken, gameData.gameID());
            this.session.getBasicRemote().sendText(new Gson().toJson(join));
        }
        catch(IOException e)
        {
            throw new ResponseException(500, "Error: " + e.getMessage());
        }

        return "You have joined game: " + RESET_TEXT_COLOR + gameData.gameName() + SET_TEXT_COLOR_BLUE
                + " on the " + color + " team as " + RESET_TEXT_COLOR + username + SET_TEXT_COLOR_BLUE + ".\n";
    }

    public String observeGame(String authToken) throws ResponseException
    {
        try
        {
            UserGameCommand join = new UserGameCommand(CONNECT, authToken, gameData.gameID());
            this.session.getBasicRemote().sendText(new Gson().toJson(join));
        }
        catch(IOException e)
        {
            throw new ResponseException(500, "Error: " + e.getMessage());
        }

        whitePlayer = true;

        return "You are observing game: " + RESET_TEXT_COLOR + gameData.gameName() + SET_TEXT_COLOR_BLUE + ".\n";
    }

    public String redraw(String... params)
    {
        return "";
    }

    public String highlight(String... params)
    {
        return "";
    }

    public String makeMove(String... params)
    {
        return "";
    }

    public String resign(String... params)
    {
        return "";
    }

    public String help()
    {
        return SET_TEXT_COLOR_WHITE + """
			- redraw - Redraws chess board.
			- highlight <position> - Highlights legal moves of piece at specified position.
			- move <start position> <end position> - Moves piece from start to end position (validates move is valid).
			- resign
			- leave
			- help
			""";
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

    private void assertCommandLength(int length, String message, String... params) throws ResponseException
    {
        if(params.length != length)
        {
            throw new ResponseException(400, message);
        }
    }
}
