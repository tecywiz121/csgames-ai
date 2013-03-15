package org.csgames.ai.client.network;

import java.io.BufferedWriter;
import java.io.IOException;

import org.csgames.ai.client.AvailableMoves;

public class NextMoveSender {

	private String[][] map;
	private BufferedWriter writer;
	private boolean moveSent = false;

	public NextMoveSender(String[][] map, BufferedWriter writer) {
		this.map = map;
		this.writer = writer;
	}

	/**
	 * This function sends your final move for this round to the server.
	 * This method may only be called once per loop (subsequent calls are just dropped).
	 * @param move
	 * @throws IOException
	 */
	public void setMoveAndSend(AvailableMoves move) throws IOException {
		if(!moveSent) {
			writer.append(move.toString() + "\n");
			writer.flush();
			moveSent = true;
		}
	}

	/**
	 * Get the map as it was before your move.
	 * @return
	 */
	public String[][] getCurrentMap() {
		return map;
	}
}
