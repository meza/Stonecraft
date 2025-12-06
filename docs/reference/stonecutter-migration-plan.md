# Stonecraft → Stonecutter Migration Plan

Stonecutter 0.7 and 0.8 introduce sweeping changes that Stonecraft needs to account for:

- **Task model overhaul (0.7)**: Chiseled tasks are gone; Stonecutter now auto-generates versioned sources and exposes a `stonecutter.tasks` container plus ordering helpers.
- **Configuration DSL refresh (0.7)**: Constants, swaps, dependencies, filters, and replacements now live inside scoped containers (`constants {}`, `filters {}`); Groovy and Kotlin receive parity, and Stonecutter flags moved into `StonecutterFlag`.
- **Version semantics updates (0.7)**: Version parsing/evaluation is unified, `stonecutter.current` can be null, and a new semver engine adds operators like `!=`.
- **File processing groundwork (0.7)**: Pattern-based file filtering replaces `allowExtensions`/`excludeFiles`, simplifying multi-branch setups and improving config-cache behavior.
- **Parser & file handler rewrite (0.8)**: ANTLR powers the new parser with richer diagnostics; Stonecutter supports custom file handlers, line/word scope improvements, parametrized swaps, inline comment notes, and better replacement performance/error handling.
- **Developer ergonomics (0.8)**: `stonecutter.current.parsed` comparisons, comment-splitting scopes, stricter identifier grammar, and optional comment suppression highlight the alpha features.

The following steps outline how Stonecraft should adopt these releases without breaking its interface.

## Phase 1 – Upgrade to Stonecutter 0.7.x (stable)

### Iteration 1 – Bump dependencies and docs
1. Set `stonecutter = "0.7.11"` in `gradle/libs.versions.toml` and regenerate dependency locks if used.
2. Update README, docs, and template snippets (`README.md:31-88`, `docs/`, `src/test/resources/fixtures/settings.gradle.kts`) to reference `0.7.+`, reflecting the new workflow.
3. Note the upgrade plan in CHANGELOG/releases so downstream users know Stonecraft now expects 0.7+.

### Iteration 2 – Replace StonecutterController/chiseled tasks
1. Remove the `StonecutterController` branch in `ModPlugin` (`src/main/kotlin/gg/meza/stonecraft/ModPlugin.kt:25-30`).
2. Re-write `configureChiseledTasks` to register wrapper tasks (same names as today) that depend on `stonecutter.tasks.named(...)` via the new 0.7 task container (`src/main/kotlin/gg/meza/stonecraft/configurations/ChiseledTasks.kt`). Use `stonecutter.tasks.order(...)` to preserve execution order when needed.
3. Call the new helper after obtaining `StonecutterBuild` so wrappers exist both in `stonecutter.gradle[.kts]` and subprojects.
4. Adjust/extend tests to ensure the wrapper tasks appear when Stonecutter exposes the underlying tasks.

### Iteration 3 – Adopt the 0.7 parameter/filter DSL
1. Migration of `stonecutter.consts(...)`/`allowExtensions(...)` in `configureStonecutterConstants` (`src/main/kotlin/gg/meza/stonecraft/configurations/Stonecutter.kt`). Use the new container syntax:
   ```kotlin
   stonecutter.constants {
       match(project.mod.loader, "fabric", "forge", "neoforge", "quilt")
       this["forgeLike"] = project.mod.isForgeLike
       this["fabricLike"] = project.mod.isFabricLike
   }

   stonecutter.filters {
       extensions.allow("txt", "md")
   }
   ```
2. Inspect other Stonecutter DSL usages (constants, swaps, dependencies, filters) and port them to the 0.7 idioms.
3. Update README/examples to mirror the new syntax.

### Iteration 4 – Handle nullable active versions
1. Guard `stonecutter.current` usage in `ModPlugin`, `Tasks`, `Loom`, `Quirks`, etc. (`stonecutter.current` may now be null/unset). Fail fast with a helpful message in `ModPlugin` when no active version is set.
2. Ensure helper tasks (`configureTasks`) and Loom configuration skip registering run/configure tasks when no active version exists.
3. Add tests covering “active version set/unset” behaviors to avoid regressions.

### Iteration 5 – Regression testing and release
1. Run `./gradlew build` across fixtures to confirm plain `build` handles all versions (per 0.7 behavior).
2. Update documentation/CHANGELOG summarizing the 0.7 integration (removal of chiseled tasks, new DSL, nullable active versions).
3. Publish a Stonecraft release targeting Stonecutter 0.7.x.

## Phase 2 – Adopt Stonecutter 0.8-alpha.14

