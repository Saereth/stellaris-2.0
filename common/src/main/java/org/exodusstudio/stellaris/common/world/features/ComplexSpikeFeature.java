package org.exodusstudio.stellaris.common.world.features;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.phys.Vec3;
import org.exodusstudio.stellaris.common.world.configuration.SpikeFeatureConfiguration;

public class ComplexSpikeFeature extends Feature<SpikeFeatureConfiguration> {

    public ComplexSpikeFeature(Codec<SpikeFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<SpikeFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        RandomSource random = context.random();

        // 1. Verify the placement context (e.g., must be placed in stone/cave walls)
        if (!level.getBlockState(origin.below()).isSolidRender()) {
            return false;
        }

        // Define our crystal variables
        BlockState baseBlock = context.config().blockState();
//        BlockState baseBlock = context.config().blockState();
//        int numberOfSpikes = 2;
//        int numberOfSpikes = context.config().branchCount().sample(random);


        for (Direction direction : Direction.Plane.HORIZONTAL) {
            int coreRadius = 3;
            int length = 12;
//            IntProvider angle = UniformInt.of(30,60);

//        int coreRadius = context.config().baseRadius().sample(random);
//        int length = context.config().baseLength().sample(random);
//        IntProvider angle = context.config().baseAngle();
            Vec3 originBottomCorner = new Vec3(origin.getX(), origin.getY(), origin.getZ());
//            int theta = angle.sample(random);
//            int phi = angle.sample(random);
            Vec3 tip = origin.relative(direction).getCenter().add(length, length, length);
            Vec3 ray = tip.subtract(originBottomCorner);
            int minX = Math.min(origin.getX() - coreRadius, (int) tip.x);
            int minY = Math.min(origin.getY() - coreRadius, (int) tip.y);
            int minZ = Math.min(origin.getZ() - coreRadius, (int) tip.z);
            int maxX = Math.max(origin.getX() + coreRadius, (int) tip.x);
            int maxY = Math.max(origin.getY() + coreRadius, (int) tip.y);
            int maxZ = Math.max(origin.getZ() + coreRadius, (int) tip.z);

            for (int x = minX; x <= maxX; x++) {
                for (int y = minY; y <= maxY; y++) {
                    for (int z = minZ; z <= maxZ; z++) {
                        Vec3 blockPos = new Vec3(x, y, z);
                        if(!level.getBlockState(convertVec3toBlockPos(blockPos)).isAir()) {
                            continue;
                        }
                        Vec3 relativePos = blockPos.subtract(originBottomCorner);
                        double fraction = Mth.clamp(relativePos.dot(ray) / ray.dot(ray), 0, 1);
                        Vec3 projectedPos = ray.scale(fraction);
                        double projectionDistance = relativePos.distanceTo(projectedPos);
                        double radius = (1 - fraction) * coreRadius;
                        if (projectionDistance < radius) {
                            setBlock(level, convertVec3toBlockPos(blockPos), baseBlock);
                        }
                    }
                }


            }

        }




        return true;
    }

    private BlockPos convertVec3toBlockPos(Vec3 blockPos) {
        return new BlockPos((int) blockPos.x, (int) blockPos.y, (int) blockPos.z);
    }
}