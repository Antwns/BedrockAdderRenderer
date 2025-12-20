package hut.dev.bedrockadderrenderer.workers;

import com.loohp.blockmodelrenderer.render.Hexahedron;
import com.loohp.blockmodelrenderer.render.Model;
import com.loohp.blockmodelrenderer.render.Point3D;
import org.slf4j.Logger;

import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public final class ModelBuildingWorker
{
    private final Logger logger;

    public ModelBuildingWorker(Logger logger)
    {
        this.logger = logger;
    }

    public Model buildModelFromMinecraftJson(MinecraftModelJson minecraftModelJson, Path cacheAssetsDirectoryPath) throws Exception
    {
        if (minecraftModelJson.elements == null || minecraftModelJson.elements.isEmpty())
        {
            throw new IllegalStateException("Model has no elements.");
        }

        TextureLoadingWorker textureLoadingWorker = new TextureLoadingWorker(logger);
        TextureResolvingWorker textureResolvingWorker = new TextureResolvingWorker(logger);

        Map<String, BufferedImage> loadedTextureCache = new HashMap<>();

        Model combinedModel = new Model();

        for (MinecraftModelElementJson elementJson : minecraftModelJson.elements)
        {
            Model elementModel = buildOneElementModel(elementJson, minecraftModelJson, cacheAssetsDirectoryPath, textureResolvingWorker, textureLoadingWorker, loadedTextureCache);
            combinedModel.append(elementModel);
        }

        return combinedModel;
    }

    private Model buildOneElementModel(
            MinecraftModelElementJson elementJson,
            MinecraftModelJson minecraftModelJson,
            Path cacheAssetsDirectoryPath,
            TextureResolvingWorker textureResolvingWorker,
            TextureLoadingWorker textureLoadingWorker,
            Map<String, BufferedImage> loadedTextureCache
    ) throws Exception
    {
        double[] fixed = new ElementDimensionFixingWorker().fixFromToArray(elementJson.from, elementJson.to);

        Point3D cubeCornerMin = new Point3D(fixed[0], fixed[1], fixed[2]);
        Point3D cubeCornerMax = new Point3D(fixed[3], fixed[4], fixed[5]);

        // Hexahedron.fromCorners expects image order: [Up, Down, North, East, South, West]
        BufferedImage[] faceImages = new BufferedImage[6];

        FaceImageBakingWorker faceImageBakingWorker = new FaceImageBakingWorker(logger);

        faceImages[0] = faceImageBakingWorker.bakeFaceImage("up", elementJson, minecraftModelJson, cacheAssetsDirectoryPath, textureResolvingWorker, textureLoadingWorker, loadedTextureCache);
        faceImages[1] = faceImageBakingWorker.bakeFaceImage("down", elementJson, minecraftModelJson, cacheAssetsDirectoryPath, textureResolvingWorker, textureLoadingWorker, loadedTextureCache);
        faceImages[2] = faceImageBakingWorker.bakeFaceImage("north", elementJson, minecraftModelJson, cacheAssetsDirectoryPath, textureResolvingWorker, textureLoadingWorker, loadedTextureCache);
        faceImages[3] = faceImageBakingWorker.bakeFaceImage("east", elementJson, minecraftModelJson, cacheAssetsDirectoryPath, textureResolvingWorker, textureLoadingWorker, loadedTextureCache);
        faceImages[4] = faceImageBakingWorker.bakeFaceImage("south", elementJson, minecraftModelJson, cacheAssetsDirectoryPath, textureResolvingWorker, textureLoadingWorker, loadedTextureCache);
        faceImages[5] = faceImageBakingWorker.bakeFaceImage("west", elementJson, minecraftModelJson, cacheAssetsDirectoryPath, textureResolvingWorker, textureLoadingWorker, loadedTextureCache);

        Hexahedron hexahedron = Hexahedron.fromCorners(cubeCornerMin, cubeCornerMax, faceImages);

        Model elementModel = new Model(hexahedron);

        // Apply element rotation around its origin (Blockbench style).
        if (elementJson.rotation != null && elementJson.rotation.origin != null && elementJson.rotation.axis != null)
        {
            new ElementRotationApplyingWorker(logger).applyElementRotation(elementModel, elementJson.rotation);
        }

        return elementModel;
    }
}