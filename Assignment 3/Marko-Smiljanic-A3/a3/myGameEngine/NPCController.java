package myGameEngine;

import ray.rage.scene.*;
import ray.rml.*;
import ray.ai.behaviortrees.*;
import ray.physics.PhysicsObject;

import a3.MyGame;

public class NPCController {
	private NPC[] NPClist = new NPC[5];
	BehaviorTree[] bt = {new BehaviorTree(BTCompositeType.SELECTOR)/*, new BehaviorTree(BTCompositeType.SELECTOR), new BehaviorTree(BTCompositeType.SELECTOR)*/};
	private float thinkStartTime, tickStateTime, lastThinkUpdateTime, lastTickUpdateTime;
	private MyGame game;
	private ProtocolClient protClient;
	private SceneNode[] dronesN;
	private PhysicsObject[] drones;
	private int host;
	
	public NPCController(MyGame g, ProtocolClient pc, SceneNode[] dN, PhysicsObject[] d, int hostStatus) {
		game = g;
		protClient = pc;
		dronesN = dN;
		drones = d;
		host = hostStatus;
		if(host < 1) {
			startHost();
		}
		else {
			startClient();
		}
		
		System.out.println("NPCController created.");
	}
    
	public void updateNPCs() {
	    for(int i = 0; i < NPClist.length; i++) {
	    	if(NPClist[i] != null) {
	    		NPClist[i].updateNPCLocation();
	    		setupBehaviorTree(NPClist[i], bt[i]);
	    	}
	    }
    }
   
    public void startHost() {
	    thinkStartTime = System.nanoTime();
	    tickStateTime = System.nanoTime();
	    lastThinkUpdateTime = thinkStartTime;
	    lastTickUpdateTime = tickStateTime;
	    setupNPC();
	    
	    //npcLoop();
    }
    
    public void startClient() {
    	setupNPC();
    }
    

   
    public void setupNPC() {
	    for(int i = 0; i < NPClist.length; i++) {
	    	if(dronesN[i] != null) {
	    		NPClist[i] = new NPC(dronesN[i], drones[i], i);
	    	}
	    }
    }
    
   public NPC getNPC(int i) {
	   return NPClist[i];
   }
   
    public void npcLoop() {
	    
 	    long currentTime = System.nanoTime();
	    float elapsedThinkMilliSecs = (currentTime - lastThinkUpdateTime)/(1000000.0f);
	    float elapsedTickMilliSecs = (currentTime - lastTickUpdateTime)/(1000000.0f);
	   
	    if(elapsedTickMilliSecs >= 50.0f) { //TICK
		    lastTickUpdateTime = currentTime;
		    updateNPCs();
		    //protClient.sendNPCInfo
	    }
	    if(elapsedThinkMilliSecs >= 500.0f) { //THINK
		    lastThinkUpdateTime = currentTime;
		    bt[0].update(game.getElapsTime());
		    //bt[1].update(game.getElapsTime());
		    //bt[2].update(game.getElapsTime());
	    }
	    Thread.yield();
    }
   
    public void setupBehaviorTree(NPC n, BehaviorTree bt) {
    	
	    bt.insertAtRoot(new BTSequence(10));
	    bt.insertAtRoot(new BTSequence(20));
	    bt.insert(10, new CharacterFar(n, false));
	    bt.insert(10, new MoveHome(n));
	    bt.insert(20, new CharacterNear(n, false));
	    bt.insert(20, new ChaseCharacter(n));
	    
	    //bt.insert(10, new OneSecPassed(this, npc, false))
    }
    
    public class NPC {
 	   double[] transform;
 	   Vector3 origin;
 	   private SceneNode target;
 	   private PhysicsObject physObj;
 	   private Vector3 objective;
 	   int npcNum;
 	   private boolean headingHome;
 	   private boolean activated;
 	   
 	   public NPC(SceneNode sn, PhysicsObject po, int n) {
 		   npcNum = n;
 		   target = sn;
 		   physObj = po;
 		   origin = Vector3f.createFrom(target.getLocalPosition().x() + 1.0f, target.getLocalPosition().y() - 5.0f, target.getLocalPosition().z() + 1);
 		   transform = physObj.getTransform();
 		   objective = origin;
 		   //physObj.setTransform(transform);
 	   }
 	   
 	   
 	   public double[] getNPCTransform() {
 		   return transform;
 	   }
 	   public Vector3 getNPCLocation() {
 		   return target.getLocalPosition();
 	   }
 	   public SceneNode getNode() {
 		   return target;
 	   }
 	   
