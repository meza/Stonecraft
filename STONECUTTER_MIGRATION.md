# Stonecutter Migration Plan

## Current Divergences

- The plugin still depends on Stonecutter 0.6.2 (`gradle/libs.versions.toml:7-20`) even though the 0.7+ changelog removes chiseled tasks and reorganizes the configuration DSL.
- End-user docs in `README.md:31-88` tell people to apply `dev.kikugie.stonecutter` `0.6.+` and to rely on controller-based chiseled tasks, which conflicts with the 0.7 "Updating to 0.7" blog post where chiseled tasks no longer exist.
- `ModPlugin` branches on `StonecutterController` to register `configureChiseledTasks(...)` (`src/main/kotlin/gg/meza/stonecraft/ModPlugin.kt:25-30`), and `configureChiseledTasks` itself is entirely controller-based (`src/main/kotlin/gg/meza/stonecraft/configurations/ChiseledTasks.kt:3-35`). Both APIs vanish in 0.7.
- `configureStonecutterConstants` uses the legacy `stonecutter.consts(...)` and `stonecutter.allowExtensions(...)` helpers (`src/main/kotlin/gg/meza/stonecraft/configurations/Stonecutter.kt:10-15`). The 0.7 changelog replaces these with the map/filters container DSL, so the old helpers will stop compiling.
- Several places assume the active version is always present (`stonecutter.current.version` in `ModPlugin`, `Tasks`, `Loom`, `Quirks`), even though the 0.7 changelog explicitly mentions nullable active versions. We need guard rails.
- Fixtures and docs under `docs/` and `src/test/resources/fixtures` still pin `0.5`/`0.6` Stonecutter setups, so integration tests don’t exercise the up-to-date workflow (`src/test/resources/fixtures/settings.gradle.kts:16-33`).

---

## Phase 1 – Adopt Stonecutter 0.7 (stable)

### Iteration 1 – Align dependencies and documentation

1. Bump the catalog entry in `gradle/libs.versions.toml:7-20` to `0.7.11` (latest stable per `/blog/changes/0.7`) and refresh the Gradle lockfiles if any (`./gradlew dependencies --write-verification-metadata` if used).
2. Update every Stonecutter reference in docs and fixtures:
   - `README.md:31-88` (settings + `stonecutter.gradle[.kts]` snippets) and generated docs under `docs/` to mention `0.7.+`.
   - `src/test/resources/fixtures/settings.gradle.kts:16-33` to the 0.7 DSL (see iteration 3) so TestKit runs exercise the right behavior.
3. Capture these changes in release notes and the template repository instructions to keep end users on the same path.

### Iteration 2 – Reproduce chiseled task UX without the controller

Per `/blog/wall/0.7-update` and `/blog/changes/0.7`, chiseled tasks are gone; instead, Stonecutter exposes `stonecutter.tasks` (task container) and `stonecutter.tasks.named(...)` / `order(...)` accessors.

1. Remove the `StonecutterController` branch (`src/main/kotlin/gg/meza/stonecraft/ModPlugin.kt:25-30`) and delete the direct dependency on `dev.kikugie.stonecutter.controller`.
2. Re-implement `configureChiseledTasks` as wrapper tasks that keep the old task names (`chiseledBuild`, `chiseledTest`, etc.) but depend on `stonecutter.tasks.named("build")` and friends (`src/main/kotlin/gg/meza/stonecraft/configurations/ChiseledTasks.kt`). Use the new container:

   ```kotlin
   stonecutter.tasks {
       val builds = named("build")
       tasks.register("chiseledBuild") {
           group = "modsAll"
           dependsOn(builds)
       }
       order(builds) // optional ordering hook
   }
   ```

3. Wire this helper from `ModPlugin` after we obtain the `StonecutterBuild` extension so the wrappers exist both in the controller project (`stonecutter.gradle.kts`) and the actual subprojects.
4. For loaders that require serialized publishing (per `/blog/wall/0.7-update`), tap into `stonecutter.tasks.order(...)` to maintain deterministic execution matching the original `StonecutterController` sequencing.
5. Update tests to assert that registering our plugin still produces the `chiseled*` tasks when `stonecutter.tasks` exposes a matching task name.

### Iteration 3 – Adopt the 0.7 parameter/filter DSL

The changelog shows `stonecutter.consts` and `allowExtensions` were replaced by scoped containers.

1. Replace `stonecutter.consts(...)` and `stonecutter.allowExtensions("txt", "md")` in `configureStonecutterConstants` (`src/main/kotlin/gg/meza/stonecraft/configurations/Stonecutter.kt:10-15`) with:

   ```kotlin
   stonecutter.constants {
       this[project.mod.loader] = true
       match(project.mod.loader, "fabric", "forge", "neoforge", "quilt")
       // forgeLike/fabricLike go here too
   }
   stonecutter.filters {
       extensions.allow("txt", "md")
   }
   ```

