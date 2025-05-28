package dataaccess;

import chess.ChessGame;
import chess.ChessPiece;
import com.google.gson.*;

import java.lang.reflect.Type;

public class ChessPieceAdapter implements JsonDeserializer<ChessPiece>
{
	@Override
	public ChessPiece deserialize(JsonElement json, Type type, JsonDeserializationContext context)
			throws JsonParseException
	{
		JsonObject pieceObject = json.getAsJsonObject();

		ChessGame.TeamColor color = context.deserialize(pieceObject.get("color"), ChessGame.TeamColor.class);
		ChessPiece.PieceType pieceType = context.deserialize(pieceObject.get("type"), ChessPiece.PieceType.class);
		boolean hasMoved = pieceObject.get("hasMoved").getAsBoolean();

		return new ChessPiece(color, pieceType, hasMoved);
	}
}
