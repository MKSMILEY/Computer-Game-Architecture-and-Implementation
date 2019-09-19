package a3;

//default imports
import java.awt.*;
import java.io.*;
import java.net.InetAddress;
import java.rmi.UnknownHostException;
import java.util.ArrayList;


import net.java.games.input.Controller;
import ray.rage.*;
import ray.rage.game.*;
import ray.rage.rendersystem.*;
import ray.rage.rendersystem.Renderable.*;
import ray.rage.scene.*;
import ray.rage.scene.Camera.Frustum.*;
import ray.rage.scene.controllers.*;
import ray.rage.util.BufferUtil;
import ray.rml.*;
import ray.rage.rendersystem.gl4.GL4RenderSystem;
import ray.rage.rendersystem.shader.GpuShaderProgram;

//imports for inputManager
import ray.rage.rendersystem.states.*;
import ray.rage.asset.material.Material;
import ray.rage.asset.texture.*;
import ray.ai.behaviortrees.BTCompositeType;
import ray.input.*;
import ray.input.action.*;

//imports for Skybox
import ray.rage.rendersystem.states.*;
import ray.rage.asset.texture.*;
import ray.rage.util.*;
import java.awt.geom.*;

//imports for scripting
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.util.*;

//imports for networking
import ray.networking.IGameConnection.ProtocolType;

//imports for animation
import static ray.rage.scene.SkeletalEntity.EndType.*;

//imports for physics
import ray.physics.PhysicsEngine;
import ray.physics.PhysicsObject;
import ray.physics.PhysicsEngineFactory;

//imports for Mouse control
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseListener;
import java.awt.event.MouseEvent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import ray.audio.*;

import myGameEngine.*;


public class MyGame extends VariableFrameRateGame implements MouseListener, MouseMotionListener{
	
	GL4RenderSystem rs;
	float elapsTime = 0.0f;
	String elapsTimeStr, counterStr, dispStr;
	int elapsTimeSec = 0;
	
	//game actions
	private InputManager im;
	private SceneManager sm;
	
	private Camera camera1;
	private SceneNode camera1N;
	
	private SceneNode manChar;
	
	private SceneNode planets;
	
	private SceneNode moonN;
	
	private double moonS;
	
	private Robot robot;
	private RenderWindow rw;
	private float prevMouseX, prevMouseY, curMouseX, curMouseY;
	private int centerX, centerY;
	private boolean isRecentering;
	
	private int dolphin1S = 0;
	
	//Node Controllers
	
	private Controller gamepad, keyboard, mouse = null;
	private boolean padFound = false;
	
	//Added in assignment 2
	private Camera1Pcontroller cameraController1;
	private Camera1Pcontroller cameraController2;
	
	//Added in assignment 3
	private static final String SKYBOX_NAME = "SkyBox";
	private boolean skyBoxVisible = true;
	
	static ScriptEngineManager factory;
    static ScriptEngine jsEngine;
    
    File scriptFile1 = new File("Parameters.js");
    
    private String serverAddress;
    private int serverPort;
    private ProtocolType serverProtocol;
    private static ProtocolClient protClient;
    private boolean isClientConnected;
    private Vector<UUID> gameObjectsToRemove;
    
    private GhostAvatar avatar;
    
    private SceneNode tessN;
    
    //Added in Milestone 3
    private PhysicsEngine physicsEng;
    private PhysicsObject character, gndPlaneP, ghost, moonPO;
    private PhysicsObject[] walls;
    private PhysicsObject[] drones;
    private SkeletalEntity manSE, ghostSE;
    
    private double sensitivity;
    private boolean mouseInit = false;
    
    private SceneNode[] dronesN;
    private SceneNode lights;
    
    private NPCController npcCon;
    
    private int hostStatus = 0;
    
    private static String skin;
    
    IAudioManager audioMgr;
    Sound robotSound;
    
    private SceneNode buttons;
    
    private float health = 100.0f;
    
    private boolean b1 = false;
    

    public MyGame(String serverAddr, int sPort, String protocol) {
        super();
        
        this.serverAddress = serverAddr;
        this.serverPort = sPort;
        if(protocol.toUpperCase().compareTo("UDP")==0) {
        	this.serverProtocol = ProtocolType.UDP;
        }
        

		
    }

