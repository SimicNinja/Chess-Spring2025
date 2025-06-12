package websocket.messages;

public class ServerErrorMessage extends ServerMessage
{
    private String errorMessage;

    public ServerErrorMessage(ServerMessageType type, String msg)
    {
        super(type);
        this.errorMessage = msg;
    }

    public String getErrorMessage()
    {
        return errorMessage;
    }
}
