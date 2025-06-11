import chess.*;
import client.REPL;

public class Main
{
    public static void main(String[] args)
    {
        String serverURL = "http://localhost:8080";

        System.out.println("Started test HTTP server on 8080");

        new REPL(serverURL).run();
    }
}