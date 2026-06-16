/*? if < 1.21.5 {*/
/*package gg.meza.stonecraft.e2e.legacy;

import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;

//? if neoforge {
/^import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
^///?}

public class CodeGameTests {
    //? if fabric {
    public static void register() {
    }
    //?}

    //? if neoforge {
    /^public static void register(IEventBus modEventBus) {
        modEventBus.addListener(CodeGameTests::registerGameTests);
    }

    private static void registerGameTests(RegisterGameTestsEvent event) {
        event.register(CodeGameTests.class);
    }
    ^///?}

    @GameTest(
        //? if fabric {
        template = "fabric-gametest-api-v1:empty",
        //?}
        //? if neoforge {
        /^templateNamespace = "minecraft",
        template = "empty",
        ^///?}
        setupTicks = 1,
        required = true
    )
    public void codeRegisteredNoop(GameTestHelper ctx) {
        ctx.succeed();
    }
}
*//*?}*/
