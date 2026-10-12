package org.exodusstudio.stellaris.neoforge.datagen.tags;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.BiomeTagsProvider;
import net.minecraft.tags.BiomeTags;
import org.exodusstudio.stellaris.Stellaris;
import org.exodusstudio.stellaris.common.registries.BiomeRegistry;
import org.exodusstudio.stellaris.common.registries.TagsRegistry;

import java.util.concurrent.CompletableFuture;

public class StellarisBiomeTagsProvider extends BiomeTagsProvider {

    public StellarisBiomeTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider, Stellaris.MOD_ID);
    }

    @Override
    protected void addTags(HolderLookup.Provider registries) {
        tag(TagsRegistry.BiomeTags.IS_MARS)
                .addOptional(BiomeRegistry.RED_DESERT)
                .addOptional(BiomeRegistry.ICE_SPIKES);
        tag(BiomeTags.SPAWNS_COLD_VARIANT_FROGS)
                .addOptional(BiomeRegistry.ICE_CAVES)
                .addOptional(BiomeRegistry.ICE_SPIKES);
        tag(BiomeTags.SPAWNS_WARM_VARIANT_FROGS)
                .addOptional(BiomeRegistry.RED_DESERT);

    }



}
