/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package  main.java.org.volumelighting.vl;


import com.jme3.asset.AssetManager;
import com.jme3.light.SpotLight;
import com.jme3.material.Material;
import com.jme3.math.FastMath;
import com.jme3.math.Matrix4f;
import com.jme3.math.Vector3f;
import com.jme3.post.SceneProcessor;
import com.jme3.renderer.Camera;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.Renderer;
import com.jme3.renderer.ViewPort;
import com.jme3.renderer.queue.GeometryList;
import com.jme3.renderer.queue.OpaqueComparator;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;
import com.jme3.shadow.AbstractShadowRenderer;
import com.jme3.shadow.ShadowUtil;
import com.jme3.texture.FrameBuffer;
import com.jme3.texture.Image;
import com.jme3.texture.Image.Format;
import com.jme3.texture.Texture2D;
import com.jme3.ui.Picture;

/**
 * BasicShadowRenderer uses standard shadow mapping with one map
 * it's useful to render shadows in a small scene, but edges might look a bit jagged.
 * 
 *  * Toucher - This is a massively cut down crappy version
 * 
 * @author Kirill Vainer
 * @author Toucher
 *
 */

public class VLRenderer extends AbstractShadowRenderer {

    private RenderManager renderManager;
    private ViewPort viewPort;
    private FrameBuffer shadowFB;
    public Texture2D shadowMap;
    private Camera shadowCam;
    private Material preshadowMat;
    private Material postshadowMat;
    private Picture dispPic = new Picture("Picture");
    private boolean noOccluders = false;
    private Vector3f[] points = new Vector3f[8];
    private Vector3f direction = new Vector3f();

    
    public SpotLight spot; 
   
    
    /**
     * Creates a BasicShadowRenderer
     * @param manager the asset manager
     * @param size the size of the shadow map (the map is square)
     */
    public VLRenderer(AssetManager manager, int size) {
        shadowFB = new FrameBuffer(size, size, 1);
        shadowMap = new Texture2D(size, size, Image.Format.Depth);
        shadowFB.setDepthTexture(shadowMap);
        //shadowCam = new Camera(size, size);

        preshadowMat = new Material(manager, "Common/MatDefs/Shadow/PreShadow.j3md");


       dispPic.setTexture(manager, shadowMap, false);

        for (int i = 0; i < points.length; i++) {
            points[i] = new Vector3f();
        }
    }

 /**
     * set the post shadow material for this renderer
     *
     * @param postShadowMat
     */
    protected final void setPostShadowMaterial2(Material postShadowMat) {
        this.postshadowMat = postShadowMat;

        postshadowMat.setTexture("ShadowDepthMap", shadowMap);

    }
    
    public void setShadowCam(Camera cam) {
        this.shadowCam = cam;
    }
    
    
    
    protected void updateCam(Camera viewCam) {

//        float zFar = zFarOverride;
//        if (zFar == 0) {
//          float  zFar = viewCam.getFrustumFar();
//        }

        //We prevent computing the frustum points and splits with zeroed or negative near clip value
//        float frustumNear = Math.max(viewCam.getFrustumNear(), 0.001f);
//        ShadowUtil.updateFrustumPoints(viewCam, frustumNear, zFar, 1.0f, points);
        //shadowCam.setDirection(direction);

        //shadowCam.setFrustumPerspective(spot.getSpotOuterAngle() * FastMath.RAD_TO_DEG * 2.0f, 1, 1f, spot.getSpotRange());
//        shadowCam.getRotation().lookAt(spot.getDirection(), shadowCam.getUp());
 //       shadowCam.setLocation(spot.getPosition());

  //      shadowCam.update();
  //      shadowCam.updateViewProjection();

    }
    