    public static void main(String[] args) {
        BufferedReader reader = new BufferedReader (new InputStreamReader(System.in));
    	
    	System.out.println("Please Enter in the IP for the server: ");
		System.out.print("Enter 'S' for Scientist skin. Enter 'G' for Guard skin: ");
		try {
			skin = reader.readLine();
		}
		catch(IOException e) {
			e.printStackTrace();
		}
        Game game = new MyGame(args[0],Integer.parseInt(args[1]),args[2]);
        
        factory = new ScriptEngineManager();
        java.util.List<ScriptEngineFactory> list = factory.getEngineFactories();
        
        System.out.println("Script Engine Factories found: ");
        for(ScriptEngineFactory f : list) {
        	System.out.println("Name = " + f.getEngineName() + "; Language = " + f.getLanguageName() + "; extensions: " + f.getExtensions());
        }
        
        jsEngine = factory.getEngineByName("js");
        
        File file = new File(".");
        for(String fileNames : file.list()) System.out.println(fileNames);
        
        try {
            game.startup();
            game.run();
        } catch (Exception e) {
            e.printStackTrace(System.err);
        } finally {
        	protClient.sendByeMessage();
            game.shutdown();
            game.exit();
        }
    }
    
    private void setupNetworking() {
    	gameObjectsToRemove = new Vector<UUID>();
    	isClientConnected = false;
    	try {
    		protClient = new ProtocolClient(InetAddress.getByName(serverAddress), serverPort, serverProtocol, this);
    	}
    	catch(UnknownHostException e) {
    		e.printStackTrace();
    	}
    	catch(IOException e) {
    		e.printStackTrace();
    	}
    	
    	if(protClient == null) {
    		System.out.println("missing protocol host");
    	}
    	else {
    		//ask client protocol to send initial join message to server, with a uniqui identifier for this client
    		protClient.sendJoinedMessage();
    	}
    }
	
	@Override
	protected void setupWindow(RenderSystem rs, GraphicsEnvironment ge) {
		rw = rs.createRenderWindow(new DisplayMode(1000, 700, 24, 60), false);
	}
	
	protected void setupWindowViewports(RenderWindow rw) {
		this.rw.addKeyListener(this);
		
		Viewport viewport = rw.getViewport(0);
		viewport.setDimensions(0f, 0f, 1f, 1f);
		viewport.setClearColor(new Color(1.0f, .7f, .7f));
		
	}

    @Override
    protected void setupCameras(SceneManager sm, RenderWindow rw) {
    	this.sm = sm;
        SceneNode rootNode = sm.getRootSceneNode();
        camera1 = sm.createCamera("MainCamera", Projection.PERSPECTIVE);
        rw.getViewport(0).setCamera(camera1);
        camera1N = rootNode.createChildSceneNode("MainCameraNode");
        camera1N.attachObject(camera1);
        camera1.setMode('n');
        //camera1.getFrustum().setFarClipDistance(1000.0f);
        
        initMouseMode(rs, rw);
    }
	
