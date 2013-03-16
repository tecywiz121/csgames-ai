package org.csgames.ai.client;

import java.util.ArrayList;
import java.util.List;

import org.csgames.ai.client.Util.Point2D;

public class Player {

	private int mBombLeft = Values.DEFAULT_BOMB_COUNT;
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
			
			PlayerBomb[] bombs = mBombList.toArray(new PlayerBomb[mBombList.size()]);
			
			for(PlayerBomb bomb : bombs){
				System.out.println(bomb);
				if(bomb.timeBeforeExplode(time) <= 0) {
					mBombList.remove(bomb);
					mBombLeft++;
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

	public void setLocation(int x, int y) {
		mLocation = new Point2D(x,y);
	}

	public Point2D getLocation(){ return mLocation; }
	public int getTotalRange(){ 
		return getRangePU() + Values.DEFAULT_BOMB_RANGE;
	}

	// Powerups
	public int getBombPU(){ return mPowUpBomb; }
	public void addBombPU(){ mPowUpBomb++; }

	public int getRangePU(){ return mPowUpRange; }
	public void addRangePU(){ mPowUpRange++; }

	public boolean hasDetonationPU(){ return mHasPowUpDet; }
	public void setDetonationPU(){ mHasPowUpDet = true; }

	public boolean hasKickPU(){ return mHasPowUpKick; }
	public void setKickPU(){ mHasPowUpKick = true; }

	public String toString(){
		return String.format("Player %s@%s, bomb_left:%d, bomb_range:%d, bomb_pu:%d, range_pu:%d",
				mPlayerNumber, 
				mLocation,
				getBombLeft(),
				getTotalRange(),
				getBombPU(),
				getRangePU()
				);
	}

	// Helper classes
	public static class Values{
		public final static int DEFAULT_BOMB_RANGE = 2;
		public final static int DEFAULT_BOMB_COUNT = 3;
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
		public String getOwner(){ return mPlayerNumber; }
		public String toString(){
			long now = System.currentTimeMillis();
			return String.format("bomb@%s from p%s ttl:%d", location, getOwner(), timeBeforeExplode(now));
		}
		
		public List<Point2D> getDangerZone(){
			List<Point2D> list = new ArrayList<Point2D>();
			int x = Math.max(0, location.x + getRange() - 1);
			int y = Math.max(0, location.y + getRange() - 1);
			for(; x < 100; x++){
				for(; y < 100; y++){
					list.add(new Point2D(x,y));
				}
			}
			return list;
		}
	}
}
