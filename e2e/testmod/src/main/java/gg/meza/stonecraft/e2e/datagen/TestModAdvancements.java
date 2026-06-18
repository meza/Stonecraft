package gg.meza.stonecraft.e2e.datagen;

import java.util.function.Consumer;

import gg.meza.stonecraft.e2e.TestMod;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
//? if >= 1.21.5 {
import net.minecraft.advancements.criterion.InventoryChangeTrigger;
//?}
//? if < 1.21.5 {
/*import net.minecraft.advancements.critereon.InventoryChangeTrigger;
*///?}
import net.minecraft.world.item.Items;

public final class TestModAdvancements {
    public static final String STONE_ADVANCEMENT_ID = TestMod.MOD_ID + ":datagen/stone";

    private TestModAdvancements() {
    }

    public static void generate(Consumer<AdvancementHolder> exporter) {
        Advancement.Builder.advancement()
            .addCriterion("has_stone", InventoryChangeTrigger.TriggerInstance.hasItems(Items.STONE))
            .save(exporter, STONE_ADVANCEMENT_ID);
    }
}
