package cosinus.slava7777;

import cosinus.slava7777.Managers.AnimationManager;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.entity.Display;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Hologram implements Listener {

    private final CosHolograms plugin;
    private final String name;
    private final Location location;
    private final List<String> lines;
    private final double visibilityDistance;
    private final double lineSpacing;
    private final String backgroundColor;
    private final int brightness;
    private final String billboardType;
    private final boolean textShadow;
    private final String textType;
    private Vector3f scale;
    private List<TextDisplay> displays;
    private final AnimationManager animationManager;

    public Hologram(CosHolograms plugin,
                    String name,
                    Location location,
                    List<String> lines,
                    double visibilityDistance,
                    double lineSpacing,
                    String backgroundColor,
                    int brightness,
                    String billboardType,
                    Vector3f scale,
                    boolean textShadow,
                    String textType) {
        this.plugin = plugin;
        this.name = name;
        this.location = location;
        this.lines = lines;
        this.visibilityDistance = visibilityDistance;
        this.lineSpacing = lineSpacing;
        this.backgroundColor = backgroundColor;
        this.brightness = brightness;
        this.billboardType = billboardType;
        this.scale = scale;
        this.textType = textType;
        this.textShadow = textShadow;
        this.animationManager = new AnimationManager(plugin);

        setupDisplays();
    }

    private void setupDisplays() {
        displays = new ArrayList<>();
        MiniMessage miniMessage = MiniMessage.miniMessage();

        for (int i = 0; i < lines.size(); i++) {
            int finalI = i;
            Location lineLocation = location.clone().add(0, -lineSpacing * i, 0);

            // Создаем текст
            TextDisplay textDisplay = lineLocation.getWorld().spawn(lineLocation, TextDisplay.class, (d) -> {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    String line = lines.get(finalI);
                    line = PlaceholderAPI.setPlaceholders(player, line);

                    Matcher matcher = Pattern.compile("<#(.*?)>").matcher(line);
                    if (matcher.find()) {
                        String animationName = matcher.group(1);
                        List<AnimationManager.AnimationFrame> animation = animationManager.getAnimation(animationName);
                        if (animation != null) {
                            startAnimation(d, animation, player, line, matcher.start(), matcher.end());
                        }
                    } else {
                        Component component = miniMessage.deserialize(line);
                        d.text(component);

                        configureTextDisplay(d);
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
                        showDisplays(player); // Показываем голограммы
                    } else {
                        hideDisplays(player); // Скрываем голограммы
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 5L);
    }

    private void configureTextDisplay(TextDisplay display) {
        setTextAlignmentType(display, textType);
        display.setGravity(false);
        display.setVisibleByDefault(true);
        display.setBrightness(new Display.Brightness(brightness, brightness));
        setBackgroundColorWithAlpha(display, backgroundColor);
        setBillboardType(display, billboardType);
        setTextScale(display, scale);
        setTextShadow(display, textShadow); // Устанавливаем тени для текста
        display.setSeeThrough(true); // Видимость сквозь блоки
    }


    private void startAnimation(TextDisplay display, List<AnimationManager.AnimationFrame> animation, Player player, String originalLine, int start, int end) {
        configureTextDisplay(display);

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
                display.text(component);
                index++;
            }
        }.runTaskTimer(plugin, 0L, animation.get(0).interval());
    }

    private void setBillboardType(TextDisplay display, String type) {
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
                display.setSeeThrough(true); // Видимость сквозь блоки
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

    public String getTextType () {
        return textType;
    }

    private void setTextScale(TextDisplay display, Vector3f scale) {
        // Устанавливаем масштаб текста используя внутренние методы
        Matrix4f transformationMatrix = new Matrix4f().scaling(scale.x, scale.y, scale.z);
        display.setTransformationMatrix(transformationMatrix);
    }

    private void showDisplays(Player player) {
        for (TextDisplay display : displays) {
            player.showEntity(plugin, display);
        }
    }

    private void hideDisplays(Player player) {
        for (TextDisplay display : displays) {
            player.hideEntity(plugin, display);
        }
    }

    public void remove() {
        for (TextDisplay display : displays) {
            display.remove();
        }
    }

    public String getName() {
        return name;
    }

    public List<String> getLines() {
        return lines;
    }

    public double getVisibilityDistance() {
        return visibilityDistance;
    }

    public double getLineSpacing() {
        return lineSpacing;
    }

    public String getBackgroundColor() {
        return backgroundColor;
    }

    public int getBrightness() {
        return brightness;
    }

    public Location getLocation() {
        return location;
    }

    public String getBillboardType() {
        return billboardType;
    }

    public Vector3f getScale() {
        return scale;
    }

    public void setScale(Vector3f scale) {
        if (!Objects.equals(this.scale, scale)) {
            this.scale = scale;
            for (TextDisplay display : displays) {
                setTextScale(display, scale);
            }
        }
    }

    public boolean hasTextShadow() {
        return textShadow;
    }

    private void setTextShadow(TextDisplay display, boolean shadow) {
        display.setShadowed(shadow);
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
            default -> null; // No background color
        };
    }
}
