package hut.dev.bedrockadderrenderer.workers;

import com.loohp.blockmodelrenderer.render.Model;
import org.slf4j.Logger;

public final class ElementRotationApplyingWorker
{
    private final Logger logger;

    public ElementRotationApplyingWorker(Logger logger)
    {
        this.logger = logger;
    }

    public void applyElementRotation(Model elementModel, MinecraftModelElementRotationJson rotationJson)
    {
        double originX = rotationJson.origin[0];
        double originY = rotationJson.origin[1];
        double originZ = rotationJson.origin[2];

        elementModel.translate(-originX, -originY, -originZ);

        double xRot = 0.0;
        double yRot = 0.0;
        double zRot = 0.0;

        if (rotationJson.axis.equalsIgnoreCase("x"))
        {
            xRot = rotationJson.angle;
        }
        else if (rotationJson.axis.equalsIgnoreCase("y"))
        {
            yRot = rotationJson.angle;
        }
        else if (rotationJson.axis.equalsIgnoreCase("z"))
        {
            zRot = rotationJson.angle;
        }

        elementModel.rotate(xRot, yRot, zRot, false);

        elementModel.translate(originX, originY, originZ);
    }
}