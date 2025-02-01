package cosinus.slava7777;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.joml.Vector3f;

import java.io.File;
import java.io.IOException;
import java.util.List;
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

                ConfigurationSection scaleholo = holo.getConfigurationSection("scale");
                if (scaleholo == null) {
                    return;
                }
                String name = holo.getString("name");
                List<String> lines = holo.getStringList("line");
                double visibilityDistance = holo.getDouble("distance-to-visibility", 30);
                double lineSpacing = holo.getDouble("line-spacing", 0.1);
                String backgroundColor = holo.getString("background-color", "none");
                int brightness = holo.getInt("brightness", 15);
                String billboardType = holo.getString("billboard-type", "fixed");
                Vector3f scale = new Vector3f(
                        (float) scaleholo.getDouble("x", 1.0),
                        (float) scaleholo.getDouble("y", 1.0),
                        (float) scaleholo.getDouble("z", 1.0)
                );
                boolean textShadow = holo.getBoolean("text-shadow", false);
                String textType = holo.getString("text-type", "center");
                String worldName = locholo.getString("world", "world");
                double x = locholo.getDouble("x");
                double y = locholo.getDouble("y");
                double z = locholo.getDouble("z");
                Location location = new Location(plugin.getServer().getWorld(worldName), x, y, z);

                Hologram hologram = new Hologram(plugin, name, location, lines, visibilityDistance, lineSpacing, backgroundColor, brightness, billboardType, scale , textShadow, textType);
                plugin.getHologramManager().addHologram(hologram);
            }
        }
    }

    public void saveHolograms() {
        File hologramsFolder = new File(plugin.getDataFolder(), "holograms");
        if (!hologramsFolder.exists()) {
            hologramsFolder.mkdirs();
        }

        for (Hologram hologram : plugin.getHologramManager().getHolograms()) {
            File file = new File(hologramsFolder, hologram.getName() + ".yml");
            FileConfiguration config = YamlConfiguration.loadConfiguration(file);

            ConfigurationSection holo = config.getConfigurationSection("hologram");
            if (holo == null) {
                return;
            }

            ConfigurationSection locholo = holo.getConfigurationSection("location");
            if (locholo == null) {
                return;
            }

            ConfigurationSection scaleholo = holo.getConfigurationSection("scale");
            if (scaleholo == null) {
                return;
            }

            holo.set("name", hologram.getName());
            holo.set("line", hologram.getLines());
            holo.set("distance-to-visibility", hologram.getVisibilityDistance());
            holo.set("line-spacing", hologram.getLineSpacing());
            holo.set("background-color", hologram.getBackgroundColor());
            holo.set("brightness", hologram.getBrightness());
            holo.set("billboard-type", hologram.getBillboardType());
            scaleholo.set("x", hologram.getScale().x());
            scaleholo.set("y", hologram.getScale().y());
            scaleholo.set("z", hologram.getScale().z());
            holo.set("text-shadow", hologram.hasTextShadow());
            holo.set("text-type", hologram.getTextType());
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
