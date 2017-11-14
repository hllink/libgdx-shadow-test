package com.mygdx.shadowtest;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.graphics.g3d.utils.FirstPersonCameraController;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.graphics.profiling.GLProfiler;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

public class ShadowTestGame extends ApplicationAdapter {

    private AssetManager assets;

    private PerspectiveCamera camera;
    public FirstPersonCameraController camController;

    private ModelBatch modelBatch;

    private Array<ModelInstance> lstModelInstances;

    private Boolean isWorldCreated = false;
    private GLProfiler profiler;

    @Override
    public void create() {
        this.assets = new AssetManager();
        this.modelBatch = new ModelBatch();
        this.lstModelInstances = new Array<ModelInstance>();

        setupCamera();
        loadModels();

        this.profiler = new GLProfiler(Gdx.graphics);
        this.profiler.enable();

    }

    private void loadModels() {
        this.assets.load("model/chair.g3db", Model.class);
        this.assets.load("model/terrain.g3db", Model.class);
    }

    private void createWorldSpace() {
        ModelInstance terrain = new ModelInstance(this.assets.get("model/terrain.g3db", Model.class));
        terrain.transform.setTranslation(Vector3.Zero);
        this.lstModelInstances.add(terrain);

        int generatedInstances = MathUtils.random(10,150);
        for(int i =0; i < generatedInstances;i++){
            ModelInstance chair = new ModelInstance(this.assets.get("model/chair.g3db", Model.class));
            chair.transform.setTranslation(new Vector3(MathUtils.random(-10f,10f),0,MathUtils.random(-10f,10f)));
            Quaternion rotation = new Quaternion();
            rotation.setEulerAngles(MathUtils.random(360),0,0);
            chair.transform.rotate(rotation);
            this.lstModelInstances.add(chair);
        }

        this.isWorldCreated = true;
    }

    private void setupCamera() {
        this.camera = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        this.camera.position.set(10f, 2f, 10f);
        this.camera.lookAt(0, 0, 0);
        this.camera.near = 1f;
        this.camera.far = 300f;
        this.camera.update();
        this.camController = new FirstPersonCameraController(this.camera);
        Gdx.input.setInputProcessor(camController);
    }

    @Override
    public void render() {
        camController.update();

        Gdx.gl.glClearColor(1, 1, 1, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        if (!this.assets.update()) {
            Gdx.app.log("Assets","Loading Assets: "+ this.assets.getProgress()*100 + "% completed.");
            return;
        }
        if (!this.isWorldCreated) {
            createWorldSpace();
            Gdx.app.log("Assets","Assets Loaded.");
        }



        this.modelBatch.begin(this.camera);
        this.modelBatch.render(lstModelInstances);
        this.modelBatch.end();
        this.camera.update();

        //Gdx.app.log("profiler vertex count",((Float)this.profiler.getVertexCount().total).toString());
        this.profiler.reset();
    }

    @Override
    public void dispose() {
        this.assets.dispose();
        this.modelBatch.dispose();
    }
}
