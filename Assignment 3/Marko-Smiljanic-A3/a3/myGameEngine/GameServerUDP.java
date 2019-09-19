package myGameEngine;

import java.io.IOException;
import java.net.InetAddress;
import java.util.UUID;

import ray.networking.server.GameConnectionServer;
import ray.networking.server.IClientInfo;

public class GameServerUDP extends GameConnectionServer<UUID> {
	
	int clientNum = 0;
	public GameServerUDP(int localPort) throws IOException {
		super(localPort, ProtocolType.UDP);
		System.out.println("UDP Server has been created.");
		System.out.println("Server Address: " + this.getLocalInetAddress());
		System.out.println("Server Port: " + localPort);
	}
	
	@Override
	public void processPacket(Object o, InetAddress senderIP, int sndPort) {
		String message = (String) o;
		String[] msgTokens = message.split(",");
		
		if(msgTokens.length > 0) {
			//case where server receives a JOIN message
			//format: join,localid
			if(msgTokens[0].compareTo("join") == 0) {
				try {
					IClientInfo ci;
					ci = getServerSocket().createClientInfo(senderIP, sndPort);
					UUID clientID = UUID.fromString(msgTokens[1]);
					addClient(ci, clientID);
					sendJoinedMessage(clientID, true, clientNum++);
					System.out.println(clientID + " has joined the server as client " + clientNum + ".");
				}
				catch(IOException e) {
					e.printStackTrace();
				}
			}
			
			//case where server recieves a CREATE message
			//format: create,localid,x,y,z
			if(msgTokens[0].compareTo("create")==0) {
				UUID clientID = UUID.fromString(msgTokens[1]);
				String[] pos = {msgTokens[2], msgTokens[3], msgTokens[4], msgTokens[5],
						msgTokens[6], msgTokens[7], msgTokens[8], msgTokens[9],
						msgTokens[10], msgTokens[11], msgTokens[12], msgTokens[13],
						msgTokens[14], msgTokens[15], msgTokens[16], msgTokens[17]};
				String skin = msgTokens[18];
				sendCreateMessages(clientID, pos, skin);
				sendWantsDetailsMessages(clientID);
			}
			
			//case where server receives a BYE message
			//format: bye,localid
			if(msgTokens[0].compareTo("bye")==0) {
				UUID clientID = UUID.fromString(msgTokens[1]);
				System.out.println(clientID + " has left the server.");
				removeClient(clientID);
				sendByeMessage(clientID);
				clientNum--;
			}
			
			//case where server receives a DETAILS-FOR message
			//format: details,localid
			if(msgTokens[0].compareTo("details")==0) {
				UUID clientID = UUID.fromString(msgTokens[1]);
				UUID remID = UUID.fromString(msgTokens[2]);
				String[] pos = {msgTokens[3], msgTokens[4], msgTokens[5], msgTokens[6],
						msgTokens[7], msgTokens[8], msgTokens[9], msgTokens[10],
						msgTokens[11], msgTokens[12], msgTokens[13], msgTokens[14],
						msgTokens[15], msgTokens[16], msgTokens[17], msgTokens[18]};
				String skin = msgTokens[19];
				sendDetailsMessage(clientID, remID, pos, skin);
			}
			//case where server receives a MOVE message
			//format: details,localid,x,y,z
			if(msgTokens[0].compareTo("move")==0) {
				UUID clientID = UUID.fromString(msgTokens[1]);
				String[] pos = {msgTokens[2], msgTokens[3], msgTokens[4], msgTokens[5],
						msgTokens[6], msgTokens[7], msgTokens[8], msgTokens[9],
						msgTokens[10], msgTokens[11], msgTokens[12], msgTokens[13],
						msgTokens[14], msgTokens[15], msgTokens[16], msgTokens[17]};
				sendMoveMessages(clientID, pos);
			}
			//case where server receives a NPC message
			//format: npc,npcNum,transform
			if(msgTokens[0].compareTo("npc")==0) {
				UUID clientID = UUID.fromString(msgTokens[1]);
				int npcNum = Integer.parseInt(msgTokens[2]);
				String[] pos = {msgTokens[3], msgTokens[4], msgTokens[5], msgTokens[6],
						msgTokens[7], msgTokens[8], msgTokens[9], msgTokens[10],
						msgTokens[11], msgTokens[12], msgTokens[13], msgTokens[14],
						msgTokens[15], msgTokens[16], msgTokens[17], msgTokens[18]};
				String[] vec = {msgTokens[19], msgTokens[20], msgTokens[21]}; 
				sendNPCMessages(clientID, npcNum, pos, vec);
			}
			if(msgTokens[0].compareTo("button") ==0) {
				UUID clientID = UUID.fromString(msgTokens[1]);
				sendButton(clientID);
			}
		}
	}
	