    protected GeometryList sceneReceivers;
    //@SuppressWarnings("fallthrough")
    public void postQueue(RenderQueue rq) {
        
        GeometryList occluders = new GeometryList(new OpaqueComparator());//rq.getShadowQueueContent(RenderQueue.ShadowMode.Cast);
        sceneReceivers = new GeometryList(new OpaqueComparator());//rq.getShadowQueueContent(RenderQueue.ShadowMode.Receive);
//        if (sceneReceivers.size() == 0 || occluders.size() == 0) {
//            return;
//        }

        updateCam(viewPort.getCamera());

        Renderer r = renderManager.getRenderer();
        renderManager.setForcedMaterial(preshadowMat);
        renderManager.setForcedTechnique("PreShadow");

       // for (int shadowMapIndex = 0; shadowMapIndex < nbShadowMaps; shadowMapIndex++) {
//
//            if (debugfrustums) {
//                doDisplayFrustumDebug(shadowMapIndex);
//            }
        int shadowMapIndex = 0;
            renderShadowMap(shadowMapIndex, occluders, sceneReceivers);

        //}

//        debugfrustums = false;
//        if (flushQueues) {
//            occluders.clear();
//        }
        //restore setting for future rendering
        r.setFrameBuffer(viewPort.getOutputFrameBuffer());
        renderManager.setForcedMaterial(null);
        renderManager.setForcedTechnique(null);
        renderManager.setCamera(viewPort.getCamera(), false);

    }
    
    Matrix4f lightViewProjectionsMatrix = new Matrix4f();
    protected GeometryList shadowMapOccluders = new GeometryList(new OpaqueComparator());
    
    protected void renderShadowMap(int shadowMapIndex, GeometryList occluders, GeometryList receivers) {
       // shadowMapOccluders = getOccludersToRender(shadowMapIndex, occluders, receivers, shadowMapOccluders);
shadowMapOccluders = getOccludersToRender(shadowMapIndex, occluders);        
//Camera shadowCam = getShadowCam(shadowMapIndex);

        //saving light view projection matrix for this split            
        lightViewProjectionsMatrix.set(shadowCam.getViewProjectionMatrix());
        renderManager.setCamera(shadowCam, false);

        renderManager.getRenderer().setFrameBuffer(shadowFB);
        renderManager.getRenderer().clearBuffers(false, true, false);

        // render shadow casters to shadow map
        viewPort.getQueue().renderShadowQueue(shadowMapOccluders, renderManager, shadowCam, true);
        
    }
    protected GeometryList getOccludersToRender(int shadowMapIndex, GeometryList sceneOccluders, GeometryList sceneReceivers, GeometryList shadowMapOccluders) {
        getGeometriesInCamFrustum(sceneOccluders, shadowCam, shadowMapOccluders);
        return shadowMapOccluders;
    }
    
    public static void getGeometriesInCamFrustum(GeometryList inputGeometryList,
            Camera camera,
            GeometryList outputGeometryList) {
        for (int i = 0; i < inputGeometryList.size(); i++) {
            Geometry g = inputGeometryList.get(i);
            int planeState = camera.getPlaneState();
            camera.setPlaneState(0);
            //if (camera.contains(g.getWorldBound()) != Camera.FrustumIntersect.Outside) {
                outputGeometryList.add(g);
          //  }
            camera.setPlaneState(planeState);
        }

    }

    //@Override
    protected GeometryList getOccludersToRender(int shadowMapIndex, GeometryList shadowMapOccluders) {
        for (Spatial scene : viewPort.getScenes()) {
            ShadowUtil.getGeometriesInCamFrustum(scene, shadowCam, RenderQueue.ShadowMode.Cast, shadowMapOccluders);
        }
        return shadowMapOccluders;
    }
    
    public void initialize(RenderManager rm, ViewPort vp) {
        renderManager = rm;
        viewPort = vp;

        reshape(vp, vp.getCamera().getWidth(), vp.getCamera().getHeight());
    }

    public boolean isInitialized() {
        return viewPort != null;
    }

    /**
     * returns the light direction used for this processor
     * @return 
     */
    public Vector3f getDirection() {
        return direction;
    }

    /**
     * sets the light direction to use to computs shadows
     * @param direction 
     */
    public void setDirection(Vector3f direction) {
        this.direction.set(direction).normalizeLocal();
    }

