/*? if neoforge {*/
/*package gg.meza.stonecraft.e2e.datagen.neoforge;

import java.util.List;

import gg.meza.stonecraft.e2e.datagen.TestModAdvancements;
import net.minecraft.data.DataProvider;
import net.minecraft.data.advancements.AdvancementProvider;
import net.minecraft.data.advancements.AdvancementSubProvider;
import net.neoforged.bus.api.SubscribeEvent;
//? if >= 1.21.5
import net.neoforged.fml.common.EventBusSubscriber;
//? if < 1.21.5
//import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.data.event.GatherDataEvent;

//? if >= 1.21.5
@EventBusSubscriber(modid = "stonecraft_testmod")
//? if < 1.21.5
//@Mod.EventBusSubscriber(modid = "stonecraft_testmod", bus = Mod.EventBusSubscriber.Bus.MOD)
public final class TestModDataGenerator {
    private TestModDataGenerator() {
    }

    //? if >= 1.21.5 {
    @SubscribeEvent
    public static void gatherServerData(GatherDataEvent.Server event) {
        event.createProvider((output, lookupProvider) ->
            new AdvancementProvider(output, lookupProvider, List.of((AdvancementSubProvider) (registries, exporter) ->
                TestModAdvancements.generate(exporter)
            ))
        );
    }
    //?}
    //? if < 1.21.5 {
    /^@SubscribeEvent
    public static void gatherServerData(GatherDataEvent event) {
        event.getGenerator().addProvider(
            event.includeServer(),
            (DataProvider.Factory<AdvancementProvider>) output -> new AdvancementProvider(output, event.getLookupProvider(), List.of((AdvancementSubProvider) (registries, exporter) ->
                TestModAdvancements.generate(exporter)
            ))
        );
    }
    ^///?}
}
*//*?}*/
