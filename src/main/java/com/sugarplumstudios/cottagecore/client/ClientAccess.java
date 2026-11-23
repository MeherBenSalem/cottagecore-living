package com.sugarplumstudios.cottagecore.client;

import com.sugarplumstudios.cottagecore.client.screen.CollectionBookScreen;
import com.sugarplumstudios.cottagecore.network.OpenBookPacket;
import net.minecraft.client.Minecraft;

/** Client-only entry points called from common code through DistExecutor. */
public final class ClientAccess {
    private ClientAccess() {
    }

    public static void openBook(OpenBookPacket packet) {
        Minecraft.getInstance().setScreen(new CollectionBookScreen(packet));
    }
}
