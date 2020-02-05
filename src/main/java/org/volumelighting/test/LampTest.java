package main.java.org.volumelighting.test;

import com.jme3.app.SimpleApplication;

import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.light.SpotLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.post.FilterPostProcessor;
import com.jme3.post.filters.BloomFilter;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;

import com.jme3.shadow.DirectionalLightShadowFilter;
import com.jme3.shadow.EdgeFilteringMode;


import main.java.org.volumelighting.vl.VolumeLightFilter;

 
public class LampTest extends SimpleApplication {
 
    
    private FilterPostProcessor filterPostProcessor;
    
    public static void main(String[] args) {
        LampTest app = new LampTest();
        app.start();
    }
      
    @Override
    public void simpleInitApp() {

        flyCam.setMoveSpeed(10f);
        this.setDisplayStatView(false);
        cam.lookAt(Vector3f.ZERO, Vector3f.UNIT_Y);
         
         //Background color - alpha should be very low
         viewPort.setBackgroundColor(new ColorRGBA(0.00f,0.00f,0.00f,0.01f));
         //
         filterPostProcessor = new FilterPostProcessor(assetManager);
                 
         //Scene
         Spatial scene= assetManager.loadModel("Models/lampScene.j3o");
         Node sceneAsNode=((Node)((Node)scene).getChild("Scene"));
         rootNode.attachChild(sceneAsNode);
         //Important - parent node should get CastAndReceive
         sceneAsNode.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);
         //Glow
         Material bulbMat = new Material(assetManager,  "Common/MatDefs/Misc/Unshaded.j3md");
         bulbMat.setColor("Color", new ColorRGBA(  1f,1f, 1.f, 0.01f));
         bulbMat.setColor("GlowColor",   ColorRGBA.Yellow);
         sceneAsNode.getChild("bulb").setMaterial(bulbMat); 
         BloomFilter bloom=new BloomFilter(BloomFilter.GlowMode.Objects);
         bloom.setExposurePower(1); 
         bloom.setDownSamplingFactor(2);  
         bloom.setBloomIntensity(4);  
         bloom.setBlurScale(1.5f);//1.5
         filterPostProcessor.addFilter(bloom);
         
          //Light
        DirectionalLight sun = new DirectionalLight();
        sun.setColor(ColorRGBA.White.mult(0.1f));
        sun.setDirection(new Vector3f(-0f,-1.0f,0.0f).normalizeLocal());
        sceneAsNode.addLight(sun);
        //
        AmbientLight al = new AmbientLight();
        al.setColor(ColorRGBA.White.mult(0.03f));
        sceneAsNode.addLight(al);
        
        // drone spotLight
        ColorRGBA droneColor=new ColorRGBA(0.8f , 0.6f , 0f,1.0f ).mult(0.5f);
        Vector3f pos= new Vector3f(1.4053185f, 5.5330953f, -1.5833765f);//sceneAsNode.getChild("bulb").getLocalTranslation().clone();
         //
        SpotLight spot = new SpotLight();
        spot.setSpotRange(6);
        spot.setSpotInnerAngle(0.1f);
        spot.setSpotOuterAngle(0.4f);
        spot.setColor(droneColor);
        spot.setPosition(pos);
        spot.setDirection(new Vector3f(-0f,-1.0f,0.0f)); 
        rootNode.addLight(spot);
        
        //
        VolumeLightFilter vsf = new VolumeLightFilter(spot, 128, 0.46f, rootNode);
        vsf.setInensity(3);
        filterPostProcessor.addFilter(vsf);
        viewPort.addProcessor(filterPostProcessor);
          
        //Shadows
         DirectionalLightShadowFilter dlsf = new DirectionalLightShadowFilter(assetManager, 512, 4);
         dlsf.setEdgeFilteringMode(EdgeFilteringMode.PCFPOISSON);
         dlsf.setShadowIntensity(0.1f);
         dlsf.setLight(sun);
         dlsf.setEnabled(true);
         filterPostProcessor.addFilter(dlsf);
         //
        for(Spatial child: sceneAsNode.getChildren())
           child.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);//CastAndReceive
       sceneAsNode.getChild("Plane").setShadowMode(RenderQueue.ShadowMode.Receive);//CastAndReceive
        
    }
    
    
 
    @Override
    public void simpleUpdate(float tpf) {
      
    }
   
    
    @Override
    public void simpleRender(RenderManager rm) {
        //TODO: add render code
    }
}