 	   public float distanceToHome() {
 		   return game.distanceFrom(target.getLocalPosition(), origin);
 	   }
 	   public void setNPCTransform(double[] loc) {
 		   transform = loc;
 		   physObj.setTransform(transform);
 	   }
 	   
 	   public void updateNPCLocation() {
 		   
 		   if(game.distanceFrom(getNPCLocation(), objective) > 7.0f) {
 			  target.lookAt(objective.x(), objective.y() - 2.5f, objective.z());
 			   float[] targ = {target.getLocalForwardAxis().x() * 12.0f, 0.0f, target.getLocalForwardAxis().z() * 12.0f};
 			   physObj.setLinearVelocity(targ);
 		   }
 		   else if(!headingHome){
 			   //game.damagePlayer();
 		   }
		   transform = physObj.getTransform();
		   protClient.sendNPCMessage(transform, npcNum, objective);
 	   }
 	   
 	   public void chase() {
 		  Vector3 npcVec = getNPCLocation();
		   SceneNode[] characters = game.getCharacters();
 		  float dist = 40.0f;
 		  SceneNode chaser = characters[0];
		   

		   for(int i = 0; i < 2; i++) {
			   if(characters[i] != null) {
				   Vector3 vec = characters[i].getLocalPosition();
				   float temp = game.distanceFrom((Vector3f) npcVec, (Vector3f) vec);
				   if(temp < dist) {
					   chaser = characters[i];
					   dist = temp;
				   }
			   }
		   }
		   
		   objective = chaser.getLocalPosition();
		   headingHome = false;
		   activated = true;
		   
		   
 	   }
 	   
 	   public void home() {
 		   objective = origin;
 		   headingHome = true;
 	   }
    }   
    
    public class CharacterNear extends BTCondition {
 	   private NPC npc;
 	   
 	   public CharacterNear(NPC n, boolean toNegate) {
 		   super(toNegate);
 		   npc = n;
 	   }
 	   protected boolean check() {
 		   //if there is a player nearby, return true
 		   boolean ret = false;
 		   
 		   Vector3 npcVec = npc.getNPCLocation();
 		   
 		   SceneNode[] characters = game.getCharacters();
 		   for(int i = 0; i < characters.length; i++) {
 			   if(characters[i] != null) {
	 			   Vector3 vec = characters[i].getLocalPosition();
	 			   float dist = game.distanceFrom((Vector3f) npcVec, (Vector3f) vec);
	 			   float distHome = npc.distanceToHome();
	 			   if(dist < 40.0f && distHome < 60.0f) {
	 				   ret = true;
	 			   }
 			   }
 		   }
 		   return ret;
 	   }
    }
    
    public class CharacterFar extends BTCondition {
    	private NPCController npcc;
  	   private NPC npc;
  	   
  	   public CharacterFar(NPC n, boolean toNegate) {
  		   super(toNegate);
  		   npc = n;
  	   }
  	   protected boolean check() {
  		   //if there is a player nearby, return true
  		   boolean ret = false;
  		   
  		   Vector3 npcVec = npc.getNPCLocation();
  		   
  		   SceneNode[] characters = game.getCharacters();
  		   for(int i = 0; i < characters.length; i++) {
  			   if(characters[i] != null) {
	  			   Vector3 vec = characters[i].getLocalPosition();
	  			   float dist = game.distanceFrom((Vector3f) npcVec, (Vector3f) vec);
	  			   float distHome = npc.distanceToHome();
	  			   if(dist > 40.0f || distHome > 60.0f) {
	  				   ret = true;
	  			   }
  			   }
  		   }
  		   return ret;
  	   }
    }
    
    public class ChaseCharacter extends BTAction {
    	private NPC npc;
    	
    	public ChaseCharacter(NPC n) {
    		npc = n;
    	}
    	
    	protected BTStatus update(float elapsedTime) {
    		npc.chase();
    		return BTStatus.BH_SUCCESS;
    	}
    }
    
    public class MoveHome extends BTAction {
    	private NPC npc;
    	
    	public MoveHome (NPC n) {
    		npc = n;
    	}
    	
    	protected BTStatus update(float elapsedTime) {
    		npc.home();
    		return BTStatus.BH_SUCCESS;
    	}
    }
}

