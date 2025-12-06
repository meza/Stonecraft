---
title: Charting the Stonecutter Upgrade
description: We’re mapping how Stonecraft will adopt Stonecutter 0.7 and 0.8 without disrupting existing projects.
slug: stonecutter-upgrade
authors: meza
---

Stonecutter has been evolving quickly, and the jump from 0.6.x to 0.7 and 0.8 comes with sweeping internal changes. Task orchestration, the configuration DSL, version semantics, and even the file parser are all being reworked. That is exciting, but it also means we have to be deliberate about how Stonecraft, which wraps Stonecutter for multi-loader, multi-version builds, migrates forward.

<!-- truncate -->

## What This Means for You

Our goal is simple: **Stonecraft consumers should not need to change how they use Stonecraft**. The plugin will keep presenting the same entry points, tasks, and extension knobs you use today. All of the disruption should happen inside Stonecraft’s integration layer so your existing projects build the same way before and after the upgrade.

## Why It’s Challenging

Stonecutter 0.7 removes chiseled tasks, replaces the constants/filters API, and allows nullable active versions. Version 0.8 swaps in a brand-new ANTLR parser with custom handler support, upgrades replacements, and adds features like parametrized swaps and the `sc` shorthand. We need to adapt each of those layers carefully, test across multi-loader matrices, and keep Architectury and Loom integrations happy.

## Our Plan

We are taking this one iteration at a time. The work is tracked in our [Stonecutter Migration Plan](https://github.com/meza/Stonecraft/blob/main/docs/reference/stonecutter-migration-plan.md), which breaks the effort down into small, reviewable steps. We’ll move through that checklist in order. First landing 0.7 compatibility, then experimenting with the 0.8 alpha stream once the foundation is stable.

## Release Guarantees

We can’t promise a timeline; the scope touches every part of the plugin and we’d rather be right than fast. What we *can* promise is clear SemVer. If we do need to expose a breaking change, it will come with a major version bump and explicit release notes. Minor and patch releases will continue to behave the way they do today, just with better Stonecutter support under the hood.

Stay tuned to the blog and the migration plan for progress updates, and thanks for trusting Stonecraft with your multi-loader builds.

## Edits (2025-11-29)

Phase 1 of the migration checklist wrapped up on November 29, 2025. Stonecraft 1.8.x now ships Stonecutter 0.7.11, wires the `stonecutter.tasks` container so the familiar `chiseled*` wrappers keep working, migrates to the 0.7 constants/filters DSL, and guards places where `stonecutter.current` may be unset. If you are wondering what this means for your CI entry points, read the follow-up post [Stonecutter 0.7 and the Future of chiseled* Tasks](/blog/native-tasks-deprecation) for the rationale.
