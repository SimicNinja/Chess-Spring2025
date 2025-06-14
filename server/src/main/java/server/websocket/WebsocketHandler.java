package server.websocket;

import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPiece;
import chess.InvalidMoveException;
import com.google.gson.Gson;
import dataaccess.DataAccessException;
import model.GameData;
import org.eclipse.jetty.websocket.api.RemoteEndpoint;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import service.DAOManagement;
import websocket.commands.*;
import websocket.messages.ServerErrorMessage;
import websocket.messages.LoadGame;
import websocket.messages.Notification;
import websocket.messages.ServerMessage;

import java.io.IOException;

import static chess.ChessGame.TeamColor.*;
import static websocket.messages.ServerMessage.ServerMessageType.*;

@WebSocket
public class WebsocketHandler
{

    private final ConnectionManager connections = new ConnectionManager();
    private final DAOManagement daoManager;

    public WebsocketHandler(DAOManagement daoManager)
    {
        this.daoManager = daoManager;
    }

    @OnWebSocketMessage
    public void onMessage(Session session, String message) throws IOException
    {
        try
        {
            UserGameCommand command = new Gson().fromJson(message, UserGameCommand.class);

            String username = getUsername(command.getAuthToken());

            switch(command.getCommandType())
            {
                case CONNECT -> connect(session, username, command);
                case MAKE_MOVE ->
                {
                    MakeMove moveCommand = new Gson().fromJson(message, MakeMove.class);
                    makeMove(session, username, moveCommand);
                }
                case LEAVE -> leaveGame(session, username, command);
                case RESIGN -> resign(session, username, command);
            }
        }
        catch(UnauthorizedException e)
        {
            sendMessage(session.getRemote(), new ServerErrorMessage(ERROR, "Error: Unauthorized"));
        }
        catch(Exception e)
        {
            sendMessage(session.getRemote(), new ServerErrorMessage(ERROR, "Error: " + e.getMessage()));
        }
    }

    private String getUsername(String authToken) throws UnauthorizedException, DataAccessException
    {
        return daoManager.getAuthorizations().authorizeToken(authToken);
    }

    private void connect(Session session, String username, UserGameCommand command)
            throws IOException, DataAccessException
    {
        connections.add(username, command.getGameID(), session);

        int gameID = command.getGameID();

        LoadGame load = new LoadGame(LOAD_GAME, daoManager.getGames().getGame(gameID));
        sendMessage(session.getRemote(), load);

        String msg = String.format("%s has joined the game.", username);
        connections.broadcast(username, gameID, new Notification(NOTIFICATION, msg));
    }

    private void makeMove(Session session, String username, MakeMove command) throws IOException
    {
        ChessMove move = command.getMove();
        int gameID = command.getGameID();
        GameData gameData;
        ChessGame game = null;

        try
        {
            gameData = daoManager.getGames().getGame(gameID);
            game = gameData.game();
            ChessPiece piece = game.getBoard().getPiece(move.getStartPosition());

            if(!username.equals(getTeamUsername(piece.getTeamColor(), gameData)))
            {
                throw new InvalidMoveException("You may only move your own pieces while it is your color's turn.");
            }

            game.makeMove(move);
            daoManager.getGames().setGame(gameID, game);

            LoadGame load = new LoadGame(LOAD_GAME, gameData);
            connections.broadcast(null, gameID, load);

            String msg = String.format("%s moved a %s from %s to %s.", username, piece.getPieceType(),
                    move.getStartPosition(), move.getEndPosition());
            connections.broadcast(username, gameID, new Notification(NOTIFICATION, msg));

            ChessGame.TeamColor endangeredTeam = game.otherTeam(piece.getTeamColor());

            if(game.isInCheckmate(endangeredTeam))
            {
                game.setGameOver();
                daoManager.getGames().setGame(gameID, game);
                String message = String.format("%s is in checkmate. %s wins!", getTeamUsername(endangeredTeam, gameData), username);
                connections.broadcast(null, gameID, new Notification(NOTIFICATION, message));
            }
            else if(game.isInStalemate(endangeredTeam))
            {
                game.setGameOver();
                daoManager.getGames().setGame(gameID, game);
                String message = "The game is at a stalemate.";
                connections.broadcast(null, gameID, new Notification(NOTIFICATION, message));
            }
        }
        catch(InvalidMoveException | DataAccessException e)
        {
            sendMessage(session.getRemote(), new ServerErrorMessage(ERROR, "Error: " + e.getMessage()));
        }
    }

    private void leaveGame(Session session, String username, UserGameCommand command) throws IOException
	{
        int gameID = command.getGameID();
        GameData gameData;
        ChessGame game;

        try
        {
            gameData = daoManager.getGames().getGame(gameID);
            game = gameData.game();

            if(username.equals(getTeamUsername(WHITE, gameData)))
            {
                daoManager.getGames().leaveGame(gameID, WHITE);
            }
            else if(username.equals(getTeamUsername(BLACK, gameData)))
            {
                daoManager.getGames().leaveGame(gameID, BLACK);
            }

            connections.broadcast(username, gameID, new Notification(NOTIFICATION, String.format("%s has left the game.", username)));
            connections.remove(username);
        }
        catch(DataAccessException e)
        {
            sendMessage(session.getRemote(), new ServerErrorMessage(ERROR, "Error " + e.getMessage()));
        }
    }

    private void resign(Session session, String username, UserGameCommand command) throws IOException
	{
        int gameID = command.getGameID();
        GameData gameData;
        ChessGame game;

        try
        {
            gameData = daoManager.getGames().getGame(gameID);
            game = gameData.game();

            //Checks whether the gameOver flag is set, or if the player making the request is an observer
            if(game.isGameOver() || (!username.equals(getTeamUsername(WHITE, gameData))
                    && !username.equals(getTeamUsername(BLACK, gameData))))
            {
                throw new InvalidMoveException("You are only allowed to resign if you are a player and the game isn't over.");
            }

            game.setGameOver();
            daoManager.getGames().setGame(gameID, game);

            connections.broadcast(null, gameID, new Notification(NOTIFICATION, String.format("%s has resigned.", username)));
        }
        catch(DataAccessException | InvalidMoveException e)
        {
            sendMessage(session.getRemote(), new ServerErrorMessage(ERROR, "Error: " + e.getMessage()));
        }
    }

    private String getTeamUsername(ChessGame.TeamColor color, GameData gameData)
    {
        if(color == WHITE)
        {
            return gameData.whiteUsername();
        }
        return gameData.blackUsername();
    }

    private void sendMessage(RemoteEndpoint remote, ServerMessage message) throws IOException
    {
        remote.sendString(new Gson().toJson(message));
    }
}