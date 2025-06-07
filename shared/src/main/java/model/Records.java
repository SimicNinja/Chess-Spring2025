package model;

import chess.ChessGame;

public class Records
{
	public record LoginRequest(String username, String password) {}
	public record LoginResult(String username, String authToken){}
	public record NewGameRequest(String authToken, String gameName) {}
	public record NewGameResult(int gameID) {}
	public record JoinGameRequest(String authToken, ChessGame.TeamColor playerColor, int gameID) {}
}
