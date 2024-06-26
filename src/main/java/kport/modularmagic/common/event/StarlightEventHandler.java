package kport.modularmagic.common.event;

import hellfirepvp.astralsorcery.common.event.StarlightNetworkEvent;
import kport.modularmagic.common.tile.TileStarlightInput;
import kport.modularmagic.common.tile.TileStarlightOutput;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class StarlightEventHandler {

    @SubscribeEvent
    public static void onStarlightTransmissionRegister(StarlightNetworkEvent.TransmissionRegister event) {
        event.getRegistry().registerProvider(new TileStarlightInput.StarlightProviderReceiverProvider());
    }

    @SubscribeEvent
    public static void onStarlightSourceRegister(StarlightNetworkEvent.SourceProviderRegistry event) {
        event.getRegistry().registerProvider(new TileStarlightOutput.Provider());
    }
}
