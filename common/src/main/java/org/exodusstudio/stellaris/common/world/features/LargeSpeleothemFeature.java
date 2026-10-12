package org.exodusstudio.stellaris.common.world.features;

import com.mojang.serialization.Codec;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.FloatProvider;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Column;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;
import org.exodusstudio.stellaris.common.world.configuration.LargeSpeleothemConfiguration;
import org.exodusstudio.stellaris.common.world.features.utils.SpeleothemUtils;
import org.jetbrains.annotations.Nullable;


import java.util.Optional;

public class LargeSpeleothemFeature extends Feature<LargeSpeleothemConfiguration> {

    public LargeSpeleothemFeature(Codec<LargeSpeleothemConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<LargeSpeleothemConfiguration> context) {
        WorldGenLevel level = context.level();
        ChunkGenerator chunkGenerator = context.chunkGenerator();
        RandomSource random = context.random();
        BlockPos origin = context.origin();
        LargeSpeleothemConfiguration config = context.config();
        if (!SpeleothemUtils.isEmptyOrWater(level, origin)) {
            return false;
        }

        Optional<Column> column = Column.scan(
                level,
                origin,
                config.floorToCeilingSearchRange,
                SpeleothemUtils::isEmptyOrWater,
                state -> SpeleothemUtils.isBaseOrLava(state, config.baseBlock.getBlock(), config.replaceableBlocks)
        );
        if (!(!column.isEmpty() && column.get() instanceof Column.Range columnRange)) {
            return false;
        }

        if (columnRange.height() < 4) {
            return false;
        }

        int maxColumnRadiusBasedOnColumnHeight = (int)(columnRange.height() * config.maxColumnRadiusToCaveHeightRatio);
        int maxColumnRadius = Math.clamp(maxColumnRadiusBasedOnColumnHeight, config.columnRadius.minInclusive(), config.columnRadius.maxInclusive());
        int radius = Mth.randomBetweenInclusive(random, config.columnRadius.minInclusive(), maxColumnRadius);
        LargeSpeleothemFeature.LargeSpeleothem stalactite = makeSpeleothem(
                config.baseBlock, origin.atY(columnRange.ceiling() - 1), false, random, radius, config.stalactiteBluntness, config.heightScale
        );
        LargeSpeleothemFeature.LargeSpeleothem stalagmite = makeSpeleothem(
                config.baseBlock, origin.atY(columnRange.floor() + 1), true, random, radius, config.stalagmiteBluntness, config.heightScale
        );
        LargeSpeleothemFeature.WindOffsetter wind;
        if (stalactite.isSuitableForWind(config.minRadiusForWind, config.minBluntnessForWind)
                && stalagmite.isSuitableForWind(config.minRadiusForWind, config.minBluntnessForWind)) {
            wind = new LargeSpeleothemFeature.WindOffsetter(origin.getY(), random, config.windSpeed, 16 - radius);
        } else {
            wind = LargeSpeleothemFeature.WindOffsetter.noWind();
        }

        boolean stalactiteBaseEmbeddedInStone = stalactite.moveBackUntilBaseIsInsideStoneAndShrinkRadiusIfNecessary(level, wind);
        boolean stalagmiteBaseEmbeddedInStone = stalagmite.moveBackUntilBaseIsInsideStoneAndShrinkRadiusIfNecessary(level, wind);
        if (stalactiteBaseEmbeddedInStone) {
            stalactite.placeBlocks(level, random, wind);
        }

        if (stalagmiteBaseEmbeddedInStone) {
            stalagmite.placeBlocks(level, random, wind);
        }

        if (SharedConstants.DEBUG_LARGE_DRIPSTONE) {
            this.placeDebugMarkers(level, origin, columnRange, wind, config);
        }

        return true;
    }

    private static LargeSpeleothemFeature.LargeSpeleothem makeSpeleothem(
            final BlockState baseBlock,
            final BlockPos root,
            final boolean pointingUp,
            final RandomSource random,
            final int radius,
            final FloatProvider bluntness,
            final FloatProvider heightScale
    ) {
        return new LargeSpeleothemFeature.LargeSpeleothem(baseBlock, root, pointingUp, radius, bluntness.sample(random), heightScale.sample(random));
    }

    private void placeDebugMarkers(final WorldGenLevel level, final BlockPos origin, final Column.Range range, final LargeSpeleothemFeature.WindOffsetter wind, LargeSpeleothemConfiguration config) {
        level.setBlock(wind.offset(origin.atY(range.ceiling() - 1)), Blocks.DIAMOND_BLOCK.defaultBlockState(), 2);
        level.setBlock(wind.offset(origin.atY(range.floor() + 1)), Blocks.GOLD_BLOCK.defaultBlockState(), 2);

        for (BlockPos.MutableBlockPos pos = origin.atY(range.floor() + 2).mutable(); pos.getY() < range.ceiling() - 1; pos.move(Direction.UP)) {
            BlockPos windAdjustedPos = wind.offset(pos);
            if (SpeleothemUtils.isEmptyOrWater(level, windAdjustedPos) || level.getBlockState(windAdjustedPos).is(config.baseBlock.getBlock())) {
                level.setBlock(windAdjustedPos, Blocks.CREEPER_HEAD.defaultBlockState(), 2);
            }
        }
    }



