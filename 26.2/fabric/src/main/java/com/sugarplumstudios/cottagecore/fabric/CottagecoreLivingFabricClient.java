package com.sugarplumstudios.cottagecore.fabric;

import com.sugarplumstudios.cottagecore.client.ClientAccess;
import com.sugarplumstudios.cottagecore.client.WaterTint;
import com.sugarplumstudios.cottagecore.config.CottageConfig;
import com.sugarplumstudios.cottagecore.network.OpenBookPacket;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.BlockColorRegistry;

import java.util.List;

/**
 * Fabric client set-up. Since 26.1 vanilla picks each block quad's render layer (solid, cutout, translucent) from
 * its texture, so the decorations need no BlockRenderLayerMap entries.
 */
public final class CottagecoreLivingFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        CottageConfig.loadClient();
        // Handlers run on the client's main thread.
        ClientPlayNetworking.registerGlobalReceiver(OpenBookPacket.TYPE, (packet, context) -> ClientAccess.openBook(packet));
        BlockColorRegistry.register(List.of(WaterTint.INSTANCE), WaterTint.watery());
    }
}