    @Override
    protected void setupScene(Engine eng, SceneManager sm) throws IOException {
    	setupInputs();
    	setupNetworking();
    	
        this.executeScript(jsEngine, "Parameters.js");    	
        
        sensitivity = (double) jsEngine.get("sensitivity");
    	
    	buttons = sm.getRootSceneNode().createChildSceneNode("mybuttonsNodeGroup");
    	
    	lights = sm.getRootSceneNode().createChildSceneNode("myLightsNodeGroup");
    	
    	FrontFaceState cwfaceState = (FrontFaceState) sm.getRenderSystem().createRenderState(RenderState.Type.FRONT_FACE);
    	cwfaceState.setVertexWinding(FrontFaceState.VertexWinding.CLOCKWISE);
    	
        
        //Buttons--------------------------------------------------//
    	
        Entity button1E = sm.createEntity("button1", "cube.obj");
        button1E.setPrimitive(Primitive.TRIANGLES);
        
        Texture tex = eng.getTextureManager().getAssetByPath("chain-fence.jpeg");
        TextureState tstate = (TextureState) sm.getRenderSystem().createRenderState(RenderState.Type.TEXTURE);
        tstate.setTexture(tex);
        button1E.setRenderState(tstate);
        
        SceneNode button1 = buttons.createChildSceneNode(button1E.getName() + "Node");
        button1.setLocalPosition(20.0f, 0.0f, 20.0f);
        button1.scale(.5f, 4.0f, .5f);
        button1.attachObject(button1E);
        
        
        /*Entity button2E = sm.createEntity("button2", "cube.obj");
        button2E.setPrimitive(Primitive.TRIANGLES);
        
        tex = eng.getTextureManager().getAssetByPath("chain-fence.jpeg");
        tstate = (TextureState) sm.getRenderSystem().createRenderState(RenderState.Type.TEXTURE);
        tstate.setTexture(tex);
        button2E.setRenderState(tstate);
        
        SceneNode button2 = buttons.createChildSceneNode(button2E.getName() +" Node");
        button2.setLocalPosition(-30.0f, 0.0f, 40.0f);
        button2.scale(.5f, 4.0f, .5f);
        button2.attachObject(button2E);
        
        Entity button3E = sm.createEntity("button3", "cube.obj");
        button3E.setPrimitive(Primitive.TRIANGLES);
        
        tex = eng.getTextureManager().getAssetByPath("chain-fence.jpeg");
        tstate = (TextureState) sm.getRenderSystem().createRenderState(RenderState.Type.TEXTURE);
        tstate.setTexture(tex);
        button3E.setRenderState(tstate);
        
        SceneNode button3 = buttons.createChildSceneNode(button3E.getName() + "Node");
        button3.setLocalPosition(10.0f, 0.0f,-50.0f);
        button3.scale(.5f, 4.0f, .5f);
        button3.attachObject(button3E);*/
        
    	
    	//manChar----------------------------------------------//
        manSE = sm.createSkeletalEntity("manAv", "man_update.rkm", "man_update.rks");
        
        if(skin.equalsIgnoreCase("s")) {
        	tex = eng.getTextureManager().getAssetByPath("scientist.png");
        }
        else if(skin.equalsIgnoreCase("g")) {
        	tex = eng.getTextureManager().getAssetByPath("security.png");
        }
        else {
        	tex = eng.getTextureManager().getAssetByPath("manmade.png");
        }
        tstate = (TextureState) sm.getRenderSystem().createRenderState(RenderState.Type.TEXTURE);
    	tstate.setTexture(tex);
    	manSE.setRenderState(tstate);
    	
    	
        
        manChar = sm.getRootSceneNode().createChildSceneNode(manSE.getName() + "Node");
        //manChar.moveUp(2.0f);
        manChar.attachObject(manSE);
        //manChar.yaw(Degreef.createFrom(90.0f));
        //manChar.scale(0.1f, 0.1f, 0.1f);
        manChar.setLocalPosition(0.0f, 0.0f, 0.0f);
        
        manSE.loadAnimation("joggingAnimation", "jogging.rka");
        manSE.loadAnimation("standingAnimation", "standing.rka");
        stopJogging(manSE);
        
        
        //Drones-----------------------------------------------//
        
        drones = new PhysicsObject[5];
        dronesN = new SceneNode[5];
        Entity drone1E = sm.createEntity("drone1", "drone.obj");
        drone1E.setPrimitive(Primitive.TRIANGLES);
        
        tex = eng.getTextureManager().getAssetByPath("hexagons.jpeg");
        tstate = (TextureState) sm.getRenderSystem().createRenderState(RenderState.Type.TEXTURE);
        tstate.setTexture(tex);
        drone1E.setRenderState(tstate);
        
        
        dronesN[0] = sm.getRootSceneNode().createChildSceneNode(drone1E.getName() + "Node");
        dronesN[0].attachObject(drone1E);
        dronesN[0].setLocalPosition(20.0f, 0.0f, 20.0f);
        dronesN[0].scale(2.0f, 2.0f, 2.0f);
        
        /*Entity drone2E = sm.createEntity("drone2", "drone.obj");
        drone2E.setPrimitive(Primitive.TRIANGLES);
        
        tex = eng.getTextureManager().getAssetByPath("hexagons.jpeg");
        tstate = (TextureState) sm.getRenderSystem().createRenderState(RenderState.Type.TEXTURE);
        tstate.setTexture(tex);
        drone2E.setRenderState(tstate);
        
        
        dronesN[1] = sm.getRootSceneNode().createChildSceneNode(drone2E.getName() + "Node");
        dronesN[1].attachObject(drone2E);
        dronesN[1].setLocalPosition(-30.0f, -4.0f, 40.0f);
        dronesN[1].scale(2.0f, 2.0f, 2.0f);
        
        Entity drone3E = sm.createEntity("drone3", "drone.obj");
        drone3E.setPrimitive(Primitive.TRIANGLES);
        
        tex = eng.getTextureManager().getAssetByPath("hexagons.jpeg");
        tstate = (TextureState) sm.getRenderSystem().createRenderState(RenderState.Type.TEXTURE);
        tstate.setTexture(tex);
        drone3E.setRenderState(tstate);
        
        
        dronesN[2] = sm.getRootSceneNode().createChildSceneNode(drone3E.getName() + "Node");
        dronesN[2].attachObject(drone3E);
        dronesN[2].setLocalPosition(10.0f, -4.0f, -50.0f);
        dronesN[2].scale(2.0f, 2.0f, 2.0f);*/
        
        //Lights-----------------------------------------------//
        sm.getAmbientLight().setIntensity(new Color(.1f, .1f, .1f));
		
		Light plight = sm.createLight("sun", Light.Type.POINT);
		plight.setAmbient(new Color(.3f, .3f, .3f));
        plight.setDiffuse(new Color(.7f, .7f, .7f));
		plight.setSpecular(new Color(1.0f, 1.0f, 1.0f));
        plight.setRange(100f);
		
		SceneNode plightNode = sm.getRootSceneNode().createChildSceneNode("plightNode");
		plightNode.moveUp(10.0f);
        plightNode.attachObject(plight);
        
        Light b1light = sm.createLight("button1l", Light.Type.POINT);
        b1light.setDiffuse(new Color(.7f, 0.0f, 0.0f));
		b1light.setSpecular(new Color(1.0f, 0.0f, 0.0f));
		b1light.setRange(8.0f);
		
		SceneNode b1lightN = button1.createChildSceneNode(b1light.getName() + "Node");
		b1lightN.attachObject(b1light);
		
		
		/*Light b2light = sm.createLight("button2l", Light.Type.POINT);
        b2light.setDiffuse(new Color(.7f, 0.0f, 0.0f));
		b2light.setSpecular(new Color(1.0f, 0.0f, 0.0f));
		b2light.setRange(8.0f);
		
		SceneNode b2lightN = button2.createChildSceneNode(b2light.getName() + "Node");
		b2lightN.attachObject(b2light);
		
		Light b3light = sm.createLight("button3l", Light.Type.POINT);
        b3light.setDiffuse(new Color(.7f, 0.0f, 0.0f));
		b3light.setSpecular(new Color(1.0f, 0.0f, 0.0f));
		b3light.setRange(8.0f);
		
		SceneNode b3lightN = button1.createChildSceneNode(b3light.getName() + "Node");
		b3lightN.attachObject(b2light);*/
		
        
        
        //Terrain------------------------------------------------//
        Tessellation tessE = sm.createTessellation("tessE", 6);
        
        //tessE.setSubdivisions(8f);
        
        tessN = sm.getRootSceneNode().createChildSceneNode("tessN");
        tessN.attachObject(tessE);
        
        //tessN.scale(100, 1, 100);
        tessN.setLocalPosition(0, -2.0f, 0);
        tessE.setHeightMap(this.getEngine(), "height_map.jpg");
        tessE.setTexture(this.getEngine(), "chain-fence.jpeg");

        
        
        
        
    	//SkyBox-----------------------------------------------//
    	Configuration conf = eng.getConfiguration();
    	TextureManager tm = getEngine().getTextureManager();
    	tm.setBaseDirectoryPath(conf.valueOf("assets.skyboxes.path"));
    	Texture front = tm.getAssetByPath("lagoon_ft.jpg");
    	Texture back = tm.getAssetByPath("lagoon_bk.jpg");
    	Texture left = tm.getAssetByPath("lagoon_lf.jpg");
    	Texture right = tm.getAssetByPath("lagoon_rt.jpg");
    	Texture top = tm.getAssetByPath("lagoon_up.jpg");
    	Texture bottom = tm.getAssetByPath("lagoon_dn.jpg");
    	tm.setBaseDirectoryPath(conf.valueOf("assets.textures.path"));
    	
    	AffineTransform xform = new AffineTransform();
    	xform.translate(0, front.getImage().getHeight());
    	xform.scale(1d, -1d);
    	
    	front.transform(xform);
    	back.transform(xform);
    	left.transform(xform);
    	right.transform(xform);
    	top.transform(xform);
    	bottom.transform(xform);
    	
    	SkyBox sb = sm.createSkyBox(SKYBOX_NAME);
    	sb.setTexture(front, SkyBox.Face.FRONT);
    	sb.setTexture(back, SkyBox.Face.BACK);
    	sb.setTexture(left, SkyBox.Face.LEFT);
    	sb.setTexture(right, SkyBox.Face.RIGHT);
    	sb.setTexture(top, SkyBox.Face.TOP);
    	sb.setTexture(bottom, SkyBox.Face.BOTTOM);
    	sm.setActiveSkyBox(sb);
    	
    	initPhysicsSystem();
    	createPhysicsWorld();
    	//initAudio();
    	
    	//Controllers----------------------------------------------//
    	
    	if(padFound) {
    		cameraController2 = new Camera1Pcontroller(this, camera1, manChar, manSE, gamepad, im, protClient);
    	}
    	else {
    		//System.out.println("keyboard for controller: " + keyboard.getName());
    		cameraController1 = new Camera1Pcontroller(this, camera1, manChar, manSE, keyboard, mouse, im, protClient);
    	}
    	
    	
    	
    }
    
