package com.mygdx.shadowtest.shader;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader;
import com.badlogic.gdx.graphics.g3d.shaders.DepthShader;
import com.badlogic.gdx.graphics.g3d.utils.BaseShaderProvider;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;

public class TestShaderProvider extends BaseShaderProvider {

    private ShaderProgram shaderProgram;
private Shader shader;
    public TestShaderProvider() {
        shaderProgram = new ShaderProgram(Gdx.files.internal("shader/depthmap_v.glsl"), Gdx.files.internal("shader/depthmap_f.glsl"));
    }

    @Override
    protected Shader createShader(Renderable renderable) {

        if(shader == null) {
            DepthShader.Config config = new DepthShader.Config();
            shader = new DefaultShader(renderable, config, shaderProgram);
            shader.init();
        }
        return shader;
    }

    public ShaderProgram getShaderProgram() {
        return shaderProgram;
    }
}
