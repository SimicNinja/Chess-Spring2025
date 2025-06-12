package websocket.commands;

import model.GameData;

public class Connect extends UserGameCommand
{
    private final GameData game;

    public Connect(CommandType commandType, String authToken, Integer gameID, GameData game)
    {
        super(commandType, authToken, gameID);

        this.game = game;
    }

    public GameData getGame()
    {
        return game;
    }
}
