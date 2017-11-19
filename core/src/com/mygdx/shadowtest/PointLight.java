package com.mygdx.shadowtest;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Cubemap;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.glutils.FrameBufferCubemap;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;


public class PointLight extends Light
{

	public FrameBufferCubemap frameBuffer;
	public Cubemap            depthMap;

	public PointLight(final ShadowEngine mainScreen, final Vector3 position)
	{
		super(mainScreen);
		this.position = position;
		init();
	}

	@Override
	public void applyToShader(final ShaderProgram sceneShaderProgram)
	{
		final int textureNum = 2;
		depthMap.bind(textureNum);
		sceneShaderProgram.setUniformf("u_type", 2);
		sceneShaderProgram.setUniformi("u_depthMapCube", textureNum);
		sceneShaderProgram.setUniformf("u_cameraFar", camera.far);
		sceneShaderProgram.setUniformf("u_lightPosition", position);
	}

	@Override
	public void init()
	{
		super.init();

		camera = new PerspectiveCamera(90f, ShadowEngine.DEPTHMAPSIZE, ShadowEngine.DEPTHMAPSIZE);
		camera.near = 4f;
		camera.far = 70;
		camera.position.set(position);
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
			frameBuffer = FrameBufferCubemap.createFrameBufferCubemap(Format.RGBA8888, ShadowEngine.DEPTHMAPSIZE, ShadowEngine.DEPTHMAPSIZE, true);
		}

		shaderProgram.begin();
		shaderProgram.setUniformf("u_cameraFar", camera.far);
		shaderProgram.setUniformf("u_lightPosition", position);
		shaderProgram.end();

		for (int s = 0; s <= 5; s++)
		{
			final Cubemap.CubemapSide side = Cubemap.CubemapSide.values()[s];
			frameBuffer.begin();
			bindSide(side, camera);
			Gdx.gl.glClearColor(0, 0, 0, 1);
			Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

			modelBatch.begin(camera);
			modelBatch.render(lstModelInstance);
			modelBatch.end();


		}

		frameBuffer.end();
		depthMap = frameBuffer.getColorBufferTexture();
	}

	@Override
	public void act(final float delta)
	{
		// TODO Auto-generated method stub

	}

	public void bindSide(Cubemap.CubemapSide side, Camera camera)
	{
		switch (side)
		{
			case NegativeX:
				camera.up.set(0, -1, 0);
				camera.direction.set(-1, 0, 0);
				break;
			case NegativeY:
				camera.up.set(0, 0, -1);
				camera.direction.set(0, -1, 0);
				break;
			case NegativeZ:
				camera.up.set(0, -1, 0);
				camera.direction.set(0, 0, -1);
				break;
			case PositiveX:
				camera.up.set(0, -1, 0);
				camera.direction.set(1, 0, 0);
				break;
			case PositiveY:
				camera.up.set(0, 0, 1);
				camera.direction.set(0, 1, 0);
				break;
			case PositiveZ:
				camera.up.set(0, -1, 0);
				camera.direction.set(0, 0, 1);
				break;
			default:
				break;
		}
		camera.update();
		Gdx.gl20.glFramebufferTexture2D(GL20.GL_FRAMEBUFFER, GL20.GL_COLOR_ATTACHMENT0, side.glEnum, frameBuffer.getColorBufferTexture().getTextureObjectHandle(), 0);
	}
}
