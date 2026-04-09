package pl.kuba6000.ae2webintegration.core.icons;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import pl.kuba6000.ae2webintegration.core.AE2WebIntegration;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class IconCache {

    private static Path baseAtlasPath;

    private static List<IconInfo> loadedIcons = new ArrayList<>();

    public static void init(final File configDir) {
        baseAtlasPath = configDir.toPath().resolve("ae2webintegration").resolve("atlas");
    }

    public static int loadCache() {
        loadedIcons.clear();

        Path atlasInfos = baseAtlasPath.resolve("info.json");

        if (Files.exists(atlasInfos)) {
            return loadAtlas(atlasInfos);
        } else {
            AE2WebIntegration.LOG.atWarn().log("Error while loading icon cache : {} not found", atlasInfos.toFile().getAbsolutePath());
            return -1;
        }
    }

    private static int loadAtlas(final Path atlasInfoPath) {

        String jsonIn;
        try {
            jsonIn = new String(Files.readAllBytes(atlasInfoPath));
        } catch (IOException e) {
            AE2WebIntegration.LOG.atError().withThrowable(e).log("Error while loading icon cache :");
            return -1;
        }

        Type listType = new TypeToken<List<IconInfo>>(){}.getType();
        loadedIcons = new Gson().fromJson(jsonIn, listType);
        loadedIcons.forEach(IconInfo::extractNbtInfos);
        return loadedIcons.size();
    }

    public static boolean isActive() {
        return !loadedIcons.isEmpty();
    }

    private static byte[] readPngFromId(IconInfo id) {
        Path toRead = baseAtlasPath.resolve(id.elementId + ".png");
        try {
            return Files.readAllBytes(toRead);
        } catch (IOException e) {
            AE2WebIntegration.LOG.atError().withThrowable(e).log("Error while reading atlas texture {}", toRead.toFile().getAbsolutePath());
            return null;
        }
    }

    public static byte[] getItemTexture(final ItemStack toSearch) {
        // Search with registry name
        String nameToSearch = toSearch.getItem().getRegistryName().toString();
        List<IconInfo> listSearchByRegistry = loadedIcons.stream()
                .filter(info -> info.registryName.equals(nameToSearch))
                .collect(Collectors.toList());

        if (listSearchByRegistry.isEmpty()) {
            return null;
        } else if (listSearchByRegistry.size() == 1) {
            return readPngFromId(listSearchByRegistry.get(0));
        }

        // Multiple elements have the same registry name, searching with damage value
        List<IconInfo> listSearchByDamage = listSearchByRegistry.stream()
                .filter(info -> info.damageValue == toSearch.getItemDamage())
                .collect(Collectors.toList());

        if (listSearchByDamage.isEmpty()) {
            // Instead of returning null, we return the first available texture by registry id
            return readPngFromId(listSearchByRegistry.get(0));
        } else if (listSearchByDamage.size() == 1) {
            return readPngFromId(listSearchByDamage.get(0));
        }

        // Multiples textures share the same damage value, searching with NBT
        NBTTagCompound itemTags = toSearch.getTagCompound();
        if (itemTags == null) {
            // first element returned by damage
            return readPngFromId(listSearchByDamage.get(0));
        }

        // Creating score for each element
        Map<IconInfo, Integer> iconScoreMap = new HashMap<>();
        listSearchByDamage.forEach(iconInfo -> iconScoreMap.put(iconInfo, 0));

        // Evaluating each key and incrementing score on match
        for (String tagKey : itemTags.getKeySet()) {
            byte tagType = itemTags.getTagId(tagKey);
            for (IconInfo iconInfo : iconScoreMap.keySet()) {
                try {
                    if (compareValue(itemTags, iconInfo.nbtInfos, tagKey, tagType)) {
                        iconScoreMap.put(iconInfo, iconScoreMap.get(iconInfo) + 1);
                    }
                } catch (Exception ignore) {
                    AE2WebIntegration.LOG.atWarn().withThrowable(ignore).log("Error while comparing NBT (key: {}) for item {}", tagKey, toSearch.getItem().getRegistryName().toString());
                }
            }
        }

        // Getting the max score
        IconInfo iconInfo = iconScoreMap.entrySet().stream()
                .sorted((o1, o2) -> Integer.compare(o2.getValue(), o1.getValue()))
                .map(Map.Entry::getKey)
                .findFirst().orElse(listSearchByDamage.get(0));

        return readPngFromId(iconInfo);
    }

    private static boolean compareValue(NBTTagCompound a, NBTTagCompound b, String key, byte type) {
        switch (type) {
            case NBTType.BYTE:
                return a.getByte(key) == b.getByte(key);

            case NBTType.SHORT:
                return a.getShort(key) == b.getShort(key);

            case NBTType.INT:
                return a.getInteger(key) == b.getInteger(key);

            case NBTType.LONG:
                return a.getLong(key) == b.getLong(key);

            case NBTType.FLOAT:
                return a.getFloat(key) == b.getFloat(key);

            case NBTType.DOUBLE:
                return a.getDouble(key) == b.getDouble(key);

            case NBTType.STRING:
                return a.getString(key).equals(b.getString(key));

            case NBTType.COMPOUND:
                return a.getCompoundTag(key).equals(b.getCompoundTag(key));

            case NBTType.LIST:
                return a.getTagList(key, 0).equals(b.getTagList(key, 0));

            case NBTType.BYTE_ARRAY:
                return Arrays.equals(a.getByteArray(key), b.getByteArray(key));

            case NBTType.INT_ARRAY:
                return Arrays.equals(a.getIntArray(key), b.getIntArray(key));

            case NBTType.LONG_ARRAY:
                NBTBase tagA = a.getTag(key);
                NBTBase tagB = b.getTag(key);
                return tagA.equals(tagB);

            default:
                return false;
        }
    }

    public static final class NBTType {
        public static final byte END = 0;
        public static final byte BYTE = 1;
        public static final byte SHORT = 2;
        public static final byte INT = 3;
        public static final byte LONG = 4;
        public static final byte FLOAT = 5;
        public static final byte DOUBLE = 6;
        public static final byte BYTE_ARRAY = 7;
        public static final byte STRING = 8;
        public static final byte LIST = 9;
        public static final byte COMPOUND = 10;
        public static final byte INT_ARRAY = 11;
        public static final byte LONG_ARRAY = 12;

        private NBTType() {}
    }
}
