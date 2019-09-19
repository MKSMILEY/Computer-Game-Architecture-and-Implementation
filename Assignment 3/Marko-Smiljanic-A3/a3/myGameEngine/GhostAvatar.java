package myGameEngine;

import java.util.UUID;

import ray.rage.scene.*;
import ray.rml.*;


public class GhostAvatar {
	private UUID id;
	private SceneNode node;
	private Entity entity;
	
	public GhostAvatar(UUID id, Vector3 position) {
		this.id = id;
	}
	
	public UUID getID() {
		return id;
	}
	
	public SceneNode getNode() {
		return node;
	}
	
	public Entity getEntity() {
		return entity;
	}
	
	public void setID(UUID id) {
		this.id = id;
	}
	
	public void setNode(SceneNode node) {
		this.node = node;
	}
	
	public void setEntity(Entity entity) {
		this.entity = entity;
	}
}
