package hut.dev.bedrockadderrenderer.workers;

public final class ElementDimensionFixingWorker
{
    // Keep this tiny so it doesn't visually "thicken" rods.
    private static final double EPSILON_THICKNESS = 0.0001;

    public double[] fixFromToArray(double[] fromArray, double[] toArray)
    {
        double fromX = fromArray[0];
        double fromY = fromArray[1];
        double fromZ = fromArray[2];

        double toX = toArray[0];
        double toY = toArray[1];
        double toZ = toArray[2];

        // Ensure min <= max
        double minX = Math.min(fromX, toX);
        double maxX = Math.max(fromX, toX);

        double minY = Math.min(fromY, toY);
        double maxY = Math.max(fromY, toY);

        double minZ = Math.min(fromZ, toZ);
        double maxZ = Math.max(fromZ, toZ);

        // If paper-thin, expand slightly
        if (Math.abs(maxX - minX) < 0.0000001)
        {
            maxX = minX + EPSILON_THICKNESS;
        }

        if (Math.abs(maxY - minY) < 0.0000001)
        {
            maxY = minY + EPSILON_THICKNESS;
        }

        if (Math.abs(maxZ - minZ) < 0.0000001)
        {
            maxZ = minZ + EPSILON_THICKNESS;
        }

        return new double[]
                {
                        minX, minY, minZ,
                        maxX, maxY, maxZ
                };
    }
}