    protected void setupInputs() {
    	im = new GenericInputManager();
    	
    	ArrayList<Controller> controllers = im.getControllers();
    	for (Controller c : controllers) {
    		if(c.getType() == Controller.Type.KEYBOARD) { 
    			keyboard = c;
    			System.out.println(c.getName());
    			
    		}
    		else if(c.getType() == Controller.Type.MOUSE) {
    			mouse = c;
    			System.out.println(c.getName());
    		}
    		else if(c.getType() == Controller.Type.GAMEPAD || c.getType() == Controller.Type.STICK) {
    			gamepad = c;
    			padFound = true;
    			System.out.println(c.getName());
    		}
    	}
    }

    @Override
    protected void update(Engine engine) {
		// build and set HUD
		rs = (GL4RenderSystem) engine.getRenderSystem();
		float time = engine.getElapsedTimeMillis();
		elapsTime += engine.getElapsedTimeMillis();
		elapsTimeSec = Math.round(elapsTime/1000.0f);
		elapsTimeStr = Integer.toString(elapsTimeSec);
		
		int vpHeight = rs.getRenderWindow().getViewport(0).getActualHeight();
		int vpWidth = rs.getRenderWindow().getViewport(0).getActualWidth();
    	
		dispStr = "Time = " + elapsTimeStr;
		dispStr += "    Health: " + health;
		
		rs.setHUD(dispStr, (int) (vpWidth * 0.05), (int) (vpHeight * 0.05));
    	
		
		//processNetworking  
		processNetworking(elapsTime);
		
		
		//tell the input manager to process the inputs
		im.update(elapsTime);
		if(health > 0) {
			if(gamepad != null) {
				cameraController2.update();
			}
			else {
				cameraController1.update();
			}
		}
		if(hostStatus == 0) {
			npcCon = new NPCController(this, protClient, dronesN, drones, hostStatus);
	    	System.out.println("npcCon created as: " + hostStatus);
	    	hostStatus = -1;
		}
		if(hostStatus == -1) {
			npcCon.npcLoop();
		}
		
		manSE.update();
		
		Tessellation tessE = ((Tessellation) tessN.getAttachedObject("tessE"));
	    

		float[] loc = {0.0f, 0.0f, 0.0f};
		float[] loc1 = {0.0f, 0.0f, 0.0f};
		Matrix4 mat;
		physicsEng.update(time);
		for(SceneNode s : engine.getSceneManager().getSceneNodes()) {
			if(s.getPhysicsObject() != null) {
				mat = Matrix4f.createFrom(toFloatArray(s.getPhysicsObject().getTransform()));
				
				//float height = tessE.getWorldHeight(s.getWorldPosition().x(), s.getWorldPosition().z());
				
				loc[0] = mat.value(0, 3);
				loc[1] = mat.value(1, 3);
				loc[2] = mat.value(2, 3);
				
				if(s.getName().equalsIgnoreCase("manAvNode")) {
					loc[1] += 5.5f;
				}
				else if(avatar != null) {
					if(s.getName().equalsIgnoreCase(avatar.getID().toString())) {
						loc[1] += 5.5f;
					}
				}
				else if(s.getName().contains("drone")) {
					loc[1]  += 1.0f;
				}
				
				s.setLocalPosition(loc[0], loc[1], loc[2]);
			}
		}
		//manChar.setLocalPosition(force[0], force[1], force[2]);
		dispStr = "";
		if(distanceFrom(manChar.getLocalPosition(), this.getEngine().getSceneManager().getSceneNode("button1Node").getLocalPosition()) < 10.0f) {
			dispStr = "Press E to hit the button!";
    	
		}
		rs.setHUD2(dispStr, (int) (vpWidth * 0.5), (int) (vpHeight * 0.3));
		mouseInit = true;
		
		damagePlayer();
		
		if(b1) {
			Light light =  this.getEngine().getSceneManager().getLight("button1l");
			   light.setDiffuse(new Color(0.0f, 0.7f, 0.7f));
			   light.setSpecular(new Color(0.0f, 1.0f, 1.0f));
		}
		
		//robotSound.setLocation(dronesN[0].getWorldPosition());
		//setEarParameters(sm);
 	}
    
