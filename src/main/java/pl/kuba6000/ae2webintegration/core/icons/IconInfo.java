package pl.kuba6000.ae2webintegration.core.icons;

import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;

public class IconInfo {

    public int elementId; // <id>.png

    public String registryName;
    public int damageValue;

    public transient NBTTagCompound nbtInfos;
    private String nbtInfosSerial;

    public void extractNbtInfos() throws NBTException {
        if (nbtInfosSerial == null || nbtInfosSerial.isEmpty()) {
            nbtInfos = null;
        } else {
            nbtInfos = JsonToNBT.getTagFromJson(nbtInfosSerial);
        }
    }

    public void prepareNbtInfosForWrite() {
        if (nbtInfos == null) {
            nbtInfosSerial = null;
        } else {
            nbtInfosSerial = nbtInfos.toString();
        }
    }
}
