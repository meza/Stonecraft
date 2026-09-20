__STONECRAFT_#GAMETESTS__
package __STONECRAFT_BASE_PACKAGE__.gametest;

import __STONECRAFT_BASE_PACKAGE__.__STONECRAFT_ENTRYPOINT_CLASS__;
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
/*@EventBusSubscriber(modid = __STONECRAFT_ENTRYPOINT_CLASS__.MOD_ID)
*//*?}*/
public final class ExampleGameTests {
    private static final String NOOP_TEST_FUNCTION = "noop";

    /*? if fabric {*/
    public ExampleGameTests() {
        Registry.register(
            BuiltInRegistries.TEST_FUNCTION,
            Identifier.fromNamespaceAndPath(__STONECRAFT_ENTRYPOINT_CLASS__.MOD_ID, NOOP_TEST_FUNCTION),
            ExampleGameTests::noop
        );
    }
    /*?}*/

    /*? if neoforge {*/
    /*@SubscribeEvent
    public static void registerTestFunctions(RegisterEvent event) {
        Identifier noopFunctionId = Identifier.fromNamespaceAndPath(__STONECRAFT_ENTRYPOINT_CLASS__.MOD_ID, NOOP_TEST_FUNCTION);
        event.register(Registries.TEST_FUNCTION, noopFunctionId, () -> ExampleGameTests::noop);
    }
    *//*?}*/

    public static void noop(GameTestHelper context) {
        context.succeed();
    }
}
__STONECRAFT_/GAMETESTS__
