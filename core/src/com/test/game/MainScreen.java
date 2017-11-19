package com.test.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.loader.G3dModelLoader;
import com.badlogic.gdx.graphics.g3d.utils.FirstPersonCameraController;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.UBJsonReader;
import com.mygdx.shadowtest.DirectionalLight;
import com.mygdx.shadowtest.MovingPointLight;
import com.mygdx.shadowtest.PointLight;
import com.mygdx.shadowtest.ShadowEngine;

/**
 * Main screen
 *
 * @author jb
 */
public class MainScreen extends ApplicationAdapter {

    private Array<ModelInstance> lstModel;
    private PerspectiveCamera camera;
    private FirstPersonCameraController firstPersonCameraController;
    private ShadowEngine shadowEngine = new ShadowEngine();

    /**
     * Called on start
     */
    @Override
    public void create() {
        this.lstModel = new Array<ModelInstance>();
        initCameras();
        this.shadowEngine =  new ShadowEngine();
        this.shadowEngine.init();
        this.shadowEngine.addLight(new PointLight(this.shadowEngine, new Vector3(0f, 1f, 0f)));
        this.shadowEngine.addLight(new PointLight(this.shadowEngine, new Vector3(-25.5f, 12.0f, -26f)));
        this.shadowEngine.addLight(new DirectionalLight(this.shadowEngine, new Vector3(33, 10, 3), new Vector3(-10, 0, 0)));
        this.shadowEngine.addLight(new MovingPointLight(this.shadowEngine, new Vector3(0f, 30.0f, 0f)));
        initWorld();
    }

    private void initWorld(){
        // Load the scene, it is just one big model
        final G3dModelLoader loader = new G3dModelLoader(new UBJsonReader());
//		final Model model = loader.loadModel(Gdx.files.internal("model/test1.g3db"));
        final Model model = loader.loadModel(Gdx.files.internal("model/scene_f0.g3db"));
        ModelInstance modelInstance = new ModelInstance(model);
        modelInstance.transform.setToScaling(4f, 4f, 4f);
        lstModel.add(modelInstance);


        for (int i = 0; i < 20; i++) {
            final Model myModel = loader.loadModel(Gdx.files.internal("model/test1.g3db"));
            ModelInstance myModelInstance = new ModelInstance(myModel);
//		modelInstance.transform.setToScaling(4f, 4f, 4f);
            myModelInstance.transform.translate(MathUtils.random(0,i) + MathUtils.random(18), MathUtils.random(0,i)+8, MathUtils.random(0,i) + MathUtils.random(18));
            lstModel.add(myModelInstance);
        }
    }

    /**
     * Load camera(s)
     */
    public void initCameras() {
        camera = new PerspectiveCamera(60f, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.near = 1f;
        camera.far = 200;
        camera.position.set(20, 2, 20);
        camera.lookAt(0, 0, 0);
        camera.update();

        firstPersonCameraController = new FirstPersonCameraController(camera);
        firstPersonCameraController.setVelocity(30);
        Gdx.input.setInputProcessor(firstPersonCameraController);

    }

    /**
     * Render a frame
     */
    @Override
    public void render() {
        act();

        this.shadowEngine.renderAll(camera, lstModel);
    }

    /**
     * Everything that is not directly drawing but needs to be computed each frame
     *
     *
     */
    public void act() {
        firstPersonCameraController.update(Gdx.graphics.getDeltaTime());
    }

    /**
     * Window resized
     */
    @Override
    public void resize(final int width, final int height) {
        camera.viewportHeight = height;
        camera.viewportWidth = width;
        camera.update();
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }


    @Override
    public void dispose() {

    }

}
