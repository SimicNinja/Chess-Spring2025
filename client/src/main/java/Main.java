import chess.*;
import client.REPL;
import server.Server;

public class Main
{
    public static void main(String[] args)
    {
        String serverURL = "http://localhost:8080";

        Server server = new Server();
        var port = server.run(8080);
        System.out.println("Started test HTTP server on " + port);

        new REPL(serverURL).run();

    }
}