package org.csgames.ai.client;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

public class Util {
	
	public class Point2D{
		public final int x;
		public final int y;
		public Point2D(int x, int y){ this.x = x; this.y = y; }
	}
	
	public enum Artifact{
		Player1("1"),
		Player2("2"),
		Player3("3"),
		Player4("4"),
		Myself("Y"),
		BrickWall("W"),
		HardWall("H"),
		Bomb("B"),
		Explosion("E"),
		SuddenDeathAlert("A"),
		PowUpBomb("b"),
		PowUpRange("r"),
		PowUpDet("d");
		
		private String val;
		Artifact(String s){
			val = s;
		}
	}
	
	private String[][] map;
	
	public void updateMap(String[][] map){
		this.map = map;
	}
	
	public List<Point2D> search(int x, int y, int max, Artifact type){
		ArrayList<Point2D> list = new ArrayList<Point2D>();
		return list;
	}
	
	public double distance(int x1, int y1, int x2, int y2){
		int dX = x2 - x1;
		int dY = y2 - y1;
		return Math.sqrt(dX*dX + dY*dY);
	}
	
	
	public double checkSafety(int x, int y){
		return 0.0;
	}
}
