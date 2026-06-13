/*? if fabric {*/
package gg.meza.stonecraft.e2e.datagen.fabric;

//? if >= 1.21.5 {
import java.util.concurrent.CompletableFuture;
//?}
import java.util.function.Consumer;

import gg.meza.stonecraft.e2e.datagen.TestModAdvancements;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
//? if >= 1.21.5 {
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
//?} else {
/*import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
*///?}
import net.fabricmc.fabric.api.datagen.v1.provider.FabricAdvancementProvider;
import net.minecraft.advancements.AdvancementHolder;
//? if >= 1.21.5 {
import net.minecraft.core.HolderLookup;
//?}

public class TestModDataGenerator implements DataGeneratorEntrypoint {
    @Override
    public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
        FabricDataGenerator.Pack pack = fabricDataGenerator.createPack();

        pack.addProvider(Advancements::new);
    }

    private static class Advancements extends FabricAdvancementProvider {
        //? if >= 1.21.5 {
        protected Advancements(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registryLookup) {
            super(output, registryLookup);
        }
        //?} else {
        /*protected Advancements(FabricDataOutput output) {
            super(output);
        }
        *///?}

        @Override
        //? if >= 1.21.5 {
        public void generateAdvancement(HolderLookup.Provider registryLookup, Consumer<AdvancementHolder> exporter) {
            TestModAdvancements.generate(exporter);
        }
        //?} else {
        /*public void generateAdvancement(Consumer<AdvancementHolder> exporter) {
            TestModAdvancements.generate(exporter);
        }
        *///?}
    }
}
/*?}*/
