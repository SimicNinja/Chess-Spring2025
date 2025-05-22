package server;

import com.google.gson.Gson;
import dataaccess.DataAccessException;
import model.Records;
import model.UserData;
import spark.*;
import service.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Server
{
    private final DAOManagement daoManager = new DAOManagement();
    private final UserManagement userManager = new UserManagement(daoManager);
    private final GameManagement gameManager = new GameManagement(daoManager);

    public int run(int desiredPort)
    {
        Spark.port(desiredPort);

        Spark.staticFiles.location("web");

        // Register your endpoints and handle exceptions here.
        Spark.delete("/db", this::clear);
        Spark.post("/user", this::addUser);
        Spark.post("/session", this::login);
        Spark.delete("/session", this::logout);
        Spark.post("/game", this::newGame);
        Spark.put("/game", this::joinGame);
        Spark.get("/game", this::listGames);

        //This line initializes the server and can be removed once you have a functioning endpoint 
        Spark.init();

        Spark.awaitInitialization();
        return Spark.port();
    }

    public void stop()
    {
        Spark.stop();
        Spark.awaitStop();
    }

    private Object clear(Request request, Response response)
    {
        daoManager.clearApplication();
        return http200(response);
    }

    private Object addUser(Request request, Response response)
    {
        UserData registerRequest = new Gson().fromJson(request.body(), UserData.class);

        try
        {
            Records.LoginResult result = userManager.register(registerRequest);
            response.status(200);
            return new Gson().toJson(result);
        }
        catch(DataAccessException e)
        {
            return http400s(e, response);
        }
    }

    private Object login(Request request, Response response)
    {
        Records.LoginRequest loginRequest = new Gson().fromJson(request.body(), Records.LoginRequest.class);

        try
        {
            Records.LoginResult result = userManager.login(loginRequest);
            response.status(200);
            return new Gson().toJson(result);
        }
        catch(DataAccessException e)
        {
            return http400s(e, response);
        }
    }

    private Object logout(Request request, Response response)
    {
        String authToken = request.headers("authorization");

        try
        {
            userManager.logout(authToken);
            return http200(response);
        }
        catch(DataAccessException e)
        {
            return http400s(e, response);
        }
    }

    private Object newGame(Request request, Response response)
    {
        String authToken = request.headers("authorization");
        Records.NewGameRequest deserialize = new Gson().fromJson(request.body(), Records.NewGameRequest.class);
        Records.NewGameRequest newGameRequest = new Records.NewGameRequest(authToken, deserialize.gameName());

        try
        {
            Records.NewGameResult result = gameManager.makeGame(newGameRequest);
            response.status(200);
            return new Gson().toJson(result);
        }
        catch(DataAccessException e)
        {
            return http400s(e, response);
        }
    }

    private Object joinGame(Request request, Response response)
    {
        String authToken = request.headers("authorization");
        Records.JoinGameRequest deserialize = new Gson().fromJson(request.body(), Records.JoinGameRequest.class);
        Records.JoinGameRequest joinRequest = new Records.JoinGameRequest(authToken, deserialize.playerColor(), deserialize.gameID());

        try
        {
            gameManager.joinGame(joinRequest);
            return http200(response);
        }
        catch(DataAccessException e)
        {

            return http400s(e, response);
        }
    }

    private Object listGames(Request request, Response response)
    {
        String authToken = request.headers("authorization");

        try
        {
            List<Records.ListedGame> result = gameManager.listGames(authToken);
            response.status(200);

            Map<String, Object> jsonWrapper = new HashMap<>();
            jsonWrapper.put("games", result);

            return new Gson().toJson(jsonWrapper);
        }
        catch(DataAccessException e)
        {
            return http400s(e, response);
        }
    }

    private Object http400s(DataAccessException e, Response response)
    {
        String message = e.getMessage();
        if(message.contains("must provide") || message.contains("A game with") || message.contains("Invalid team color"))
        {
            response.status(400);
            return new Gson().toJson(new JSONResponse("Error: bad request"));
        }
        else if(message.contains("no authorization token") || message.contains("Incorrect password") ||
                message.contains("does not exist"))
        {
            response.status(401);
            return new Gson().toJson(new JSONResponse("Error: unauthorized"));
        }
        else if(message.contains("already exists.") || message.contains("Another user has"))
        {
            response.status(403);
            return new Gson().toJson(new JSONResponse("Error: already taken"));
        }
        else
        {
            return http500(e, response);
        }
    }

    private Object http200(Response response)
    {
        response.status(200);
        return new Gson().toJson(new JSONResponse(""));
    }

    private Object http500(DataAccessException e, Response response)
    {
        response.status(500);
        return new Gson().toJson(new JSONResponse("Error: " + e.getMessage()));
    }

    public record JSONResponse(String message) {}
}
