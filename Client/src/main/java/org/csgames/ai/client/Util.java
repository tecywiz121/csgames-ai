package org.csgames.ai.client;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.csgames.ai.client.Player.PlayerBomb;

public class Util {

	public final static String PLAYER_1 = "1";
	public final static String PLAYER_2 = "2";
	public final static String PLAYER_3 = "3";
	public final static String PLAYER_4 = "4";
	public final static String MYSELF = "Y";
	public final static String BRICK_WALL = "W";
	public final static String HARD_WALL = "H";
	public final static String EMPTY = "";
	public final static String BOMB = "B";
	public final static String EXPLOSION = "E";
	public final static String SUDDEN_DEATH_ALERT = "A";
	public final static String POW_UP_BOMB = "b";
	public final static String POW_UP_RANGE = "r";
	public final static String POW_UP_DET = "d";
	public final static String POW_UP_KICK = "k";

	private String[][] mOldMap;
	private String[][] mMap;

	private boolean isInitialized = false;

	// need TreeMap for fast iteration
	private TreeMap<String, Player> mPlayers = new TreeMap<String,Player>();

	public void updateMap(String[][] map, long time){
		long start = System.currentTimeMillis();
		if(!isInitialized){
			init(map);
			isInitialized = true;
		} 

		if( mMap == null ) mOldMap = map;
		else mOldMap = mMap;
		mMap = map;

		updateStateFromMap(map, time);

		for(Player p : mPlayers.values()){
			p.update(time);
			System.out.println(p);
		}

		long end = System.currentTimeMillis() - start;
		System.out.printf("Util.update() in %d ms\n", end);

	}

	private void updateStateFromMap(String[][] map, long time) {
		for(int i = 0; i < map.length; i++){
			for(int j = 0; j < map[0].length; j++){

				String cell = map[i][j]; 
				if(cell.contains(PLAYER_1) || 
						cell.contains(PLAYER_2) || 
						cell.contains(PLAYER_3) || 
						cell.contains(PLAYER_4) ||
						cell.contains(MYSELF) ){
					updatePlayer(cell, i,j, time);
				}
			}
		}
	}

	private void init(String[][] map){
		for(int i = 0; i < map.length; i++){
			for(int j = 0; j < map[0].length; j++){
				if(map[i][j].equals("")) continue;
				String cell = String.valueOf(map[i][j].charAt(0));
				switch(cell){
				case PLAYER_1:
				case PLAYER_2:
				case PLAYER_3:
				case PLAYER_4:
				case MYSELF:
					mPlayers.put(cell, new Player(cell));
					break;
				default:
					// Not a player
				}
			}
		}
	}

	private void updatePlayer(String cell, int x, int y, long time){
		// get player (string can contain B character too)
		String player;
		if(cell.contains(PLAYER_1)) player = PLAYER_1;
		else if(cell.contains(PLAYER_2)) player = PLAYER_2;
		else if(cell.contains(PLAYER_3)) player = PLAYER_3;
		else if(cell.contains(PLAYER_4)) player = PLAYER_4;
		else if(cell.contains(MYSELF)) player = MYSELF;
		else return;  // not a player

		Player p = mPlayers.get(player);

		p.setLocation(x,y);

		if( mOldMap[x][y].equals(POW_UP_BOMB) ){
			// update bomb power up
			p.addBombPU();
		}

		if( mOldMap[x][y].equals(POW_UP_DET) ){
			p.setDetonationPU();
		}

		if( mOldMap[x][y].equals(POW_UP_KICK) ){
			p.setKickPU();
		}

		if( mOldMap[x][y].equals(POW_UP_RANGE) ){
			p.addRangePU();
		}

		// contains because when player sits on it, the string is "YB" or "1B"
		if( mMap[x][y].contains(BOMB) ){
			PlayerBomb b = p.new PlayerBomb(p.getLocation(), time);
			p.addBomb(b);
		}

		// save changes
		mPlayers.put(player, p);
	}


	public List<Point2D> search(int x, int y, int max, String type){
		ArrayList<Point2D> list = new ArrayList<Point2D>();

		int lowBoundX = Math.max(0, x - max);
		int lowBoundY = Math.max(0, y - max);
		int hiBoundX = Math.min(mMap.length, x + max);
		int hiBoundY = Math.min(mMap[0].length, y + max);

		for(int col = lowBoundX; col < hiBoundX; col++){
			for(int row = lowBoundY; row < hiBoundY; row++){

				if( mMap[col][row].equals(type) ){
					list.add(new Point2D(col, row));
				}
			}
		}

		return list;
	}

