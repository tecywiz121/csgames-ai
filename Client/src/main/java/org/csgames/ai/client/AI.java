package org.csgames.ai.client;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.csgames.ai.client.network.NextMoveSender;

import org.csgames.ai.client.AvailableMoves;
import org.csgames.ai.client.Util.Artifact;
import org.csgames.ai.client.Util.Point2D;

public class AI {	
	private class Action {
		private AvailableMoves mType = AvailableMoves.None;
		
		public AvailableMoves getAvailableMove() {
			return mType;
		}
		
		public double getScore() {
			double score = 0.0;
			Util.Point2D me = mUtil.getMyLocation();
			
			// Bomb based score
			List<Point2D> bombs = mUtil.search(me.x, me.y, getBombRadius(), Artifact.Bomb);
			
			for (Point2D bomb : bombs) {
				score += mUtil.distance(me.x, me.y, bomb.x, bomb.y);
			}
			
			if (score > 0) {
				score = 1.0/score;
			}
			
			return score;
		}
	}

	private Util mUtil;
	private ArrayList<Action> mPossibleActions = new ArrayList<>();
	
	private long mFirstTurn;
	
	public AI (Util util) {
		mUtil = util;
	}

	public void playMove(NextMoveSender nextMoveSender) throws IOException {
		// Set the turn time
		if (mFirstTurn == 0) {
			mFirstTurn = System.currentTimeMillis();
		}
		
		// Clear the possible moves
		mPossibleActions.clear();
		
		runFromBombs();
		lookForPowerups();
		attack();
		
		// Pick the best action
		Action bestAction = new Action();
		for (Action action : mPossibleActions) {
			if (action.getScore() > bestAction.getScore()) {
				bestAction = action;
			}
		}
		
		// Execute Action
		nextMoveSender.setMoveAndSend(bestAction.getAvailableMove());
	}
	
	private long getElapsedTime() {
		return System.currentTimeMillis() - mFirstTurn;
	}
	
	private int getBombRadius() {
		return 2; // TODO: make this return how many powerups we have plus one
	}
	
	private void runFromBombs() {
		Util.Point2D me = mUtil.getMyLocation();
		Util.Point2D above = new Util.Point2D(me.x, me.y-1);
		Util.Point2D below = new Util.Point2D(me.x, me.y+1);
		Util.Point2D left = new Util.Point2D(me.x-1, me.y);
		Util.Point2D right = new Util.Point2D(me.x+1, me.y);
		
		List<Util.Point2D> bombs = mUtil.search(me.x, me.y, getBombRadius(), Artifact.Bomb);
		
		
		
		for (Util.Point2D bomb : bombs) {
			if (bomb.x == me.x) {
				if (mUtil.at(above) == Artifact.Empty) {
					addAction(AvailableMoves.Up); // TODO: check this to see if we move in correct direction
				}
				if (mUtil.at(below) == Artifact.Empty) {
					addAction(AvailableMoves.Down);
				}
			}
			if (bomb.y == me.y) {
				if (mUtil.at(left) == Artifact.Empty) {
					addAction(AvailableMoves.Left); // TODO: check this to see if we move in correct direction
				}
				if (mUtil.at(right) == Artifact.Empty) {
					addAction(AvailableMoves.Right);
				}
			}
		}
	}
	
	private void addAction(AvailableMoves action) {
		Action a = new Action();
		a.mType = action;
		mPossibleActions.add(a);
	}
	
	private void lookForPowerups() {
		
	}
	
	private void attack() {
		
	}
}
