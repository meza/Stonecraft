package gg.meza.stonecraft.e2e;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*? if < 1.21.5 {*/
/*import gg.meza.stonecraft.e2e.legacy.CodeGameTests;
*//*?}*/

/*? if fabric {*/
import net.fabricmc.api.ModInitializer;
/*?}*/

/*? if forge {*/
/*import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
*/
/*?}*/

/*? if neoforge {*/
/*import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
//? if >= 1.21.5 {
import net.neoforged.fml.ModContainer;
//?}

import gg.meza.stonecraft.e2e.datagen.neoforge.TestModDataGenerator;
*//*?}*/


/*? if forgeLike {*/
/*@Mod("stonecraft_testmod")
public class TestMod {
*//*?}*/

/*? if fabric {*/
public class TestMod implements ModInitializer {
/*?}*/
    public static final String MOD_ID = "stonecraft_testmod";
    public static Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    /*? if forge {*/
    /*public TestMod(final FMLJavaModLoadingContext context) {
        LOGGER.info("Hello Forge world!");
    }*/
    /*?}*/

    /*? if neoforge {*/
    /*//? if >= 1.21.5 {
    public TestMod(IEventBus modEventBus, ModContainer modContainer) {
    //?}
    //? if < 1.21.5 {
    /^public TestMod(IEventBus modEventBus) {
    ^///?}
        CodeGameTests.register(modEventBus);
        TestModDataGenerator.register(modEventBus);
        LOGGER.info("Hello Neoforge world!");
    }
    *//*?}*/

    /*? if fabric {*/
    @Override
    public void onInitialize() {
        CodeGameTests.register();
        LOGGER.info("Hello Fabric world!");
    }
    /*?}*/
}
