package server.websocket;

import com.google.gson.Gson;
import dataaccess.DataAccessException;
import org.eclipse.jetty.websocket.api.RemoteEndpoint;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import service.DAOManagement;
import websocket.commands.*;
import websocket.messages.Error;
import websocket.messages.LoadGame;
import websocket.messages.Notification;

import java.io.IOException;

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
                case MAKE_MOVE -> makeMove(session, username, (MakeMove) command);
                case LEAVE -> leaveGame(session, username, command);
                case RESIGN -> resign(session, username, command);
            }
        }
        catch(UnauthorizedException e)
        {
            sendMessage(session.getRemote(), new Error(ERROR, "Error: Unauthorized"));
        }
        catch(Exception e)
        {
            sendMessage(session.getRemote(), new Error(ERROR, "Error: " + e.getMessage()));
        }
    }

    private String getUsername(String authToken) throws UnauthorizedException, DataAccessException
    {
        return daoManager.getAuthorizations().authorizeToken(authToken);
    }

    private void connect(Session session, String username, UserGameCommand command)
            throws IOException, DataAccessException
    {
        connections.add(username, session);

        String msg = String.format("%s has joined the game.", username);
        Notification notification = new Notification(NOTIFICATION, msg);
        connections.broadcast(username, notification);

        int gameID = command.getGameID();

        LoadGame load = new LoadGame(LOAD_GAME, daoManager.getGames().getGame(gameID));
        connections.broadcast(username, load);
    }

    private void makeMove(Session session, String username, MakeMove command)
    {

    }

    private void leaveGame(Session session, String username, UserGameCommand command)
    {

    }

    private void resign(Session session, String username, UserGameCommand command)
    {

    }

    private void sendMessage(RemoteEndpoint remote, Error error) throws IOException
    {
        remote.sendString(new Gson().toJson(error));
    }
}