    /**
     * debug only
     * @return 
     */
    public Vector3f[] getPoints() {
        return points;
    }

    /**
     * debug only
     * returns the shadow camera 
     * @return 
     */
    public Camera getShadowCamera() {
        return shadowCam;
    }
    public void showPic() {
        if (true) return;
        //Camera cam = vp.getCamera();
        renderManager.setCamera(shadowCam, true);
        int h = shadowCam.getHeight();
        dispPic.setPosition(200, 0);
        dispPic.setWidth(200);
        dispPic.setHeight(200);
        
        dispPic.updateGeometricState();
        renderManager.renderGeometry(dispPic);
        
        //rm.setCamera(cam, false);
    
    }

//    public void postQueue(RenderQueue rq) {
//        GeometryList occluders = rq.getShadowQueueContent(ShadowMode.Cast);
//        if (occluders.size() == 0) {
//            noOccluders = true;
//            return;
//        } else {
//            noOccluders = false;
//        }
//
//        GeometryList receivers = rq.getShadowQueueContent(ShadowMode.Receive);
//
//        // update frustum points based on current camera
//        Camera viewCam = viewPort.getCamera();
//        ShadowUtil.updateFrustumPoints(viewCam,
//                viewCam.getFrustumNear(),
//                viewCam.getFrustumFar(),
//                1.0f,
//                points);
//
//        Vector3f frustaCenter = new Vector3f();
//        for (Vector3f point : points) {
//            frustaCenter.addLocal(point);
//        }
//        frustaCenter.multLocal(1f / 8f);
//
//        // update light direction
//        shadowCam.setProjectionMatrix(null);
//        shadowCam.setParallelProjection(true);
////        shadowCam.setFrustumPerspective(45, 1, 1, 20);
//
//        shadowCam.lookAtDirection(direction, Vector3f.UNIT_Y);
//        shadowCam.update();
//        shadowCam.setLocation(frustaCenter);
//        shadowCam.update();
//        shadowCam.updateViewProjection();
//
//        // render shadow casters to shadow map
//        ShadowUtil.updateShadowCamera(occluders, receivers, shadowCam, points);
//
//        Renderer r = renderManager.getRenderer();
//        renderManager.setCamera(shadowCam, false);
//        renderManager.setForcedMaterial(preshadowMat);
//
//        r.setFrameBuffer(shadowFB);
//        r.clearBuffers(false, true, false);
//        viewPort.getQueue().renderShadowQueue(ShadowMode.Cast, renderManager, shadowCam, true);
//        r.setFrameBuffer(viewPort.getOutputFrameBuffer());
//
//        renderManager.setForcedMaterial(null);
//        renderManager.setCamera(viewCam, false);
//    }

    /**
     * debug only
     * @return 
     */
    public Picture getDisplayPicture() {
        return dispPic;
    }

    public void postFrame(FrameBuffer out) {
        //showPic();
        if (!noOccluders) {
            postshadowMat.setMatrix4("LightViewProjectionMatrix", shadowCam.getViewProjectionMatrix());
//            renderManager.setForcedMaterial(postshadowMat);
//            viewPort.getQueue().renderShadowQueue(RenderQueue.ShadowMode.Receive, renderManager, viewPort.getCamera(), true);
//            renderManager.setForcedMaterial(null);
        }
        
    }

    public void preFrame(float tpf) {
    }

    public void cleanup() {
    }

    public void reshape(ViewPort vp, int w, int h) {
        dispPic.setPosition(w / 20f, h / 20f);
        dispPic.setWidth(w / 5f);
        dispPic.setHeight(h / 5f);
    }

    @Override
    protected void initFrustumCam() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected void updateShadowCams(Camera camera) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected Camera getShadowCam(int i) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected void getReceivers(GeometryList gl) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected void clearMaterialParameters(Material mtrl) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected void setMaterialParameters(Material mtrl) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected boolean checkCulling(Camera camera) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}

