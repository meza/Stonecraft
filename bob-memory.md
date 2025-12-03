# 2025-12-03
- Confirmed requirement to support concurrent maintenance branches (e.g., `1.7.x`, `1.8.x`) with semantic-release so Renovate + update-pack-versions workflows can publish security patches per branch.
- Need to document process for configuring semantic-release multi-branch strategy, ensuring branch-specific release rules and workflow triggers remain isolated from `main`.
- Follow up: design/update release config + docs so that each maintenance branch gets proper release channel without manual intervention.

# 2025-02-14
- Reviewed docs/blog/2025-11-25-post-obfuscation-stonecraft.md to understand promised Mojmap support toggle between Yarn and Mojang mappings while keeping legacy behavior unchanged.
- Inspected key plugin entry points (`ModPlugin`, `configurations/Dependencies.kt`, `Loom.kt`) to see where mapping selection currently hardcodes Yarn.
- Re-confirmed requirement from AGENTS/Copilot instructions: work must follow strict TDD—write failing tests before production code; no implementation without corresponding test expectation.
- Next when implementation starts: introduce config flag for Mojmap/Yarn selection, thread through dependency + Loom config, update docs/templates, ensure tests cover both paths.
