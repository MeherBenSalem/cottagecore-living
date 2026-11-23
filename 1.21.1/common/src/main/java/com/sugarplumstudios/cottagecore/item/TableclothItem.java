package com.sugarplumstudios.cottagecore.item;

import com.sugarplumstudios.cottagecore.catalog.DecorSet;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/** A folded tablecloth. Right-click a cottage table to dress it (handled by the table). */
public class TableclothItem extends Item {
    private final DecorSet set;

    public TableclothItem(DecorSet set, Properties properties) {
        super(properties);
        this.set = set;
    }

    public DecorSet set() {
        return set;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.cottagecoreliving.set", set.displayName())
                .withStyle(Style.EMPTY.withColor(TextColor.fromRgb(set.color()))));
        tooltip.add(Component.translatable("item.cottagecoreliving.tablecloth.desc").withStyle(ChatFormatting.GRAY));
    }
}
