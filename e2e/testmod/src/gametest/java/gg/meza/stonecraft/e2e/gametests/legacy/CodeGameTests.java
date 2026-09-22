/*? if < 1.21.5 {*/
/*package gg.meza.stonecraft.e2e.gametests.legacy;

import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;

//? if forge {
/^import net.minecraftforge.gametest.GameTestHolder;
^///?}
//? if neoforge {
/^import net.neoforged.neoforge.gametest.GameTestHolder;
^///?}
//? if forgeLike
//@GameTestHolder("stonecraft_testmod_gametest")
public class CodeGameTests {
    @GameTest(
        //? if fabric {
        template = "fabric-gametest-api-v1:empty",
        //?}
        //? if forge {
        /^template = "stonecraft_testmod:codegametests.empty",
        ^///?}
        //? if neoforge {
        /^templateNamespace = "stonecraft_testmod",
        template = "empty",
        ^///?}
        setupTicks = 1,
        required = true
    )
    public void codeRegisteredNoop(GameTestHelper ctx) {
        ctx.setNight();
        ctx.succeed();
    }
}
*//*?}*/
