package main.java.org.volumelighting.vl;

import com.jme3.asset.AssetManager;
import com.jme3.export.InputCapsule;
import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.export.OutputCapsule;
import com.jme3.light.SpotLight;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.post.Filter;
import com.jme3.renderer.Camera;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.VertexBuffer;
import com.jme3.texture.FrameBuffer;
import com.jme3.texture.Image.Format;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture2D;
import com.jme3.ui.Picture;
import com.jme3.util.BufferUtils;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class VolumeLightFilter extends Filter {

    private Filter.Pass lightVolumePass;
    private RenderManager renderManager;
    private ViewPort viewPort;
    private VLRenderer shadowVolumeRenderer;
    
    private Material volumetricLight_mat;
    private Geometry lightVolume;
    private int resolution;
    private float intensity = 10f;
    
    
    private Camera lightCam;
    private SpotLight light;
    private FrustumVolumeMesh fvm;
    
    
    public VolumeLightFilter(SpotLight spot, int resolution, float startFrom, Node rn) {
        super("Volumetric Light Filter");
       
        this.light = spot;
        this.resolution = resolution;
        
        this.lightCam = new Camera(resolution, resolution);
        this.lightCam.setFrustumPerspective(this.light.getSpotOuterAngle() * FastMath.RAD_TO_DEG * 2.0f, 1, startFrom, this.light.getSpotRange());
         this.lightCam.update();
        
        // !! NEED TO SET METHOD OF LIGHT FRONT PLANE
        
        fvm = new FrustumVolumeMesh(this.resolution, this.lightCam);
        this.lightVolume = new Geometry("fvm", fvm);
        this.lightVolume.setIgnoreTransform(false);
        this.lightVolume.setCullHint(Spatial.CullHint.Always);
        rn.attachChild(this.lightVolume); // pretty dirty
        
        syncLightCam();
        
    }


    
    private void syncLightCam() {
        this.lightCam.getRotation().lookAt(this.light.getDirection(), this.lightCam.getUp());
        this.lightCam.setLocation(this.light.getPosition());
    }
    
    @Override
    protected void initFilter(AssetManager manager, RenderManager renderManager, ViewPort vp, int w, int h) {
        
       // System.out.println("Init Filter");
        
        shadowVolumeRenderer = new VLRenderer(manager, this.resolution);
        shadowVolumeRenderer.setShadowCam(lightCam);
        shadowVolumeRenderer.initialize(renderManager, vp);
        
        this.renderManager = renderManager;
        this.viewPort = vp;
        
        postRenderPasses = new ArrayList<Filter.Pass>();

        volumetricLight_mat = new Material(manager, "MatDefs/VolumetricLighting/VolumetricLight.j3md");

        volumetricLight_mat.getAdditionalRenderState().setWireframe(false); // good for debugging
         // volumeShadow_mat.setTexture("CookieMap", assetManager.loadTexture("Textures/Cookie2.png")); // Cookie coming soon
         volumetricLight_mat.setMatrix4("LightViewProjectionMatrix", this.lightCam.getViewProjectionMatrix());
         volumetricLight_mat.setMatrix4("LightViewProjectionInverseMatrix", this.lightCam.getViewProjectionMatrix().invert());
         volumetricLight_mat.setVector3("CameraPos", vp.getCamera().getLocation());
         volumetricLight_mat.setColor("LightColor", this.light.getColor());
         volumetricLight_mat.setFloat("LightIntensity", this.intensity);
         volumetricLight_mat.setVector2("LinearDepthFactorsLight", getLinearDepthFactors(lightCam));
         volumetricLight_mat.setVector2("LinearDepthFactorsCam", getLinearDepthFactors(vp.getCamera()));
         volumetricLight_mat.setVector2("LightNearFar", new Vector2f(this.lightCam.getFrustumNear(), fvm.farPlaneGridDistance())); // this is nasty, but is silly to calculate every frame
        
        lightVolume.setMaterial(volumetricLight_mat);
        
        lightVolumePass = new Filter.Pass() {
//            @Override
//            public boolean requiresDepthAsTexture() {
//                return true;
//            }
            
//            @Override
//            public boolean requiresSceneAsTexture() {
//                return true;
//            }

        };
        
        lightVolumePass.init(renderManager.getRenderer(), w, h, Format.RGBA32F, Format.Depth, 1, true); // 
        // could perhaps change to a more supported format, and render front faces then back
        
        material = new Material(manager, "MatDefs/VolumetricLighting/VolumetricLightFilter.j3md");
        material.setTexture("LightingVolumeTex", lightVolumePass.getRenderedTexture());
        shadowVolumeRenderer.setPostShadowMaterial2(volumetricLight_mat);
    }
    
    
    // pre calculate the depth linerization factors so they can be passed to the shader since they rarely change
    public Vector2f getLinearDepthFactors(Camera cam) {
        float near = cam.getFrustumNear();
        float far  = cam.getFrustumFar();
        float a = far / (far - near);
        float b = far * near / (near - far);

        return new Vector2f(a,b);
    }
     
   

    @Override
    protected Material getMaterial() {
        return material;
    }

    @Override
    protected boolean isRequiresDepthTexture() {
        return true;
    }
    

    @Override
    protected void postQueue(RenderQueue queue) {
         shadowVolumeRenderer.postQueue(queue);
    }
    
    @Override
    protected void postFrame(RenderManager renderManager, ViewPort viewPort, FrameBuffer prevFilterBuffer, FrameBuffer sceneBuffer) {
      
        shadowVolumeRenderer.postFrame(sceneBuffer);
        
        renderManager.setCamera(viewPort.getCamera(), false);
       
        volumetricLight_mat.setTexture("SceneDepthTexture", sceneBuffer.getDepthBuffer().getTexture());
        
        // sync volume 
        lightVolume.setLocalTranslation(this.lightCam.getLocation());
        lightVolume.setLocalRotation(this.lightCam.getRotation());
        
        renderManager.getRenderer().setFrameBuffer(lightVolumePass.getRenderFrameBuffer());
        renderManager.getRenderer().clearBuffers(true, true, true);   
        //renderManager.setForcedTechnique("ShadowVolume");
        renderManager.renderGeometry(lightVolume);
        //renderManager.setForcedTechnique(null);
        
    }

    @Override
    protected void preFrame(float tpf) {
        syncLightCam();
        
        volumetricLight_mat.setVector3("CameraPos", viewPort.getCamera().getLocation());
        volumetricLight_mat.setVector3("LightPos", this.lightCam.getLocation());
    }
     
     
    @Override
    public void write(JmeExporter ex) throws IOException {
        super.write(ex);
        OutputCapsule oc = ex.getCapsule(this);
        //oc.write(intensity, "Intensity", 1.0f);
    }

    @Override
    public void read(JmeImporter im) throws IOException {
        super.read(im);
        InputCapsule ic = im.getCapsule(this);
        //intensity = ic.readFloat("Intensity", 1.0f);
    }

    /**
     * @return the intensity
     */
    public float getIntensity() {
        return intensity;
    }

    /**
     * @param intensity the intensity to set
     */
    public void setInensity(float intensity) {
        this.intensity = intensity;
        if (volumetricLight_mat!=null) {
            volumetricLight_mat.setFloat("LightIntensity", intensity);
        }
    }
}

