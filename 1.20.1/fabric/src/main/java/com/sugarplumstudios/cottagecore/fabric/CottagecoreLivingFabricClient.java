package com.sugarplumstudios.cottagecore.fabric;

import com.sugarplumstudios.cottagecore.client.ClientAccess;
import com.sugarplumstudios.cottagecore.client.WaterTint;
import com.sugarplumstudios.cottagecore.config.CottageConfig;
import com.sugarplumstudios.cottagecore.network.OpenBookPacket;
import com.sugarplumstudios.cottagecore.registry.ModBlocks;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;

public final class CottagecoreLivingFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        CottageConfig.loadClient();
        ClientPlayNetworking.registerGlobalReceiver(OpenBookPacket.ID, (client, handler, buf, sender) -> {
            OpenBookPacket packet = OpenBookPacket.decode(buf);
            client.execute(() -> ClientAccess.openBook(packet));
        });
        Block[] watery = WaterTint.watery();
        ColorProviderRegistry.BLOCK.register((state, level, pos, tint) -> WaterTint.block(level, pos), watery);
        ColorProviderRegistry.ITEM.register((stack, tint) -> WaterTint.item(tint), watery);
        // Forge reads each model's render_type; on Fabric the layer is set per block.
        for (Block block : ModBlocks.all()) {
            BlockRenderLayerMap.INSTANCE.putBlock(block, FabricRenderLayers.TRANSLUCENT.contains(
                    BuiltInRegistries.BLOCK.getKey(block).getPath()) ? RenderType.translucent() : RenderType.cutout());
        }
    }
}
