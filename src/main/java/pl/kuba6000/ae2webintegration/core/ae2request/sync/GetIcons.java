package pl.kuba6000.ae2webintegration.core.ae2request.sync;

import net.minecraft.item.ItemStack;
import pl.kuba6000.ae2webintegration.core.AE2Controller;
import pl.kuba6000.ae2webintegration.core.AE2WebIntegration;
import pl.kuba6000.ae2webintegration.core.api.JSON_DetailedItem;
import pl.kuba6000.ae2webintegration.core.api.JSON_IconItem;
import pl.kuba6000.ae2webintegration.core.icons.IconCache;
import pl.kuba6000.ae2webintegration.core.interfaces.IAE;
import pl.kuba6000.ae2webintegration.core.interfaces.IAEGrid;
import pl.kuba6000.ae2webintegration.core.interfaces.IItemList;
import pl.kuba6000.ae2webintegration.core.interfaces.IItemStack;
import pl.kuba6000.ae2webintegration.core.interfaces.service.IAEStorageGrid;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class GetIcons extends ISyncedRequest {

    public static final String MISSING_TEXTURE =
            "iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAIAAACQkWg2AAAAIGNIUk0AAHolAACAgwAA+f8AAIDp"
            + "AAB1MAAA6mAAADqYAAAXb5JfxUYAAAAjSURBVCjPY/zD8J8BG2BhYMQqzjiqgSYaGHAAXAaNaq"
            + "CJBgBNyh/pMWe+mgAAAABJRU5ErkJggg==";

    List<Integer> hashCodes = null;
    Base64.Encoder b64Encoder = Base64.getEncoder();

    @Override
    boolean init(Map<String, String> getParams) {
        if (!IconCache.isActive()) {
            deny("ICON_CACHE_INACTIVE");
            setData("Icon cache not set on the server!");
            return false;
        }

        if (!getParams.containsKey("items")) {
            noParam("items");
            return false;
        }

        hashCodes = Arrays.stream(getParams.get("items").split(","))
                .map(Integer::parseInt)
                .collect(Collectors.toList());
        return true;
    }

    @Override
    public void handle(IAE iae) {
        List<JSON_IconItem> iconsList = hashCodes.stream()
                .map(this::getIcon)
                .collect(Collectors.toList());

        setData(iconsList);
        done();
    }

    private JSON_IconItem getIcon(final int hashcode) {
        JSON_IconItem icon = new JSON_IconItem();
        icon.hashcode = hashcode;

        IItemStack iItemStack = AE2Controller.hashcodeToAEItemStack.get(hashcode);
        if (iItemStack == null) {
            icon.pngData = MISSING_TEXTURE;
            return icon;
        }

        ItemStack itemStack = iItemStack.web$getItemStack();
        if (itemStack == null) {
            icon.pngData = MISSING_TEXTURE;
            return icon;
        }

        byte[] itemTexture = IconCache.getItemTexture(itemStack);
        if (itemTexture == null) {
            icon.pngData = MISSING_TEXTURE;
        } else {
            icon.pngData = b64Encoder.encodeToString(itemTexture);
        }
        return icon;
    }
}