	public Player getSelfState(){
		return mPlayers.get(MYSELF);
	}

	public Player getPlayer(String playerNumber){
		return mPlayers.get(playerNumber);
	}

	public PlayerBomb getBombAtLocation(Point2D location){
		for(Player p : mPlayers.values()){
			for(PlayerBomb bomb : p.getBombList()){
				if(bomb.location.equals(location)) return bomb;
			}
		}
		return null;
	}

	public boolean passable(Point2D location){
		String cell = at(location);
		if( cell.equals(EMPTY) ||  // NOT contains, cuz everyrthing contains empty string
				cell.contains(POW_UP_BOMB) ||
				cell.contains(POW_UP_DET) ||
				cell.contains(POW_UP_RANGE) ||
				cell.contains(POW_UP_KICK) ){
			return true;
		}

		return false;
	}

	public double distance(Point2D first, Point2D second){
		return distance(first.x, first.y, second.x, second.y);
	}

	public double distance(int x1, int y1, int x2, int y2){
		int dX = x2 - x1;
		int dY = y2 - y1;
		return Math.sqrt(dX*dX + dY*dY);
	}

	/**
	public double scoreToBrick(Point2D location){
		if( !passable(location) ) return 0.0;
		List<Point2D> brickLocList = search(location.x, location.y, 100, BRICK_WALL);
		
		
	}
	

	public double distanceToClosest(String type){
		// stupidly expensive since it will be called for each cell, most likely
		for(int i = 0; i < mMap.length; i++){
			for(int j = 0; j < mMap[0].length; j++){
				if( at(i,j).equals(type) ){
					
				}
			}
		}
		
		double distanceToClosestBomb = Double.MAX_VALUE;
		for(Point2D aBomb : bombList){
			
			double distanceToBomb = distance(aBomb, checkedPoint);
			if( distanceToBomb < distanceToClosestBomb ){
				distanceToClosestBomb = distanceToBomb;
			}
		}
	}
	*/
	
	public double checkSafety(Point2D p) { return checkSafety(p.x, p.y); }

	public double checkSafety(int x, int y){
		if( at(x,y).equals(BOMB) 
				|| at(x,y).equals(EXPLOSION) 
				|| at(x,y).equals(SUDDEN_DEATH_ALERT) ){
			return 0.0;
		}

		Point2D checkedPoint = new Point2D(x,y);

		List<Point2D> bombList = search(x,y, Player.Values.DEFAULT_BOMB_RANGE, BOMB);
		
		// don't care about bombs that are in diagonal
		for(Point2D bomb : bombList.toArray(new Point2D[0])){
			if(bomb.x != x && bomb.y != y ) bombList.remove(bomb);
		}
		

		List<PlayerBomb> pBomb = new ArrayList<PlayerBomb>();
		for(Point2D aBombLocation : bombList){
			PlayerBomb b = getBombAtLocation(aBombLocation);
			if(b != null) pBomb.add(b);
		}
		
		double distanceToClosestBomb = Double.MAX_VALUE;
		for(Point2D aBomb : bombList){
			
			double distanceToBomb = distance(aBomb, checkedPoint);
			if( distanceToBomb < distanceToClosestBomb ){
				distanceToClosestBomb = distanceToBomb;
			}
		}

		double rangedDistance = 
				Math.max(distanceToClosestBomb, 
						(double) Player.Values.DEFAULT_BOMB_RANGE);

		return rangedDistance / (double) Player.Values.DEFAULT_BOMB_RANGE;
	}

	public Point2D getMyLocation(){
		return mPlayers.get(MYSELF).getLocation();
	}

	public String at(Point2D p) { return at(p.x, p.y); }

	public String at(int x, int y){
		if( x < 0
				|| x >= mMap.length
				|| y < 0
				|| y > mMap[0].length) return HARD_WALL;
		return mMap[x][y];
	}

	public static class Point2D{
		public final int x;
		public final int y;

		public Point2D(int x, int y){ 
			this.x = x; 
			this.y = y; 
		}

		public boolean equals(Object other){
			if(other == null) return false;
			if(!(other instanceof Point2D)) return false;
			Point2D p = (Point2D) other;
			return p.x == x && p.y == y;
		}

		public String toString(){
			return String.format("[%d, %d]", x, y);
		}
	}
}
