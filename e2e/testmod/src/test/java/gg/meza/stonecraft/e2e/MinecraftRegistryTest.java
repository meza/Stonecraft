package gg.meza.stonecraft.e2e;

import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.minecraft.core.registries.BuiltInRegistries;

import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MinecraftRegistryTest {
    @BeforeAll
    static void bootstrapMinecraftRegistries() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    //? if fabric {
    @Test
    void itemStackCanUseBootstrappedMinecraftRegistries() {
        assertEquals(Items.STONE, item(minecraft("stone")));
    }
    //? }

    //? if neoforge {
    /*@Test
    void itemRegistryCanResolveVanillaItems() {
        Identifier stoneId = minecraft("stone");

        assertEquals(stoneId, BuiltInRegistries.ITEM.getKey(item(stoneId)));
    }
    *///?}

    private static Item item(Identifier id) {
        //? if >= 1.21.5 {
        return BuiltInRegistries.ITEM.getValue(id);
        //?}
        //? if < 1.21.5 {
        /*return BuiltInRegistries.ITEM.get(id);
        *///?}
    }

    private static Identifier minecraft(String path) {
        //? if >= 1.21.5 {
        return Identifier.withDefaultNamespace(path);
        //?}
        //? if < 1.21.5 {
        /*return new Identifier(Identifier.DEFAULT_NAMESPACE, path);
        *///?}
    }
}
