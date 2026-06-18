# Stonecraft

Read the [README.md](./README.md) and [CONTRIBUTING.md](./CONTRIBUTING.md) files.

## Testing the clients

While developing the mod, you will need to test that the clients are working correctly. This is not something you can do yet without the user.

You must tell the user that you're about to start all the clients, and ask the user to do the manual checks that you can't do automatically.

You can and will see the logs of the clients, so you can check for errors and crashes, but you won't be able to see the actual game windows, so you need the user to confirm that they opened correctly and that the mod is working as expected.

If you need to verify a specific version-loader combination, you can switch to the given version with stonecutter and then run `./gradlew runActive`.

If you need to run ALL the clients, then run `./scripts/run-all-clients.ps1` which will sequence them properly.
