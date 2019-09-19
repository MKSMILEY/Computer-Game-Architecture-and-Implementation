package myGameEngine;

import java.io.IOException;
import ray.networking.IGameConnection.ProtocolType;
import java.net.InetAddress;
import java.util.UUID;

import ray.networking.server.GameConnectionServer;
import ray.networking.server.IClientInfo;

public class NetworkingServer {
	private GameServerUDP thisUDPServer;
	//private NPCcontroller npcCtrl;
	
	public NetworkingServer(int serverPort, String protocol) {
		try {
			thisUDPServer = new GameServerUDP(serverPort);
		}
		catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		if(args.length > 1) {
			NetworkingServer app = new NetworkingServer(Integer.parseInt(args[0]), args[1]);
		}
	}
	


}