    private void initPhysicsSystem() {
    	String engine = "ray.physics.JBullet.JBulletPhysicsEngine";
    	float[] gravity = {0, -3.0f, 0};
    	physicsEng = PhysicsEngineFactory.createPhysicsEngine(engine);
    	physicsEng.initSystem();
    	physicsEng.setGravity(gravity);
    }
    
    private void createPhysicsWorld() {
    	float mass = 100.0f;
    	float mass2 = 1000.0f;
    	float up[] = {0, 1, 0};
    	float right[] = {-1, 0, 0};
    	float size[] = {1.0f, 15.0f, 10.0f};
    	double[] temptf;
    	
    	temptf = toDoubleArray(manChar.getLocalTransform().toFloatArray());
    	character = physicsEng.addSphereObject(physicsEng.nextUID(), mass, temptf, 2.0f);
    	
    	character.setSleepThresholds(0.0f, 0.0f);
    	character.setDamping(0.99f, 0.99f);
    	manChar.setPhysicsObject(character);
    	
    	
    	temptf = toDoubleArray(tessN.getLocalTransform().toFloatArray());
    	gndPlaneP = physicsEng.addStaticPlaneObject(physicsEng.nextUID(), temptf, up, 0.0f);
    	
    	tessN.setPhysicsObject(gndPlaneP);
    	tessN.scale(200.0f, 10.0f, 200.0f);
    	
    	
    	temptf = toDoubleArray(dronesN[0].getLocalTransform().toFloatArray());
    	drones[0] = physicsEng.addSphereObject(physicsEng.nextUID(), mass2, temptf, 2.0f);
    	
    	drones[0].setSleepThresholds(0.0f, 0.0f);
    	//drones[0].setDamping(0.5f, 0.5f);
    	
    	dronesN[0].setPhysicsObject(drones[0]);
    	
    	/*temptf = toDoubleArray(dronesN[1].getLocalTransform().toFloatArray());
    	drones[1] = physicsEng.addSphereObject(physicsEng.nextUID(), mass2, temptf, 2.0f);
    	
    	drones[1].setSleepThresholds(0.0f, 0.0f);
    	drones[1].setDamping(0.99f, 0.99f);
    	
    	dronesN[1].setPhysicsObject(drones[1]);
    	
    	temptf = toDoubleArray(dronesN[2].getLocalTransform().toFloatArray());
    	drones[2] = physicsEng.addSphereObject(physicsEng.nextUID(), mass2, temptf, 2.0f);
    	
    	drones[2].setSleepThresholds(0.0f, 0.0f);
    	drones[2].setDamping(0.99f, 0.99f);
    	
    	dronesN[2].setPhysicsObject(drones[2]);*/
    }
    
