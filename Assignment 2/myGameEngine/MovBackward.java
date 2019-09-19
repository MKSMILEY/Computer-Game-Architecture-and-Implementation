package myGameEngine;

import ray.input.action.AbstractInputAction;
import ray.rage.scene.*;
import net.java.games.input.Event;

public class MovBackward extends AbstractInputAction {
	private Camera3Pcontroller con;
	private SceneNode target;
	
	public MovBackward(Camera3Pcontroller c, SceneNode tar) {
		con = c;
		target = tar;
	}
	
	public void performAction(float time, Event e) {
		con.movBackward(target);
	}
}