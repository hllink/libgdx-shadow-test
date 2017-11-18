package com.mygdx.shadowtest.shader;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Cubemap;

/**
 * Created by hllink on 11/17/17.
 */
public class FrameBufferUtils {
    public static Cubemap.CubemapSide bindSide(Cubemap.CubemapSide side, Camera camera)
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
        return side;

    }
}
