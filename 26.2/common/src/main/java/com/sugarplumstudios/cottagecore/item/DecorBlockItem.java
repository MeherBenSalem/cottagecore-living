package com.sugarplumstudios.cottagecore.item;

import com.sugarplumstudios.cottagecore.block.DecorBlock;
import com.sugarplumstudios.cottagecore.catalog.DecorFamily;
import com.sugarplumstudios.cottagecore.catalog.DecorSet;
import com.sugarplumstudios.cottagecore.catalog.Interaction;
import com.sugarplumstudios.cottagecore.catalog.Kind;
import com.sugarplumstudios.cottagecore.catalog.Mount;
import com.sugarplumstudios.cottagecore.config.CottageConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

/** The item for every decoration: its tooltip names the set and explains what it does. */
public class DecorBlockItem extends BlockItem {
    public DecorBlockItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display,
                                Consumer<Component> tooltip, TooltipFlag flag) {
        if (!(getBlock() instanceof DecorBlock decor)) {
            return;
        }
        DecorFamily family = decor.family();
        DecorSet set = decor.variant().set();
        if (set != null) {
            tooltip.accept(Component.translatable("tooltip.cottagecoreliving.set", set.displayName())
                    .withStyle(Style.EMPTY.withColor(TextColor.fromRgb(set.color()))));
        }
        if (!CottageConfig.detailedTooltips()) {
            return;
        }
        tooltip.accept(Component.translatable(family.descriptionKey()).withStyle(ChatFormatting.GRAY));
        for (Component hint : hints(family)) {
            tooltip.accept(Component.literal(" ✿ ").append(hint).withStyle(ChatFormatting.DARK_GREEN));
        }
    }

    /** Short "what can I do with it" lines, shared with the collection book. */
    public static List<Component> hints(DecorFamily family) {
        List<Component> out = new java.util.ArrayList<>();
        if (family.interaction() != Interaction.NONE) {
            out.add(Component.translatable(family.interaction().translationKey()));
        }
        switch (family.lightMode()) {
            case CANDLE -> out.add(Component.translatable("tooltip.cottagecoreliving.light.candle"));
            case SWITCH -> out.add(Component.translatable("tooltip.cottagecoreliving.light.switch"));
            case ALWAYS -> out.add(Component.translatable("tooltip.cottagecoreliving.light.always"));
            default -> {
            }
        }
        if (family.kind() == Kind.TABLE) {
            out.add(Component.translatable("tooltip.cottagecoreliving.cloth"));
        }
        if (family.stacks()) {
            out.add(Component.translatable("tooltip.cottagecoreliving.stack", family.maxCount()));
        }
        List<Mount> mounts = family.mounts();
        if (mounts.size() == 1 && mounts.get(0) == Mount.WALL) {
            out.add(Component.translatable("tooltip.cottagecoreliving.wall"));
        } else if (mounts.size() == 1 && mounts.get(0) == Mount.CEILING) {
            out.add(Component.translatable("tooltip.cottagecoreliving.ceiling"));
        } else if (mounts.contains(Mount.FLOOR) && mounts.contains(Mount.CEILING)) {
            out.add(Component.translatable("tooltip.cottagecoreliving.floor_or_ceiling"));
        } else if (mounts.contains(Mount.FLOOR) && mounts.contains(Mount.WALL)) {
            out.add(Component.translatable("tooltip.cottagecoreliving.wall_or_floor"));
        } else if (mounts.contains(Mount.CEILING) && mounts.contains(Mount.WALL)) {
            out.add(Component.translatable("tooltip.cottagecoreliving.ceiling_or_wall"));
        }
        if (family.hasFacing() && !(mounts.size() == 1 && mounts.get(0) == Mount.WALL)) {
            out.add(Component.translatable("tooltip.cottagecoreliving.rotate"));
        }
        return out;
    }
}
