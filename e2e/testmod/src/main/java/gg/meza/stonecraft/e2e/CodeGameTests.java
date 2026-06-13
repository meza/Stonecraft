package gg.meza.stonecraft.e2e;

import net.minecraft.gametest.framework.GameTestHelper;

/*? if fabric {*/
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
/*?}*/

/*? if neoforge {*/
/*import java.util.function.Consumer;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.FunctionGameTestInstance;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.RegisterEvent;
*//*?}*/

public class CodeGameTests {
    private static final String NOOP_TEST_FUNCTION = "noop";
    private static final String CODE_REGISTERED_NOOP_TEST = "code_registered_noop";

    /*? if fabric {*/

    public static void register() {
        Registry.register(
            BuiltInRegistries.TEST_FUNCTION,
            Identifier.fromNamespaceAndPath(TestMod.MOD_ID, NOOP_TEST_FUNCTION),
            CodeGameTests::noop
        );
    }

    /*?}*/

    /*? if neoforge {*/
    /*private static final Holder<TestEnvironmentDefinition<?>> DEFAULT_ENVIRONMENT =
        Holder.direct(new TestEnvironmentDefinition.AllOf());

    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(CodeGameTests::registerGameTests);
    }

    private static void registerGameTests(RegisterEvent event) {
        Identifier noopFunctionId = Identifier.fromNamespaceAndPath(TestMod.MOD_ID, NOOP_TEST_FUNCTION);
        Identifier codeRegisteredTestId = Identifier.fromNamespaceAndPath(TestMod.MOD_ID, CODE_REGISTERED_NOOP_TEST);
        ResourceKey<Consumer<GameTestHelper>> codeRegisteredFunctionKey =
            ResourceKey.create(Registries.TEST_FUNCTION, codeRegisteredTestId);

        event.register(Registries.TEST_FUNCTION, noopFunctionId, () -> CodeGameTests::noop);
        event.register(Registries.TEST_FUNCTION, codeRegisteredTestId, () -> CodeGameTests::noop);
        event.register(Registries.TEST_INSTANCE, codeRegisteredTestId, () -> new FunctionGameTestInstance(
            codeRegisteredFunctionKey,
            new TestData<>(
                DEFAULT_ENVIRONMENT,
                Identifier.withDefaultNamespace("empty"),
                1,
                1,
                true
            )
        ));
    }
    *//*?}*/

    public static void noop(GameTestHelper ctx) {
        ctx.succeed();
    }
}
