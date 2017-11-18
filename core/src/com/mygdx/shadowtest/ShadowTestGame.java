package com.mygdx.shadowtest;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.FPSLogger;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g3d.*;
import com.badlogic.gdx.graphics.g3d.utils.DefaultShaderProvider;
import com.badlogic.gdx.graphics.g3d.utils.FirstPersonCameraController;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.profiling.GLProfiler;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.mygdx.shadowtest.shader.*;

import java.util.ArrayList;

public class ShadowTestGame extends ApplicationAdapter {

    private AssetManager assets;
    public FirstPersonCameraController camController;


    private Array<ModelInstance> lstModelInstances;
    private Boolean isWorldCreated = false;
//    private GLProfiler profiler;

    private ShaderProgram shaderProgram;
    private ModelBatch modelBatch;
    private PerspectiveCamera camera;
    public static final int DEPTHMAPSIZE = 1024;

    private ModelBatch modelBatchShadows;
    private ShaderProgram shaderProgramShadows;
    private FrameBuffer frameBufferShadows;

    public ArrayList<Light> lights = new ArrayList<Light>();

    @Override
    public void create() {
        this.assets = new AssetManager();
        this.lstModelInstances = new Array<ModelInstance>();
        loadModels();



        GLProfiler.enable();


        initCameras();
        initShaders();

        lights.add(new PointLight(this, new Vector3(0f, 1.8f, 0f)));
//        lights.add(new PointLight(this, new Vector3(-25.5f, 12.0f, -26f)));
//        lights.add(new DirectionalLight(this, new Vector3(33, 10, 3), new Vector3(-10, 0, 0)));
        //lights.add(new MovingPointLight(this, new Vector3(0f, 30.0f, 0f)));
        camController = new FirstPersonCameraController(camera);

    }

    public void initCameras() {
        camera = new PerspectiveCamera(60f, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.near = 1f;
        camera.far = 200;
        camera.position.set(0, 1, 0);
        camera.lookAt(0, 0, 0);
        camera.update();

//		firstPersonCameraController = new IntFirstPersonCameraController(camera);
//		firstPersonCameraController.setVelocity(30);
//		Gdx.input.setInputProcessor(firstPersonCameraController);

    }

    public void initShaders() {
        shaderProgram = setupShader("scene");
        modelBatch = new ModelBatch(new DefaultShaderProvider() {
            @Override
            protected Shader createShader(final Renderable renderable) {
                return new SimpleTextureShader(renderable, shaderProgram);
            }
        });

        final ShadowTestGame self = this;
        shaderProgramShadows = setupShader("shadows");
        modelBatchShadows = new ModelBatch(new DefaultShaderProvider() {
            @Override
            protected Shader createShader(final Renderable renderable) {
                return new ShadowMapShader(self, renderable, shaderProgramShadows);
            }
        });
    }

    private void loadModels() {
        this.assets.load("model/chair.g3db", Model.class);
        this.assets.load("model/terrain.g3db", Model.class);
        this.assets.load("model/scene_f0.g3db", Model.class);
    }

    public FrameBuffer frameBuffer;
    public static final int DEPTHMAPIZE = 1024;


    private void createWorldSpace() {
        ModelInstance terrain = new ModelInstance(this.assets.get("model/terrain.g3db", Model.class));
        terrain.transform.setTranslation(Vector3.Zero);
        this.lstModelInstances.add(terrain);

        ModelInstance scene = new ModelInstance(this.assets.get("model/scene_f0.g3db", Model.class));
        scene.transform.setTranslation(Vector3.Zero);
        this.lstModelInstances.add(scene);

        int generatedInstances = MathUtils.random(10, 150);
        for (int i = 0; i < generatedInstances; i++) {
            ModelInstance chair = new ModelInstance(this.assets.get("model/chair.g3db", Model.class));
            chair.transform.setTranslation(new Vector3(MathUtils.random(-10f, 10f), 0, MathUtils.random(-10f, 10f)));
            Quaternion rotation = new Quaternion();
            rotation.setEulerAngles(MathUtils.random(360), 0, 0);
            chair.transform.rotate(rotation);
            this.lstModelInstances.add(chair);
        }

        this.isWorldCreated = true;
    }


    FPSLogger fpsLogger = new FPSLogger();

    @Override
    public void render() {
        camController.update();

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        if (!this.assets.update()) {
            Gdx.app.log("Assets", "Loading Assets: " + this.assets.getProgress() * 100 + "% completed.");
            return;
        }
        if (!this.isWorldCreated) {
            createWorldSpace();
            Gdx.app.log("Assets", "Assets Loaded.");
        }


        act(Gdx.graphics.getDeltaTime());
        for (final Light light : lights) {
            for (ModelInstance instance : lstModelInstances) {
                light.render(instance);
            }
        }
        renderShadows();
        renderScene();

        fpsLogger.log();
        //Gdx.app.log("profiler vertex count",((Float)this.profiler.getVertexCount().total).toString());
       GLProfiler.reset();
    }

    @Override
    public void dispose() {
        this.assets.dispose();
        this.modelBatch.dispose();
    }


    public ShaderProgram setupShader(final String prefix)
    {
        ShaderProgram.pedantic = false;
        final ShaderProgram shaderProgram = new ShaderProgram(Gdx.files.internal("shader/" + prefix + "_v.glsl"), Gdx.files.internal("shader/" + prefix
                + "_f.glsl"));
        if (!shaderProgram.isCompiled())
        {
            System.err.println("Error with shader " + prefix + ": " + shaderProgram.getLog());
            System.exit(1);
        }
        else
        {
            Gdx.app.log("init", "Shader " + prefix + " compilled " + shaderProgram.getLog());
        }
        return shaderProgram;
    }


    public void renderScene() {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT | (Gdx.graphics.getBufferFormat().coverageSampling ? GL20.GL_COVERAGE_BUFFER_BIT_NV : 0));
        shaderProgram.begin();
        final int textureNum = 4;
        frameBufferShadows.getColorBufferTexture().bind(textureNum);
        shaderProgram.setUniformi("u_shadows", textureNum);
        shaderProgram.setUniformf("u_screenWidth", Gdx.graphics.getWidth());
        shaderProgram.setUniformf("u_screenHeight", Gdx.graphics.getHeight());
        shaderProgram.end();

        modelBatch.begin(camera);
        for (ModelInstance instance : lstModelInstances) {
            modelBatch.render(instance);
        }
        modelBatch.end();

    }

    /**
     * Render the scene shadow map
     */
    public void renderShadows() {
        if (frameBufferShadows == null) {
            frameBufferShadows = new FrameBuffer(Pixmap.Format.RGBA8888, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
        }
        frameBufferShadows.begin();

        Gdx.gl.glClearColor(0.4f, 0.4f, 0.4f, 0.4f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        modelBatchShadows.begin(camera);
        for (ModelInstance instance : lstModelInstances) {
            modelBatchShadows.render(instance);
        }
        modelBatchShadows.end();

        frameBufferShadows.end();
    }


    public void act(final float delta) {

        for (final Light light : lights) {
            light.needsUpdate = true;

            light.act(delta);


        }
    }
}
