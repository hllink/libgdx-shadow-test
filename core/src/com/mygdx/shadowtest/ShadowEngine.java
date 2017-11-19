package com.mygdx.shadowtest;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.utils.DefaultShaderProvider;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.Array;

/**
 * Created by hllink on 11/18/17.
 */
public class ShadowEngine {

    private ModelBatch modelBatch;
    private ModelBatch modelBatchShadows;
    private ShaderProgram shaderProgram;
    private ShaderProgram shaderProgramShadows;
    private FrameBuffer frameBufferShadows;
    public Array<Light> lights = new Array<Light>();
    public static final int DEPTHMAPSIZE = 1024;

    private SpriteBatch sb;


    public void init() {
        initShaders();
        sb = new SpriteBatch();
    }


    public void addLight(Light light) {
        lights.add(light);
    }

    /**
     * Load shader(s)
     */
    private void initShaders() {
        shaderProgram = setupShader("scene");
        modelBatch = new ModelBatch(new DefaultShaderProvider() {
            @Override
            protected Shader createShader(final Renderable renderable) {
                return new SimpleTextureShader(renderable, shaderProgram);
            }
        });

        final ShadowEngine self = this;
        shaderProgramShadows = setupShader("shadows");
        modelBatchShadows = new ModelBatch(new DefaultShaderProvider() {
            @Override
            protected Shader createShader(final Renderable renderable) {
                return new ShadowMapShader(self, renderable, shaderProgramShadows);
            }
        });
    }


    public void renderLights(Array<ModelInstance> lstModel) {
        for (final Light light : lights) {
            light.render(lstModel);
        }
    }

    public void renderAll(Camera camera, Array<ModelInstance> lstModelInstance) {
        act();
        renderLights(lstModelInstance);

        beginRenderShadows(camera);
        getModelBatchShadows().render(lstModelInstance);
        endRenderShadows();

        beginRenderScene(camera);
        getModelBatch().render(lstModelInstance);
        endRenderScene();

    }

    final int textureNum = 4;

    public void beginRenderScene(Camera camera) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT | (Gdx.graphics.getBufferFormat().coverageSampling ? GL20.GL_COVERAGE_BUFFER_BIT_NV : 0));
        shaderProgram.begin();

        frameBufferShadows.getColorBufferTexture().bind(textureNum);
        shaderProgram.setUniformi("u_shadows", textureNum);
        shaderProgram.setUniformf("u_screenWidth", Gdx.graphics.getWidth());
        shaderProgram.setUniformf("u_screenHeight", Gdx.graphics.getHeight());
        shaderProgram.end();

        modelBatch.begin(camera);
    }

    public void endRenderScene() {
        modelBatch.end();
    }

    public void beginRenderShadows(Camera camera) {
        if (frameBufferShadows == null) {
            frameBufferShadows = FrameBuffer.createFrameBuffer(Pixmap.Format.RGBA8888, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
        }
        frameBufferShadows.begin();

        Gdx.gl.glClearColor(0.4f, 0.4f, 0.4f, 0.4f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        modelBatchShadows.begin(camera);
    }

    public void endRenderShadows() {
        modelBatchShadows.end();
        frameBufferShadows.end();

        sb.begin();
        sb.draw(frameBufferShadows.getColorBufferTexture(), 0, 0);
        sb.end();
    }

    public void act() {
        actLights(Gdx.graphics.getDeltaTime());
    }


    private void actLights(final float delta) {
        for (final Light light : lights) {
            light.act(delta);
        }
    }


    /**
     * Initialize a shader, vertex shader must be named prefix_v.glsl, fragment shader must be named
     * prefix_f.glsl
     *
     * @param prefix
     * @return
     */
    public ShaderProgram setupShader(final String prefix) {
        ShaderProgram.pedantic = false;
        final String packageName = getClass().getPackage().getName().substring(1 + getClass().getPackage().getName().lastIndexOf("."));

        final ShaderProgram shaderProgram = new ShaderProgram(Gdx.files.internal("shader/" + prefix + "_v.glsl"), Gdx.files.internal("shader/" + prefix
                + "_f.glsl"));
        if (!shaderProgram.isCompiled()) {
            System.err.println("Error with shader " + prefix + ": " + shaderProgram.getLog());
            System.exit(1);
        } else {
            Gdx.app.log("init", "Shader " + prefix + " compilled " + shaderProgram.getLog());
        }
        return shaderProgram;
    }

    public ModelBatch getModelBatch() {
        return modelBatch;
    }

    public ModelBatch getModelBatchShadows() {
        return modelBatchShadows;
    }


}
