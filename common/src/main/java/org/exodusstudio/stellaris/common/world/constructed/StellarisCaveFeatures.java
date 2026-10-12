package org.exodusstudio.stellaris.common.world.constructed;

import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.worldgen.features.FeatureUtils;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.util.valueproviders.ClampedInt;
import net.minecraft.util.valueproviders.UniformFloat;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.BiomeFilter;
import net.minecraft.world.level.levelgen.placement.CountPlacement;
import net.minecraft.world.level.levelgen.placement.InSquarePlacement;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import org.exodusstudio.stellaris.common.registries.TagsRegistry;
import org.exodusstudio.stellaris.common.world.FeaturesRecord;
import net.minecraft.world.level.block.Block;
import org.exodusstudio.stellaris.common.world.ModFeatures;
import org.exodusstudio.stellaris.common.world.configuration.LargeSpeleothemConfiguration;

public class StellarisCaveFeatures {
    public static final FeaturesRecord LARGE_ICICLE = FeaturesRecord.create("large_icicle");

    public static void bootstrapConfigured(BootstrapContext<ConfiguredFeature<?,?>> context) {
        HolderGetter<Block> blocks = context.lookup(Registries.BLOCK);
        FeatureUtils.register(
                context,
                LARGE_ICICLE.configuredFeature(),
                ModFeatures.LARGE_ICICLE.get(),
                new LargeSpeleothemConfiguration(
                        Blocks.PACKED_ICE.defaultBlockState(),
                        blocks.getOrThrow(TagsRegistry.BlockTags.LARGE_ICICLE_REPLACEABLE),
                        30,
                        ClampedInt.of(UniformInt.of(3, 19), 3, 16),
                        UniformFloat.of(0.4f, 2f),
                        0.33f,
                        UniformFloat.of(0.3F, 0.9F),
                        UniformFloat.of(0.4F, 1.0F),
                        UniformFloat.of(0.0F, 0.3F),
                        4,
                        0.6f
                )
        );
    }

    public static void bootstrapPlaced(BootstrapContext<PlacedFeature> context) {
        HolderGetter<ConfiguredFeature<?, ?>> configureFeatures = context.lookup(Registries.CONFIGURED_FEATURE);
        PlacementUtils.register(
                context,
                LARGE_ICICLE.placedFeature(),
                configureFeatures.getOrThrow(LARGE_ICICLE.configuredFeature()),
                CountPlacement.of(UniformInt.of(48,64)),
                InSquarePlacement.spread(),
                PlacementUtils.RANGE_BOTTOM_TO_MAX_TERRAIN_HEIGHT,
                BiomeFilter.biome()
        );
    }
}
