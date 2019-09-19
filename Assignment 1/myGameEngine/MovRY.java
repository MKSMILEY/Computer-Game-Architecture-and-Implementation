package myGameEngine;

import ray.input.action.AbstractInputAction;
import ray.rage.scene.*;
import ray.rage.game.*;
import ray.rml.*;
import net.java.games.input.Event;
import a1.MyGame;

public class MovRY extends AbstractInputAction {
	private MyGame game;
	
	public MovRY(MyGame g) {
		game = g;
	}
	
	public void performAction(float time, Event e) {
		if(game.getRYAxis() > 0.25f) {
			game.pitchUp();
		}
		else if(game.getRYAxis() < 0.25f) {
			game.pitchDown();
		}
	}
}
