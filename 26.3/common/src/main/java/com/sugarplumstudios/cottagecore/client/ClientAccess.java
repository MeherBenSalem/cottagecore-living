package com.sugarplumstudios.cottagecore.client;

import com.sugarplumstudios.cottagecore.client.screen.CollectionBookScreen;
import com.sugarplumstudios.cottagecore.network.OpenBookPacket;
import net.minecraft.client.Minecraft;

/** Client-only entry points, called by the loaders' client packet handlers. */
public final class ClientAccess {
    private ClientAccess() {
    }

    public static void openBook(OpenBookPacket packet) {
        Minecraft.getInstance().gui.setScreen(new CollectionBookScreen(packet));
    }
}
