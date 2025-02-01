package cosinus.slava7777.Managers;

import cosinus.slava7777.CosHolograms;
import cosinus.slava7777.Hologram;
import cosinus.slava7777.HologramConfig;
import org.bukkit.Location;
import org.bukkit.event.Listener;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

public class HologramManager implements Listener {

    private final CosHolograms plugin;
    private final List<Hologram> holograms;

    public HologramManager(CosHolograms plugin) {
        this.plugin = plugin;
        this.holograms = new ArrayList<>();
    }

    public void createHologram(String name, Location location) {
        List<String> defaultLines = List.of("&aDefault line");
        double defaultVisibilityDistance = 30;
        double defaultLineSpacing = 0.1;
        String defaultBackgroundColor = "none";
        int defaultBrightness = 15;
        String defaultBillboardType = "fixed";
        Vector3f defaultScale = new Vector3f(1.0f, 1.0f, 1.0f);
        boolean defaultTextShadow = false;
        String defaultTextType = "center";

        Hologram hologram = new Hologram(plugin, name, location, defaultLines, defaultVisibilityDistance, defaultLineSpacing, defaultBackgroundColor, defaultBrightness, defaultBillboardType,  defaultScale, defaultTextShadow, defaultTextType);
        holograms.add(hologram);
        new HologramConfig(plugin).saveHolograms();
    }

    public void removeHologram(Hologram hologram) {
        hologram.remove();
        holograms.remove(hologram);
        new HologramConfig(plugin).saveHolograms();
    }

    public List<Hologram> getHolograms() {
        return holograms;
    }

    public void addHologram(Hologram hologram) {
        holograms.add(hologram);
    }

    public void removeAllHolograms() {
        for (Hologram hologram : new ArrayList<>(holograms)) {
            hologram.remove();
            holograms.remove(hologram);
        }
    }
}
