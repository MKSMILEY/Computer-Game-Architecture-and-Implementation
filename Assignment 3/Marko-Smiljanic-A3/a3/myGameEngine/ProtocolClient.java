package myGameEngine;

import java.io.IOException;
import java.net.InetAddress;
import java.util.UUID;
import java.util.Vector;

import javax.vecmath.Vector3d;

import ray.networking.client.GameConnectionClient;
import ray.rage.scene.*;
import ray.rml.*;

import a3.*;

public class ProtocolClient extends GameConnectionClient {
	private MyGame game;
	private UUID id;
	private Vector<GhostAvatar> ghostAvatars;
	
	public ProtocolClient(InetAddress remAddr, int remPort, ProtocolType pType, MyGame game) throws IOException {
		super(remAddr, remPort, pType);
		this.game = game;
		this.id = UUID.randomUUID();
		this.ghostAvatars = new Vector<GhostAvatar>();
		System.out.println("Protocol client created for " + id);
	}
	
	@Override
	protected void processPacket(Object o) {
		String message = (String) o;
		String[] msgTokens = message.split(",");
		
		if(msgTokens.length > 0) {
			if(msgTokens[0].compareTo("join")==0) { //receive "join"
				//format: join,success or join,failure
				if(msgTokens[1].compareTo("success")==0) {
					game.setIsConnected(true, Integer.parseInt(msgTokens[2]));
					sendCreateMessage(game.getPlayerPosition());
				}
				if(msgTokens[1].compareTo("failure")==0) {
					game.setIsConnected(false, Integer.parseInt(msgTokens[2]));
				}
			}
			
			if(msgTokens[0].compareTo("bye")==0) { //receive "bye"
				//format: bye,remoteID
				UUID ghostID = UUID.fromString(msgTokens[1]);
				game.removeGhostAvatarFromGameWorld(ghostID);
				System.out.println("bye message recieved from " + ghostID);
			}
			
			if((msgTokens[0].compareTo("details")==0) || msgTokens[0].compareTo("create")==0) { //receive "dsfr" or "create"
				//format: create,remoteID,x,y,z or details,remoteID,x,y,z
				UUID ghostID = UUID.fromString(msgTokens[1]);
				float[] ghostPosition = {Float.parseFloat(msgTokens[2]), Float.parseFloat(msgTokens[3]), Float.parseFloat(msgTokens[4]), Float.parseFloat(msgTokens[5]),
						Float.parseFloat(msgTokens[6]), Float.parseFloat(msgTokens[7]), Float.parseFloat(msgTokens[8]), Float.parseFloat(msgTokens[9]),
						Float.parseFloat(msgTokens[10]), Float.parseFloat(msgTokens[11]), Float.parseFloat(msgTokens[12]), Float.parseFloat(msgTokens[13]),
						Float.parseFloat(msgTokens[14]), Float.parseFloat(msgTokens[15]), Float.parseFloat(msgTokens[16]), Float.parseFloat(msgTokens[17])};
				String skin = msgTokens[18];
				try {
					game.addGhostAvatarToGameWorld(ghostID, skin);
				}
				catch(IOException e) {
					System.out.println("error creating ghost avatar");
				}
			}
			
			if(msgTokens[0].compareTo("wants")==0) { //receive "wants"
				//format: wants
				UUID wantsID = UUID.fromString(msgTokens[1]);
				sendDetailsForMessage(wantsID, game.getPlayerPosition());
			}
			if(msgTokens[0].compareTo("move")==0) {
				//receive "move"
				UUID ghostID = UUID.fromString(msgTokens[1]);
				float[] ghostPosition = {Float.parseFloat(msgTokens[2]), Float.parseFloat(msgTokens[3]), Float.parseFloat(msgTokens[4]), Float.parseFloat(msgTokens[5]),
						Float.parseFloat(msgTokens[6]), Float.parseFloat(msgTokens[7]), Float.parseFloat(msgTokens[8]), Float.parseFloat(msgTokens[9]),
						Float.parseFloat(msgTokens[10]), Float.parseFloat(msgTokens[11]), Float.parseFloat(msgTokens[12]), Float.parseFloat(msgTokens[13]),
						Float.parseFloat(msgTokens[14]), Float.parseFloat(msgTokens[15]), Float.parseFloat(msgTokens[16]), Float.parseFloat(msgTokens[17])};
				game.moveGhostAvatar(ghostID, ghostPosition);
				
			}
			if(msgTokens[0].compareTo("npc")==0) {
				//receive "npc"
				UUID ghostID = UUID.fromString(msgTokens[1]);
				int npcNum = Integer.parseInt(msgTokens[2]);
				float[] npcPosition = {Float.parseFloat(msgTokens[3]), Float.parseFloat(msgTokens[4]), Float.parseFloat(msgTokens[5]), Float.parseFloat(msgTokens[6]),
						Float.parseFloat(msgTokens[7]), Float.parseFloat(msgTokens[8]), Float.parseFloat(msgTokens[9]), Float.parseFloat(msgTokens[10]),
						Float.parseFloat(msgTokens[11]), Float.parseFloat(msgTokens[12]), Float.parseFloat(msgTokens[13]), Float.parseFloat(msgTokens[14]),
						Float.parseFloat(msgTokens[15]), Float.parseFloat(msgTokens[16]), Float.parseFloat(msgTokens[17]), Float.parseFloat(msgTokens[18])};
				Vector3 vec = Vector3f.createFrom(Float.parseFloat(msgTokens[19]), Float.parseFloat(msgTokens[20]), Float.parseFloat(msgTokens[21]));
				game.moveNPCs(npcNum, npcPosition, vec);
			}
			
			if(msgTokens[0].compareTo("button") == 0) {
				game.flipButton();
			}
		}
	}
	
