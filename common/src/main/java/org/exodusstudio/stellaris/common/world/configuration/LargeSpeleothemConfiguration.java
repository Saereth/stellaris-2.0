package org.exodusstudio.stellaris.common.world.configuration;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.valueproviders.FloatProvider;
import net.minecraft.util.valueproviders.FloatProviders;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.IntProviders;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.LargeDripstoneConfiguration;
import net.minecraft.world.level.block.Block;


public class LargeSpeleothemConfiguration extends LargeDripstoneConfiguration {
    public final BlockState baseBlock;
    public final HolderSet<Block> replaceableBlocks;

    public LargeSpeleothemConfiguration(BlockState baseBlock, HolderSet<Block> replaceableBlocks, int floorToCeilingSearchRange, IntProvider columnRadius, FloatProvider heightScale, float maxColumnRadiusToCaveHeightRatio, FloatProvider stalactiteBluntness, FloatProvider stalagmiteBluntness, FloatProvider windSpeed, int minRadiusForWind, float minBluntnessForWind) {
        super(floorToCeilingSearchRange, columnRadius, heightScale, maxColumnRadiusToCaveHeightRatio, stalactiteBluntness, stalagmiteBluntness, windSpeed, minRadiusForWind, minBluntnessForWind);
        this.baseBlock = baseBlock;
        this.replaceableBlocks = replaceableBlocks;
    }

    public static final Codec<LargeSpeleothemConfiguration> CODEC = RecordCodecBuilder.create((i) -> i.group(
            BlockState.CODEC.fieldOf("base_block").forGetter(c -> c.baseBlock),
            RegistryCodecs.homogeneousList(Registries.BLOCK).fieldOf("replaceable_blocks").forGetter(c -> c.replaceableBlocks),
            Codec.intRange(1, 512).fieldOf("floor_to_ceiling_search_range").orElse(30).forGetter((c) -> c.floorToCeilingSearchRange),
            IntProviders.codec(1, 60).fieldOf("column_radius").forGetter((c) -> c.columnRadius),
            FloatProviders.codec(0.0F, 20.0F).fieldOf("height_scale").forGetter((c) -> c.heightScale),
            Codec.floatRange(0.1F, 1.0F).fieldOf("max_column_radius_to_cave_height_ratio").forGetter((c) -> c.maxColumnRadiusToCaveHeightRatio),
            FloatProviders.codec(0.1F, 10.0F).fieldOf("stalactite_bluntness").forGetter((c) -> c.stalactiteBluntness),
            FloatProviders.codec(0.1F, 10.0F).fieldOf("stalagmite_bluntness").forGetter((c) -> c.stalagmiteBluntness),
            FloatProviders.codec(0.0F, 2.0F).fieldOf("wind_speed").forGetter((c) -> c.windSpeed), Codec.intRange(0, 100).fieldOf("min_radius_for_wind").forGetter((c) -> c.minRadiusForWind),
            Codec.floatRange(0.0F, 5.0F).fieldOf("min_bluntness_for_wind").forGetter((c) -> c.minBluntnessForWind)).apply(i, LargeSpeleothemConfiguration::new));


}
