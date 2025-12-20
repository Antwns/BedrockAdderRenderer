package hut.dev.bedrockadderrenderer.workers;

import com.loohp.blockmodelrenderer.render.Model;
import org.slf4j.Logger;

public final class ModelTransformApplyingWorker
{
    private final Logger logger;

    public ModelTransformApplyingWorker(Logger logger)
    {
        this.logger = logger;
    }

    public void applyGuiDisplayTransform(Model model, MinecraftModelJson modelJson)
    {
        // Center on block center like you did in cube test.
        model.translate(-8.0, -8.0, -8.0);

        if (modelJson.display == null)
        {
            model.rotate(30.0, 225.0, 0.0, false);
            return;
        }

        MinecraftModelDisplayTransformJson guiTransform = modelJson.display.get("gui");
        if (guiTransform == null)
        {
            model.rotate(30.0, 225.0, 0.0, false);
            return;
        }

        if (guiTransform.rotation != null && guiTransform.rotation.length == 3)
        {
            model.rotate(guiTransform.rotation[0], guiTransform.rotation[1], guiTransform.rotation[2], false);
        }

        if (guiTransform.scale != null && guiTransform.scale.length == 3)
        {
            model.scale(guiTransform.scale[0], guiTransform.scale[1], guiTransform.scale[2]);
        }

        if (guiTransform.translation != null && guiTransform.translation.length == 3)
        {
            // translation in model json is in "pixels" (1 unit = 1/16 block), but this file likely uses direct units.
            model.translate(guiTransform.translation[0], guiTransform.translation[1], guiTransform.translation[2]);
        }
    }
}