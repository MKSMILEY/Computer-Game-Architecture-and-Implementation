package myGameEngine;

import ray.input.action.AbstractInputAction;
import ray.rage.scene.*;
import net.java.games.input.Event;

public class YawLeft extends AbstractInputAction {
	private Camera3Pcontroller con;
	private SceneNode target;
	
	public YawLeft(Camera3Pcontroller c, SceneNode t) {
		con = c;
		target = t;
	}
	
	public void performAction(float time, Event e) {
		con.yawLeft(target);
	}
}
