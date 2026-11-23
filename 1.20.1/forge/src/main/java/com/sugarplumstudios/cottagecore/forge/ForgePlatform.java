package com.sugarplumstudios.cottagecore.forge;

import com.sugarplumstudios.cottagecore.CottagecoreLiving;
import com.sugarplumstudios.cottagecore.network.OpenBookPacket;
import com.sugarplumstudios.cottagecore.network.TakePacket;
import com.sugarplumstudios.cottagecore.platform.IPlatform;
import com.sugarplumstudios.cottagecore.platform.RegistryEntry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Supplier;

public final class ForgePlatform implements IPlatform {
    private static final Map<ResourceKey<?>, DeferredRegister<?>> REGISTERS = new LinkedHashMap<>();

    /** Hands every deferred register to the mod event bus. Call after {@link CottagecoreLiving#init()}. */
    public static void registerAll(IEventBus modBus) {
        REGISTERS.values().forEach(register -> register.register(modBus));
    }

    @Override
    public String name() {
        return "Forge";
    }

    @Override
    public Path configDir() {
        return FMLPaths.CONFIGDIR.get();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <R, T extends R> RegistryEntry<T> register(ResourceKey<? extends Registry<R>> registry, String name, Supplier<T> factory) {
        DeferredRegister<R> register = (DeferredRegister<R>) REGISTERS.computeIfAbsent(registry,
                key -> DeferredRegister.create(registry, CottagecoreLiving.MOD_ID));
        RegistryObject<T> object = register.register(name, factory);
        return new RegistryEntry<>(object.getId(), object);
    }

    @Override
    public CreativeModeTab.Builder creativeTab(@Nullable ResourceLocation after) {
        CreativeModeTab.Builder builder = CreativeModeTab.builder();
        if (after != null) {
            builder.withTabsBefore(ResourceKey.create(Registries.CREATIVE_MODE_TAB, after));
        }
        return builder;
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public <T extends BlockEntity> BlockEntityType<T> blockEntityType(BiFunction<BlockPos, BlockState, T> factory, Block... blocks) {
        return BlockEntityType.Builder.of(factory::apply, blocks).build(null);
    }

    @Override
    public void sendToPlayer(ServerPlayer player, OpenBookPacket packet) {
        ForgeNetwork.sendTo(player, packet);
    }

    @Override
    public void sendToServer(TakePacket packet) {
        ForgeNetwork.sendToServer(packet);
    }
}
