package org.exodusstudio.stellaris.common.world;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.feature.Feature;
import org.exodusstudio.stellaris.Stellaris;
import org.exodusstudio.stellaris.common.world.configuration.LargeSpeleothemConfiguration;
import org.exodusstudio.stellaris.common.world.configuration.SequenceConfiguration;
import org.exodusstudio.stellaris.common.world.features.LargeSpeleothemFeature;
import org.exodusstudio.stellaris.common.world.features.SequenceFeature;

public class ModFeatures {
    public static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(Stellaris.MOD_ID, Registries.FEATURE);

    public static final RegistrySupplier<Feature<LargeSpeleothemConfiguration>> LARGE_ICICLE = FEATURES.register("large_icicle", () -> new LargeSpeleothemFeature(LargeSpeleothemConfiguration.CODEC));
    public static final RegistrySupplier<Feature<SequenceConfiguration>> SEQUENCE = FEATURES.register(
            "sequence", () -> new SequenceFeature(SequenceConfiguration.CODEC));

}
