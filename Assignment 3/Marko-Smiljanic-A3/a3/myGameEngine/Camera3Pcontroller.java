package myGameEngine;

import net.java.games.input.Controller;
import ray.rage.scene.*;
import ray.rml.*;
import ray.input.*;
import ray.input.action.*;


public class Camera3Pcontroller {
	private SceneNode cameraN;
	private SceneNode target;
	private float cameraAzimuth = 180.0f;
	private float cameraElevation = 20.0f;
	private float radius = 3.0f;
	private Vector3 worldUpVec = Vector3f.createFrom(0.0f, 1.0f, 0.0f);
	private float dFriction = 0.05f;
	private Angle rotAmt = Degreef.createFrom(0.5f);
	private Angle negRotAmt = Degreef.createFrom(-0.5f);
	private float camRotAmt = 0.5f;
	
	private ProtocolClient protClient;
	
	public Camera3Pcontroller(SceneNode camN, SceneNode targ, Controller gp, InputManager im, ProtocolClient pc) {
		cameraN = camN;
		target = targ;
		protClient = pc;
		
		System.out.println("Gamepad Camera Controller Created");
		
		setupGPInput(im, gp);
		updateCameraPosition();
	}
	
	public Camera3Pcontroller(SceneNode camN, SceneNode targ, Controller kb, Controller m, InputManager im, ProtocolClient pc) {
		cameraN = camN;
		target = targ;
		protClient = pc;
		
		System.out.println("Keyboard Camera Controller Created");
		
		setupKBMInput(im, kb, m);
		updateCameraPosition();
	}
	
