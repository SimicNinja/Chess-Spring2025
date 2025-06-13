package server.websocket;

import org.eclipse.jetty.websocket.api.Session;
import websocket.messages.ServerMessage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class ConnectionManager
{
	public final ConcurrentHashMap<String, WebsocketConnection> connections = new ConcurrentHashMap<>();

	public void add(String username, int gameID, Session session)
	{
		WebsocketConnection connection = new WebsocketConnection(username, gameID, session);
		connections.put(username, connection);
	}

	public void remove(String username)
	{
		connections.remove(username);
	}

	public void broadcast(String excludeUsername, int gameID, ServerMessage message) throws IOException
	{
		var removeList = new ArrayList<WebsocketConnection>();
		for(var conn : connections.values())
		{
			if(conn.session.isOpen())
			{
				if(!conn.username.equals(excludeUsername) && conn.gameID == gameID)
				{
					conn.send(message);
				}
			}
			else
			{
				removeList.add(conn);
			}
		}

		// Clean up any connections that were left open.
		for(var conn : removeList)
		{
			connections.remove(conn.username);
		}
	}
}