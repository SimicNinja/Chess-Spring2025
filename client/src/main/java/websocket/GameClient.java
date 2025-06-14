package websocket;

import chess.*;
import com.google.gson.Gson;
import model.GameData;
import serverfacade.ResponseException;
import websocket.commands.MakeMove;
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
    public final GameData gameData;
    private ChessGame game;
    private final ChessGame.TeamColor color;
    private final String authToken;
    private final Session session;

    public GameClient(GameData data, ChessGame.TeamColor color, String authToken) throws ResponseException
    {
        this.gameData = data;
        this.color = color;
        this.game = gameData.game();
        this.authToken = authToken;

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
            case NOTIFICATION ->
            {
                Notification msg = new Gson().fromJson(message, Notification.class);
                displayNotification(msg);
            }
            case ERROR ->
            {
                ServerErrorMessage msg = new Gson().fromJson(message, ServerErrorMessage.class);
                displayError(msg);
            }
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

    public void displayNotification(Notification notification)
    {
        System.out.println(SET_TEXT_COLOR_YELLOW + notification.getMessage() + RESET_TEXT_COLOR);
        System.out.print(RESET_TEXT_COLOR + "[" + gameData.gameName() + "] >>> " + SET_TEXT_COLOR_GREEN);
    }

    public void displayError(ServerErrorMessage error)
    {
        System.out.println(SET_TEXT_COLOR_RED + "Error: " + error.getErrorMessage() + RESET_TEXT_COLOR);
        System.out.print(RESET_TEXT_COLOR + "[" + gameData.gameName() + "] >>> " + SET_TEXT_COLOR_GREEN);
    }

    public void loadGame(LoadGame load)
    {
        game = load.getGame().game();
        System.out.println("\n" + printBoard(whitePerspective(color)));
        System.out.print(RESET_TEXT_COLOR + "[" + gameData.gameName() + "] >>> " + SET_TEXT_COLOR_GREEN);
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
            case "leave" -> leave(params);
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

        return "You are observing game: " + RESET_TEXT_COLOR + gameData.gameName() + SET_TEXT_COLOR_BLUE + ".\n";
    }

    public String redraw(String... params) throws ResponseException
	{
        assertCommandLength(0, "Redraw command has no additional inputs.", params);

        return printBoard(whitePerspective(color));
    }

    public String highlight(String... params)
    {
        return "";
    }

    public String makeMove(String... params) throws ResponseException
	{
        assertCommandLength(2, "Expected: <start position> <end position>\n" + SET_TEXT_COLOR_YELLOW +
                "Please format your positions with the column letter first and then the row number. Ex: a7", params);

        ChessPosition start = parseUserPosition(params[0]);
        ChessPosition end = parseUserPosition(params[1]);

        ChessMove move = new ChessMove(start, end, null);

        sendCommand(new MakeMove(MAKE_MOVE, authToken, gameData.gameID(),move));

        return String.format("You moved a %s from %s to %s.", game.getBoard().getPiece(start).getPieceType(),
                move.getStartPosition(), move.getEndPosition());
    }

    public String resign(String... params) throws ResponseException
	{
        assertCommandLength(0, "Resign command has no additional inputs.", params);
        sendCommand(new UserGameCommand(RESIGN, authToken, gameData.gameID()));

        return "You have resigned from this game. You must leave the game using the command before you quit.";
    }

    public String leave(String... params) throws ResponseException
    {
        assertCommandLength(0, "Leave command has no additional inputs.", params);
        sendCommand(new UserGameCommand(LEAVE, authToken, gameData.gameID()));

        return "leave";
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

    private ChessPosition parseUserPosition(String input)
    {
        char col = input.charAt(0);
        char row = input.charAt(1);

        return new ChessPosition((int) row - '0', (int) parseColumnLetter(col));
    }

    private int parseColumnLetter(char letter)
    {
        return switch(letter) {
            case 'a', 'A' -> 1;
            case 'b', 'B' -> 2;
            case 'c', 'C' -> 3;
            case 'd', 'D' -> 4;
            case 'e', 'E' -> 5;
            case 'f', 'F' -> 6;
            case 'g', 'G' -> 7;
            case 'h', 'H' -> 8;
            default -> throw new IllegalStateException("Unexpected value: " + letter);
        };
    }

    private boolean whitePerspective(ChessGame.TeamColor color)
    {
        return color == ChessGame.TeamColor.WHITE;
    }

    private String printBoard(boolean whitePerspective)
    {
        StringBuilder output = new StringBuilder();
        ChessBoard board = game.getBoard();
        boolean isWhite = true;

        // Set up column labels
        String[] cols = whitePerspective
                ? new String[]{"A", "B", "C", "D", "E", "F", "G", "H"}
                : new String[]{"H", "G", "F", "E", "D", "C", "B", "A"};

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
