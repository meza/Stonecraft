__STONECRAFT_#DATAGEN__
/*? if neoforge {*/
/*package __STONECRAFT_BASE_PACKAGE__.datagen.neoforge;

import __STONECRAFT_BASE_PACKAGE__.__STONECRAFT_ENTRYPOINT_CLASS__;
import __STONECRAFT_BASE_PACKAGE__.datagen.ExampleAdvancements;
//? if >= 26.3 {
import net.minecraft.advancements.Advancement;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
//?}
import net.minecraft.data.advancements.AdvancementProvider;
import net.minecraft.data.advancements.AdvancementSubProvider;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.List;

@EventBusSubscriber(modid = __STONECRAFT_ENTRYPOINT_CLASS__.MOD_ID)
public final class ExampleDataGenerator {
    private ExampleDataGenerator() {
    }

    //? if < 26.3 {
    @SubscribeEvent
    public static void gatherServerData(GatherDataEvent.Server event) {
        event.createProvider((output, lookupProvider) ->
            new AdvancementProvider(
                output,
                lookupProvider,
                List.of((AdvancementSubProvider) (registries, exporter) ->
                    ExampleAdvancements.generate(exporter)
                )
            )
        );
    }
    //?}

    //? if >= 26.3 {
    @SubscribeEvent
    public static void gatherServerData(GatherDataEvent.Server event) {
        event.createReloadableRegistryObjects(
            new RegistrySetBuilder().add(
                Registries.ADVANCEMENT,
                new AdvancementProvider(List.of(Advancements::new))
            )
        );
    }

    private static final class Advancements extends AdvancementSubProvider {
        private Advancements(BootstrapContext<Advancement> output) {
            super(output);
        }

        @Override
        public void generate() {
            ExampleAdvancements.generate(advancement -> advancement.register(output));
        }
    }
    //?}
}
*//*?}*/
__STONECRAFT_/DATAGEN__
