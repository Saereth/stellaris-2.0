package org.exodusstudio.stellaris.common.world.configuration;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import org.jspecify.annotations.NonNull;

import java.util.stream.Stream;

public class SequenceConfiguration implements FeatureConfiguration {
    private final HolderSet<PlacedFeature> placedFeatures;

    public static final Codec<SequenceConfiguration> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(ExtraCodecs.nonEmptyHolderSet(PlacedFeature.LIST_CODEC).fieldOf("features").forGetter(SequenceConfiguration::features)).apply(instance, SequenceConfiguration::new));

    public SequenceConfiguration(HolderSet<PlacedFeature> placedFeatures) {
        this.placedFeatures = placedFeatures;
    }

    public HolderSet<PlacedFeature> features() {
        return placedFeatures;
    }

    @Override
    @NonNull
    public Stream<Holder<ConfiguredFeature<?, ?>>> getSubFeatures() {
        return this.features().stream().flatMap(f -> f.value().getFeatures());
    }


}
