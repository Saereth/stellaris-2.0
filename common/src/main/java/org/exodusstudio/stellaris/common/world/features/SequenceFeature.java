package org.exodusstudio.stellaris.common.world.features;

import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeatureCountTracker;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import org.exodusstudio.stellaris.common.world.configuration.SequenceConfiguration;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class SequenceFeature extends Feature<SequenceConfiguration> {
    private final List<BlockPos> positions = new ArrayList<>();
    private final IntList modifierIndices = new IntArrayList();
    private final List<BlockPos> modifiedPositions = new ArrayList<>();

    public SequenceFeature(Codec<SequenceConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<SequenceConfiguration> context) {
        SequenceConfiguration config = context.config();
        HolderSet<PlacedFeature> features = config.features();
        for (Holder<PlacedFeature> feature: features) {
            if (!place(feature.value(), context.random(), context.origin(), false, context)) {
                return false;
            }
        }

        return true;
    }


    private boolean place(final PlacedFeature placedFeature, final RandomSource random, final BlockPos origin, final boolean biomeCheck, FeaturePlaceContext<SequenceConfiguration> placementContext) {
        Optional<PlacedFeature> topFeature = biomeCheck ? Optional.of(placedFeature) : Optional.empty();
        PlacementContext context = new PlacementContext(placementContext.level(), placementContext.chunkGenerator(), topFeature);
        ConfiguredFeature<?, ? extends Feature<?>> feature = placedFeature.feature().value();
        List<PlacementModifier> placement = placedFeature.placement();
        if (placement.isEmpty()) {
            if (SharedConstants.DEBUG_FEATURE_COUNT) {
                FeatureCountTracker.featurePlaced(placementContext.level().getLevel(), feature, topFeature);
            }

            return feature.place(context.getLevel(), context.generator(), random, origin);
        }
        else {
            boolean placedAny = false;
            this.positions.add(origin);
            this.modifierIndices.add(0);

            for (; !this.positions.isEmpty(); this.modifiedPositions.clear()) {
                BlockPos pos = this.positions.removeLast();
                int modifierIndex = this.modifierIndices.removeInt(this.modifierIndices.size() - 1);
                PlacementModifier modifier = placement.get(modifierIndex);
                Stream<BlockPos> positions =  modifier.getPositions(context, random, pos);
                this.modifiedPositions.addAll(positions.toList());
                int nextModifierIndex = modifierIndex + 1;
                if (nextModifierIndex < placement.size()) {
                    for (int i = this.modifiedPositions.size() - 1; i >= 0; i--) {
                        this.positions.add(this.modifiedPositions.get(i));
                        this.modifierIndices.add(nextModifierIndex);
                    }
                } else {
                    for (BlockPos nextPos : this.modifiedPositions) {
                        placedAny |= feature.place(context.getLevel(), context.generator(), random, nextPos);
                        if (SharedConstants.DEBUG_FEATURE_COUNT) {
                            FeatureCountTracker.featurePlaced(context.getLevel().getLevel(), feature, topFeature);
                        }
                    }
                }
            }

            return placedAny;
        }
    }
}
