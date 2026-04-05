package pl.kuba6000.ae2webintegration.altasgenerator;

import com.google.common.collect.Lists;
import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class AtlasGeneratorCommand extends CommandBase {

    static {
        ClientEventHandler.renderer = new ItemBatchRenderer(256, new ItemBatchRenderer.Listener() {
            @Override
            public void onProgress(int current, int total) {
                Minecraft.getMinecraft().player.sendStatusMessage(new TextComponentString("Progress: " + current + "/" + total), true);
            }

            @Override
            public void onFinished(int total) {
                Minecraft.getMinecraft().player.sendMessage(new TextComponentString("Finished exporting " + total + " items"));
            }

            @Override
            public void onException(final Exception ex) {
                Minecraft.getMinecraft().player.sendMessage(new TextComponentString("Error while generating atlas : " + ex.getClass() + " : " + ex.getMessage()));
            }
        });
    }

    @Override
    public @Nonnull String getName() {
        return "genAtlas";
    }

    @Override
    public @Nonnull String getUsage(final @NotNull ICommandSender sender) {
        return "";
    }

    @Override
    public void execute(final @NotNull MinecraftServer server, final @NotNull ICommandSender sender, final @Nonnull String[] args) throws CommandException {
        if (ClientEventHandler.renderer != null) {
            if (ClientEventHandler.renderer.isRunning()) {
                sender.sendMessage(new TextComponentString("The renderer is currently running !"));
            } else {
                List<ItemStack> registeredItems = new ArrayList<>(JeiExporter.getRegisteredItems());

                // use argument "small" to generate small atlas
                ArrayList<String> strings = Lists.newArrayList(args);
                if (strings.contains("small")) {
                    List<ItemStack> list2 = new ArrayList<>();
                    for (int i = 0; i < 50; i++) {
                        list2.add(registeredItems.get(i));
                    }
                    ClientEventHandler.renderer.start(list2);
                } else {
                    ClientEventHandler.renderer.start(registeredItems);
                }
            }
        }
    }
}
