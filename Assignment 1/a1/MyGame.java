package a1;

//default imports
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;

import net.java.games.input.Controller;
import net.java.games.input.ControllerEnvironment;
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
import ray.input.*;
import ray.input.action.*;

import myGameEngine.*;


public class MyGame extends VariableFrameRateGame {
	
	GL4RenderSystem rs;
	float elapsTime = 0.0f;
	String elapsTimeStr, counterStr, dispStr;
	int elapsTimeSec = 0;
	
	//game actions
	private InputManager im;
	private Action movForward;
	private Action movBackward;
	private Action movRight;
	private Action movLeft;
	private Action yawLeft;
	private Action yawRight;
	private Action pitchUp;
	private Action pitchDown;
	private Action rollRight;
	private Action rollLeft;
	private Action swapCamStatus;
	private Action movX;
	private Action movY;
	private Action movRX;
	private Action movRY;
	
	private Camera camera;
	private SceneNode cameraNode;
	private SceneNode dolphinN;
	private SceneNode dolphinCamN;
	
	private SceneNode earthN;
	private SceneNode moonN;
	private SceneNode darkN;
	
	private SceneNode earthDN;
	private SceneNode moonDN;
	private SceneNode darkDN;
	
	//measures camera distance to planets
	private float earthD;
	private float moonD; 
	private float darkD;
	
	//measures dolphin distance to planets
	private float earthDD;
	private float moonDD;
	private float darkDD;
	
	private boolean earthScore = false;
	private boolean moonScore = false;
	private boolean darkScore = false;
	
	//planet scales
	private float earthS = (float) (Math.random() * 5) + 5;
	private float moonS = (float) (Math.random() * 2.5f) + 2.5f;
	private float darkS = (float) (Math.random() * 5f) + 2.5f;
	
	private int score = 0;
	
	private Controller gamepad;
	
	//camStatus = false represents 'n' mode
	//camStatus = true represents 'c' mode
	private boolean camStatus = true;
	
	private float friction = 0.01f;
	private float dFriction = 0.05f;
	private Angle rotAmt = Degreef.createFrom(0.5f);
	private Angle negRotAmt = Degreef.createFrom(-0.5f);
	
	//private float camMovSpeed = 0.5f;

    public MyGame() {
        super();
		System.out.println("Use W/A/S/D to move around and space to mount/dismount dolphin");
		System.out.println("Use left/right to yaw, up/down to pitch, and q/e to roll");
		System.out.println("Use left stick to move and right stick to yaw and pitch");
    }

    public static void main(String[] args) {
        Game game = new MyGame();
        try {
            game.startup();
            game.run();
        } catch (Exception e) {
            e.printStackTrace(System.err);
        } finally {
            game.shutdown();
            game.exit();
        }
    }
	
	@Override
	protected void setupWindow(RenderSystem rs, GraphicsEnvironment ge) {
		rs.createRenderWindow(new DisplayMode(1000, 700, 24, 60), false);
	}

    @Override
    protected void setupCameras(SceneManager sm, RenderWindow rw) {
        SceneNode rootNode = sm.getRootSceneNode();
        camera = sm.createCamera("MainCamera", Projection.PERSPECTIVE);
        rw.getViewport(0).setCamera(camera);
		
		camera.setRt((Vector3f)Vector3f.createFrom(1.0f, 0.0f, 0.0f));
		camera.setUp((Vector3f)Vector3f.createFrom(0.0f, 1.0f, 0.0f));
		camera.setFd((Vector3f)Vector3f.createFrom(0.0f, 0.0f, -1.0f));
		
		camera.setPo((Vector3f)Vector3f.createFrom(0.0f, 0.0f, 0.0f));

        cameraNode = rootNode.createChildSceneNode(camera.getName() + "Node");
        cameraNode.attachObject(camera);
    }
	
