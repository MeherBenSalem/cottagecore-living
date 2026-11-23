package com.sugarplumstudios.cottagecore.item;

import com.sugarplumstudios.cottagecore.collection.CollectionData;
import com.sugarplumstudios.cottagecore.platform.Services;
import com.sugarplumstudios.cottagecore.network.OpenBookPacket;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

/**
 * The Decoration Collection Book. Using it asks the server for the player's
 * collection and the current season, then the client opens the book screen.
 */
public class CollectionBookItem extends Item {
    public CollectionBookItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (player instanceof ServerPlayer serverPlayer) {
            CollectionData.scanInventory(serverPlayer);
            Services.PLATFORM.sendToPlayer(serverPlayer, OpenBookPacket.of(serverPlayer));
            level.playSound(null, player.blockPosition(), SoundEvents.BOOK_PAGE_TURN, SoundSource.PLAYERS, 1.0F, 1.0F);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display,
                                Consumer<Component> tooltip, TooltipFlag flag) {
        tooltip.accept(Component.translatable("item.cottagecoreliving.collection_book.desc").withStyle(ChatFormatting.LIGHT_PURPLE));
    }
}