	public void sendJoinedMessage(UUID clientID, boolean success, int clientNum) {
		//format: join,success or join,failure
		try {
			String message = new String("join,");
			if(success) {
				message += "success," + clientNum;
			}
			else {
				message += "failure," + clientNum;
			}
			sendPacket(message, clientID);
		}
		catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	public void sendCreateMessages(UUID clientID, String[] pos, String skin) {
		//format: create,remoteid,x,y,z
		try {
			String message = new String("create," + clientID.toString()+
					"," + pos[0] + "," + pos[1] + "," + pos[2] + "," + pos[3] + 
					   "," + pos[4] + "," + pos[5] + "," + pos[6] + "," + pos[7] + 
					   "," + pos[8] + "," + pos[9] + "," + pos[10] + "," + pos[11] +
					   "," + pos[12] + "," + pos[13] + "," + pos[14] + "," + pos[15] +
					   "," + skin);
			forwardPacketToAll(message, clientID);
		}
		catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	public void sendDetailsMessage(UUID clientID, UUID remoteID, String[] pos, String skin) {
		try {
			String message = new String("details," + clientID.toString() +
					"," + pos[0] + "," + pos[1] + "," + pos[2] + "," + pos[3] + 
					   "," + pos[4] + "," + pos[5] + "," + pos[6] + "," + pos[7] + 
					   "," + pos[8] + "," + pos[9] + "," + pos[10] + "," + pos[11] +
					   "," + pos[12] + "," + pos[13] + "," + pos[14] + "," + pos[15] + 
					   "," + skin);
			sendPacket(message, remoteID);
		}
		catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	public void sendWantsDetailsMessages(UUID clientID) {
		// etc...
		try {
			String message = new String("wants," + clientID.toString());
			forwardPacketToAll(message, clientID);
		}
		catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	public void sendMoveMessages(UUID clientID, String[] pos) {
		// etc...
		try {
			String message = new String("move," + clientID.toString() + 
						"," + pos[0] + "," + pos[1] + "," + pos[2] + "," + pos[3] + 
					   "," + pos[4] + "," + pos[5] + "," + pos[6] + "," + pos[7] + 
					   "," + pos[8] + "," + pos[9] + "," + pos[10] + "," + pos[11] +
					   "," + pos[12] + "," + pos[13] + "," + pos[14] + "," + pos[15]);
			forwardPacketToAll(message, clientID);
		}
		catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	public void sendByeMessage(UUID clientID) {
		// etc...
		try {
			String message = new String("bye," + clientID.toString());
			forwardPacketToAll(message,clientID);
		}
		catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	public void sendNPCMessages(UUID clientID, int npcNum, String[] pos, String[] vec) {
		try {
			String message = new String("npc," + clientID.toString() + "," + npcNum + 
					"," + pos[0] + "," + pos[1] + "," + pos[2] + "," + pos[3] + 
					   "," + pos[4] + "," + pos[5] + "," + pos[6] + "," + pos[7] + 
					   "," + pos[8] + "," + pos[9] + "," + pos[10] + "," + pos[11] +
					   "," + pos[12] + "," + pos[13] + "," + pos[14] + "," + pos[15] +
					   "," + vec[0] + "," + vec[1] + "," + vec[2]);
			forwardPacketToAll(message, clientID);
		}
		catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	public void sendButton(UUID clientID) {
		try {
			String message = new String("button," + clientID.toString());
			forwardPacketToAll(message, clientID);
		}
		catch(IOException e) {
			e.printStackTrace();
		}
	}
}


