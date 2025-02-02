package cosinus.slava7777;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.joml.Vector3f;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class HologramConfig {

    private final CosHolograms plugin;

    public HologramConfig(CosHolograms plugin) {
        this.plugin = plugin;
    }

    public void loadHolograms() {
        File hologramsFolder = new File(plugin.getDataFolder(), "holograms");
        if (!hologramsFolder.exists()) {
            hologramsFolder.mkdirs();
        }

        for (File file : Objects.requireNonNull(hologramsFolder.listFiles())) {
            if (file.isFile() && file.getName().endsWith(".yml")) {
                FileConfiguration config = YamlConfiguration.loadConfiguration(file);
                ConfigurationSection holo = config.getConfigurationSection("hologram");
                if (holo == null) {
                    return;
                }
                ConfigurationSection locholo = holo.getConfigurationSection("location");
                if (locholo == null) {
                    return;
                }

                String name = holo.getString("name");
                double visibilityDistance = holo.getDouble("distance-to-visibility", 30);
                int brightness = holo.getInt("brightness", 15);

                List<Hologram.Line> lines = new ArrayList<>();
                ConfigurationSection linesSection = holo.getConfigurationSection("lines");
                if (linesSection != null) {
                    for (String key : linesSection.getKeys(false)) {
                        ConfigurationSection lineConfig = linesSection.getConfigurationSection(key);
                        if (lineConfig != null) {
                            String content = lineConfig.getString("content", "");
                            double lineSpacing = lineConfig.getDouble("line-spacing", 0.1);
                            String backgroundColor = lineConfig.getString("background-color", "none");
                            String billboardType = lineConfig.getString("billboard-type", "fixed");
                            Vector3f scale = new Vector3f(
                                    (float) lineConfig.getDouble("scale.x", 1.0),
                                    (float) lineConfig.getDouble("scale.y", 1.0),
                                    (float) lineConfig.getDouble("scale.z", 1.0)
                            );
                            boolean textShadow = lineConfig.getBoolean("text-shadow", false);
                            String textType = lineConfig.getString("text-type", "center");

                            lines.add(new Hologram.Line(content, lineSpacing, backgroundColor, billboardType, scale, textShadow, textType));
                        }
                    }
                }

                String worldName = locholo.getString("world", "world");
                double x = locholo.getDouble("x");
                double y = locholo.getDouble("y");
                double z = locholo.getDouble("z");
                Location location = new Location(plugin.getServer().getWorld(worldName), x, y, z);

                Hologram hologram = new Hologram(plugin, name, location, lines, visibilityDistance, brightness);
                plugin.getHologramManager().addHologram(hologram);
            }
        }
    }

    public void saveHolograms() {
        File hologramsFolder = new File(plugin.getDataFolder(), "holograms");
        if (!hologramsFolder.exists()) {
            hologramsFolder.mkdirs();
        }

        for (Map.Entry<String, Hologram> entry : plugin.getHologramManager().getHolograms().entrySet()) {
            Hologram hologram = entry.getValue();
            File file = new File(hologramsFolder, hologram.getName() + ".yml");
            FileConfiguration config = YamlConfiguration.loadConfiguration(file);

            ConfigurationSection holo = config.createSection("hologram");
            ConfigurationSection locholo = holo.createSection("location");
            ConfigurationSection linesSection = holo.createSection("lines");

            holo.set("name", hologram.getName());
            holo.set("distance-to-visibility", hologram.getVisibilityDistance());
            holo.set("brightness", hologram.getBrightness());

            for (int i = 0; i < hologram.getLines().size(); i++) {
                Hologram.Line line = hologram.getLines().get(i);
                ConfigurationSection lineConfig = linesSection.createSection("line-" + i);
                lineConfig.set("content", line.content());
                lineConfig.set("line-spacing", line.lineSpacing());
                lineConfig.set("background-color", line.backgroundColor());
                lineConfig.set("billboard-type", line.billboardType());
                lineConfig.set("scale.x", line.scale().x());
                lineConfig.set("scale.y", line.scale().y());
                lineConfig.set("scale.z", line.scale().z());
                lineConfig.set("text-shadow", line.textShadow());
                lineConfig.set("text-type", line.textType());
            }

            locholo.set("world", hologram.getLocation().getWorld().getName());
            locholo.set("x", hologram.getLocation().getX());
            locholo.set("y", hologram.getLocation().getY());
            locholo.set("z", hologram.getLocation().getZ());

            try {
                config.save(file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
