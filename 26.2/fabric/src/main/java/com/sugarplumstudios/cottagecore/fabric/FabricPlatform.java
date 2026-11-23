package com.sugarplumstudios.cottagecore.fabric;

import com.sugarplumstudios.cottagecore.CottagecoreLiving;
import com.sugarplumstudios.cottagecore.network.OpenBookPacket;
import com.sugarplumstudios.cottagecore.network.TakePacket;
import com.sugarplumstudios.cottagecore.platform.IPlatform;
import com.sugarplumstudios.cottagecore.platform.RegistryEntry;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Supplier;

public final class FabricPlatform implements IPlatform {
    @Override
    public String name() {
        return "Fabric";
    }

    @Override
    public Path configDir() {
        return FabricLoader.getInstance().getConfigDir();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <R, T extends R> RegistryEntry<T> register(ResourceKey<? extends Registry<R>> registry, String name, Supplier<T> factory) {
        Registry<R> target = (Registry<R>) BuiltInRegistries.REGISTRY.getValue(registry.identifier());
        if (target == null) {
            throw new IllegalArgumentException("Unknown registry " + registry);
        }
        T value = Registry.register(target, CottagecoreLiving.id(name), factory.get());
        return new RegistryEntry<>(CottagecoreLiving.id(name), () -> value);
    }

    @Override
    public CreativeModeTab.Builder creativeTab(@Nullable Identifier after) {
        // Fabric lists modded tabs in registration order.
        return FabricCreativeModeTab.builder();
    }

    @Override
    public <T extends BlockEntity> BlockEntityType<T> blockEntityType(BiFunction<BlockPos, BlockState, T> factory, Block... blocks) {
        return new BlockEntityType<>(factory::apply, Set.of(blocks));
    }

    @Override
    public void sendToPlayer(ServerPlayer player, OpenBookPacket packet) {
        ServerPlayNetworking.send(player, packet);
    }

    @Override
    public void sendToServer(TakePacket packet) {
        ClientPlayNetworking.send(packet);
    }
}
