package org.csgames.ai.client.bootstrap;

import java.io.IOException;
import java.net.UnknownHostException;

import org.csgames.ai.client.MoveGenerator;
import org.csgames.ai.client.network.NetworkLayer;

public class BombermanClient {
	
	public static void main(String[] args) throws UnknownHostException, IOException {
		NetworkLayer network = new NetworkLayer();
		network.connectToServer();
		
		new MoveGenerator(network).generateMoves();
	}
}
