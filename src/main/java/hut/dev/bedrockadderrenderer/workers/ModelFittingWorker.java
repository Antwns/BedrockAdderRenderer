package hut.dev.bedrockadderrenderer.workers;

import com.loohp.blockmodelrenderer.render.Face;
import com.loohp.blockmodelrenderer.render.Hexahedron;
import com.loohp.blockmodelrenderer.render.Model;
import org.slf4j.Logger;

public final class ModelFittingWorker
{
    private final Logger logger;

    public ModelFittingWorker(Logger logger)
    {
        this.logger = logger;
    }

    public void centerModelOnOriginUsingXYBounds(Model model)
    {
        double minX = Double.POSITIVE_INFINITY;
        double maxX = Double.NEGATIVE_INFINITY;
        double minY = Double.POSITIVE_INFINITY;
        double maxY = Double.NEGATIVE_INFINITY;

        for (Hexahedron hexahedron : model.getComponents())
        {
            for (Face face : hexahedron.getFacesByAverageZ())
            {
                minX = Math.min(minX, face.getMinX());
                maxX = Math.max(maxX, face.getMaxX());
                minY = Math.min(minY, face.getMinY());
                maxY = Math.max(maxY, face.getMaxY());
            }
        }

        double centerX = (minX + maxX) / 2.0;
        double centerY = (minY + maxY) / 2.0;

        model.translate(-centerX, -centerY, 0.0);
    }

    public double getMaxXYDimension(Model model)
    {
        double minX = Double.POSITIVE_INFINITY;
        double maxX = Double.NEGATIVE_INFINITY;
        double minY = Double.POSITIVE_INFINITY;
        double maxY = Double.NEGATIVE_INFINITY;

        for (Hexahedron hexahedron : model.getComponents())
        {
            for (Face face : hexahedron.getFacesByAverageZ())
            {
                minX = Math.min(minX, face.getMinX());
                maxX = Math.max(maxX, face.getMaxX());
                minY = Math.min(minY, face.getMinY());
                maxY = Math.max(maxY, face.getMaxY());
            }
        }

        double width = maxX - minX;
        double height = maxY - minY;
        return Math.max(width, height);
    }
}