    private static final class LargeSpeleothem {
        private final BlockState baseBlock;
        private BlockPos root;
        private final boolean pointingUp;
        private int radius;
        private final double bluntness;
        private final double scale;

        private LargeSpeleothem(
                final BlockState baseBlock, final BlockPos root, final boolean pointingUp, final int radius, final double bluntness, final double scale
        ) {
            this.baseBlock = baseBlock;
            this.root = root;
            this.pointingUp = pointingUp;
            this.radius = radius;
            this.bluntness = bluntness;
            this.scale = scale;
        }

        private int getHeight() {
            return this.getHeightAtRadius(0.0F);
        }

        private boolean moveBackUntilBaseIsInsideStoneAndShrinkRadiusIfNecessary(final WorldGenLevel level, final LargeSpeleothemFeature.WindOffsetter wind) {
            while (this.radius > 1) {
                BlockPos.MutableBlockPos newRoot = this.root.mutable();
                int maxTries = Math.min(10, this.getHeight());

                for (int i = 0; i < maxTries; i++) {
                    if (level.getBlockState(newRoot).is(Blocks.LAVA)) {
                        return false;
                    }

                    if (SpeleothemUtils.isCircleMostlyEmbeddedInStone(level, wind.offset(newRoot), this.radius)) {
                        this.root = newRoot;
                        return true;
                    }

                    newRoot.move(this.pointingUp ? Direction.DOWN : Direction.UP);
                }

                this.radius /= 2;
            }

            return false;
        }

        private int getHeightAtRadius(final float checkRadius) {
            return (int)SpeleothemUtils.getSpeleothemHeight(checkRadius, this.radius, this.scale, this.bluntness);
        }

        private void placeBlocks(final WorldGenLevel level, final RandomSource random, final LargeSpeleothemFeature.WindOffsetter wind) {
            for (int dx = -this.radius; dx <= this.radius; dx++) {
                for (int dz = -this.radius; dz <= this.radius; dz++) {
                    float currentRadius = Mth.sqrt(dx * dx + dz * dz);
                    if (!(currentRadius > this.radius)) {
                        int height = this.getHeightAtRadius(currentRadius);
                        if (height > 0) {
                            if (random.nextFloat() < 0.2) {
                                height = (int)(height * Mth.randomBetween(random, 0.8F, 1.0F));
                            }

                            BlockPos.MutableBlockPos pos = this.root.offset(dx, 0, dz).mutable();
                            boolean hasBeenOutOfStone = false;
                            int maxY = this.pointingUp ? level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, pos.getX(), pos.getZ()) : Integer.MAX_VALUE;

                            for (int i = 0; i < height && pos.getY() < maxY; i++) {
                                BlockPos windAdjustedPos = wind.offset(pos);
                                if (SpeleothemUtils.isEmptyOrWaterOrLava(level, windAdjustedPos)) {
                                    hasBeenOutOfStone = true;
                                    Block block = SharedConstants.DEBUG_LARGE_DRIPSTONE ? Blocks.GLASS : this.baseBlock.getBlock();
                                    level.setBlock(windAdjustedPos, block.defaultBlockState(), 2);
                                } else if (hasBeenOutOfStone && level.getBlockState(windAdjustedPos).is(BlockTags.BASE_STONE_OVERWORLD)) {
                                    break;
                                }

                                pos.move(this.pointingUp ? Direction.UP : Direction.DOWN);
                            }
                        }
                    }
                }
            }
        }

        private boolean isSuitableForWind(final int minRadiusForWind, final float minBluntnessForWind) {
            return this.radius >= minRadiusForWind && this.bluntness >= minBluntnessForWind;
        }


    }

    private static final class WindOffsetter {
        private final int originY;
        private final @Nullable Vec3 windSpeed;
        private final int maxOffset;

        private WindOffsetter(final int originY, final RandomSource random, final FloatProvider windSpeedRange, final int maxOffset) {
            this.originY = originY;
            this.maxOffset = maxOffset;
            float speed = windSpeedRange.sample(random);
            float direction = Mth.randomBetween(random, 0.0F, (float) Math.PI);
            this.windSpeed = new Vec3(Mth.cos(direction) * speed, 0.0, Mth.sin(direction) * speed);
        }

        private WindOffsetter() {
            this.originY = 0;
            this.windSpeed = null;
            this.maxOffset = 0;
        }

        private static LargeSpeleothemFeature.WindOffsetter noWind() {
            return new LargeSpeleothemFeature.WindOffsetter();
        }

        private BlockPos offset(final BlockPos pos) {
            if (this.windSpeed == null) {
                return pos;
            }

            int dy = this.originY - pos.getY();
            Vec3 totalWindAdjust = this.windSpeed.scale(dy);
            int dx = Math.clamp(Mth.floor(totalWindAdjust.x), -this.maxOffset, this.maxOffset);
            int dz = Math.clamp(Mth.floor(totalWindAdjust.z), -this.maxOffset, this.maxOffset);
            return pos.offset(dx, 0, dz);
        }
    }
}
