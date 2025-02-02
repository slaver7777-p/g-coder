package cosinus.slava7777.Managers;

import cosinus.slava7777.CosHolograms;
import cosinus.slava7777.Hologram;
import cosinus.slava7777.HologramConfig;
import org.bukkit.Location;
import org.bukkit.event.Listener;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unused")
public class HologramManager implements Listener {

    private final CosHolograms plugin;
    private final Map<String, Hologram> holograms;

    public HologramManager(CosHolograms plugin) {
        this.plugin = plugin;
        this.holograms = new HashMap<>();
    }

    public void createHologram(String name, Location location) {
        List<Hologram.Line> defaultLines = List.of(
                new Hologram.Line("&aDefault line",
                        0.1,
                        "none",
                        "fixed",
                        new Vector3f(1.0f, 1.0f, 1.0f),
                        false,
                        "center")
        );
        double defaultVisibilityDistance = 30;
        int defaultBrightness = 15;

        Hologram hologram = new Hologram(plugin, name, location, defaultLines, defaultVisibilityDistance, defaultBrightness);
        holograms.put(name, hologram);
        new HologramConfig(plugin).saveHolograms();
    }

    public void removeHologram(Hologram hologram) {
        hologram.remove();
        holograms.remove(hologram.getName());
        new HologramConfig(plugin).saveHolograms();
    }

    public Hologram getHologramByName(String name) {
        return holograms.get(name);
    }

    public Map<String, Hologram> getHolograms() {
        return holograms;
    }

    public void addHologram(Hologram hologram) {
        holograms.put(hologram.getName(), hologram);
    }

    public void removeAllHolograms() {
        for (Hologram hologram : new ArrayList<>(holograms.values())) {
            hologram.remove();
            holograms.remove(hologram.getName());
        }
    }
}
