package com.sugarplumstudios.cottagecore.catalog;

import com.sugarplumstudios.cottagecore.block.BlanketDecorBlock;
import com.sugarplumstudios.cottagecore.block.ClockDecorBlock;
import com.sugarplumstudios.cottagecore.block.CurtainDecorBlock;
import com.sugarplumstudios.cottagecore.block.DecorBlock;
import com.sugarplumstudios.cottagecore.block.SeasonalDecorBlock;
import com.sugarplumstudios.cottagecore.block.StorageDecorBlock;
import com.sugarplumstudios.cottagecore.block.TableDecorBlock;
import net.minecraft.world.level.block.Block;

import java.util.function.BiFunction;

/** Which block class a family uses. */
public enum Kind {
    PLAIN(DecorBlock::new),
    STORAGE(StorageDecorBlock::new),
    CLOCK(ClockDecorBlock::new),
    SEASONAL(SeasonalDecorBlock::new),
    CURTAIN(CurtainDecorBlock::new),
    TABLE(TableDecorBlock::new),
    BLANKET(BlanketDecorBlock::new);

    private final BiFunction<DecorFamily, DecorVariant, Block> factory;

    Kind(BiFunction<DecorFamily, DecorVariant, Block> factory) {
        this.factory = factory;
    }

    public Block create(DecorFamily family, DecorVariant variant) {
        return factory.apply(family, variant);
    }
}
