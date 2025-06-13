package server.websocket;

import com.google.gson.Gson;
import org.eclipse.jetty.websocket.api.Session;
import websocket.messages.ServerMessage;

import java.io.IOException;

public class WebsocketConnection
{
    public String username;
    public int gameID;
    public Session session;

    public WebsocketConnection(String username, int gameID, Session session)
    {
        this.username = username;
        this.gameID = gameID;
        this.session = session;
    }

    public void send(ServerMessage msg) throws IOException
    {
        session.getRemote().sendString(new Gson().toJson(msg));
    }
}