    private void initAudio() {
    	AudioResource resource;
    	audioMgr = AudioManagerFactory.createAudioManager("ray.audio.joal.JOALAudioManager");
    	if(!audioMgr.initialize()) {
    		System.out.println("Audio Manager failed to initialize!");
    		return;
    	}
    	resource = audioMgr.createAudioResource("370196__inspectorj__synth-gliss-a.wav", AudioResourceType.AUDIO_SAMPLE);
    	//"Synth Gliss, A.wav" by InspectorJ (www.jshaw.co.uk) of Freesound.org
    	
    	robotSound = new Sound(resource, SoundType.SOUND_EFFECT, 100, true);
    	robotSound.initialize(audioMgr);
    	robotSound.setMaxDistance(15.0f);
    	robotSound.setMinDistance(0.5f);
    	robotSound.setRollOff(5.0f);
    	
    	robotSound.play();
    	
    }
    
    private void setEarParameters(SceneManager sm) {
    	Vector3 avDir = manChar.getWorldForwardAxis();
    	
    	audioMgr.getEar().setLocation(manChar.getWorldPosition());
    	audioMgr.getEar().setOrientation(avDir, Vector3f.createFrom(0, 1, 0));
    }
    
    private void executeScript(ScriptEngine engine, String scriptFileName) {
    	try {
    		FileReader fileReader = new FileReader(scriptFileName);
    		engine.eval(fileReader);
    		fileReader.close();
    	}
    	catch(FileNotFoundException e1) {
    		System.out.println(scriptFileName + " not found " + e1);
    	}
    	catch(IOException e2) {
    		System.out.println("IO problemn with " + scriptFileName + e2);
    	}
    	catch(ScriptException e3) {
    		System.out.println("ScriptException in " + scriptFileName + e3);
    	}
    	catch(NullPointerException e4) {
    		System.out.println("NullPointerException in " + scriptFileName + e4);
    	}
    }
    
    protected void processNetworking(float elapsTime) {
    	//Process packets received by the client from the server
    	if(protClient != null) {
    		protClient.processPackets();
    	}
    	//remove ghost avatars for players who have left the game
    	Iterator<UUID> it = gameObjectsToRemove.iterator();
    	while(it.hasNext()) {
    		sm.destroySceneNode(it.next().toString());
    	}
    	gameObjectsToRemove.clear();
    }
    
    
    private void initMouseMode(RenderSystem r, RenderWindow w) {
    	Viewport v = rw.getViewport(0);
    	int left = rw.getLocationLeft();
    	int top = rw.getLocationTop();
    	int wid = v.getActualScissorWidth();
    	int hi = v.getActualScissorHeight();
    	centerX = left + wid/2;
    	centerY = top + hi/2;
    	isRecentering = false;
    	
    	try {
    		robot = new Robot();
    	} 
    	catch (AWTException ex) {
    		throw new RuntimeException("Couldn't create Robot!");
    	}
    	
    	
    	
    	recenterMouse();
    	prevMouseX = centerX;
    	prevMouseY = centerY;
    	
    	w.addMouseMotionListener(this);
    }
    
