package myGameEngine;

import ray.input.action.AbstractInputAction;
import ray.rage.scene.*;
import ray.rage.game.*;
import ray.rml.*;
import net.java.games.input.Event;
import a1.MyGame;

public class MovZ extends AbstractInputAction {
	private MyGame game;
	
	public MovZ(MyGame g) {
		game = g;
	}
	
	public void performAction(float time, Event e) {
		if(game.getYAxis() > 0.25f) {
			game.movBackward();
		}
		else if(game.getYAxis() < 0.25f) {
			game.movForward();
		}
	}
}
