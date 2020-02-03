package test.org.volumelighting.test;

import com.jme3.app.SimpleApplication;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.light.SpotLight;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.post.FilterPostProcessor;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.shadow.DirectionalLightShadowFilter;
import com.jme3.shadow.EdgeFilteringMode;
import org.volumelighting.vl.VolumeLightFilter;

public class SunTest extends SimpleApplication {
 
    
    private FilterPostProcessor filterPostProcessor;
    
    public static void main(String[] args) {
        SunTest app = new SunTest();
        app.start();
    }
      
    @Override
    public void simpleInitApp() {

        flyCam.setMoveSpeed(10f);
        this.setDisplayStatView(false);
        cam.lookAt(Vector3f.ZERO, Vector3f.UNIT_Y);
         //Background color - alpha should be very low
           viewPort.setBackgroundColor(new ColorRGBA(0.15f,0.15f,0.3f,0.1f));
       
         //Scene
         Spatial scene= assetManager.loadModel("Models/sunScene.j3o");
         Node sceneAsNode=((Node)((Node)scene).getChild("Scene"));
         rootNode.attachChild(sceneAsNode);
         //Important - parent node should get CastAndReceive
         sceneAsNode.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);
           
        
          //Light
        DirectionalLight sun = new DirectionalLight();
        sun.setColor(ColorRGBA.White.mult(1.5f));
        sun.setDirection(new Vector3f(-0.31799296f,-0.30489448f,0.89773023f).normalizeLocal());
        sceneAsNode.addLight(sun);
        // 
        AmbientLight al = new AmbientLight();
        al.setColor(ColorRGBA.White.mult(0.1f));
        sceneAsNode.addLight(al);
        
        // drone spotLight
        ColorRGBA droneColor=new ColorRGBA(0.8f , 0.6f , 0f,1.0f ).mult(0.8f);
        SpotLight spot = new SpotLight();
        //  
        spot.setSpotRange(75);
        spot.setSpotInnerAngle(0.1f);
        spot.setSpotOuterAngle(1.2f);
        spot.setColor(droneColor);
        spot.setPosition(sceneAsNode.getChild("Sun").getLocalTranslation());
        spot.setDirection(new Vector3f(-0.31799296f,-0.30489448f,0.89773023f)); 
        rootNode.addLight(spot);
       
        //
        filterPostProcessor = new FilterPostProcessor(assetManager);
        VolumeLightFilter vsf = new VolumeLightFilter(spot, 512,0.9f,  rootNode);
       // VolumeLightFilter vsf = new VolumeLightFilter(spot, 1024,  rootNode);
        vsf.setInensity(15);
        filterPostProcessor.addFilter(vsf);
        viewPort.addProcessor(filterPostProcessor);
          
        //Shadows
         DirectionalLightShadowFilter dlsf = new DirectionalLightShadowFilter(assetManager, 512, 3);
         dlsf.setEdgeFilteringMode(EdgeFilteringMode.PCFPOISSON);
         dlsf.setShadowIntensity(0.1f);
         dlsf.setLambda(0.1f);
         dlsf.setLight(sun);
         dlsf.setEnabled(true);
         filterPostProcessor.addFilter(dlsf);
         //
        for(Spatial child: sceneAsNode.getChildren())
           child.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);//CastAndReceive
        
    }
    
    
 
    @Override
    public void simpleUpdate(float tpf) {
      
    }
   
     
    @Override
    public void simpleRender(RenderManager rm) {
        //TODO: add render code
    }
}
