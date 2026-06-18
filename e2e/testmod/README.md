# Stonecraft Test Project

The project itself is part of Stonecraft. This project is a testbed for Stonecraft.

Stonecaft influences how mods are made, built and run. We can do a lot with unit tests in Stonecraft, but the only way to truly test that the mod works with all the loaders and Minecraft versions is to have a real mod that is built and run with Stonecraft. This project is that mod. It's focus is to ensure that Stonecraft projects can be built, tested and run without issues, and to be a place to test new features in Stonecraft itself.

Tests here are expensive because they involve building and running the mod, which will also start actual Minecraft servers and clients. So we use this as final e2e testing and to test that Stonecraft configured the modloaders correctly.

For more information on how to contribute, see the [CONTRIBUTING.md](./CONTRIBUTING.md) file.
