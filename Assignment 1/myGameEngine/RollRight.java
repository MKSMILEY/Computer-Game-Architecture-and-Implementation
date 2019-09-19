package myGameEngine;

import ray.input.action.AbstractInputAction;
import ray.rage.scene.*;
import ray.rage.game.*;
import ray.rml.*;
import net.java.games.input.Event;
import a1.MyGame;

public class RollRight extends AbstractInputAction {
	private MyGame game;
	
	public RollRight(MyGame g) {
		game = g;
	}
	
	public void performAction(float time, Event e) {
		game.rollRight();
	}
}