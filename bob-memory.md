# 2025-02-14
- Reviewed docs/blog/2025-11-25-post-obfuscation-stonecraft.md to understand promised Mojmap support toggle between Yarn and Mojang mappings while keeping legacy behavior unchanged.
- Inspected key plugin entry points (`ModPlugin`, `configurations/Dependencies.kt`, `Loom.kt`) to see where mapping selection currently hardcodes Yarn.
- Re-confirmed requirement from AGENTS/Copilot instructions: work must follow strict TDD—write failing tests before production code; no implementation without corresponding test expectation.
- Next when implementation starts: introduce config flag for Mojmap/Yarn selection, thread through dependency + Loom config, update docs/templates, ensure tests cover both paths.

# 2025-11-28
- Reviewed Stonecutter 0.7 blog guidance and audited repo: dependency already at 0.7.11 but code still references StonecutterController/chiseled tasks and old constants/filters DSL, and docs instruct users to apply 0.6.+.
- Next: execute STONECUTTER_MIGRATION Iterations 2-4 (stonecutter.tasks wrappers, new DSL, nullable current guards) plus refresh README/docs before attempting any 0.8 work.
