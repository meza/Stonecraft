//? if >= 1.21.5 {
package gg.meza.stonecraft.e2e.gametests;

import gg.meza.stonecraft.e2e.TestMod;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;

/*? if fabric {*/
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
/*?}*/

/*? if forgeLike {*/
/*import net.minecraft.core.registries.Registries;
*//*?}*/

/*? if forge {*/
/*import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.RegisterEvent;
*//*?}*/

/*? if neoforge {*/
/*import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.RegisterEvent;
*//*?}*/

/*? if forge {*/
/*@Mod.EventBusSubscriber(modid = TestMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
*//*?}*/

/*? if neoforge {*/
/*@EventBusSubscriber(modid = TestMod.MOD_ID)
*//*?}*/
public class CodeGameTests {
    private static final String NOOP_TEST_FUNCTION = "noop";

    /*? if fabric {*/

    public CodeGameTests() {
        Registry.register(
                BuiltInRegistries.TEST_FUNCTION,
                Identifier.fromNamespaceAndPath(TestMod.MOD_ID, NOOP_TEST_FUNCTION),
                CodeGameTests::noop
        );
    }


    /*?}*/

    /*? if forgeLike {*/
    /*@SubscribeEvent
    public static void registerTestFunctions(RegisterEvent event) {
        Identifier noopFunctionId = Identifier.fromNamespaceAndPath(TestMod.MOD_ID, NOOP_TEST_FUNCTION);

        event.register(Registries.TEST_FUNCTION, noopFunctionId, () -> CodeGameTests::noop);
    }
    *//*?}*/

    public static void noop(GameTestHelper ctx) {
        if (CodeGameTests.class.getResource("/data/stonecraft_testmod/advancement/datagen/stone.json") == null) {
            throw new AssertionError("Generated advancement is missing from the GameTest runtime classpath");
        }
        ctx.succeed();
    }
}
//? }
