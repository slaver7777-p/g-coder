package cosinus.slava7777.Managers;

import cosinus.slava7777.CosHolograms;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.*;

public class AnimationManager {

    private final CosHolograms plugin;
    private final Map<String, List<AnimationFrame>> animations = new HashMap<>();

    public AnimationManager(CosHolograms plugin) {
        this.plugin = plugin;
        loadAnimations();
    }

    private void loadAnimations() {
        File animationsFolder = new File(plugin.getDataFolder(), "animations");
        if (!animationsFolder.exists()) {
            animationsFolder.mkdirs();
        }

        for (File file : Objects.requireNonNull(animationsFolder.listFiles())) {
            if (file.isFile() && file.getName().endsWith(".yml")) {
                FileConfiguration config = YamlConfiguration.loadConfiguration(file);
                List<AnimationFrame> frames = new ArrayList<>();
                String animationName = file.getName().substring(0, file.getName().length() - 4); // Remove .yml extension

                List<Map<?, ?>> rawFrames = config.getMapList("animations");
                for (Map<?, ?> rawFrame : rawFrames) {
                    String string = (String) rawFrame.get("string");
                    int interval = (int) rawFrame.get("interval");
                    frames.add(new AnimationFrame(string, interval));
                }

                animations.put(animationName, frames);
            }
        }
    }

    public List<AnimationFrame> getAnimation(String name) {
        return animations.get(name);
    }

    public record AnimationFrame(String string, int interval) {
    }
}
