package org.exodusstudio.stellaris.common.registries;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import org.exodusstudio.stellaris.common.utils.IdentifierUtils;

public class BiomeRegistry {
    public static final ResourceKey<Biome> RED_DESERT = register("red_desert");
    public static final ResourceKey<Biome> ICE_CAVES = register("ice_caves");
    public static final ResourceKey<Biome> ICE_SPIKES = register("ice_spikes");

    public static ResourceKey<Biome> register(String path, String modid) {
        return register(Identifier.fromNamespaceAndPath(modid, path));
    }

    public static ResourceKey<Biome> register(String path) {
        return register(IdentifierUtils.id(path));
    }

    public static ResourceKey<Biome> register(Identifier id) {
        return ResourceKey.create(Registries.BIOME, id);
    }

    public static void init() {}
}
