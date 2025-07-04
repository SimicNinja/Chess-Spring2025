package websocket.messages;

import java.util.Objects;

/**
 * Represents a Message the server can send through a WebSocket
 * <p>
 * Note: You can add to this class, but you should not alter the existing
 * methods.
 */
public class ServerMessage
{
    ServerMessageType serverMessageType;

    public ServerMessage(ServerMessageType type)
    {
        this.serverMessageType = type;
    }

    public ServerMessageType getServerMessageType()
    {
        return this.serverMessageType;
    }

    @Override
    public boolean equals(Object o)
    {
        if(this == o)
        {
            return true;
        }
        if(!(o instanceof ServerMessage that))
        {
            return false;
        }
        return getServerMessageType() == that.getServerMessageType();
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(getServerMessageType());
    }

    public enum ServerMessageType
    {
        LOAD_GAME, ERROR, NOTIFICATION
    }
}
