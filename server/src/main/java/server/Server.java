package server;

import spark.*;
import service.*;

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
}