    public void mouseMoved(MouseEvent e) {
    	//If robot is recentering and the MouseEvent location is in the center,
    	//then this event was generated by the robot
    	if(mouseInit) {
	    	if(isRecentering && centerX == e.getXOnScreen() && centerY == e.getYOnScreen()) {
	    		isRecentering = false;
	    	}
	    	curMouseX = e.getXOnScreen();
	    	curMouseY = e.getYOnScreen();
	    	float mouseDeltaX = prevMouseX - curMouseX;
	    	float mouseDeltaY = prevMouseY - curMouseY;
	    	
	    	
	    	manChar.rotate(Degreef.createFrom((float) (mouseDeltaX * sensitivity)), Vector3f.createFrom(0.0f, 1.0f, 0.0f));
	    	camera1N.pitch(Degreef.createFrom((float) (-mouseDeltaY * sensitivity)));
	    	prevMouseX = curMouseX;
	    	prevMouseY = curMouseY;
	    	
	    	
	    	//tell the robot to put the cursor to the center (since the user has moved it)
	    	recenterMouse();
	    	prevMouseX = centerX;
	    	prevMouseY = centerY;
    	}
    }
    
    private void recenterMouse() {
    	//use the robot to move the mouse to the center point.
    	//Note that this generates one MouseEvent.
    	Viewport v = rw.getViewport(0);
    	int left = rw.getLocationLeft();
    	int top = rw.getLocationTop();
    	int wid = v.getActualScissorWidth();
    	int hi = v.getActualScissorHeight();
    	centerX = left + wid/2;
    	centerY = top + hi/2;
    	isRecentering = true;
    	
    	//Canvas canvas = rs.getCanvas();
    	robot.mouseMove((int)centerX, (int)centerY);
    }
    
    public double[] getPlayerPosition() {
    	double[] ret = character.getTransform();
    	return ret;
    }
    
    public void addGhostAvatarToGameWorld(UUID id, String skin) throws IOException {
    	Vector3 vec = Vector3f.createFrom(0.0f, 0.0f, 0.0f);
    	avatar = new GhostAvatar(id, vec);
    	if(avatar != null) {
    		SceneNode ghostN = sm.getRootSceneNode().createChildSceneNode(avatar.getID().toString());
    		ghostN.setLocalPosition(0.0f, 0.0f, 0.0f);
    		avatar.setNode(ghostN);
    		
    		
    		ghostSE = sm.createSkeletalEntity("ghostAv", "man_update.rkm", "man_update.rks");
        
	        Texture tex;
	        if(skin.equalsIgnoreCase("s")) {
	        	tex = this.getEngine().getTextureManager().getAssetByPath("scientist.png");
	        }
	        else if(skin.equalsIgnoreCase("g")) {
	        	tex = this.getEngine().getTextureManager().getAssetByPath("security.png");
	        }
	        else {
	        	tex = this.getEngine().getTextureManager().getAssetByPath("manmade.png");
	        }
	        TextureState tstate = (TextureState) sm.getRenderSystem().createRenderState(RenderState.Type.TEXTURE);
	    	tstate.setTexture(tex);
	    	ghostSE.setRenderState(tstate);
	    	
	    	
	        ghostN.attachObject(ghostSE);
	        ghostSE.loadAnimation("joggingAnimation", "jogging.rka");
	        ghostSE.loadAnimation("standingAnimation", "standing.rka");
    		
	        
	        float mass = 100.0f;
	    	float up[] = {0, 1, 0};
	    	float size[] = {6.0f, 6.0f, 6.0f};
	    	double[] temptf;
	    	
	    	temptf = toDoubleArray(manChar.getLocalTransform().toFloatArray());
	    	ghost = physicsEng.addSphereObject(physicsEng.nextUID(), mass, temptf, 2.0f);
	    	
	    	ghost.setSleepThresholds(0.0f, 0.0f);
	    	ghost.setDamping(0.99f, 0.99f);
	    	
	    	//character.setFriction(1.0f);
	    	//character.setBounciness(1.0f);
	    	ghostN.setPhysicsObject(ghost);
    		//avatar.setPosition();
    		
    	}
    }
    
