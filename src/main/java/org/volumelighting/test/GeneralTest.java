package main.java.org.volumelighting.test;

import com.jme3.app.SimpleApplication;
import com.jme3.cinematic.MotionPath;

import com.jme3.cinematic.events.MotionEvent;

import com.jme3.light.PointLight;
import com.jme3.light.SpotLight;
import com.jme3.material.Material;

import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;

import com.jme3.math.Vector3f;

import com.jme3.post.FilterPostProcessor;

import com.jme3.renderer.RenderManager;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Sphere;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import main.java.org.volumelighting.vl.VolumeLightFilter;

public class GeneralTest extends SimpleApplication {

    private Random random = new Random(4l);
    
    public static void main(String[] args) {
        GeneralTest app = new GeneralTest();
        app.start();
    }
      
    @Override
    public void simpleInitApp() {

        flyCam.setMoveSpeed(20f);
        cam.lookAt(Vector3f.ZERO, Vector3f.UNIT_Y);
        
        for (int ii=0; ii<100; ii++) {
            addRandomObject();
        }
         
       rootNode.attachChild(randObjects);
        
       filterPostProcessor = new FilterPostProcessor(assetManager);
       addSpotlightDrone(new ColorRGBA(1.0f, 0.96f, 0.7f, 1.0f).mult(.8f), 20f * FastMath.DEG_TO_RAD);
       addSpotlightDrone(ColorRGBA.Cyan, 8f * FastMath.DEG_TO_RAD);
       addSpotlightDrone(new ColorRGBA(1.0f, 0.96f, 0.7f, 1.0f).mult(6f), 4f * FastMath.DEG_TO_RAD);
       viewPort.addProcessor(filterPostProcessor);
        
    }
    
    static final private float DRONE_RANGE = 80f;
    
    //List<MotionPath> motionPaths = new ArrayList<MotionPath>();
    List<MotionEvent> motionControls = new ArrayList<MotionEvent>();
    
    List<PointLight> pointLights = new ArrayList<PointLight>();
    List<SpotLight> spotLights = new ArrayList<SpotLight>();
    List<Spatial> drones = new ArrayList<Spatial>();
    List<Spatial> targets = new ArrayList<Spatial>();
    
    private FilterPostProcessor filterPostProcessor;
    
    private final float numOfObjects = 4;
    private final float randomObjectMaxRange = 4;
    private Node randObjects = new Node("randObjects");
    
        
    
