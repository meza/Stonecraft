/*? if forge {*/
/*package gg.meza.stonecraft.e2e.datagen.forge;

import java.util.List;

import gg.meza.stonecraft.e2e.datagen.TestModAdvancements;
import net.minecraft.data.DataProvider;
import net.minecraft.data.advancements.AdvancementProvider;
import net.minecraft.data.advancements.AdvancementSubProvider;
//? if >= 1.21.5
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
//? if < 1.21.5
//import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "stonecraft_testmod", bus = Mod.EventBusSubscriber.Bus.MOD)
public final class TestModDataGenerator {
    private TestModDataGenerator() {
    }

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        event.getGenerator().addProvider(
            event.includeServer(),
            (DataProvider.Factory<AdvancementProvider>) output -> new AdvancementProvider(output, event.getLookupProvider(), List.of((AdvancementSubProvider) (registries, exporter) ->
                TestModAdvancements.generate(exporter)
            ))
        );
    }
}
*//*?}*/