	private void setupKBMInput(InputManager im, Controller kb, Controller m) {
		Action movFKB = new MovForward(this, target);
		Action movBKB = new MovBackward(this, target);
		Action movRKB = new MovRight(this, target);
		Action movLKB = new MovLeft(this, target);
		//Action yawLKB = new YawLeft(this, target);
		//Action yawRKB = new YawRight(this, target);
		
		Action camLKB = new CamLeftAction();
		Action camRKB = new CamRightAction();
		Action camUKB = new CamUpAction();
		Action camDKB = new CamDownAction();
		Action camIKB = new CamInAction();
		Action camOKB = new CamOutAction();
		
		im.associateAction(kb.getName(), net.java.games.input.Component.Identifier.Key.W, movFKB, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		im.associateAction(kb.getName(), net.java.games.input.Component.Identifier.Key.A, movLKB, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		im.associateAction(kb.getName(), net.java.games.input.Component.Identifier.Key.S, movBKB, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		im.associateAction(kb.getName(), net.java.games.input.Component.Identifier.Key.D, movRKB, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		//im.associateAction(kb.getName(), net.java.games.input.Component.Identifier.Key.Q, yawLKB, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		//im.associateAction(kb.getName(), net.java.games.input.Component.Identifier.Key.E, yawRKB, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		
		im.associateAction(kb.getName(), net.java.games.input.Component.Identifier.Key.LEFT, camLKB, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		im.associateAction(kb.getName(), net.java.games.input.Component.Identifier.Key.RIGHT, camRKB, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		im.associateAction(kb.getName(), net.java.games.input.Component.Identifier.Key.UP, camUKB, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		im.associateAction(kb.getName(), net.java.games.input.Component.Identifier.Key.DOWN, camDKB, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		im.associateAction(kb.getName(), net.java.games.input.Component.Identifier.Key.T, camIKB, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		im.associateAction(kb.getName(), net.java.games.input.Component.Identifier.Key.G, camOKB, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		
		System.out.println("Inputs setup for Keyboard and Mouse");
	}
	
	private void setupGPInput(InputManager im, Controller gp) {
		Action oAA = new OrbitAroundAction();
		Action pAA = new PitchAroundAction();
		Action mFBA = new MoveForBackAction();
		Action mLRA = new MoveLeftRightAction();
		
		//Action yawLGP = new YawLeft(this, target);
		//Action yawRGP = new YawRight(this, target); 
		Action camIGP = new CamInAction();
		Action camOGP = new CamOutAction();
		
		im.associateAction(gp.getName(),  net.java.games.input.Component.Identifier.Axis.X, mLRA, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		im.associateAction(gp.getName(),  net.java.games.input.Component.Identifier.Axis.RX, oAA, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		im.associateAction(gp.getName(),  net.java.games.input.Component.Identifier.Axis.Y, mFBA, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		im.associateAction(gp.getName(),  net.java.games.input.Component.Identifier.Axis.RY, pAA, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		
		//im.associateAction(gp.getName(),  net.java.games.input.Component.Identifier.Button._5, yawLGP, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		//im.associateAction(gp.getName(),  net.java.games.input.Component.Identifier.Button._6, yawRGP, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		im.associateAction(gp.getName(),  net.java.games.input.Component.Identifier.Button._2, camOGP, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		im.associateAction(gp.getName(),  net.java.games.input.Component.Identifier.Button._4, camIGP, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
	}
	
	public void updateCameraPosition() {
		double theta = Math.toRadians(cameraAzimuth); // rot around target
		double phi = Math.toRadians(cameraElevation); // altitude angle
		float x = (float) (radius * Math.cos(phi) * Math.sin(theta));
		float y = (float) (radius * Math.sin(phi));
		float z = (float) (radius * Math.cos(phi) * Math.cos(theta));	
		
		cameraN.setLocalPosition(Vector3f.createFrom((float)x, (float)y, (float)z).add(target.getWorldPosition()));
		cameraN.lookAt(target, worldUpVec);
	}
	
	public class OrbitAroundAction extends AbstractInputAction {
		
		public void performAction(float time, net.java.games.input.Event evt) {
			float rotAmt;
			if(evt.getValue() < -0.2) {
				rotAmt = -0.2f;
			}
			else {
				if(evt.getValue() > 0.2) {
					rotAmt = 0.2f;
				}
				else {
					rotAmt = 0.0f;
				}
			}
			cameraAzimuth += rotAmt;
			cameraAzimuth = cameraAzimuth % 360;
			updateCameraPosition();
		}
	}
	public class PitchAroundAction extends AbstractInputAction {
		public void performAction(float time, net.java.games.input.Event evt) {
			float rotAmt;
			if(evt.getValue() < -0.2) {
				rotAmt = -0.2f;
			}
			else {
				if(evt.getValue() > 0.2) {
					rotAmt = 0.2f;
				}
				else {
					rotAmt = 0.0f;
				}
			}
			cameraElevation += rotAmt;
			cameraElevation = cameraElevation % 360;
			updateCameraPosition();
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
			updateCameraPosition();
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
			updateCameraPosition();
		}
	}
	
	public class CamLeftAction extends AbstractInputAction {
		
		public void performAction(float time, net.java.games.input.Event evt) {
			cameraAzimuth += camRotAmt;
			cameraAzimuth = cameraAzimuth % 360;
			updateCameraPosition();
		}
	}
	public class CamRightAction extends AbstractInputAction {
		
		public void performAction(float time, net.java.games.input.Event evt) {
			cameraAzimuth -= camRotAmt;
			cameraAzimuth = cameraAzimuth % 360;
			updateCameraPosition();
		}
	}
	public class CamUpAction extends AbstractInputAction {
		
		public void performAction(float time, net.java.games.input.Event evt) {
			cameraElevation += camRotAmt;
			cameraElevation = cameraElevation % 360;
			updateCameraPosition();
		}
	}
	public class CamDownAction extends AbstractInputAction {
		
		public void performAction(float time, net.java.games.input.Event evt) {
			cameraElevation -= camRotAmt;
			cameraElevation = cameraElevation % 360;
			updateCameraPosition();
		}
	}
	public class CamInAction extends AbstractInputAction {
		public void performAction(float time, net.java.games.input.Event evt) {
    		radius -= 0.1f; 
	    	if(radius <= 0.5f) {
	    		radius = 0.5f;
	    	}
	    	updateCameraPosition();
	    }
    }
    public class CamOutAction extends AbstractInputAction {
    	public void performAction(float time, net.java.games.input.Event evt) {
    		radius += 0.1f;
    		if(radius >= 5.0f) {
	    		radius = 5.0f;
	    	}
	    	updateCameraPosition();
    	}
    }
	
	public void movForward (SceneNode target) {
		target.moveForward(dFriction); 
		//protClient.sendMoveMessage(target.getWorldPosition());
		}
    public void movBackward(SceneNode target) {target.moveBackward(dFriction);}
    public void movRight(SceneNode target)    {target.moveRight(-dFriction);  }
    public void movLeft(SceneNode target)     {target.moveLeft(-dFriction);   }
    public void yawLeft(SceneNode target)	{target.yaw(rotAmt);	 }
    public void yawRight(SceneNode target)  {target.yaw(negRotAmt);  }
    
   
}
