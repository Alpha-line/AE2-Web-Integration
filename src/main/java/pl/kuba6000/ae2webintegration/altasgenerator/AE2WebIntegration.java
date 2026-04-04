package pl.kuba6000.ae2webintegration.altasgenerator;

import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pl.kuba6000.ae2webintegration.Tags;

import static pl.kuba6000.ae2webintegration.altasgenerator.AE2WebIntegration.MODID;

@Mod(
        modid = MODID,
        version = Tags.VERSION,
        name = "AE2WebIntegration-AtlasGenerator",
        acceptedMinecraftVersions = "[1.12.2]",
        acceptableRemoteVersions = "*",
        clientSideOnly = true
)
public class AE2WebIntegration {

    public static final String MODID = "ae2webintegration-atlasgenerator";
    public static final Logger LOG = LogManager.getLogger(MODID);

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {}

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        if (event.getSide().isClient()) {
            MinecraftForge.EVENT_BUS.register(new ClientEventHandler());
        }
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        if (event.getSide().isClient()) {
            ClientCommandHandler.instance.registerCommand(new AtlasGeneratorCommand());
        }
    }

    

    @Mod.EventHandler
    public void serverStarting(FMLServerStartingEvent event) {

    }

}
