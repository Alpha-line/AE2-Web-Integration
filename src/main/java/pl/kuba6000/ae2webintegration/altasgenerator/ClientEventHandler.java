package pl.kuba6000.ae2webintegration.altasgenerator;

import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class ClientEventHandler {

    public static ItemBatchRenderer renderer;

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {

        if (event.phase != TickEvent.Phase.END) return;

        if (renderer != null && renderer.isRunning()) {
            renderer.tick();
        }
    }
}
