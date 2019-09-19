package myGameEngine;

import ray.input.action.AbstractInputAction;
import ray.rage.scene.*;
import net.java.games.input.Event;

public class MovForward extends AbstractInputAction {
	private Camera3Pcontroller con;
	private SceneNode target;

	public MovForward(Camera3Pcontroller c, SceneNode t) {
		con = c;
		target = t;
	}

	public void performAction(float time, Event e) {
		con.movForward(target);
	}
}
