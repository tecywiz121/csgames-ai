package org.csgames.ai.client;

import static org.junit.Assert.*;

import org.csgames.ai.client.Util.Artifact;
import org.junit.Before;
import org.junit.Test;

public class UtilTest {

	private String[][] map;
	private Util util;
	
	@Before
	public void before(){
		map = new String[4][4];
		for(int i = 0; i < 4; i++)
			for(int j = 0; j < 4; j++)
				map[i][j] = "H";
		
		map[2][2] = "B";
		
		util = new Util();
		util.updateMap(map);
	}
	
	@Test
	public void testSearch(){
		util.search(0, 0, 5, Artifact.Bomb);
	}

}
