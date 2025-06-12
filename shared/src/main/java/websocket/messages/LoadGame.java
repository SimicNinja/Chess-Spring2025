package websocket.messages;

import model.GameData;

public class LoadGame extends ServerMessage
{
    private GameData game;

    public LoadGame(ServerMessageType type, GameData game)
    {
        super(type);
        this.game = game;
    }

    public GameData getGame()
    {
        return game;
    }
}