2. Audit other uses of `stonecutter.const/consts/swap/dependency` helpers (none today, but keep an eye out after running `rg "stonecutter." src/main`).
3. Because file filters now operate from the registered handlers’ extensions, verify that we still want `.txt`/`.md`. If they’re plain comments, wiring `filters.extensions.allow` is enough; if not, instruct users to register custom handlers later (Phase 2).
4. Update examples in README/docs accordingly.

### Iteration 4 – Handle nullable active versions and tighten validations

The 0.7 changelog (“The active version is now allowed to be unset”) means our `stonecutter.current` calls need guard rails.

1. After grabbing the `StonecutterBuild` extension (`ModPlugin:46-67`), fail fast if `stonecutter.current.isActive` is false or `stonecutter.current` is null, and tell users to run `stonecutterSwitchTo` first. This prevents `NullPointerException` on `stonecutter.current.version`.
2. In `configureTasks` (`src/main/kotlin/gg/meza/stonecraft/configurations/Tasks.kt:23-50`), keep the dynamic tasks tied to the active project but ensure they skip when `stonecutter.current` is null.
3. Same audit for `Loom.kt`, `Quirks.kt`, and anywhere else referencing `stonecutter.current.version`. Consider caching `val targetVersion = stonecutter.current.version ?: return` and logging when the active version is missing.
4. Extend TestKit tests to cover both “active version set” and “unset” cases to verify the guard rails.

### Iteration 5 – Regression and release

1. Run `./gradlew build` for a multi-version sample (fixtures) to verify that plain `build` drives all generated sources as described in `/blog/changes/0.7`.
2. Document migration notes (why chiseled tasks disappeared / wrappers added) in CHANGELOG and docs (`README.md` and docs site).
3. Cut a Stonecraft release targeting Stonecutter 0.7 with the updated integration.

---

## Phase 2 – Adopt Stonecutter 0.8 (currently `0.8-alpha.x`)

0.8 introduces the ANTLR-based parser, file handler API, replacement rewrites, and new syntax utilities per `/blog/changes/0.8` and `/blog/story/dev-update-3`.

### Iteration 6 – Prepare code for 0.8 assumptions

1. Ensure we already rely on the 0.7 DSL (Phase 1) so the 0.8 upgrade is mostly a drop-in.
2. Add configuration switches for experimental features introduced in 0.8 (comment splitting, line scopes, etc.) if Stonecraft wants to expose them; at minimum, make sure we can access `stonecutter.flags[...]` to toggle `StonecutterFlag.GENERATE_SOURCES_ON_SYNC` and friends.
3. Abstract version comparisons (`stonecutter.eval(...)`) behind helper functions so we can swap to the new `current.parsed` API cleanly in the next iteration.

### Iteration 7 – Integrate the 0.8 APIs

1. Bump Stonecutter to the desired `0.8-alpha.x` once it’s the target (`gradle/libs.versions.toml` again) and re-import.
2. File handler API: decide how `.txt`/`.md` files should be processed.
   - If we only rely on them for documentation, register a trivial handler that treats them as plain text (per `/blog/changes/0.8-alpha.3`).
   - Otherwise, restrict Stonecutter processing to code files and drop the extension override to avoid parsing failures.
3. Adopt the new replacement DSL if we generate replacements/swap definitions inside Stonecraft (not currently). The plan should include scaffolding for `stonecutter.replacements { ... }` so users can opt in without editing our source.
4. Move version logic to `stonecutter.current.parsed`/`matches`/`eq` for clarity once we depend on alpha. This also buys us faster comparisons and better error messages (per `/blog/changes/0.8-alpha.7` and `/blog/story/dev-update-3`).
5. Run the fixtures with files that include nested comments or yaml/access-widener syntax to ensure the new parser handles them (especially because we allow `.txt`/`.md`). If we hit unsupported formats, set up a handler instead of `allowExtensions`.

### Iteration 8 – Documentation, QA, and release

1. Update `README.md` and docs with explicit mention that Stonecraft targets Stonecutter 0.8 (and still exposes the same `chiseled*` wrappers for backwards compatibility).
2. Add migration notes describing the parser/file handler changes and any new flags Stonecraft toggles on behalf of users.
3. Expand TestKit coverage to include at least one YAML or access widener file so the new handler path is exercised.
4. Only cut the release once 0.8 is no longer alpha or when we’re comfortable depending on the alpha; call that out clearly in release notes so users know the stability expectations.