    @Override
    protected void setupScene(Engine eng, SceneManager sm) throws IOException {
    	setupInputs();
    	
    	//Axes--------------------------------------------------//
    	ManualObject xAxis = makeXAxis(eng, sm);
    	SceneNode xNode = sm.getRootSceneNode().createChildSceneNode("xNode");
    	xNode.attachObject(xAxis);
    	
    	ManualObject yAxis = makeYAxis(eng, sm);
    	SceneNode yNode = sm.getRootSceneNode().createChildSceneNode("yNode");
    	yNode.attachObject(yAxis);
    	
    	ManualObject zAxis = makeZAxis(eng, sm);
    	SceneNode zNode = sm.getRootSceneNode().createChildSceneNode("zNode");
    	zNode.attachObject(zAxis);
    	
    	//Earth Planet------------------------------------------//
    	Entity earthE = sm.createEntity("myEarth", "earth.obj");
    	earthE.setPrimitive(Primitive.TRIANGLES);
    	
    	Entity earthI = sm.createEntity("earthInside", "earth.obj");
    	earthI.setPrimitive(Primitive.TRIANGLES);
    	
    	earthN = sm.getRootSceneNode().createChildSceneNode(earthE.getName() + "Node");
    	earthN.setLocalPosition((float)((Math.random()*25) + 10), (float)((Math.random()*20) - 10), (float)((Math.random()*25) - 50));
    	
    	System.out.println(earthS);
    	earthN.scale(earthS, earthS, earthS);
    	earthN.attachObject(earthE);
    	
    	earthN.attachObject(earthI);
    	FrontFaceState cwfaceState = (FrontFaceState) sm.getRenderSystem().createRenderState(RenderState.Type.FRONT_FACE);
    	cwfaceState.setVertexWinding(FrontFaceState.VertexWinding.CLOCKWISE);
    	earthI.setRenderState(cwfaceState);
    	
    	ManualObject eDiamond = makeDiamond(eng, sm, "eDiamond");
    	
    	earthDN = earthN.createChildSceneNode("eDiamondNode");
    	earthDN.scale(1/earthS, 1/earthS, 1/earthS);
    	earthDN.attachObject(eDiamond);
    	
    	//Moon Planet------------------------------------------//
    	Entity moonE = sm.createEntity("myMoon", "earth.obj");
    	moonE.setPrimitive(Primitive.TRIANGLES);
    	
    	Entity moonI = sm.createEntity("moonInside", "earth.obj");
    	moonI.setPrimitive(Primitive.TRIANGLES);
    	
    	Texture tex = eng.getTextureManager().getAssetByPath("moon.jpeg");
    	TextureState tstate = (TextureState) sm.getRenderSystem().createRenderState(RenderState.Type.TEXTURE);
    	tstate.setTexture(tex);
    	moonE.setRenderState(tstate);
    	moonI.setRenderState(tstate);
    	
    	moonN = sm.getRootSceneNode().createChildSceneNode(moonE.getName() + "Node");
    	moonN.setLocalPosition((float)((Math.random()*25) - 30), (float)((Math.random()*20) + 10), (float)((Math.random()*25) + 25));
    	
    	System.out.println(moonS);
    	moonN.scale(moonS, moonS, moonS);
    	moonN.attachObject(moonE);
    	
    	moonN.attachObject(moonI);
    	moonI.setRenderState(cwfaceState);
    	
    	ManualObject mDiamond = makeDiamond(eng, sm, "mDiamond");
    	
    	moonDN = moonN.createChildSceneNode("mDiamondNode");
    	moonDN.scale(1/moonS, 1/moonS, 1/moonS);
    	moonDN.attachObject(mDiamond);
    	
    	
    	//Dark Planet------------------------------------------//
    	Entity darkE = sm.createEntity("myDark", "earth.obj");
    	moonE.setPrimitive(Primitive.TRIANGLES);
    	
    	Entity darkI = sm.createEntity("darkInside", "earth.obj");
    	darkI.setPrimitive(Primitive.TRIANGLES);
    	
    	tex = eng.getTextureManager().getAssetByPath("earth-night.jpeg");
    	tstate = (TextureState) sm.getRenderSystem().createRenderState(RenderState.Type.TEXTURE);
    	tstate.setTexture(tex);
    	darkE.setRenderState(tstate);
    	darkI.setRenderState(tstate);
    	
    	darkN = sm.getRootSceneNode().createChildSceneNode(darkE.getName() + "Node");
    	darkN.setLocalPosition((float)((Math.random()*25) - 30), (float)((Math.random()*20) - 30), (float)((Math.random()*25) - 25));
    	
    	System.out.println(darkS);
    	darkN.scale(darkS, darkS, darkS);
    	darkN.attachObject(darkE);
    	
    	darkN.attachObject(darkI);
    	darkI.setRenderState(cwfaceState);
    	
    	ManualObject dDiamond = makeDiamond(eng, sm, "dDiamond");
    	
    	darkDN = darkN.createChildSceneNode("dDiamondNode");
    	darkDN.scale(1/darkS, 1/darkS, 1/darkS);
    	darkDN.attachObject(dDiamond);
    	
    	//Dolphin----------------------------------------------//
        Entity dolphinE = sm.createEntity("myDolphin", "dolphinHighPoly.obj");
        dolphinE.setPrimitive(Primitive.TRIANGLES);

        dolphinN = sm.getRootSceneNode().createChildSceneNode(dolphinE.getName() + "Node");
        dolphinN.moveBackward(5.0f);
        dolphinN.attachObject(dolphinE);
        
        dolphinCamN = dolphinN.createChildSceneNode(dolphinE.getName() + "CamNode");
        dolphinCamN.moveUp(.4f);
        dolphinCamN.moveBackward(.3f);
        dolphinCamN.pitch(Degreef.createFrom(6.0f));

        sm.getAmbientLight().setIntensity(new Color(.1f, .1f, .1f));
		
		Light plight = sm.createLight("sun", Light.Type.POINT);
		plight.setAmbient(new Color(.3f, .3f, .3f));
        plight.setDiffuse(new Color(.7f, .7f, .7f));
		plight.setSpecular(new Color(1.0f, 1.0f, 1.0f));
        plight.setRange(100f);
		
		SceneNode plightNode = sm.getRootSceneNode().createChildSceneNode("plightNode");
        plightNode.attachObject(plight);
    }
    
