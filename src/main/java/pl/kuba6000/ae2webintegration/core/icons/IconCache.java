package pl.kuba6000.ae2webintegration.core.icons;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import pl.kuba6000.ae2webintegration.core.AE2WebIntegration;
import pl.kuba6000.ae2webintegration.core.Config;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

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
}
