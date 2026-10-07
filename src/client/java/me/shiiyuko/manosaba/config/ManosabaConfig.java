package me.shiiyuko.manosaba.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.mojang.logging.LogUtils;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Manosaba 客户端配置。持久化在 {@code config/manosaba.json}。
 *
 * <p>Fabric 侧没有 Forge 的 {@code ModConfigSpec}，这里用 Gson 手写最小实现：
 * 读取失败或文件损坏时一律退回默认值，不抛异常打断启动。
 * 字段名即 JSON 键名（{@link Data}），改动字段名等于改动配置文件格式。
 */
public final class ManosabaConfig {

    private static final Logger LOG = LogUtils.getLogger();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE =
            FabricLoader.getInstance().getConfigDir().resolve("manosaba.json");

    public enum BackgroundCharacter {
        HIRO,
        EMA
    }

    /** 序列化载体。 */
    private static final class Data {
        BackgroundCharacter backgroundCharacter = BackgroundCharacter.HIRO;
    }

    private static final Data DATA = new Data();

    private ManosabaConfig() {
    }

    /** 启动时调用一次；文件不存在或损坏则保持默认值。 */
    public static void load() {
        if (!Files.exists(FILE)) {
            return;
        }
        try (Reader reader = Files.newBufferedReader(FILE, StandardCharsets.UTF_8)) {
            Data loaded = GSON.fromJson(reader, Data.class);
            if (loaded != null && loaded.backgroundCharacter != null) {
                DATA.backgroundCharacter = loaded.backgroundCharacter;
            }
        } catch (IOException | JsonSyntaxException e) {
            LOG.warn("读取配置 {} 失败，使用默认值", FILE, e);
        }
    }

    public static void save() {
        try {
            Files.createDirectories(FILE.getParent());
            try (Writer writer = Files.newBufferedWriter(FILE, StandardCharsets.UTF_8)) {
                GSON.toJson(DATA, writer);
            }
        } catch (IOException e) {
            LOG.warn("写入配置 {} 失败", FILE, e);
        }
    }

    public static BackgroundCharacter backgroundCharacter() {
        return DATA.backgroundCharacter;
    }

    public static void toggleBackground() {
        DATA.backgroundCharacter = DATA.backgroundCharacter == BackgroundCharacter.HIRO
                ? BackgroundCharacter.EMA
                : BackgroundCharacter.HIRO;
        save();
    }
}