	public void sendJoinedMessage() {
		try {
			sendPacket(new String("join," + id.toString()));
		}
		catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	public void sendCreateMessage(double[] pos) {
		//format: create,localID,transform
		try {
			String message = new String("create," + id.toString());
			message += "," + pos[0] + "," + pos[1] + "," + pos[2] + "," + pos[3] + 
					   "," + pos[4] + "," + pos[5] + "," + pos[6] + "," + pos[7] + 
					   "," + pos[8] + "," + pos[9] + "," + pos[10] + "," + pos[11] +
					   "," + pos[12] + "," + pos[13] + "," + pos[14] + "," + pos[15];
			message += "," + game.getSkin();
			sendPacket(message);
		}
		catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	public void sendByeMessage() {
		//format: bye,localID
		try {
			String message = new String("bye," + id.toString());
			sendPacket(message);
		}
		catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	public void sendDetailsForMessage(UUID remID, double[] pos) {
		//format: details,localID,remoteID,transform
		try {
			String message = new String("details," + id.toString() + "," + remID.toString());
			message += "," + pos[0] + "," + pos[1] + "," + pos[2] + "," + pos[3] + 
					   "," + pos[4] + "," + pos[5] + "," + pos[6] + "," + pos[7] + 
					   "," + pos[8] + "," + pos[9] + "," + pos[10] + "," + pos[11] +
					   "," + pos[12] + "," + pos[13] + "," + pos[14] + "," + pos[15] +
					   "," + game.getSkin();
			sendPacket(message);
		}
		catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	public void sendMoveMessage(double[] pos) {
		//format: move,localID,transform
		try {
			String message = new String("move," + id.toString());
			message += "," + pos[0] + "," + pos[1] + "," + pos[2] + "," + pos[3] + 
					   "," + pos[4] + "," + pos[5] + "," + pos[6] + "," + pos[7] + 
					   "," + pos[8] + "," + pos[9] + "," + pos[10] + "," + pos[11] +
					   "," + pos[12] + "," + pos[13] + "," + pos[14] + "," + pos[15];
			sendPacket(message);
		}
		catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	public void sendNPCMessage(double[] pos, int npcNum, Vector3 objective) {
		//format: npc,clientID,npcNum,transform
		try {
			String message = new String("npc," + id.toString()+ ","+ npcNum);
			message += "," + pos[0] + "," + pos[1] + "," + pos[2] + "," + pos[3] + 
					   "," + pos[4] + "," + pos[5] + "," + pos[6] + "," + pos[7] + 
					   "," + pos[8] + "," + pos[9] + "," + pos[10] + "," + pos[11] +
					   "," + pos[12] + "," + pos[13] + "," + pos[14] + "," + pos[15];
			message += "," + objective.x() + "," + objective.y() + "," + objective.z();
			sendPacket(message);
		}
		catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	public void sendButton() {
		//format: button,clientID
		try {
			String message = new String("button," + id.toString());
			sendPacket(message);
		}
		catch(IOException e) {
			e.printStackTrace();
		}
	}
	
}