Stonecutter 0.8 makes two concrete changes that matter to Stonecraft:
1. `StonecutterControllerExtension.tasks` now exposes typed accessors (see the [0.8 changelog, alpha.14](https://stonecutter.kikugie.dev/blog/changes/0.8#08-alpha14)), so we can no longer pass raw strings when wiring `chiseled*` wrappers.
2. File processing is limited to extensions with registered handlers (see [0.8 changelog, alpha.3](https://stonecutter.kikugie.dev/blog/changes/0.8#08-alpha3) and the [handler wiki](https://stonecutter.kikugie.dev/wiki/config/handlers)), so our existing `stonecutter.filters.include("**/*.md", "**/*.txt")` call only works if we register matching handlers.

### Iteration 6 – Update `chiseled*` wrappers for typed task access
1. `src/main/kotlin/gg/meza/stonecraft/configurations/ChiseledTasks.kt:10-36`  
   - Replace the `Wrapper(name, target)` helper with the typed API exposed by `StonecutterControllerExtension.tasks`.  
   - For each wrapper (`chiseledBuild`, `chiseledClean`, …) depend on the typed property instead of calling `controller.tasks.named(targetTask)` with a `String`.  
   - Likewise, switch the `controller.tasks { order("buildAndCollect") }` calls to the typed constants.  
   - Source: [0.8 changelog, alpha.14](https://stonecutter.kikugie.dev/blog/changes/0.8#08-alpha14).
2. `src/test/kotlin/gg/meza/stonecraft/configurations/ChiseledTasksConfigurationTest.kt`  
   - Adjust expectations if the task names exposed by the typed API changed.  
   - Keep asserting that every legacy `chiseled*` wrapper shows up in `./gradlew tasks` so future upstream changes don’t silently break us.

### Iteration 7 – Decide how `.md` / `.txt` files should be handled
1. `src/main/kotlin/gg/meza/stonecraft/configurations/Stonecutter.kt:9-15`  
   - Decide whether Stonecraft should continue preprocessing Markdown/text files.  
   - If yes, register handlers using the DSL in the [handler wiki](https://stonecutter.kikugie.dev/wiki/config/handlers) (e.g., reuse the hash-style preset) and keep the filter entry.  
   - If no, remove `stonecutter.filters.include("**/*.md", "**/*.txt")` so we align with the default handler list (`java`, `kt`, `kts`, `yaml`, `aw`, etc.) called out in the changelog and wiki.  
   - Source references: [0.8 changelog, alpha.3](https://stonecutter.kikugie.dev/blog/changes/0.8#08-alpha3) and [handler wiki](https://stonecutter.kikugie.dev/wiki/config/handlers).
2. Tests/docs to update after the decision:  
   - Add or adjust a fixture (e.g., in `StonecutterTest` or a new TestKit case) to prove the intended behavior for `.md`/`.txt`.  
   - Update `README.md`, `docs/docs/02-Quickstart.mdx`, and any tutorials that mention `.md`/`.txt` preprocessing so users know which extensions are supported and how to add more via handlers.
3. Re-run the integration project (`src/test/resources/fixtures`) to ensure `stonecutterGenerate` → `processResources` wiring still behaves (no skipped resources, no cache regressions).

### Iteration 8 – Documentation and release packaging
1. Update every public reference to the Stonecutter version (`README.md`, `docs/docs/02-Quickstart.mdx`, blog posts) so they state “0.8-alpha.14” and summarize the two functional changes above. Cite the relevant upstream docs where useful.
2. Extend CI/TestKit coverage to fail fast if either breaking change regresses:
   - Keep `ChiseledTasksConfigurationTest` asserting the wrapper tasks.  
   - Add/extend a fixture that covers the chosen `.md`/`.txt` policy (processed vs. ignored).
3. Prepare release notes describing the dependency bump, the handler decision, and how to roll back to 0.7.x if the alpha build causes issues.

## Next Steps
1. Update `ChiseledTasks.kt` and its tests to use the typed `controller.tasks` API from [0.8-alpha.14](https://stonecutter.kikugie.dev/blog/changes/0.8#08-alpha14); run `./gradlew test` afterward.
2. Decide on the `.md`/`.txt` policy, implement it in `Stonecutter.kt`, and add the corresponding fixture assertions; sources: [handler wiki](https://stonecutter.kikugie.dev/wiki/config/handlers) and [0.8-alpha.3 changelog](https://stonecutter.kikugie.dev/blog/changes/0.8#08-alpha3).
3. Refresh the README/docs to reference Stonecutter 0.8-alpha.14, document the handler policy with links to the upstream docs, and draft release notes for downstream users.
