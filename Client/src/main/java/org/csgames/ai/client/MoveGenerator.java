package org.csgames.ai.client;

import java.io.IOException;
import java.util.Random;

import org.csgames.ai.client.network.GameEndedException;
import org.csgames.ai.client.network.NetworkLayer;
import org.csgames.ai.client.network.NextMoveSender;

public class MoveGenerator {

	private NetworkLayer network;
	private AvailableMoves[] myMoves = { AvailableMoves.Up,
			AvailableMoves.Down, AvailableMoves.Right, AvailableMoves.Left};
	
	
	private Util mUtil = new Util();
	private AI mAi = new AI(mUtil);

	public MoveGenerator(NetworkLayer network) {
		this.network = network;
	}

	public void generateMoves() {
		NextMoveSender nextMoveSender;

		try {
			while ((nextMoveSender = network.waitOnMoveToBeAsked()) != null) {
				playMove(nextMoveSender);
			}
		} catch (IOException e) {
			// Do something more clever
			generateMoves();
		} catch(GameEndedException ex) {
			System.out.println("Game ended");
		}
	}

	private void playMove(NextMoveSender nextMoveSender) throws IOException {
		String[][] map = nextMoveSender.getCurrentMap();
		
		mUtil.updateMap(map, System.currentTimeMillis());
		mAi.playMove(nextMoveSender);
		
		int randomMove = (new Random()).nextInt(9999) % myMoves.length;
		nextMoveSender.setMoveAndSend(myMoves[randomMove]);
	}
}
