package org.csgames.ai.client;

import java.util.ArrayList;
import java.util.List;

import org.csgames.ai.client.Util.Point2D;

public class Player {
	
	private int mBombLeft = 0;
	private int mPowUpBomb = 0;
	private int mPowUpRange = 0;
	private boolean mHasPowUpDet = false;
	private boolean mHasPowUpKick = false;
	private String mPlayerNumber = "";
	private Point2D mLocation;
	
	private List<PlayerBomb> mBombList = new ArrayList<PlayerBomb>();
	
	public Player(String number){
		mPlayerNumber = number;
	}
	
	// Player current state
	
	public void update(long time){
		if(!hasDetonationPU()){
			for(PlayerBomb bomb : mBombList){
				if(bomb.timeBeforeExplode(time) <= 0) {
					explode(bomb);
				}
			}
		}
	}
	
	// Get/set
	
	public int getBombLeft(){ return mBombLeft + mPowUpBomb; }
	public List<PlayerBomb> getBombList(){ return mBombList; }
	
	public String getPlayerNumber(){ return mPlayerNumber; }
	
	public boolean addBomb(PlayerBomb b){
		if(getBombLeft() <= 0) return false;
		mBombLeft--;
		return mBombList.add(b);
	}
	
	public void explode(PlayerBomb b){
		mBombLeft++;
		mBombList.remove(b);
	}
	
	public void setLocation(int x, int y) {
		mLocation = new Point2D(x,y);
	}
	
	public Point2D getLocation(){ return mLocation; }
	
	// Powerups
	public int getBombPU(){ return mPowUpBomb; }
	public void addBombPU(){ mPowUpBomb++; }
	
	public int getRangePU(){ return mPowUpRange; }
	public void addRangePU(){ mPowUpRange++; }
	
	public boolean hasDetonationPU(){ return mHasPowUpDet; }
	public void setDetonationPU(){ mHasPowUpDet = true; }
	
	public boolean hasKickPU(){ return mHasPowUpKick; }
	public void setKickPU(){ mHasPowUpKick = true; }
	
	
	// Helper classes
	public static class Values{
		public final static int DEFAULT_BOMB_RANGE = 2;
		public final static long DEFAULT_BOMB_DET_TIME = 1000;
	}
	
	public class PlayerBomb{
		public final Point2D location;
		public final long createdAt;
		public PlayerBomb(Point2D location, long time){
			this.location = location;
			createdAt = time;
		}
		public long timeBeforeExplode(long now){
			if( mHasPowUpDet ) return 0;
			return Values.DEFAULT_BOMB_DET_TIME - (now - createdAt);
		}
		public int getRange(){ return Values.DEFAULT_BOMB_RANGE + mPowUpRange; }
	}
}
