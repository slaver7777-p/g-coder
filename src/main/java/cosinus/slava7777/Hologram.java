package cosinus.slava7777;

import cosinus.slava7777.Managers.AnimationManager;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Display;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Hologram implements Listener {

    private final CosHolograms plugin;
    private final String name;
    private final Location location;
    private final List<Line> lines;
    private final double visibilityDistance;
    private final int brightness;
    private List<TextDisplay> displays;
    private final AnimationManager animationManager;
    private List<ItemDisplay> itemDisplays;

    public Hologram(CosHolograms plugin,
                    String name,
                    Location location,
                    List<Line> lines,
                    double visibilityDistance,
                    int brightness) {
        this.plugin = plugin;
        this.name = name;
        this.location = location;
        this.lines = lines;
        this.visibilityDistance = visibilityDistance;
        this.brightness = brightness;
        this.animationManager = new AnimationManager(plugin);

        setupDisplays();
    }

    private void setupDisplays() {
        displays = new ArrayList<>();
        itemDisplays = new ArrayList<>();
        MiniMessage miniMessage = MiniMessage.miniMessage();

        for (int i = 0; i < lines.size(); i++) {
            Line line = lines.get(i);
            Location lineLocation = location.clone().add(0, -line.lineSpacing() * i, 0);

            // Обработка блоков и предметов
            if (line.content().contains("<#BLOCK:")) {
                String materialName = extractMaterialName(line.content(), "<#BLOCK:");
                Material material = Material.getMaterial(materialName);
                if (material == null) {
                    material = Material.GOLDEN_APPLE;
                }
                if (material.isBlock()) {
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        createBlockDisplay(lineLocation, material, player, line);
                    }
                    continue;
                }
            } else if (line.content().contains("<#ITEM:")) {
                String materialName = extractMaterialName(line.content(), "<#ITEM:");
                Material material = Material.getMaterial(materialName);
                if (material == null) {
                    material = Material.GOLDEN_APPLE;
                }
                for (Player player : Bukkit.getOnlinePlayers()) {
                    createItemDisplay(lineLocation, material, player, line);
                }
                continue;
            }

            // Создаем текст
            TextDisplay textDisplay = lineLocation.getWorld().spawn(lineLocation, TextDisplay.class, (d) -> {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    String content = line.content();
                    content = PlaceholderAPI.setPlaceholders(player, content);

                    // Обработка многострочных текстов с использованием \n
                    String[] splitContent = content.split("\\\\n");
                    Component combinedComponent = Component.text("");
                    for (String part : splitContent) {
                        Component component = miniMessage.deserialize(part);
                        combinedComponent = combinedComponent.append(component).append(Component.newline());
                    }

                    Matcher matcher = Pattern.compile("<#(.*?)>").matcher(content);
                    if (matcher.find()) {
                        String animationName = matcher.group(1);
                        List<AnimationManager.AnimationFrame> animation = animationManager.getAnimation(animationName);
                        if (animation != null) {
                            startAnimation(d, animation, player, content, matcher.start(), matcher.end(), line);
                        }
                    } else {
                        d.text(combinedComponent);

                        configureDisplay(d, line);
                    }
                }
            });
            displays.add(textDisplay);
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (player.getLocation().distance(location) < visibilityDistance) {
                        showDisplays(player); // показывание голограммы
                    } else {
                        hideDisplays(player); // скрытие голограммы
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 5L);
    }

    private void configureDisplay(Display display, Line line) {
        setBillboardType(display, line.billboardType());
        if (display instanceof TextDisplay textDisplay) {
            setTextAlignmentType(textDisplay, line.textType());
            textDisplay.setGravity(false);
            textDisplay.setVisibleByDefault(true);
            textDisplay.setBrightness(new Display.Brightness(brightness, brightness));
            setBackgroundColorWithAlpha(textDisplay, line.backgroundColor());
            textDisplay.setShadowed(line.textShadow());
            textDisplay.setShadowRadius(0);
            textDisplay.setSeeThrough(true); // Видить ли голограмму через блоки?
        }
        setTextScale(display, line.scale());
    }

    private void createBlockDisplay(Location location, Material material, Player player, Line line) {
        ItemStack itemStack = new ItemStack(material);
        ItemDisplay display = location.getWorld().spawn(location, ItemDisplay.class, (d) -> {
            d.setItemStack(itemStack);
            setBillboardType(d, line.billboardType());
            setTextScale(d, line.scale());
        });
        player.showEntity(plugin, display);
        itemDisplays.add(display);
    }

    private void createItemDisplay(Location location, Material material, Player player, Line line) {
        ItemStack itemStack = new ItemStack(material);
        ItemDisplay display = location.getWorld().spawn(location, ItemDisplay.class, (d) -> {
            d.setItemStack(itemStack);
            setBillboardType(d, line.billboardType());
            setTextScale(d, line.scale());
        });
        player.showEntity(plugin, display);
        itemDisplays.add(display);
    }

    private String extractMaterialName(String content, String placeholder) {
        int startIndex = content.indexOf(placeholder) + placeholder.length();
        int endIndex = content.indexOf(">", startIndex);
        return content.substring(startIndex, endIndex);
    }

    private void startAnimation(TextDisplay display, List<AnimationManager.AnimationFrame> animation, Player player, String originalLine, int start, int end, Line line) {
        configureDisplay(display, line); // Assuming animation applies to the first line

        new BukkitRunnable() {
            int index = 0;

            @Override
            public void run() {
                if (index >= animation.size()) {
                    index = 0;
                }
                AnimationManager.AnimationFrame frame = animation.get(index);
                String frameText = originalLine.substring(0, start) + frame.string() + originalLine.substring(end);
                String parsedLine = PlaceholderAPI.setPlaceholders(player, frameText);
                Component component = MiniMessage.miniMessage().deserialize(parsedLine);

                // Обработка блоков и предметов в анимациях
                if (frame.string().contains("<#BLOCK:")) {
                    String materialName = extractMaterialName(frame.string(), "<#BLOCK:");
                    Material material = Material.getMaterial(materialName);
                    if (material == null) {
                        material = Material.GOLDEN_APPLE;
                    }
                    if (material.isBlock()) {
                        createBlockDisplay(display.getLocation(), material, player, line);
                    }
                } else if (frame.string().contains("<#ITEM:")) {
                    String materialName = extractMaterialName(frame.string(), "<#ITEM:");
                    Material material = Material.getMaterial(materialName);
                    if (material == null) {
                        material = Material.GOLDEN_APPLE;
                    }
                    createItemDisplay(display.getLocation(), material, player, line);
                } else {
                    display.text(component);
                }

                index++;
            }
        }.runTaskTimer(plugin, 0L, animation.get(0).interval());
    }

    private void setBillboardType(Display display, String type) {
        switch (type.toLowerCase()) {
            case "center":
                display.setBillboard(Display.Billboard.CENTER);
                break;
            case "vertical":
                display.setBillboard(Display.Billboard.VERTICAL);
                break;
            case "horizontal":
                display.setBillboard(Display.Billboard.HORIZONTAL);
                break;
            case "fixed":
            default:
                display.setBillboard(Display.Billboard.FIXED);
                if (display instanceof TextDisplay textDisplay) {
                    textDisplay.setSeeThrough(true); // Видимость сквозь блоки
                }
                break;
        }
    }

    public void setTextAlignmentType(TextDisplay display, String type) {
        switch (type.toLowerCase()) {
            case "left":
                display.setAlignment(TextDisplay.TextAlignment.LEFT);
                break;
            case "right":
                display.setAlignment(TextDisplay.TextAlignment.RIGHT);
                break;
            case "center":
            default:
                display.setAlignment(TextDisplay.TextAlignment.CENTER);
                break;
        }
    }

    private void setTextScale(Display display, Vector3f scale) {
        Matrix4f transformationMatrix = new Matrix4f().scaling(scale.x, scale.y, scale.z);
        display.setTransformationMatrix(transformationMatrix);
    }

    private void showDisplays(Player player) {
        for (TextDisplay display : displays) {
            player.showEntity(plugin, display);
        }
        for (ItemDisplay display : itemDisplays) {
            player.showEntity(plugin, display);
        }
    }

    private void hideDisplays(Player player) {
        for (TextDisplay display : displays) {
            player.hideEntity(plugin, display);
        }
        for (ItemDisplay display : itemDisplays) {
            player.hideEntity(plugin, display);
        }
    }

    public void remove() {
        for (TextDisplay display : displays) {
            display.remove();
        }
        for (ItemDisplay display : itemDisplays) {
            display.remove();
        }
    }

    public String getName() {
        return name;
    }

    public List<Line> getLines() {
        return lines;
    }

    public double getVisibilityDistance() {
        return visibilityDistance;
    }

    public int getBrightness() {
        return brightness;
    }

    public Location getLocation() {
        return location;
    }

    private void setBackgroundColorWithAlpha(TextDisplay display, String colorName) {
        Color color;
        if (colorName.equalsIgnoreCase("none")) {
            color = Color.fromARGB(0, 0, 0, 0); // Прозрачный цвет
        } else {
            color = parseBackgroundColor(colorName);
            if (color != null) {
                color = Color.fromARGB(128, color.getRed(), color.getGreen(), color.getBlue()); // Полупрозрачный фон
            }
        }
        display.setBackgroundColor(color);
    }

    private @Nullable Color parseBackgroundColor(String colorName) {
        return switch (colorName.toLowerCase()) {
            case "red" -> Color.RED;
            case "blue" -> Color.BLUE;
            case "green" -> Color.GREEN;
            case "black" -> Color.BLACK;
            case "silver" -> Color.SILVER;
            case "purple" -> Color.PURPLE;
            case "none" -> null;
            default -> null; // без цвета
        };
    }

    public record Line(String content, double lineSpacing, String backgroundColor, String billboardType, Vector3f scale,
                       boolean textShadow, String textType) {
    }
}
