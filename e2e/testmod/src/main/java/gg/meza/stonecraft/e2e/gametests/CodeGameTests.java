//? if >= 1.21.5 {
package gg.meza.stonecraft.e2e.gametests;

import gg.meza.stonecraft.e2e.TestMod;
import net.minecraft.gametest.framework.GameTestHelper;

/*? if fabric {*/
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
/*?}*/

/*? if neoforge {*/
/*import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.RegisterEvent;
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

    /*? if neoforge {*/
    /*@SubscribeEvent
    public static void registerTestFunctions(RegisterEvent event) {
        Identifier noopFunctionId = Identifier.fromNamespaceAndPath(TestMod.MOD_ID, NOOP_TEST_FUNCTION);

        event.register(Registries.TEST_FUNCTION, noopFunctionId, () -> CodeGameTests::noop);
    }
    *//*?}*/

    public static void noop(GameTestHelper ctx) {
        ctx.succeed();
    }
}
//? }