    public void moveGhostAvatar(UUID id, float[] pos) {
    	if(ghost != null) {
    		double[] temp = {pos[0], pos[1], pos[2], pos[3],
    				pos[4], pos[5], pos[6], pos[7],
    				pos[8], pos[9], pos[10], pos[11],
    				pos[12], pos[13], pos[14], pos[15]};
			
			ghost.setTransform(temp);
    		
    	}
    }
    
    public void moveNPCs(int npcNum, float[] pos, Vector3 vec) {
    	if(npcCon != null) {
	    	if(npcCon.getNPC(npcNum)!= null) {
	    		double[] temp = {pos[0], pos[1], pos[2], pos[3],
	    				pos[4], pos[5], pos[6], pos[7],
	    				pos[8], pos[9], pos[10], pos[11],
	    				pos[12], pos[13], pos[14], pos[15]};
	    		npcCon.getNPC(npcNum).setNPCTransform(temp);
	    		npcCon.getNPC(npcNum).getNode().lookAt(vec);
	    	}
    	}
    }
    
    public void removeGhostAvatarFromGameWorld(UUID id) {
    	if(id != null) {
    		gameObjectsToRemove.add(id);
    	}
    }
    
    public void setIsConnected(boolean b, int clientStatus) {
    	isClientConnected = b;
    	if(clientStatus == 0) {
    		hostStatus = clientStatus;
    	}
    	else {
    		hostStatus = clientStatus;
    		npcCon = new NPCController(this, protClient, dronesN, drones, hostStatus);
        	System.out.println("npcCon created as: " + hostStatus);
    	}
    	
    }
   
   public float distanceFrom(Vector3 v1, Vector3 v2) {
   	return (float) Math.sqrt(Math.pow(v1.x()-v2.x(), 2) + Math.pow(v1.y() - v2.y(), 2) + Math.pow(v1.z() - v2.z(), 2)); 
   }
   
   public void playJogging(SkeletalEntity skel) {
	   skel.stopAnimation();
	   skel.playAnimation("joggingAnimation", .5f, LOOP, 0);
   }
   
   public void stopJogging(SkeletalEntity skel) {
	   skel.stopAnimation();
	   skel.playAnimation("standingAnimation", .5f, LOOP, 0);
   }
   
   public void updateVerticalPosition() {
	   SceneNode tessN = this.getEngine().getSceneManager().getSceneNode("tessN");
	   Tessellation tessE = ((Tessellation) tessN.getAttachedObject("tessE"));
	    
	   Vector3 worldAvatarPosition = manChar.getWorldPosition();
	   Vector3 localAvatarPosition = manChar.getLocalPosition();
	   
	   Vector3 newAvatarPosition = Vector3f.createFrom(localAvatarPosition.x(), tessE.getWorldHeight(worldAvatarPosition.x(), worldAvatarPosition.z()) + 0.5f, localAvatarPosition.z());
	   
	   manChar.setLocalPosition(newAvatarPosition);
	   /*Vector3 loc = manChar.getWorldPosition();
	   if(loc.y() > 1.5f || loc.y() < 0.0f) {
		  manChar.setLocalPosition(loc.x(), 0.0f, loc.z()); 
	   }*/
   }

   
   public boolean getIsClientConnected() {
	   return isClientConnected;
   }
   
   private float[] toFloatArray(double[] arr) {
	   if(arr == null) {
		   return null;
	   }
	   int n = arr.length;
	   float[] ret = new float[n];
	   for(int i = 0; i < n; i++) {
		   ret[i] = (float) arr[i];
	   }
	   return ret;
   }
   
   private double[] toDoubleArray(float[] arr) {
	   if(arr == null) {
		   return null;
	   }
	   int n = arr.length;
	   double[] ret = new double[n];
	   for(int i = 0; i < n; i++) {
		   ret[i] = (double) arr[i];
	   }
	   return ret;
   }
   
   public float getElapsTime() {
	   return elapsTime;
   }
   
   public SceneNode[] getCharacters() {
	   SceneNode[] ret;
	   if(avatar != null) {
		   ret = new SceneNode[] {manChar, avatar.getNode()};
	   }
	   else {
		   ret = new SceneNode[] {manChar, null};
	   }
	   return ret;
   }
   
   public String getSkin() {
	   return skin;
   }
   
   public void damagePlayer() {
	   if(distanceFrom(manChar.getLocalPosition(), dronesN[0].getLocalPosition()) < 7.0) {
		   if (health > 0) {
			   health -= 0.2f;
		   }
	   }
   }
   
   public void interact() {
	   if(distanceFrom(manChar.getLocalPosition(), this.getEngine().getSceneManager().getSceneNode("button1Node").getLocalPosition()) < 10.0f) {
		   b1 = true;
		   protClient.sendButton();
	   }
   }
   
   public void flipButton() {
	   b1 = true;
   }
}