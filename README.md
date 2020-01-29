# VolumetricLighting
Volumetric Lighting library for JMonkey Game Engine


### Usage:

//1. FPP to add the filter
FilterPostProcessor filterPostProcessor = new FilterPostProcessor(assetManager);
//2. Basic settings
ColorRGBA color=new ColorRGBA(0.8f , 0.6f , 0f,1.0f ).mult(0.5f);
Vector3f pos= new Vector3f(1.4053185f, 5.5330953f, -1.5833765f); 
//3. Spotlight which is contains basic config data and provides real lighting
SpotLight spot = new SpotLight();
spot.setSpotRange(6);
spot.setSpotInnerAngle(0.1f); //distance from pos - origin
spot.setSpotOuterAngle(0.4f); //width of the end part
spot.setColor(color);
spot.setPosition(pos);
spot.setDirection(new Vector3f(-0f,-1.0f,0.0f)); 
rootNode.addLight(spot);        
//4. Filter config 
VolumeLightFilter vsf = new VolumeLightFilter(spot, 128, 0.46f, rootNode);
vsf.setInensity(3);
//5. Add
filterPostProcessor.addFilter(vsf);
viewPort.addProcessor(filterPostProcessor);

### Screenshots

![VolumetricLighting1](../master/img/VolumetricLighting1.jpg)

![VolumetricLighting2](../master/img/VolumetricLighting2.jpg)

![VolumetricLighting3](../master/img/VolumetricLighting3.jpg)

![VolumetricLighting4](../master/img/VolumetricLighting4.jpg)

![VolumetricLighting5](../master/img/VolumetricLighting5.jpg)


#### Credits

https://hub.jmonkeyengine.org/t/volumetric-lighting-filter-wip/27490
 
