package pl.kuba6000.ae2webintegration.altasgenerator;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.JEIPlugin;
import mezz.jei.api.ingredients.VanillaTypes;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;

@JEIPlugin
public class JeiExporter implements IModPlugin {

    private static IModRegistry iModRegistry = null;

    public JeiExporter() {}

    @Override
    public void register(final @NotNull IModRegistry registry) {
        IModPlugin.super.register(registry);
        JeiExporter.iModRegistry = registry;
    }

    public static Collection<ItemStack> getRegisteredItems() {
        if (iModRegistry == null) {
            AE2WebIntegration.LOG.atError().log("JEI Plugin is not loaded, cannot proceed to export the items list");
            return new ArrayList<>();
        }

        return iModRegistry.getIngredientRegistry().getAllIngredients(VanillaTypes.ITEM);
    }
}
