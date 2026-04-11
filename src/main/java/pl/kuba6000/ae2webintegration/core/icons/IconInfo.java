package pl.kuba6000.ae2webintegration.core.icons;

import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;

import pl.kuba6000.ae2webintegration.core.AE2WebIntegration;

public class IconInfo {

    public int elementId; // <id>.png

    public String registryName;
    public int damageValue;

    public transient NBTTagCompound nbtInfos;
    private String nbtInfosSerial;

    public void extractNbtInfos() {
        if (nbtInfosSerial == null || nbtInfosSerial.isEmpty()) {
            nbtInfos = null;
        } else {
            try {
                nbtInfos = JsonToNBT.getTagFromJson(nbtInfosSerial);
            } catch (NBTException e) {
                AE2WebIntegration.LOG.atWarn()
                    .withThrowable(e)
                    .log("Error while loading NBT info for cached icon #{}", elementId);
            }
        }
    }
}
