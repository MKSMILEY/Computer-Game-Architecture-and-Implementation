package a2;

//default imports
import java.awt.*;
import java.io.*;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
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
import ray.input.*;

import myGameEngine.*;


public class MyGame extends VariableFrameRateGame {
	
	GL4RenderSystem rs;
	float elapsTime = 0.0f;
	String elapsTimeStr, counterStr, dispStr;
	int elapsTimeSec = 0;
	
	//game actions
	private InputManager im;
	
	private Camera camera1;
	private SceneNode camera1N;
	private Camera camera2;
	private SceneNode camera2N;
	
	private SceneNode dolphin1N;
	private SceneNode dolphin2N;
	
	private SceneNode planets;
	
	private SceneNode earthN;
	private SceneNode moonN;
	private SceneNode darkN;
	
	private int earthScore = 0;
	private int moonScore = 0;
	private int darkScore = 0;
	
	private boolean earthScored = false;
	private boolean moonScored = false;
	private boolean darkScored = false;
	
	private boolean d1InEarth = false;
	private boolean d2InEarth = false;
	
	private boolean d1InMoon = false;
	private boolean d2InMoon = false;
	
	private boolean d1InDark = false;
	private boolean d2InDark = false;
	
	private boolean winner = false;
	
	//planet scales
	private float earthS = 1.0f;
	private float moonS = 1.2f;
	private float darkS = .5f;
	
	private int dolphin1S = 0;
	private int dolphin2S = 0;
	
	//Node Controllers
	private LiftController lc = new LiftController();
	private StretchController sc = new StretchController();
	private RotationController rc = new RotationController(Vector3f.createUnitVectorY(), .02f);
	
	private Controller gamepad, keyboard, mouse;
	
	//Added in assignment 2
	private Camera3Pcontroller cameraController1, cameraController2;