    private void addSpotlightDrone(ColorRGBA droneColor, float coneSize) {
        
        
        
        // drone box mesh
        Box b = new Box(.5f,.5f,1);
        Geometry g = new Geometry("drone", b);
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", droneColor);
        g.setMaterial(mat);
        rootNode.attachChild(g);
        drones.add(g);
        
        //target Node
        Node target = new Node("droneTarget");
        rootNode.attachChild(target);
        targets.add(target);
        
        // drone spotLight
        SpotLight spot = new SpotLight();
        spot.setSpotRange(200);
        spot.setSpotInnerAngle(coneSize/10);
        spot.setSpotOuterAngle(coneSize*1.4f);
        spot.setColor(droneColor);
        rootNode.addLight(spot);
        spotLights.add(spot);
        
        float duration = 10f + random.nextFloat()*80f;
        
        // motion path for drone to follow
        MotionPath path = new MotionPath();
        for (int ii=0; ii<10; ii++) {
          path.addWayPoint(new Vector3f(random.nextFloat()*DRONE_RANGE-DRONE_RANGE/2f, random.nextFloat()*DRONE_RANGE-DRONE_RANGE/2f, random.nextFloat()*DRONE_RANGE-DRONE_RANGE/2f));
        }
//        path.enableDebugShape(assetManager, rootNode);
        path.addWayPoint(path.getWayPoint(0));
       
        MotionEvent motionControl = new MotionEvent(g,path);
        motionControl.setInitialDuration(duration);
        motionControl.play();    
        motionControls.add(motionControl);
        
        // motion path for drone to target
        path = new MotionPath();
        for (int ii=0; ii<10; ii++) {
          path.addWayPoint(new Vector3f(random.nextFloat()*DRONE_RANGE-DRONE_RANGE/2f, random.nextFloat()*DRONE_RANGE-DRONE_RANGE/2f, random.nextFloat()*DRONE_RANGE-DRONE_RANGE/2f));
        }
        path.addWayPoint(path.getWayPoint(0));
//        path.enableDebugShape(assetManager, rootNode);
       
        motionControl = new MotionEvent(target,path);
        motionControl.setInitialDuration(duration);
        motionControl.play();    
        motionControls.add(motionControl);
        
//        SpotLightShadowFilter slsf = new SpotLightShadowFilter(assetManager, 128);
//        slsf.setLight(spot);    
//        slsf.setShadowIntensity(.8f);
//        slsf.setEdgeFilteringMode(EdgeFilteringMode.PCFPOISSON);  
//        filterPostProcessor.addFilter(slsf);
        
        VolumeLightFilter vsf = new VolumeLightFilter(spot, 128,coneSize*5,  rootNode);
        filterPostProcessor.addFilter(vsf);
                
    }
    
   
    
    
    private void addRandomObject() {
        
        float r = random.nextFloat();
        
        Node node = new Node("Random Object");
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", ColorRGBA.White.mult(.05f));
        
        if (r < 1f/numOfObjects) {
            Box b = new Box(1,1,1);
            Geometry g = new Geometry("randBox", b);
            g.scale(.2f);
            g.setLocalTranslation(random.nextFloat()*randomObjectMaxRange, random.nextFloat()*randomObjectMaxRange, random.nextFloat()*randomObjectMaxRange);
            node.attachChild(g);
        } else if (r < 2f/numOfObjects) {
            Sphere s = new Sphere(20,20,.5f);
            Geometry g = new Geometry("randSphere", s);
            g.setLocalTranslation(random.nextFloat()*randomObjectMaxRange, random.nextFloat()*randomObjectMaxRange, random.nextFloat()*randomObjectMaxRange);
            node.attachChild(g);
        } else if (r < 3f/numOfObjects) {
          // Node jaime = (Node)assetManager.loadModel("Models/Jaime/Jaime.j3o");
          // jaime.setLocalTranslation(random.nextFloat()*randomObjectMaxRange, random.nextFloat()*randomObjectMaxRange, random.nextFloat()*randomObjectMaxRange);
          // node.attachChild(jaime);
        } else if (r < 4f/numOfObjects) {
          // Spatial teapot = assetManager.loadModel("Models/Teapot/Teapot.obj");
          // teapot.setLocalTranslation(random.nextFloat()*randomObjectMaxRange, random.nextFloat()*randomObjectMaxRange, random.nextFloat()*randomObjectMaxRange);
          // node.attachChild(teapot);
        }
        node.scale(5f);
        node.rotate(random.nextFloat()*10, random.nextFloat()*10, random.nextFloat()*10);
        node.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);
        node.setMaterial(mat);
        
        randObjects.attachChild(node);
    }
    
   float total = -5;
    @Override
    public void simpleUpdate(float tpf) {
        // syncronize spot lights with their parent drones
        for (int ii=0; ii<drones.size(); ii++) {           
            drones.get(ii).lookAt(targets.get(ii).getLocalTranslation(), Vector3f.UNIT_Y);
            spotLights.get(ii).setPosition(drones.get(ii).getLocalTranslation());
            spotLights.get(ii).setDirection(drones.get(ii).getLocalRotation().mult(Vector3f.UNIT_Z));
        }
        
        // simple hack to loop motion paths
        for (MotionEvent control : motionControls) {
            if (!control.isEnabled()) control.setEnabled(true);
        }
        
//        total += tpf;
//        if (total > 0 && total < 10) {
//            total += 100;
//            System.out.println("eh");
//            for (Filter f : filterPostProcessor.getFilterList()) {
//                f.setEnabled(false);
//            }
//        }
        
        // makes for neater videos
      // cam.lookAt(Vector3f.ZERO, Vector3f.UNIT_Y);
    }
   
    
    @Override
    public void simpleRender(RenderManager rm) {
        //TODO: add render code
    }
}
