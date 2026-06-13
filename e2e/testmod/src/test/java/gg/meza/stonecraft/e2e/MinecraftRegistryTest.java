package gg.meza.stonecraft.e2e;

//? if fabric {
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
//? }
import net.minecraft.core.registries.BuiltInRegistries;

import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MinecraftRegistryTest {
    @BeforeAll
    static void bootstrapMinecraftRegistries() {
        //? if fabric {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        //? }
    }

    //? if fabric {
    @Test
    void itemStackCanUseBootstrappedMinecraftRegistries() {
        assertEquals(Items.STONE, BuiltInRegistries.ITEM.getValue(Identifier.withDefaultNamespace("stone")));
    }
    //? }

    //? if neoforge {
    /*@Test
    void itemRegistryCanResolveVanillaItems() {
        Identifier stoneId = Identifier.withDefaultNamespace("stone");

        assertEquals(stoneId, BuiltInRegistries.ITEM.getKey(BuiltInRegistries.ITEM.getValue(stoneId)));
    }
    *///?}
}
