package com.sugarplumstudios.cottagecore.catalog;

import org.jetbrains.annotations.Nullable;

/** One block of a family, e.g. the strawberry teapot. */
public record DecorVariant(String id, @Nullable DecorSet set) {
}
