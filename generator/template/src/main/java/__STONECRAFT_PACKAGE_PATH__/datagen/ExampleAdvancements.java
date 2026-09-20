__STONECRAFT_#DATAGEN__
package __STONECRAFT_BASE_PACKAGE__.datagen;

import __STONECRAFT_BASE_PACKAGE__.__STONECRAFT_ENTRYPOINT_CLASS__;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.resources.Identifier;
/*? if < 26.2 {*/
/*import net.minecraft.advancements.criterion.InventoryChangeTrigger;
*//*?} else {*/
import net.minecraft.advancements.triggers.InventoryChangeTrigger;
/*?}*/
import net.minecraft.world.item.Items;

import java.util.function.Consumer;

public final class ExampleAdvancements {
    public static final String STONE_ADVANCEMENT_ID = __STONECRAFT_ENTRYPOINT_CLASS__.MOD_ID + ":datagen/stone";

    private ExampleAdvancements() {
    }

    public static void generate(Consumer<AdvancementHolder> exporter) {
        AdvancementHolder advancement = Advancement.Builder.advancement()
            .addCriterion("has_stone", InventoryChangeTrigger.TriggerInstance.hasItems(Items.STONE))
            .build(Identifier.parse(STONE_ADVANCEMENT_ID));
        exporter.accept(advancement);
    }
}
__STONECRAFT_/DATAGEN__
