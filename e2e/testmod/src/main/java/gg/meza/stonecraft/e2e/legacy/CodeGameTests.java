//? if < 1.21.5 {
/*package gg.meza.stonecraft.e2e.legacy;

import gg.meza.stonecraft.e2e.TestMod;

//? if fabric {
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
//?}

//? if neoforge {
/^import java.util.Collection;
import java.util.List;

import net.minecraft.gametest.framework.GameTestGenerator;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestFunction;
import net.minecraft.world.level.block.Rotation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
^///?}

public class CodeGameTests {
    private static final String CODE_REGISTERED_NOOP_TEST = "code_registered_noop";

    //? if fabric {
    public static void register() {
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    public void codeRegisteredNoop(GameTestHelper ctx) {
        ctx.succeed();
    }
    //?}

    //? if neoforge {
    /^public static void register(IEventBus modEventBus) {
        modEventBus.addListener(CodeGameTests::registerGameTests);
    }

    private static void registerGameTests(RegisterGameTestsEvent event) {
        event.register(CodeGameTests.class);
    }

    @GameTestGenerator
    public static Collection<TestFunction> codeRegisteredTests() {
        return List.of(new TestFunction(
            "defaultBatch",
            TestMod.MOD_ID + "." + CODE_REGISTERED_NOOP_TEST,
            "minecraft:empty",
            Rotation.NONE,
            100,
            0,
            true,
            CodeGameTests::noop
        ));
    }

    public static void noop(GameTestHelper ctx) {
        ctx.succeed();
    }
    ^///?}
}
*///?}
