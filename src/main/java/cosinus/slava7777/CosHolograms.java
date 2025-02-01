package cosinus.slava7777;

import cosinus.slava7777.Cmd.CreateHologramCommand;
import cosinus.slava7777.Managers.HologramManager;
import cosinus.slava7777.Utils.HexUtils;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public final class CosHolograms extends JavaPlugin {

    private HologramManager hologramManager;
    private static final String VERSION = "1.0-RELEASE";
    private static final String AUTHORS = "slava7777";

    @Override
    public void onEnable() {
        this.hologramManager = new HologramManager(this);
        CreateHologramCommand createHoloCmd = new CreateHologramCommand(this);
        Objects.requireNonNull(getCommand("createhologram")).setExecutor(createHoloCmd);
        Objects.requireNonNull(getCommand("createhologram")).setTabCompleter(createHoloCmd);
        getServer().getPluginManager().registerEvents(hologramManager, this);
        getLogger().info("Загрузка голограмм...");
        new HologramConfig(this).loadHolograms();
        getLogger().info(HexUtils.translate("CosHologram version: " + VERSION + " authors: " + AUTHORS + " &#15FB08включен!"));
    }

    @Override
    public void onDisable() {
        getLogger().info("Разгрузка всех голограмм...");
        hologramManager.removeAllHolograms();
        new HologramConfig(this).saveHolograms();
        getLogger().info("CosHologram version: " + VERSION + "authors: " + AUTHORS + " &#FF0000выключен!");
    }

    public HologramManager getHologramManager() {
        return hologramManager;
    }
}
