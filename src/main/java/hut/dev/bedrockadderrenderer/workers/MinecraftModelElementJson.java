package hut.dev.bedrockadderrenderer.workers;

import java.util.Map;

public final class MinecraftModelElementJson
{
    public double[] from;
    public double[] to;

    public MinecraftModelElementRotationJson rotation;

    public Map<String, MinecraftModelFaceJson> faces;
}