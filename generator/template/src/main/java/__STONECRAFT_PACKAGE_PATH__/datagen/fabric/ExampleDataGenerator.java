__STONECRAFT_#DATAGEN__
/*? if fabric {*/
package __STONECRAFT_BASE_PACKAGE__.datagen.fabric;

import __STONECRAFT_BASE_PACKAGE__.datagen.ExampleAdvancements;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricAdvancementProvider;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.HolderLookup;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public final class ExampleDataGenerator implements DataGeneratorEntrypoint {
    @Override
    public void onInitializeDataGenerator(FabricDataGenerator generator) {
        generator.createPack().addProvider(Advancements::new);
    }

    private static final class Advancements extends FabricAdvancementProvider {
        private Advancements(
            FabricPackOutput output,
            CompletableFuture<HolderLookup.Provider> registryLookup
        ) {
            super(output, registryLookup);
        }

        @Override
        public void generateAdvancement(
            HolderLookup.Provider registryLookup,
            Consumer<AdvancementHolder> exporter
        ) {
            ExampleAdvancements.generate(exporter);
        }
    }
}
/*?}*/
__STONECRAFT_/DATAGEN__
