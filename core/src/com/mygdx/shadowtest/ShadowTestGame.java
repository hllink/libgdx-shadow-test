package com.mygdx.shadowtest;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
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
import com.mygdx.shadowtest.shader.DepthMapShader;

public class ShadowTestGame extends ApplicationAdapter {

    private AssetManager assets;

    private PerspectiveCamera camera;
    private PerspectiveCamera cameraLight;
    public FirstPersonCameraController camController;

    private ModelBatch modelBatch;
    private ModelBatch depthModelBatch;

    private Array<ModelInstance> lstModelInstances;

    private Boolean isWorldCreated = false;
    private GLProfiler profiler;
    private ShaderProgram shaderProgram;

    @Override
    public void create() {
        this.assets = new AssetManager();

        shaderProgram = setupShader("scene");
        this.modelBatch = new ModelBatch();
        this.depthModelBatch = new ModelBatch(new DefaultShaderProvider()
        {
            @Override
            protected Shader createShader(final Renderable renderable)
            {
                return new DepthMapShader(renderable, shaderProgram);
            }
        });
        this.lstModelInstances = new Array<ModelInstance>();

        setupCamera();
        setupLights();
        loadModels();

        this.profiler = new GLProfiler(Gdx.graphics);
        this.profiler.enable();

    }

    private void setupLights() {
        cameraLight = new PerspectiveCamera(120f, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        cameraLight.near = 1f;
        cameraLight.far = 100;
        cameraLight.position.set(3, 3, 3);
        cameraLight.lookAt(-1, 0, 0);
        cameraLight.update();
    }

    private void loadModels() {
        this.assets.load("model/chair.g3db", Model.class);
        this.assets.load("model/terrain.g3db", Model.class);
    }

    public FrameBuffer frameBuffer;
    public static final int DEPTHMAPIZE = 1024;

    public void renderLight() {
        shaderProgram.begin();
        shaderProgram.setUniformMatrix("u_lightTrans", cameraLight.combined);
        shaderProgram.setUniformf("u_cameraFar", cameraLight.far);
        shaderProgram.setUniformf("u_lightPosition", cameraLight.position);
        shaderProgram.end();

        if (frameBuffer == null) {
            frameBuffer = FrameBuffer.createFrameBuffer(Pixmap.Format.RGBA8888, DEPTHMAPIZE, DEPTHMAPIZE, true);
        }
        frameBuffer.begin();
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
        depthModelBatch.begin(cameraLight);
        depthModelBatch.render(lstModelInstances);
        depthModelBatch.end();

        frameBuffer.end();
    }

    private void createWorldSpace() {
        ModelInstance terrain = new ModelInstance(this.assets.get("model/terrain.g3db", Model.class));
        terrain.transform.setTranslation(Vector3.Zero);
        this.lstModelInstances.add(terrain);

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



        renderLight();
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


    public ShaderProgram setupShader(final String prefix) {
        ShaderProgram.pedantic = false;
        final ShaderProgram shaderProgram = new ShaderProgram(Gdx.files.internal("shader/" + prefix + "_v.glsl"), Gdx.files.internal("shader/" + "/" + prefix
                + "_f.glsl"));
        if (!shaderProgram.isCompiled()) {
            System.err.println("Error with shader " + prefix + ": " + shaderProgram.getLog());
            System.exit(1);
        } else {
            Gdx.app.log("init", "Shader " + prefix + " compilled " + shaderProgram.getLog());
        }
        return shaderProgram;
    }
}
