package com.benbenlaw.portals.portal;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.GlobalPos;

public record DimensionLink(
    GlobalPos fromPos,
    GlobalPos toPos
) {

    public static final Codec<DimensionLink> CODEC = RecordCodecBuilder.create(
        instance -> instance.group(
                GlobalPos.CODEC.fieldOf("fromPos").forGetter(DimensionLink::fromPos),
                GlobalPos.CODEC.fieldOf("toPos").forGetter(DimensionLink::toPos)
        ).apply(instance, DimensionLink::new)
    );
}