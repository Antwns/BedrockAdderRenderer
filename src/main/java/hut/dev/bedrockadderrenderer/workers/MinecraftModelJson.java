package hut.dev.bedrockadderrenderer.workers;

import java.util.List;
import java.util.Map;

public final class MinecraftModelJson
{
    public String format_version;
    public String credit;
    public String gui_light;

    public Map<String, String> textures;
    public List<MinecraftModelElementJson> elements;
    public Map<String, MinecraftModelDisplayTransformJson> display;
}