# Stonecraft → Stonecutter Migration Plan

Stonecutter 0.7 and 0.8 introduce sweeping changes that Stonecraft needs to account for:

- **Task model overhaul (0.7)**: Chiseled tasks are gone; Stonecutter now auto-generates versioned sources and exposes a `stonecutter.tasks` container plus ordering helpers.
- **Configuration DSL refresh (0.7)**: Constants, swaps, dependencies, filters, and replacements now live inside scoped containers (`constants {}`, `filters {}`); Groovy and Kotlin receive parity, and Stonecutter flags moved into `StonecutterFlag`.
- **Version semantics updates (0.7)**: Version parsing/evaluation is unified, `stonecutter.current` can be null, and a new semver engine adds operators like `!=`.
- **File processing groundwork (0.7)**: Pattern-based file filtering replaces `allowExtensions`/`excludeFiles`, simplifying multi-branch setups and improving config-cache behavior.
- **Parser & file handler rewrite (0.8)**: ANTLR powers the new parser with richer diagnostics; Stonecutter supports custom file handlers, line/word scope improvements, parametrized swaps, inline comment notes, and better replacement performance/error handling.
- **Developer ergonomics (0.8)**: `sc` shorthand, `current.parsed` comparisons, comment-splitting scopes, stricter identifier grammar, and optional comment suppression highlight the alpha features.

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

## Phase 2 – Prepare for Stonecutter 0.8 (currently alpha)

### Iteration 6 – Ready the codebase
1. Ensure Phase 1 steps are complete (0.7 DSL everywhere) and abstract version comparisons through helper functions so switching to `current.parsed` is trivial.
2. Decide which experimental flags/features (comment splitting, handler API) Stonecraft wants to surface; expose toggles if needed.

### Iteration 7 – Integrate 0.8 APIs
1. Bump the dependency to the selected `0.8-alpha.x` when ready.
2. Register file handlers (per `/blog/changes/0.8-alpha.3`) if Stonecraft still wants to process `.txt`/`.md`; otherwise remove the extension overrides.
3. Adopt `stonecutter.current.parsed` (`>=`, `matches`, `eq`) in place of raw `eval` where possible to align with the new API surface.
4. If Stonecraft exposes replacements/swaps, migrate them to the updated DSL (`stonecutter.replacements { string { ... } }`) for compatibility with the new parser.
5. Expand fixtures/tests to include files with nested comments, YAML/access wideners, etc., validating the ANTLR parser/handlers.

### Iteration 8 – Docs/testing/release
1. Refresh README/docs to state compatibility with Stonecutter 0.8 and call out any new configuration knobs.
2. Document migration notes (e.g., handler registration, new version comparison helpers).
3. Add TestKit coverage for formats touched by file handlers.
4. Release once satisfied with alpha stability (or wait for 0.8 stable); flag the dependency’s prerelease status in release notes if applicable.

## Next Steps
1. Execute Phase 1 Iteration 1 (dependency/doc updates) on a feature branch.
2. Prototype the `stonecutter.tasks`-based wrappers locally to confirm parity with the old chiseled tasks before merging.
3. After shipping the 0.7-compatible Stonecraft version, use a long-lived branch for the 0.8 experiments to isolate alpha risk.