    protected void setupInputs() {
    	im = new GenericInputManager();
    	
    	movForward = new MovForward(this);
    	movBackward = new MovBackward(this);
    	movRight = new MovRight(this);
    	movLeft = new MovLeft(this);
    	yawLeft = new YawLeft(this);
    	yawRight = new YawRight(this);
    	pitchUp = new PitchUp(this);
    	pitchDown = new PitchDown(this);
    	rollRight = new RollRight(this);
    	rollLeft = new RollLeft(this);
    	swapCamStatus = new SwapCamStatus(this);
    	movX = new MovX(this);
    	movY = new MovY(this);
    	movRX = new MovRX(this);
    	movRY = new MovRY(this);
    	
    	
    	ArrayList<Controller> controllers = im.getControllers();
    	for (Controller c : controllers) {
    		if(c.getType() == Controller.Type.KEYBOARD) { 
    			im.associateAction(c, net.java.games.input.Component.Identifier.Key.W, movForward, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
    			im.associateAction(c, net.java.games.input.Component.Identifier.Key.A, movLeft, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
    			im.associateAction(c, net.java.games.input.Component.Identifier.Key.S, movBackward, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
    			im.associateAction(c, net.java.games.input.Component.Identifier.Key.D, movRight, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
    			im.associateAction(c, net.java.games.input.Component.Identifier.Key.LEFT, yawLeft, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
    			im.associateAction(c, net.java.games.input.Component.Identifier.Key.RIGHT, yawRight, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
    			im.associateAction(c, net.java.games.input.Component.Identifier.Key.UP, pitchUp, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
    			im.associateAction(c, net.java.games.input.Component.Identifier.Key.DOWN, pitchDown, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
    			im.associateAction(c, net.java.games.input.Component.Identifier.Key.E, rollRight, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
    			im.associateAction(c, net.java.games.input.Component.Identifier.Key.Q, rollLeft, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
    			im.associateAction(c, net.java.games.input.Component.Identifier.Key.SPACE, swapCamStatus, InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
    			
    		}
    		else if(c.getType() == Controller.Type.GAMEPAD || c.getType() == Controller.Type.STICK) {
    			gamepad = c;
    			
    			im.associateAction(c,  net.java.games.input.Component.Identifier.Axis.X, movX, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
    			im.associateAction(c,  net.java.games.input.Component.Identifier.Axis.RX, movRX, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
    			im.associateAction(c,  net.java.games.input.Component.Identifier.Axis.Y, movY, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
    			im.associateAction(c,  net.java.games.input.Component.Identifier.Axis.RY, movRY, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
    			
    		}
    	}
    	
    }

    @Override
    protected void update(Engine engine) {
		// build and set HUD
		rs = (GL4RenderSystem) engine.getRenderSystem();
		elapsTime += engine.getElapsedTimeMillis();
		elapsTimeSec = Math.round(elapsTime/1000.0f);
		elapsTimeStr = Integer.toString(elapsTimeSec);
		
    	collision();
    	
		dispStr = "Time = " + elapsTimeStr;
		
		if(camStatus) {
			dispStr += "    Distance to Earth: " + (int) (earthD - earthS*2)  + ";    Moon: " + (int) (moonD - moonS*2)+ ";    Dark: " + (int) (darkD - darkS*2);
		}
		else {
			dispStr += "    Distance to Earth: " + (int) (earthDD - earthS*2) + ";    Moon: " + (int) (moonDD - moonS*2) + ";    Dark: " + (int) (darkDD - darkS*2);
		}
		dispStr += "    Score: " + score;
		
		rs.setHUD(dispStr, 15, 15);
		
		//tell the input manager to process the inputs
		im.update(elapsTime);
		
	}
    
    
   
    protected ManualObject makeDiamond(Engine eng, SceneManager sm, String name) throws IOException {
    	ManualObject diamond = sm.createManualObject(name);
    	ManualObjectSection diamondSec = diamond.createManualSection("DiamondSection");
    	diamond.setGpuShaderProgram(sm.getRenderSystem().getGpuShaderProgram(GpuShaderProgram.Type.RENDERING));
    	
    	float[] vertices = new float[] {
    			-1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f, 0.0f, 2.0f, 0.0f,    //front
    			1.0f, 0.0f, 1.0f, 1.0f, 0.0f, -1.0f, 0.0f, 2.0f, 0.0f,    //right
    			1.0f, 0.0f, -1.0f, -1.0f, 0.0f, -1.0f, 0.0f, 2.0f, 0.0f,  //back
    			-1.0f, 0.0f, -1.0f, -1.0f, 0.0f, 1.0f, 0.0f, 2.0f, 0.0f,  //left
    			0.0f, -2.0f, 0.0f, 1.0f, 0.0f, 1.0f, -1.0f, 0.0f, 1.0f,	//front below
    			0.0f, -2.0f, 0.0f, 1.0f, 0.0f, -1.0f, 1.0f, 0.0f, 1.0f,   //right below
    			0.0f, -2.0f, 0.0f, -1.0f, 0.0f, -1.0f, 1.0f, 0.0f, -1.0f,  //back below
    			0.0f, -2.0f, 0.0f, -1.0f, 0.0f, 1.0f, -1.0f, 0.0f, -1.0f,  //left below
			}; 
    		float[] texcoords = new float[] {
				0.0f, 0.0f, 1.0f, 0.0f, 0.5f, 1.0f,
				0.0f, 0.0f, 1.0f, 0.0f, 0.5f, 1.0f,
				0.0f, 0.0f, 1.0f, 0.0f, 0.5f, 1.0f,
				0.0f, 0.0f, 1.0f, 0.0f, 0.5f, 1.0f,
				0.0f, 0.0f, 1.0f, 0.0f, 0.5f, 1.0f,
				0.0f, 0.0f, 1.0f, 0.0f, 0.5f, 1.0f,
				0.0f, 0.0f, 1.0f, 0.0f, 0.5f, 1.0f,
				0.0f, 0.0f, 1.0f, 0.0f, 0.5f, 1.0f,
    		}; 
    		float[] normals = new float[] {   
    			0.0f, 1.0f, 1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f, 1.0f,
    			1.0f, 1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f, 1.0f, 0.0f,
    			0.0f, 1.0f, -1.0f, 0.0f, 1.0f, -1.0f, 0.0f, 1.0f, -1.0f,
    			-1.0f, 1.0f, 0.0f, -1.0f, 1.0f, 0.0f, -1.0f, 1.0f, 0.0f,
    			1.0f, -1.0f, 0.0f, 1.0f, -1.0f, 0.0f, 1.0f, -1.0f, 0.0f,
    			0.0f, -1.0f, 1.0f, 0.0f, -1.0f, 1.0f, 0.0f, -1.0f, 1.0f,
    			-1.0f, -1.0f, 0.0f, -1.0f, -1.0f, 0.0f, -1.0f, -1.0f, 0.0f,
    			0.0f, -1.0f, -1.0f, 0.0f, -1.0f, -1.0f, 0.0f, -1.0f, -1.0f,
    			
    		};
    		int[] indices = new int[] {0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23};
    		
    		FloatBuffer vertBuf = BufferUtil.directFloatBuffer(vertices);
    		FloatBuffer texBuf = BufferUtil.directFloatBuffer(texcoords);
    		FloatBuffer normBuf = BufferUtil.directFloatBuffer(normals);
    		IntBuffer indexBuf = BufferUtil.directIntBuffer(indices);
    		
    		diamondSec.setVertexBuffer(vertBuf);
    		diamondSec.setTextureCoordsBuffer(texBuf);
    		diamondSec.setNormalsBuffer(normBuf);
    		diamondSec.setIndexBuffer(indexBuf);
    		
    		Texture tex = eng.getTextureManager().getAssetByPath("red.jpeg");
    		TextureState texState = (TextureState)sm.getRenderSystem().createRenderState(RenderState.Type.TEXTURE);
    		texState.setTexture(tex);
    		FrontFaceState faceState = (FrontFaceState) sm.getRenderSystem().createRenderState(RenderState.Type.FRONT_FACE);
    		
    		diamond.setDataSource(DataSource.INDEX_BUFFER);
    		diamond.setRenderState(texState);
    		diamond.setRenderState(faceState);
    		return diamond;
    	
    }
    
    protected ManualObject makeXAxis(Engine eng, SceneManager sm) throws IOException {
    	ManualObject xAxis = sm.createManualObject("xAxis");
    	ManualObjectSection xAxisSection = xAxis.createManualSection("xAxisSection");
    	xAxis.setPrimitive(Primitive.LINES);
    	xAxis.setGpuShaderProgram(sm.getRenderSystem().getGpuShaderProgram(GpuShaderProgram.Type.RENDERING));
    	float [] vertices = new float[] {
    			0.0f, 0.0f, 0.0f, 100.0f, 0.0f, 0.0f
    	};
    	
    	int[] indices = new int[] {0, 1};
    	
    	FloatBuffer vertBuf = BufferUtil.directFloatBuffer(vertices);
    	IntBuffer indexBuf = BufferUtil.directIntBuffer(indices);
    	xAxisSection.setVertexBuffer(vertBuf);
    	xAxisSection.setIndexBuffer(indexBuf);
    	Material mat = sm.getMaterialManager().getAssetByPath("default.mtl");
    	mat.setEmissive(Color.RED);
    	
    	Texture tex = eng.getTextureManager().getAssetByPath("bright-red.jpeg");
    	TextureState tstate = (TextureState) sm.getRenderSystem().createRenderState(RenderState.Type.TEXTURE);
    	tstate.setTexture(tex);
    	xAxisSection.setRenderState(tstate);
    	xAxisSection.setMaterial(mat);
    	
    	return xAxis;
    }
    
    protected ManualObject makeYAxis(Engine eng, SceneManager sm) throws IOException {
    	ManualObject yAxis = sm.createManualObject("yAxis");
    	ManualObjectSection yAxisSection = yAxis.createManualSection("yAxisSection");
    	yAxis.setPrimitive(Primitive.LINES);
    	yAxis.setGpuShaderProgram(sm.getRenderSystem().getGpuShaderProgram(GpuShaderProgram.Type.RENDERING));
    	float [] vertices = new float[] {
    			0.0f, 0.0f, 0.0f, 0.0f, 100.0f, 0.0f
    	};
    	
    	int[] indices = new int[] {0, 1};
    	
    	FloatBuffer vertBuf = BufferUtil.directFloatBuffer(vertices);
    	IntBuffer indexBuf = BufferUtil.directIntBuffer(indices);
    	yAxisSection.setVertexBuffer(vertBuf);
    	yAxisSection.setIndexBuffer(indexBuf);
    	Material mat = sm.getMaterialManager().getAssetByPath("default.mtl");
    	mat.setEmissive(Color.GREEN);
    	
    	Texture tex = eng.getTextureManager().getAssetByPath("bright-green.jpeg");
    	TextureState tstate = (TextureState) sm.getRenderSystem().createRenderState(RenderState.Type.TEXTURE);
    	tstate.setTexture(tex);
    	yAxisSection.setRenderState(tstate);
    	yAxisSection.setMaterial(mat);
    	
    	return yAxis;
    }
    
    protected ManualObject makeZAxis(Engine eng, SceneManager sm) throws IOException {
    	ManualObject zAxis = sm.createManualObject("zAxis");
    	ManualObjectSection zAxisSection = zAxis.createManualSection("zAxisSection");
    	zAxis.setPrimitive(Primitive.LINES);
    	zAxis.setGpuShaderProgram(sm.getRenderSystem().getGpuShaderProgram(GpuShaderProgram.Type.RENDERING));
    	float [] vertices = new float[] {
    			0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 100.0f
    	};
    	
    	int[] indices = new int[] {0, 1};
    	
    	FloatBuffer vertBuf = BufferUtil.directFloatBuffer(vertices);
    	IntBuffer indexBuf = BufferUtil.directIntBuffer(indices);
    	zAxisSection.setVertexBuffer(vertBuf);
    	zAxisSection.setIndexBuffer(indexBuf);
    	Material mat = sm.getMaterialManager().getAssetByPath("default.mtl");
    	mat.setEmissive(Color.BLUE);
    	
    	Texture tex = eng.getTextureManager().getAssetByPath("bright-blue.jpeg");
    	TextureState tstate = (TextureState) sm.getRenderSystem().createRenderState(RenderState.Type.TEXTURE);
    	tstate.setTexture(tex);
    	zAxisSection.setRenderState(tstate);
    	zAxisSection.setMaterial(mat);
    	
    	return zAxis;
    }
    
    public void movForward() {
    	if(camStatus) {
	    	Vector3f v = camera.getFd();
			Vector3f p = camera.getPo();
			Vector3f p1 = (Vector3f)Vector3f.createFrom(friction*v.x(), friction*v.y(), friction*v.z());
			Vector3f p2 = (Vector3f)p.add((Vector3)p1);
			if(!dolphinTooFar(p2)) {
				camera.setPo((Vector3f)Vector3f.createFrom(p2.x(), p2.y(), p2.z()));
			}
    	}
    	else {
        	dolphinN.moveForward(dFriction);
        	if(dolphinTooClose((Vector3f)dolphinN.getLocalPosition())) {
        		dolphinN.moveBackward(dFriction);
        	}
    	}
    }
    public void movBackward() {
    	if(camStatus) {
	    	Vector3f v = camera.getFd();
			Vector3f p = camera.getPo();
			Vector3f p1 = (Vector3f)Vector3f.createFrom(-friction*v.x(), -friction*v.y(), -friction*v.z());
			Vector3f p2 = (Vector3f)p.add((Vector3)p1);
			if(!dolphinTooFar(p2)) {
				camera.setPo((Vector3f)Vector3f.createFrom(p2.x(), p2.y(), p2.z()));
			}
    	}
    	else {
    		dolphinN.moveBackward(dFriction);
    		if(dolphinTooClose((Vector3f)dolphinN.getLocalPosition())) {
    			dolphinN.moveForward(dFriction);
    		}
    	}
    }
    public void movRight() {
    	if(camStatus) {
	    	Vector3f v = camera.getRt();
			Vector3f p = camera.getPo();
			Vector3f p1 = (Vector3f)Vector3f.createFrom(friction*v.x(), friction*v.y(), friction*v.z());
			Vector3f p2 = (Vector3f)p.add((Vector3)p1);
			if(!dolphinTooFar(p2)) {
				camera.setPo((Vector3f)Vector3f.createFrom(p2.x(), p2.y(), p2.z()));
			}
    	}
    	else {
    		dolphinN.moveLeft(dFriction);
    		if(dolphinTooClose((Vector3f)dolphinN.getLocalPosition())) {
    			dolphinN.moveRight(dFriction);
    		}
    	}
    }
    public void movLeft() {
    	if(camStatus) {
	    	Vector3f v = camera.getRt();
			Vector3f p = camera.getPo();
			Vector3f p1 = (Vector3f)Vector3f.createFrom(-friction*v.x(), -friction*v.y(), -friction*v.z());
			Vector3f p2 = (Vector3f)p.add((Vector3)p1);
			if(!dolphinTooFar(p2)) {
				camera.setPo((Vector3f)Vector3f.createFrom(p2.x(), p2.y(), p2.z()));
			}
	}
		else {
			dolphinN.moveRight(dFriction);
			if(dolphinTooClose((Vector3f)dolphinN.getLocalPosition())) {
    			dolphinN.moveLeft(dFriction);
    		}
		}
    }
    
    public void yawLeft() {
    	if(camStatus) {
    		camera.setRt((Vector3f)(camera.getRt().rotate(rotAmt, camera.getUp()).normalize()));
    		camera.setFd((Vector3f)(camera.getFd().rotate(rotAmt, camera.getUp()).normalize()));
    	}
    	else {
    		dolphinN.yaw(rotAmt);
    	}
    }
    public void yawRight() {
    	if(camStatus) {
    		camera.setRt((Vector3f)(camera.getRt().rotate(negRotAmt, camera.getUp()).normalize()));
    		camera.setFd((Vector3f)(camera.getFd().rotate(negRotAmt, camera.getUp()).normalize()));
    	}
    	else {
    		dolphinN.yaw(negRotAmt);
    	}
    }
    public void pitchUp() {
    	if(camStatus) {
    		camera.setUp((Vector3f)(camera.getUp().rotate(rotAmt, camera.getRt()).normalize()));
    		camera.setFd((Vector3f)(camera.getFd().rotate(rotAmt, camera.getRt()).normalize()));
    	}
    	else {
    		dolphinN.pitch(negRotAmt);
    	}
    }
    public void pitchDown() {
    	if(camStatus) {
    		camera.setUp((Vector3f)(camera.getUp().rotate(negRotAmt, camera.getRt()).normalize()));
    		camera.setFd((Vector3f)(camera.getFd().rotate(negRotAmt, camera.getRt()).normalize()));
    	}
    	else {
    		dolphinN.pitch(rotAmt);
    	}
    }
    public void rollLeft() {
    	if(camStatus) {
    		camera.setUp((Vector3f)(camera.getUp().rotate(negRotAmt, camera.getFd()).normalize()));
    		camera.setRt((Vector3f)(camera.getRt().rotate(negRotAmt, camera.getFd()).normalize()));
    	}
    	else {
    		dolphinN.roll(negRotAmt);
    	}
    }
    public void rollRight() {
    	if(camStatus) {
    		camera.setUp((Vector3f)(camera.getUp().rotate(rotAmt, camera.getFd()).normalize()));
    		camera.setRt((Vector3f)(camera.getRt().rotate(rotAmt, camera.getFd()).normalize()));
    	}
    	else {
    		dolphinN.roll(rotAmt);
    	}
    }
    
    public void swapCamStatus() {
    	if(camStatus) {
    		if(distanceFrom(camera.getPo(), (Vector3f) dolphinN.getLocalPosition()) <= 5.0f) {
	    		dolphinCamN.attachChild(cameraNode);
	    		System.out.println("Camera attached to dolphin");
	    		camera.setMode('n');
	    		camStatus = !camStatus;
    		}
    		else {
    			System.out.println("Camera too far from dolphin to mount.");
    		}
    	}
    	else {
    		dolphinCamN.detachChild(cameraNode);
    		Vector3f position = (Vector3f) dolphinN.getLocalPosition();
    		camera.setPo((Vector3f) Vector3f.createFrom(position.x() - 1f, position.y(), position.z()));
    		camera.setMode('c');
    		camStatus = !camStatus;
    	}
    }
    
    public float getXAxis() {
		float conXAxis = gamepad.getComponent(net.java.games.input.Component.Identifier.Axis.X).getPollData();
		
		return conXAxis;
    }
    public float getYAxis() {
    	float conYAxis = gamepad.getComponent(net.java.games.input.Component.Identifier.Axis.Y).getPollData();
		
		return conYAxis;
    }
    public float getRXAxis() {
    	float conRXAxis = gamepad.getComponent(net.java.games.input.Component.Identifier.Axis.RX).getPollData();
		
		return conRXAxis;
    }
    public float getRYAxis() {
    		float conRYAxis = gamepad.getComponent(net.java.games.input.Component.Identifier.Axis.RY).getPollData();
		
		return conRYAxis;
    }
    
    public boolean dolphinTooClose(Vector3f v) {
    	//Distances--------------------------------------------//
    	earthDD = distanceFrom((Vector3f) dolphinN.getLocalPosition(), (Vector3f) earthN.getLocalPosition());
    	moonDD = distanceFrom((Vector3f) dolphinN.getLocalPosition(), (Vector3f) moonN.getLocalPosition());
    	darkDD = distanceFrom((Vector3f) dolphinN.getLocalPosition(), (Vector3f) darkN.getLocalPosition());
    	
    	boolean ret = false;

    	if(earthDD <= earthS*2 || moonDD <= moonS*2 || darkDD <= darkS*2) {
    		ret = true;
    	}
    	
    	return ret;
    }
    
    public boolean dolphinTooFar(Vector3f v) {
    	boolean ret = false;
    	
    	earthD = distanceFrom(camera.getPo(), (Vector3f) earthN.getLocalPosition());
    	moonD = distanceFrom(camera.getPo(), (Vector3f) moonN.getLocalPosition());
    	darkD = distanceFrom(camera.getPo(), (Vector3f) darkN.getLocalPosition());
    	
    	
    	float temp = distanceFrom((Vector3f) dolphinN.getLocalPosition(), v);
    
    	if(temp >= 20.f) {
    		ret = true;
    	}
    	return ret;
	}
    		
    public void collision() {
    	if(distanceFrom(camera.getPo(), (Vector3f) earthN.getLocalPosition()) < 2.0f && earthScore == false) {
    		earthScore = true;
    		score++;
    		earthDN.detachAllObjects();
    	}
    	else if(distanceFrom(camera.getPo(), (Vector3f) moonN.getLocalPosition()) < 2.0f && moonScore == false) {
    		moonScore = true;
    		score++;
    		moonDN.detachAllObjects();
    	}
    	else if(distanceFrom(camera.getPo(), (Vector3f) darkN.getLocalPosition()) < 2.0f && darkScore == false) {
    		darkScore = true;
    		score++;
    		darkDN.detachAllObjects();
    	}
    }
    
    public float distanceFrom(Vector3f v1, Vector3f v2) {
    	return (float) Math.sqrt(Math.pow(v1.x()-v2.x(), 2) + Math.pow(v1.y() - v2.y(), 2) + Math.pow(v1.z() - v2.z(), 2)); 
    }

}