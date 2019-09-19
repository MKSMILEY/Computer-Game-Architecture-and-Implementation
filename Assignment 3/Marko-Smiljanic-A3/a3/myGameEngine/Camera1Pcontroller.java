package myGameEngine;


import net.java.games.input.Controller;
import ray.rage.scene.*;
import ray.rml.*;
import ray.input.*;
import ray.input.action.*;
import ray.physics.PhysicsObject;

import a3.*;

public class Camera1Pcontroller {
	private Camera camera;
	private SceneNode target;
	private SkeletalEntity skeleton;
	private Vector3 worldUpVec = Vector3f.createFrom(0.0f, 1.0f, 0.0f);
	private float dFriction = 0.05f;
	private Angle rotAmt = Degreef.createFrom(0.5f);
	private Angle negRotAmt = Degreef.createFrom(-0.5f);
	private float camRotAmt = 0.5f;
	private MyGame game;
	private boolean jogging = false;
	private int runTime = -1;
	private boolean moved = false;
	private float[] targ = {0.0f, 0.0f, 0.0f};
	
	private ProtocolClient protClient;
	private PhysicsObject physObj;
	
	public Camera1Pcontroller(MyGame g, Camera cam, SceneNode targ, SkeletalEntity skel, Controller gp, InputManager im, ProtocolClient pc) {
		game = g;
		camera = cam;
		target = targ;
		skeleton = skel;
		if(pc == null) {
			System.out.println("Protocol Client did not transfer to Controller");
		}
		else {
			protClient = pc;
		}
		physObj = target.getPhysicsObject();
		target.attachChild(camera.getParentNode());
		
		camera.getParentNode().setLocalPosition(target.getLocalPosition().x(), target.getLocalPosition().y() + .5f, target.getLocalPosition().z() - 10.0f);
		System.out.println("Gamepad Camera Controller Created");
		
		
		setupGPInput(im, gp);
		update();
	}
	
	public Camera1Pcontroller(MyGame g, Camera cam, SceneNode targ, SkeletalEntity skel, Controller kb, Controller m, InputManager im, ProtocolClient pc) {
		game = g;
		camera = cam;
		target = targ;
		skeleton = skel;
		if(pc == null) {
			System.out.println("Protocol Client did not transfer to Controller");
		}
		else {
			protClient = pc;
		}
		physObj = target.getPhysicsObject();
		target.attachChild(camera.getParentNode());
		
		camera.getParentNode().setLocalPosition(target.getLocalPosition().x(), target.getLocalPosition().y() + .5f, target.getLocalPosition().z() - 10.0f);
		
		System.out.println("Keyboard Camera Controller Created");
		
		setupKBMInput(im, kb, m);
		update();
	}
	
