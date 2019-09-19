package myGameEngine;

import ray.input.action.AbstractInputAction;
import ray.rage.scene.*;
import ray.rage.game.*;
import ray.rml.*;
import net.java.games.input.Event;
import a1.MyGame;

public class MovRX extends AbstractInputAction {
	private MyGame game;
	
	public MovRX(MyGame g) {
		game = g;
	}
	
	public void performAction(float time, Event e) {
		if(game.getRXAxis() > 0.25f) {
			game.yawRight();
		}
		else if(game.getRXAxis() < 0.25f) {
			game.yawLeft();
		}
	}
}
