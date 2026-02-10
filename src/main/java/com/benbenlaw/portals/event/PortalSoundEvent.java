package com.benbenlaw.portals.event;

import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvent;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public record PortalSoundEvent(SoundEvent sound, float pitch, float volume) {

    @Contract
    public @NotNull SimpleSoundInstance getInstance() {
        return SimpleSoundInstance.forLocalAmbience(sound, pitch, volume);
    }
}