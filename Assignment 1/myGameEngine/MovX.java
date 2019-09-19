package myGameEngine;

import ray.input.action.AbstractInputAction;
import ray.rage.scene.*;
import ray.rage.game.*;
import ray.rml.*;
import net.java.games.input.Event;
import a1.MyGame;

public class MovX extends AbstractInputAction {
	private MyGame game;
	
	public MovX(MyGame g) {
		game = g;
	}
	
	public void performAction(float time, Event e) {
		if(game.getXAxis() > 0.25f) {
			game.movRight();
		}
		else if(game.getXAxis() < 0.25f) {
			game.movLeft();
		}
	}
}