class FrustumVolumeMesh extends Mesh {
    
    private int resolution;
    private Camera targetCam;

    private List<Vector3f> finalVertices;
    private List<Integer> finalFaces;
        
    public FrustumVolumeMesh(int resolution, Camera targetCam) {
        this.targetCam = targetCam;
        this.resolution = resolution;
        generateMesh();
    }
    
    public float farPlaneGridDistance() {
        //return  0.5f/FastMath.tan(2f * FastMath.atan(targetCam.getFrustumTop() *0.5f / targetCam.getFrustumNear()));
        return targetCam.getFrustumFar();
    }
    
    
    
    public void generateMesh() {
        finalVertices = new ArrayList<Vector3f>(); //(resolution*resolution + 1)
        finalFaces = new ArrayList<Integer>(); //(resolution-1)^2+(4*(resolution-1))

        Vector3f vert;// = new Vector3f();


        float stepX = 2f*(targetCam.getFrustumLeft()/targetCam.getFrustumNear())/resolution; 
        float stepY = 2f*(targetCam.getFrustumTop()/targetCam.getFrustumNear())/resolution; 


        //stepX = stepY = step;
        //offsetX = offsetY = offset;

        float x,y;
        float z = targetCam.getFrustumNear();

        stepX *= z;
        stepY *= z;

        float offsetX = -stepX*resolution/2.0f;
        float offsetY = -stepY*resolution/2.0f;

        finalVertices.add(new Vector3f(0,0,z));

        //top ring

        for (int ii=0; ii<resolution; ii++) {
            y = (float)ii*stepY+offsetY;
            vert = new Vector3f(offsetX, y, z);
            finalVertices.add(vert);
        }

        for (int ii=1; ii<resolution-1; ii++) {
            x = (float)ii*stepX+offsetX;
            vert = new Vector3f(x, offsetY, z);
            finalVertices.add(vert);
            vert = new Vector3f(x, -1f*offsetY-stepY, z);
            finalVertices.add(vert);
        }

        x = -1f*offsetX-stepX;
        for (int ii=0; ii<resolution; ii++) {
            y = (float)ii*stepY+offsetY;
            vert = new Vector3f(x, y, z);
            finalVertices.add(vert);
        }

        stepX /= z;
        stepY /= z;

        // front grid

        z = farPlaneGridDistance();
        stepX *= z;
        stepY *= z;
        offsetX = -stepX*resolution/2.0f;
        offsetY = -stepY*resolution/2.0f;

        for (int ii=0; ii<resolution; ii++) {
            for (int jj=0; jj<resolution; jj++) {
                x = ii*stepX+offsetX;
                y = jj*stepY+offsetY;
                vert = new Vector3f(
                        x,
                        y,
                        z);
                   finalVertices.add(vert);
           }
        }


         //faces
         for (int ii=1; ii<resolution; ii++) {
             for (int jj=1; jj<resolution; jj++) {

                    finalFaces.add(calcIndex(jj-1, ii-1));
                    finalFaces.add(calcIndex(jj, ii-1));
                    finalFaces.add(calcIndex(jj-1, ii));

                    finalFaces.add(calcIndex(jj, ii)); 
                    finalFaces.add(calcIndex(jj-1, ii));// swap this with the below if faces are on wrong side
                    finalFaces.add(calcIndex(jj, ii-1)); 
             }
         }
         
         //edges
        int p1,p2,p3,p4;
        for (int ii=1; ii<resolution; ii++) {
            p1 = calcTopIndex(ii-1, 0);
            p2 = calcTopIndex(ii, 0);
            p3 = calcIndex(ii-1, 0);
            p4 = calcIndex(ii, 0);

            finalFaces.add(p1);
            finalFaces.add(p2);
            finalFaces.add(p3);

            finalFaces.add(p3);
            finalFaces.add(p2);
            finalFaces.add(p4);  

            finalFaces.add(p1);
            finalFaces.add(0);
            finalFaces.add(p2);


            p1 = calcTopIndex(ii-1, resolution-1);
            p2 = calcTopIndex(ii, resolution-1);
            p3 = calcIndex(ii-1, resolution-1);
            p4 = calcIndex(ii, resolution-1);

            finalFaces.add(p1);
            finalFaces.add(p3);
            finalFaces.add(p2);

            finalFaces.add(p3);
            finalFaces.add(p4); 
            finalFaces.add(p2);

            finalFaces.add(p1);
            finalFaces.add(p2);
            finalFaces.add(0);

            p1 = calcTopIndex(0, ii-1);
            p2 = calcTopIndex(0, ii);
            p3 = calcIndex(0, ii-1);
            p4 = calcIndex(0, ii);

            finalFaces.add(p1);
            finalFaces.add(p3);
            finalFaces.add(p2);

            finalFaces.add(p3);
            finalFaces.add(p4);  
            finalFaces.add(p2);

            finalFaces.add(p1);
            finalFaces.add(p2);
            finalFaces.add(0);

            p1 = calcTopIndex(resolution-1, ii-1);
            p2 = calcTopIndex(resolution-1, ii);
            p3 = calcIndex(resolution-1, ii-1);
            p4 = calcIndex(resolution-1, ii);

            finalFaces.add(p1);
            finalFaces.add(p2);
            finalFaces.add(p3);

            finalFaces.add(p3);
            finalFaces.add(p2);
            finalFaces.add(p4);  

            finalFaces.add(p1);

            finalFaces.add(0);
            finalFaces.add(p2);
        }

        setBuffer(VertexBuffer.Type.Position, 3, BufferUtils.createFloatBuffer( (Vector3f[])finalVertices.toArray(new Vector3f[finalVertices.size()]) ));
        setBuffer(VertexBuffer.Type.Index, 1, BufferUtils.createIntBuffer( toIntArray(finalFaces) ));

        updateBound();
        createCollisionData();

    }
    
    // calculates the index for a far plane face vert
    private int calcIndex(int row, int col) {       
        int topVerts = (resolution-1)*4;
        return col * resolution + row + 1 + topVerts;
    }
    
    // calculates the index for near plane ring vert
    private int calcTopIndex(int row, int col) {
        int index = 1;
        if (col == 0) {
            return row+1;
        }
        index += resolution;
        index += (col-1)*2;
        if (col == resolution-1) {
            index += row;
        } else if (row == resolution-1) {
            index+=1;
        }
        return index;
    }
    
  
    static public int[] toIntArray(List<Integer> list) {
        int[] ret = new int[list.size()];
        int i =0;
        for (Integer ii : list) {
            ret[i++] = ii.intValue();
        }
        return ret;
    }
    
    
    
}


