---
description: AI Agent Skill.
---

# AI Agent Skill

Stonecraft embeds an agent skill for working with Stonecraft and Stonecutter.

## Install the skill for coding agents

```shell
./gradlew installStonecraftSkill
```

It writes the embedded skill to `.agents/skills/stonecraft/SKILL.md`, creating the parent directories when needed.
The repository-scoped location makes the skill discoverable to coding agents working in the repository.
If that file already exists, the task leaves it unchanged since the installer cannot safely merge project-specific edits. 
To discard an existing skill and replace it exactly with the bundled copy, use:

```shell
./gradlew installStonecraftSkill --force-overwrite
```

## Read or update the skill

```shell
./gradlew stonecraftGuidance
```

The task always prints the skill embedded in the current Stonecraft plugin JAR. 

## Suppressing Skill Reminders

Add this property to the consumer repository's root `gradle.properties` file:

```properties
stonecraft.showGuidanceReminder=false
```

For one invocation instead, pass `-Pstonecraft.showGuidanceReminder=false` on the Gradle command line. Only the case-insensitive value `false` disables the reminder. The version line remains visible. Run `./gradlew tasks` from the repository root to find the three skill tasks in the standard `Help tasks` section.
