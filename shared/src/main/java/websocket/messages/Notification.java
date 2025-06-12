package websocket.messages;

public class Notification extends ServerMessage
{
    private String message;

    public Notification(ServerMessageType type, String msg)
    {
        super(type);
        message = msg;
    }

    public String getMessage()
    {
        return message;
    }
}