    public MyGame() {
        super();
		System.out.println("Use W/A/S/D or the left stick to move the dolphin");
		System.out.println("Use left/right/up/down or the right stick to rotate the camera around the dolphin");
		System.out.println("Use g/t or button 2/button 4 to zoom in and out on the dolphin");
		System.out.println("Use q/e or button 5/button 6 to yaw around the dolphin");
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
	
	protected void setupWindowViewports(RenderWindow rw) {
		rw.addKeyListener(this);
		
		Viewport topViewport = rw.getViewport(0);
		topViewport.setDimensions(.51f, .01f, .99f, .49f);
		topViewport.setClearColor(new Color(1.0f, .7f, .7f));
		
		Viewport botViewport = rw.createViewport(.01f, .01f, .99f, .49f);
		botViewport.setClearColor(new Color(.5f, 1.0f, .5f));
	}

    @Override
    protected void setupCameras(SceneManager sm, RenderWindow rw) {
        SceneNode rootNode = sm.getRootSceneNode();
        camera1 = sm.createCamera("MainCamera", Projection.PERSPECTIVE);
        rw.getViewport(0).setCamera(camera1);
        camera1N = rootNode.createChildSceneNode("MainCameraNode");
        camera1N.attachObject(camera1);
        camera1.setMode('n');
        camera1.getFrustum().setFarClipDistance(1000.0f);
        
        camera2 = sm.createCamera("MainCamera2", Projection.PERSPECTIVE);
        rw.getViewport(1).setCamera(camera2);
        camera2N = rootNode.createChildSceneNode("MainCamera2Node");
        camera2N.attachObject(camera2);
        camera2.setMode('n');
        camera2.getFrustum().setFarClipDistance(1000.0f);
        		
    }
	
    @Override
    protected void setupScene(Engine eng, SceneManager sm) throws IOException {
    	setupInputs();

    	sm.addController(lc);
    	sm.addController(sc);
    	sm.addController(rc);
    	
    	//Floor-------------------------------------------------//
    	ManualObject floor = makeTriangle(eng, sm, "floor");
    	SceneNode floorN = sm.getRootSceneNode().createChildSceneNode("floorN");
    	floorN.attachObject(floor);
    	floorN.scale(1000.0f, 1.0f, 1000.0f);
    	floorN.moveBackward(500.0f);
    	floorN.moveLeft(500.0f);
    	
    	
    	planets = sm.getRootSceneNode().createChildSceneNode("myPlanetNodeGroup");
    	
    	//Earth Planet------------------------------------------//
    	Entity earthE = sm.createEntity("myEarth", "earth.obj");
    	earthE.setPrimitive(Primitive.TRIANGLES);
    	
    	Entity earthI = sm.createEntity("earthInside", "earth.obj");
    	earthI.setPrimitive(Primitive.TRIANGLES);
    	
    	
    	earthN = planets.createChildSceneNode(earthE.getName() + "Node");
    	earthN.setLocalPosition(10.0f, 2.0f, 20.0f);
    	
    	System.out.println(earthS);
    	earthN.scale(earthS, earthS, earthS);
    	earthN.attachObject(earthE);
    	
    	earthN.attachObject(earthI);
    	FrontFaceState cwfaceState = (FrontFaceState) sm.getRenderSystem().createRenderState(RenderState.Type.FRONT_FACE);
    	cwfaceState.setVertexWinding(FrontFaceState.VertexWinding.CLOCKWISE);
    	earthI.setRenderState(cwfaceState);
    	
    	
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
    	
    	moonN = planets.createChildSceneNode(moonE.getName() + "Node");
    	moonN.setLocalPosition(-10.0f, 3.0f, 40.0f);
    	
    	System.out.println(moonS);
    	moonN.scale(moonS, moonS, moonS);
    	moonN.attachObject(moonE);
    	
    	moonN.attachObject(moonI);
    	moonI.setRenderState(cwfaceState);
    	
    	
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
    	
    	darkN = planets.createChildSceneNode(darkE.getName() + "Node");
    	darkN.setLocalPosition(0.0f, 2.0f, 60.0f);
    	
    	System.out.println(darkS);
    	darkN.scale(darkS, darkS, darkS);
    	darkN.attachObject(darkE);
    	
    	darkN.attachObject(darkI);
    	darkI.setRenderState(cwfaceState);
    	
    	
    	//Dolphin1----------------------------------------------//
        Entity dolphinE = sm.createEntity("myDolphin", "dolphinHighPoly.obj");
        dolphinE.setPrimitive(Primitive.TRIANGLES);
        
        
        dolphin1N = sm.getRootSceneNode().createChildSceneNode(dolphinE.getName() + "Node");
        dolphin1N.moveUp(2.0f);
        dolphin1N.attachObject(dolphinE);
        
        
        Material mat2 = sm.getMaterialManager().getAssetByPath("default.mtl");
        mat2.setEmissive(Color.BLUE);
        
        //Dolphin2----------------------------------------------//
        Entity dolphin2E = sm.createEntity("myDolphin2", "dolphinHighPoly.obj");
        dolphinE.setPrimitive(Primitive.TRIANGLES);

        dolphin2N = sm.getRootSceneNode().createChildSceneNode(dolphin2E.getName() + "Node");
        dolphin2N.moveUp(2.0f);
        dolphin2N.moveLeft(5.0f);
        dolphin2E.setMaterial(mat2);
        dolphin2N.attachObject(dolphin2E);
        
        
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
        
    	cameraController1 = new Camera3Pcontroller(camera1N, dolphin1N, keyboard, mouse, im);
    	cameraController2 = new Camera3Pcontroller(camera2N, dolphin2N, gamepad, im);
    }
    
    protected void setupInputs() {
    	im = new GenericInputManager();
    	//keyboard = im.getKeyboardController();
    	//mouse = im.getMouseController();
    	//gamepad = im.getGamepadController(0);
    	
    	ArrayList<Controller> controllers = im.getControllers();
    	for (Controller c : controllers) {
    		if(c.getType() == Controller.Type.KEYBOARD) { 
    			keyboard = c;
    			
    		}
    		else if(c.getType() == Controller.Type.MOUSE) {
    			mouse = c;
    		}
    		else if(c.getType() == Controller.Type.GAMEPAD || c.getType() == Controller.Type.STICK) {
    			gamepad = c;
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
		
		int vpHeight = rs.getRenderWindow().getViewport(0).getActualHeight();
		int vpWidth = rs.getRenderWindow().getViewport(0).getActualWidth();
		
    	collision();
    	updateScores();
    	updateNodeControllers();
    	checkWinner();
    	
		dispStr = "Time = " + elapsTimeStr;
		dispStr += "    Score: " + dolphin2S;
		
		rs.setHUD(dispStr, (int) (vpWidth * 0.05), (int) (vpHeight * 0.05));
		
		dispStr = "Time = " + elapsTimeStr;
		dispStr += "    Score: " + dolphin1S;
		
		rs.setHUD2(dispStr,(int) (vpWidth * 0.05) , (int) (vpHeight + vpHeight * 0.05));
		
		//tell the input manager to process the inputs
		im.update(elapsTime);
		cameraController1.updateCameraPosition();
		cameraController2.updateCameraPosition();
		
	}
    
    protected ManualObject makeTriangle(Engine eng, SceneManager sm, String name) throws IOException {
    	ManualObject triangle = sm.createManualObject(name);
    	ManualObjectSection triangleSec = triangle.createManualSection(name + "Section");
    	triangle.setPrimitive(Primitive.TRIANGLES);
    	triangle.setGpuShaderProgram(sm.getRenderSystem().getGpuShaderProgram(GpuShaderProgram.Type.RENDERING));
    	
    	float [] vertices = new float[] {
    			0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 0.0f, 0.0f,
    			1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f
    	};
    	
    	int[] indices = new int[] {0, 1, 2, 3, 4, 5};
    	
    	FloatBuffer vertBuf = BufferUtil.directFloatBuffer(vertices);
    	IntBuffer indexBuf = BufferUtil.directIntBuffer(indices);
    	triangleSec.setVertexBuffer(vertBuf);
    	triangleSec.setIndexBuffer(indexBuf);
    	Material mat = sm.getMaterialManager().getAssetByPath("default.mtl");
    	mat.setEmissive(Color.RED);
    	
    	Texture tex = eng.getTextureManager().getAssetByPath("bright-red.jpeg");
    	TextureState tstate = (TextureState) sm.getRenderSystem().createRenderState(RenderState.Type.TEXTURE);
    	tstate.setTexture(tex);
    	triangle.setRenderState(tstate);
    	triangle.setMaterial(mat);
    	
    	return triangle;
    }
    		
   public void collision() {
    	if((distanceFrom((Vector3f) dolphin1N.getWorldPosition(), (Vector3f) earthN.getLocalPosition()) < 2.0f) 
    			&& (distanceFrom((Vector3f) dolphin2N.getWorldPosition(), (Vector3f) earthN.getLocalPosition()) > 2.0f)) {
    		earthScore = 1;
    		if(!d1InEarth) {
    			earthScored = true;
    			d1InEarth = true;
    		}
    		else if(d1InEarth && d2InEarth) {
    			earthScored = true;
        		d1InEarth = true;
    		}
    		d2InEarth = false;
    		
    	}
    	else if((distanceFrom((Vector3f) dolphin1N.getWorldPosition(), (Vector3f) earthN.getLocalPosition()) > 2.0f) 
    			&& (distanceFrom((Vector3f) dolphin2N.getWorldPosition(), (Vector3f) earthN.getLocalPosition()) < 2.0f)) {
    		earthScore = 2;
    		if(!d2InEarth) {
    			earthScored = true;
    			d2InEarth = true;
    		}
    		else if(d1InEarth && d2InEarth) {
    			earthScored = true;
    			d2InEarth = true;
    		}
    		d1InEarth = false;
    		
    	}
    	else if((distanceFrom((Vector3f) dolphin1N.getWorldPosition(), (Vector3f) earthN.getLocalPosition()) < 2.0f) 
    			&& (distanceFrom((Vector3f) dolphin2N.getWorldPosition(), (Vector3f) earthN.getLocalPosition()) < 2.0f)) {
    		earthScore = 0;
    		earthScored = true;
    		d1InEarth = true;
    		d2InEarth = true;
    	}
    	
    	
    	if((distanceFrom((Vector3f) dolphin1N.getWorldPosition(), (Vector3f) moonN.getLocalPosition()) < 2.0f) 
    			&& (distanceFrom((Vector3f) dolphin2N.getWorldPosition(), (Vector3f) moonN.getLocalPosition()) > 2.0f)) {
    		moonScore = 1;
    		if(!d1InMoon) {
    			moonScored = true;
    			d1InMoon = true;
    		}
    		else if(d1InMoon && d2InMoon) {
    			moonScored = true;
    			d1InMoon = true;
    		}
    		d2InMoon = false;
    	}
    	else if((distanceFrom((Vector3f) dolphin1N.getWorldPosition(), (Vector3f) moonN.getLocalPosition()) > 2.0f) 
    			&& (distanceFrom((Vector3f) dolphin2N.getWorldPosition(), (Vector3f) moonN.getLocalPosition()) < 2.0f)) {
    		moonScore = 2;
    		if(!d2InMoon) {
    			moonScored = true;
    			d2InMoon = true;
    		}
    		else if(d1InMoon && d2InMoon) {
    			moonScored = true;
    			d2InMoon = true;
    		}
    		d1InMoon = false;
    	}
    	else if((distanceFrom((Vector3f) dolphin1N.getWorldPosition(), (Vector3f) moonN.getLocalPosition()) < 2.0f) 
    			&& (distanceFrom((Vector3f) dolphin2N.getWorldPosition(), (Vector3f) moonN.getLocalPosition()) < 2.0f)) {
    		moonScore = 0;
    		moonScored = true;
    		d1InMoon = true;
    		d2InMoon = true;
    	}
    	
    	if((distanceFrom((Vector3f) dolphin1N.getWorldPosition(), (Vector3f) darkN.getLocalPosition()) < 2.0f) 
    			&& (distanceFrom((Vector3f) dolphin2N.getWorldPosition(), (Vector3f) darkN.getLocalPosition()) > 2.0f)) {
    		darkScore = 1;
    		if(!d1InDark) {
    			darkScored = true;
    			d1InDark = true;
    		}
    		else if(d1InDark && d2InDark) {
    			darkScored = true;
    			d1InDark = true;
    		}
    		d2InDark = false;
    	}
    	else if((distanceFrom((Vector3f) dolphin1N.getWorldPosition(), (Vector3f) darkN.getLocalPosition()) > 2.0f) 
    			&& (distanceFrom((Vector3f) dolphin2N.getWorldPosition(), (Vector3f) darkN.getLocalPosition()) < 2.0f)) {
    		darkScore = 2;
    		if(!d2InDark) {
    			darkScored = true;
    			d2InDark = true;
    		}
    		else if(d1InDark && d2InDark) {
    			darkScored = true;
    			d2InDark = true;
    		}
    		d1InMoon = false;
    	}
    	else if((distanceFrom((Vector3f) dolphin1N.getWorldPosition(), (Vector3f) darkN.getLocalPosition()) < 2.0f) 
    			&& (distanceFrom((Vector3f) dolphin2N.getWorldPosition(), (Vector3f) darkN.getLocalPosition()) < 2.0f)) {
    		darkScore = 0;
    		darkScored = true;
    		d1InDark = true;
    		d2InDark = true;
    	}
    	
    }
   
   public void updateScores() {
	   if(earthScore == 1 && moonScore == 1 && darkScore == 1) {
		   dolphin1S = 3;
	   }
	   else if(earthScore == 1 && moonScore == 1 && darkScore != 1
			   || earthScore == 1 && moonScore != 1 && darkScore == 1
			   || earthScore != 1 && moonScore == 1 && darkScore == 1) {
		   dolphin1S = 2;
	   }
	   else if(earthScore == 1 && moonScore != 1 && darkScore != 1
			   || earthScore != 1 && moonScore == 1 && darkScore != 1
			   || earthScore != 1 && moonScore != 1 && darkScore == 1) {
		   dolphin1S = 1;
	   }
	   else {
		   dolphin1S = 0;
	   }
	   
	   if(earthScore == 2 && moonScore == 2 && darkScore == 2) {
		   dolphin2S = 3;
	   }
	   else if(earthScore == 2 && moonScore == 2 && darkScore != 2
			   || earthScore == 2 && moonScore != 2 && darkScore == 2
			   || earthScore != 2 && moonScore == 2 && darkScore == 2) {
		   dolphin2S = 2;
	   }
	   else if(earthScore == 2 && moonScore != 2 && darkScore != 2
			   || earthScore != 2 && moonScore == 2 && darkScore != 2
			   || earthScore != 2 && moonScore != 2 && darkScore == 2) {
		   dolphin2S = 1;
	   }
	   else {
		   dolphin2S = 0;
	   }
	   
   }
   
   private void updateNodeControllers() {
	   if(earthScored == true) {
		   if(earthScore == 1) {
			   sc.removeNode(earthN);
			   lc.addNode(earthN);
		   }
		   else if(earthScore == 2) {
			   lc.removeNode(earthN);
			   sc.addNode(earthN);
		   }
		   else if(earthScore == 0){
			   lc.removeNode(moonN);
			   sc.removeNode(moonN);
		   }
		   earthScored = false;
	   }
	   
	   if(moonScored == true) {
		   if(moonScore == 1) {
			   sc.removeNode(moonN);
			   lc.addNode(moonN);
		   }
		   else if(moonScore == 2) {
			   lc.removeNode(moonN);
			   sc.addNode(moonN);
		   }
		   else if(moonScore == 0){
			   lc.removeNode(moonN);
			   sc.removeNode(moonN);
		   }
		   moonScored = false;
	   }
	   if(darkScored == true) {
		   if(darkScore == 1) {
			   sc.removeNode(darkN);
			   lc.addNode(darkN);
		   }
		   else if(darkScore == 2) {
			   lc.removeNode(darkN);
			   sc.addNode(darkN);
		   }
		   else {
			   lc.removeNode(darkN);
			   sc.removeNode(darkN);
		   }
		   darkScored = false;
	   }
   }
   
   public void checkWinner() {
	   if(!winner) {
		   	if(dolphin1S == 3 || dolphin2S == 3) {
		   		winner = true;
		   		rc.addNode(planets);
		   	}
	   }
   }
   
   public float distanceFrom(Vector3f v1, Vector3f v2) {
   	return (float) Math.sqrt(Math.pow(v1.x()-v2.x(), 2) + Math.pow(v1.y() - v2.y(), 2) + Math.pow(v1.z() - v2.z(), 2)); 
   }
   
   
   public class StretchController extends AbstractController {
	   private float actionRate = .003f;
	   private float cycleTime = 2000.0f;
	   private float totalTime = 0.0f;
	   private float direction = 1.0f;
	   
	   @Override
	   protected void updateImpl(float elapsedTimeMillis) {
		   totalTime += elapsedTimeMillis;
		   float scaleAmt = 1.0f + direction * actionRate;
		   
		   if(totalTime > cycleTime) {
			   direction = -direction;
			   totalTime = 0.0f;
		   }
		   for(Node n : super.controlledNodesList) {
			   Vector3 curScale = n.getLocalScale();
			   curScale = Vector3f.createFrom(curScale.x() * scaleAmt, curScale.y(),curScale.z() * scaleAmt);
			   n.setLocalScale(curScale);
		   }
	   }
   }
   
   public class LiftController extends AbstractController {
	   private float actionRate = .003f;
	   private float cycleTime = 2000.0f;
	   private float totalTime = 0.0f;
	   private float direction = 1.0f;
	   
	   @Override
	   protected void updateImpl(float elapsedTimeMillis) {
		   totalTime += elapsedTimeMillis;
		   float liftAmt = direction * actionRate;
		   
		   if(totalTime > cycleTime) {
			   direction = -direction;
			   totalTime = 0.0f;
		   }
		   for(Node n : super.controlledNodesList) {
			   Vector3 curPos = n.getLocalPosition();
			   curPos = Vector3f.createFrom(curPos.x() , curPos.y() + liftAmt, curPos.z());
			   n.setLocalPosition(curPos);
		   }
	   }
   }
}