	private void setupKBMInput(InputManager im, Controller kb, Controller m) {
		Action movFKB = new MoveForward();
		Action movBKB = new MoveBackward();
		Action movRKB = new MoveRight();
		Action movLKB = new MoveLeft();
		Action yawLKB = new YawLeft();
		Action yawRKB = new YawRight();
		Action interact = new Interact();
		Action quitKB = new QuitGame();
		
		im.associateAction(kb, net.java.games.input.Component.Identifier.Key.W, movFKB, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		im.associateAction(kb, net.java.games.input.Component.Identifier.Key.A, movLKB, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		im.associateAction(kb, net.java.games.input.Component.Identifier.Key.S, movBKB, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		im.associateAction(kb, net.java.games.input.Component.Identifier.Key.D, movRKB, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		im.associateAction(kb, net.java.games.input.Component.Identifier.Key.LEFT, yawLKB, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		im.associateAction(kb, net.java.games.input.Component.Identifier.Key.RIGHT, yawRKB, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		im.associateAction(kb, net.java.games.input.Component.Identifier.Key.E, interact, InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
		im.associateAction(kb, net.java.games.input.Component.Identifier.Key.L, quitKB, InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
		
		System.out.println("Inputs setup for Keyboard and Mouse");
	}
	
	private void setupGPInput(InputManager im, Controller gp) {
		Action mFBA = new MoveForBackAction();
		Action mLRA = new MoveLeftRightAction();
		//Action yawLGP = new YawLeft();
		//Action yawRGP = new YawRight(); 

		
		im.associateAction(gp.getName(),  net.java.games.input.Component.Identifier.Axis.X, mLRA, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		im.associateAction(gp.getName(),  net.java.games.input.Component.Identifier.Axis.Y, mFBA, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		//im.associateAction(gp.getName(),  net.java.games.input.Component.Identifier.Button._5, yawLGP, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		//im.associateAction(gp.getName(),  net.java.games.input.Component.Identifier.Button._6, yawRGP, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
	}

	public void update() {		
		//game.updateVerticalPosition();
		if(moved) {
			physObj.setLinearVelocity(targ);
			moved = false;
			targ[0] = 0.0f;
			targ[2] = 0.0f;
		}
		
		if(physObj != null) {
			double[] send = physObj.getTransform();
			protClient.sendMoveMessage(send);
		}
		
		
		
		if(jogging && runTime == 100) {
			game.stopJogging(skeleton);
			runTime = -1;
			jogging = false;

			
		}
		else if(!jogging && runTime == 0){
			game.playJogging(skeleton);
			jogging = true;
		}
		else if(jogging && runTime <= 100) {
			runTime++;
			if(runTime == 99) {
				System.out.println(runTime);
			}
		}
		
		
	}
	
	public class MoveForBackAction extends AbstractInputAction {
		public void performAction(float time, net.java.games.input.Event evt) {
			float movAmt;
			if(evt.getValue() < -0.2) {
				movAmt = -0.05f;
			}
			else {
				if(evt.getValue() > 0.2) {
					movAmt = 0.05f;
				}
				else {
					movAmt = 0.0f;
				}
			}
			target.moveBackward(movAmt);
		
		}
	}
	public class MoveLeftRightAction extends AbstractInputAction {
		public void performAction(float time, net.java.games.input.Event evt) {
			float movAmt;
			if(evt.getValue() < -0.2) {
				movAmt = -0.05f;
			}
			else {
				if(evt.getValue() > 0.2) {
					movAmt = 0.05f;
				}
				else {
					movAmt = 0.0f;
				}
			}
			target.moveLeft(movAmt);
		}
	}
	
	public class MoveForward extends AbstractInputAction {
		public void performAction(float time, net.java.games.input.Event evt) {
			//float[] targ = {target.getLocalForwardAxis().x() * 10.0f, 0.0f, target.getLocalForwardAxis().z() * 10.0f};
			targ[0] += target.getLocalForwardAxis().x() * 10.0f;
			targ[2] += target.getLocalForwardAxis().z() * 10.0f;
			//physObj.setLinearVelocity(targ);
			runTime = 0;
			moved = true;
		}
	}
	public class MoveBackward extends AbstractInputAction {
		public void performAction(float time, net.java.games.input.Event evt) {
			//float[] targ = {target.getLocalForwardAxis().x() * -10.0f, 0.0f, target.getLocalForwardAxis().z() * -10.0f};
			targ[0] += target.getLocalForwardAxis().x() * -10.0f;
			targ[2] += target.getLocalForwardAxis().z() * -10.0f;
			//physObj.setLinearVelocity(targ);
			runTime = 0;
			moved = true;
		}
	}
	public class MoveLeft extends AbstractInputAction {
		public void performAction(float time, net.java.games.input.Event evt) {
			//float[] targ = {target.getLocalRightAxis().x() * 10.0f, 0.0f, target.getLocalRightAxis().z() * 10.0f};
			targ[0] += target.getLocalRightAxis().x() * 10.0f;
			targ[2] += target.getLocalRightAxis().z() * 10.0f;
			//physObj.setLinearVelocity(targ);
			runTime = 0;
			moved = true;
		}
	}
	public class MoveRight extends AbstractInputAction {
		public void performAction(float time, net.java.games.input.Event evt) {
			//float[] targ = {target.getLocalRightAxis().x() * -10.0f, 0.0f, target.getLocalRightAxis().z() * -10.0f};
			targ[0] += target.getLocalRightAxis().x() * -10.0f;
			targ[2] += target.getLocalRightAxis().z() * -10.0f;
			//physObj.setLinearVelocity(targ);
			runTime = 0;
			moved = true;
		}
	}
	public class YawLeft extends AbstractInputAction {
		public void performAction(float time, net.java.games.input.Event evt) {
			target.yaw(rotAmt);
			//running = true;
		}
	}
	public class YawRight extends AbstractInputAction {
		public void performAction(float time, net.java.games.input.Event evt) {
			target.yaw(negRotAmt);
			//running = true;
		}
	}
	
	public class Interact extends AbstractInputAction {
		public void performAction(float time, net.java.games.input.Event evt) {
			game.interact();
		}
	}
	public class QuitGame extends AbstractInputAction {
	   	@Override
	   	public void performAction(float time, net.java.games.input.Event evt) {
	   		if(protClient != null && game.getIsClientConnected() == true) {
	  			protClient.sendByeMessage();
	  		}
	   		game.exit();
	   	}
   }
}
