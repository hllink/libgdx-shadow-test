package com.mygdx.shadowtest;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;


public class DirectionalLight extends Light
{

	public Vector3		direction;
	public FrameBuffer	frameBuffer;
	public Texture		depthMap;

	public DirectionalLight(final ShadowEngine mainScreen, final Vector3 position, final Vector3 direction)
	{
		super(mainScreen);
		this.position = position;
		this.direction = direction;
		init();
	}

	@Override
	public void applyToShader(final ShaderProgram sceneShaderProgram)
	{
		final int textureNum = 3;
		depthMap.bind(textureNum);
		sceneShaderProgram.setUniformi("u_depthMapDir", textureNum);
		sceneShaderProgram.setUniformMatrix("u_lightTrans", camera.combined);
		sceneShaderProgram.setUniformf("u_cameraFar", camera.far);
		sceneShaderProgram.setUniformf("u_type", 1);
		sceneShaderProgram.setUniformf("u_lightPosition", position);
	}

	@Override
	public void init()
	{
		super.init();

		camera = new PerspectiveCamera(120f, ShadowEngine.DEPTHMAPSIZE, ShadowEngine.DEPTHMAPSIZE);
		camera.near = 1f;
		camera.far = 70;
		camera.position.set(position);
		camera.lookAt(direction);
		camera.update();
	}

	@Override
	public void render(final Array<ModelInstance> lstModelInstance)
	{
		if (!needsUpdate)
		{
			return;
		}
		needsUpdate = false;

		if (frameBuffer == null)
		{
			frameBuffer = FrameBuffer.createFrameBuffer(Format.RGBA8888, ShadowEngine.DEPTHMAPSIZE, ShadowEngine.DEPTHMAPSIZE, true);
		}

		frameBuffer.begin();
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

		shaderProgram.begin();
		shaderProgram.setUniformf("u_cameraFar", camera.far);
		shaderProgram.setUniformf("u_lightPosition", position);
		shaderProgram.end();

		modelBatch.begin(camera);
		modelBatch.render(lstModelInstance);
		modelBatch.end();

		frameBuffer.end();
		depthMap = frameBuffer.getColorBufferTexture();
	}

	@Override
	public void act(final float delta)
	{
	}

}
