package client;

import chess.ChessGame;
import model.AuthData;
import model.Records;
import model.UserData;
import org.junit.jupiter.api.*;
import server.Server;
import serverfacade.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;


public class ServerFacadeTests {

    private static Server server;
    private static ServerFacade facade;
    private static UserData newUser;
    private static UserData existingUser;
    private String existingAuth;

    @BeforeAll
    public static void init()
    {
        server = new Server();
        var port = server.run(0);
        System.out.println("Started test HTTP server on " + port);
        facade = new ServerFacade("http://localhost:" + port);

        existingUser = new UserData("ExistingUser", "existingUserPassword", "eu@mail.com");
        newUser = new UserData("NewUser", "newUserPassword", "nu@mail.com");
    }

    @BeforeEach
    public void setup() throws ResponseException
    {
        facade.clear();
        AuthData registerResult =
                facade.register(existingUser.username(), existingUser.password(), existingUser.email());
        existingAuth = registerResult.authToken();
    }

    @AfterAll
    static void stopServer()
    {
        server.stop();
    }

    @Test
    public void successfulRegister() throws ResponseException
    {
        //submit register request
        AuthData registerResult = facade.register(newUser.username(), newUser.password(), newUser.email());

        Assertions.assertEquals(newUser.username(), registerResult.username(),
                "Response did not have the same username as was registered");
        Assertions.assertNotNull(registerResult.authToken(),
                "Response did not contain an authentication string");
    }

    @Test
    public void ninjaRegister() throws ResponseException
    {
        AuthData actual = facade.register("SimicNinja", "1234", "ninja@gmail.com");

        Assertions.assertEquals("SimicNinja", actual.username(),
                "Response did not have the same username as was registered");
        Assertions.assertNotNull(actual.authToken(),
                "Response did not contain an authentication string");
    }

    @Test
    public void failedRegister()
    {
        ResponseException e = Assertions.assertThrows(ResponseException.class, () ->
                facade.register(existingUser.username(), existingUser.password(), existingUser.email()));
        Assertions.assertEquals("Error: Already Taken", e.getMessage());
    }

    @Test
    public void successfulLogin() throws ResponseException
    {
        AuthData loginResult = facade.login(existingUser.username(), existingUser.password());
        Assertions.assertEquals(existingUser.username(), loginResult.username(),
                "Response didn't match inputted username.");
        Assertions.assertNotNull(loginResult.authToken(),
                "Response did not contain an authentication string");
    }

    @Test
    public void failedLogin()
    {
        ResponseException e = Assertions.assertThrows(ResponseException.class, () ->
                facade.login(existingUser.username(), "1234"));
        Assertions.assertEquals("Error: Unauthorized", e.getMessage());
    }

    @Test
    public void successfulLogout() throws ResponseException
    {
        facade.logout(existingAuth);
    }

    @Test
    public void failedLogout() throws ResponseException
    {
        facade.logout(existingAuth);

        ResponseException e = Assertions.assertThrows(ResponseException.class, () ->
                facade.logout(existingAuth));

        Assertions.assertEquals("Error: Unauthorized", e.getMessage());
    }

    @Test
    public void successfulNewGame() throws ResponseException
    {
        Records.NewGameResult result = facade.newGame(existingAuth, "TestGame");
        Assertions.assertNotNull(result.gameID(), "Result did not return a game ID");
        Assertions.assertTrue(result.gameID() > 0, "Result returned invalid game ID");
    }

    @Test
    public void failedNewGame() throws ResponseException
    {
        facade.logout(existingAuth);

        Assertions.assertThrows(ResponseException.class, () ->
                facade.newGame(existingAuth, "TestGame"));
    }

    @Test
    public void successfulJoinGame() throws ResponseException
    {
        String gameName = "1stGame";
        Records.NewGameResult gameResult = facade.newGame(existingAuth, gameName);
        int gameID = gameResult.gameID();

        facade.joinGame(existingAuth, ChessGame.TeamColor.BLACK, gameID);

        List<Records.ListedGame> expectedList = new ArrayList<>();
        expectedList.add(new Records.ListedGame(gameID, null, existingUser.username(), gameName));

        Assertions.assertEquals(expectedList, facade.listGames(existingAuth));
    }

    @Test
    public void failedJoinGame() throws ResponseException
    {
        String gameName = "2stGame";
        Records.NewGameResult gameResult = facade.newGame(existingAuth, gameName);
        int gameID = gameResult.gameID();

        AuthData newAuth = facade.register(newUser.username(), newUser.password(), newUser.email());
        facade.joinGame(existingAuth, ChessGame.TeamColor.BLACK, gameID);

        ResponseException e = Assertions.assertThrows(ResponseException.class, () ->
                facade.joinGame(newAuth.authToken(), ChessGame.TeamColor.BLACK, gameID));
        Assertions.assertEquals("Error: Already Taken", e.getMessage());
    }

    @Test
    public void listGames() throws ResponseException
    {
        //register a few users to create games
        UserData userA = new UserData("a", "A", "a.A");
        UserData userB = new UserData("b", "B", "b.B");
        UserData userC = new UserData("c", "C", "c.C");

        AuthData authA = facade.register(userA.username(), userA.password(), userA.email());
        AuthData authB = facade.register(userB.username(), userB.password(), userB.email());
        AuthData authC = facade.register(userC.username(), userC.password(), userC.email());

        //create games
        List<Records.ListedGame> expectedList = new ArrayList<>();

        //1 as black from A
        String game1Name = "I'm numbah one!";
        Records.NewGameResult game1 = facade.newGame(authA.authToken(), game1Name);
        facade.joinGame(authA.authToken(), ChessGame.TeamColor.BLACK, game1.gameID());
        expectedList.add(new Records.ListedGame(game1.gameID(), null, authA.username(), game1Name));


        //1 as white from B
        String game2Name = "Lonely";
        Records.NewGameResult game2 = facade.newGame(authB.authToken(), game2Name);
        facade.joinGame(authB.authToken(), ChessGame.TeamColor.WHITE, game2.gameID());
        expectedList.add(new Records.ListedGame(game2.gameID(), authB.username(), null, game2Name));


        //1 of each from C
        String game3Name = "GG";
        Records.NewGameResult game3 = facade.newGame(authC.authToken(), game3Name);
        facade.joinGame(authC.authToken(), ChessGame.TeamColor.WHITE, game3.gameID());
        facade.joinGame(authA.authToken(), ChessGame.TeamColor.BLACK, game3.gameID());
        expectedList.add(new Records.ListedGame(game3.gameID(), authC.username(), authA.username(), game3Name));


        //C play self
        String game4Name = "All by myself";
        Records.NewGameResult game4 = facade.newGame(authC.authToken(), game4Name);
        facade.joinGame(authC.authToken(), ChessGame.TeamColor.WHITE, game4.gameID());
        facade.joinGame(authC.authToken(), ChessGame.TeamColor.BLACK, game4.gameID());
        expectedList.add(new Records.ListedGame(game4.gameID(), authC.username(), authC.username(), game4Name));


        //list games
        List<Records.ListedGame> actualList = facade.listGames(existingAuth);
        Assertions.assertNotNull(actualList, "List result did not contain a list of games");
        Comparator<Records.ListedGame> gameIdComparator = Comparator.comparingInt(Records.ListedGame::gameID);
        expectedList.sort(gameIdComparator);
        actualList.sort(gameIdComparator);

        //check
        Assertions.assertEquals(expectedList, actualList, "Returned Games list was incorrect");
    }
}
