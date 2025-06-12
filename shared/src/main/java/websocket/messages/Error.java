package websocket.messages;

public class Error extends ServerMessage
{
    private String message;

    public Error(ServerMessageType type, String msg)
    {
        super(type);
        this.message = msg;
    }

    public String getMessage()
    {
        return message;
    }
}
