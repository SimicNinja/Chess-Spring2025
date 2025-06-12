package client;

import websocket.GameClient;

import java.util.Scanner;

import static ui.EscapeSequences.*;

public class REPL
{
	private final Client client;

	public REPL(String serverUrl)
	{
		client = new Client(serverUrl);
	}

	public void run()
	{
		System.out.println(SET_TEXT_BOLD + SET_TEXT_UNDERLINE + WHITE_KING + " Welcome to chess game. " + BLACK_KING
			+ " Sign in to start." + RESET_TEXT_UNDERLINE + RESET_TEXT_UNDERLINE + SET_TEXT_COLOR_BLUE);
		System.out.print(client.help());

		Scanner scanner = new Scanner(System.in);
		var result = "";
		while(!result.equals("quit"))
		{
			printPrompt();
			String line = scanner.nextLine();

			try
			{
				result = client.eval(line);
				System.out.print(SET_TEXT_COLOR_BLUE + result);
			}
			catch(Throwable e)
			{
				var msg = e.getMessage();
				System.out.print(SET_TEXT_COLOR_RED + msg);
			}
		}
		System.out.println();
	}

	public void runGame(GameClient gameClient)
	{
		Scanner scanner = new Scanner(System.in);
		var result = "";
		while(!result.equals("leave"))
		{
			printPrompt();
			String line = scanner.nextLine();

			try
			{
				result = gameClient.eval(line);
				System.out.print(SET_TEXT_COLOR_BLUE + result);
			}
			catch(Throwable e)
			{
				var msg = e.getMessage();
				System.out.print(SET_TEXT_COLOR_RED + msg);
			}
		}
		System.out.println();
	}

	private void printPrompt()
	{
		if(client.isSignedIn())
		{
			System.out.print("\n" + RESET_TEXT_COLOR + "[Logged In] ");
		}
		else
		{
			System.out.print("\n" + RESET_TEXT_COLOR + "[Logged Out] ");
		}
		System.out.print(">>> " + SET_TEXT_COLOR_GREEN);
	}
}
