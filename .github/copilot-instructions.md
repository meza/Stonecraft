# GitHub Copilot Instructions

## Project Overview

Find a comprehensive documentation of the project in the docs/docs folder.

## Core Development Principles

### Conventional Commit Messages (REQUIRED)

**ALL commits MUST use [Conventional Commits](https://www.conventionalcommits.org/en/v1.0.0/) specification.**

- Format: `<type>[optional scope]: <description>`
- Types: `feat`, `fix`, `docs`, `style`, `refactor`, `test`, `chore`
- Examples:
  - `feat: add new mod scanning functionality`
  - `fix: resolve version check error handling`
  - `test: add comprehensive tests for config validation`
  - `docs: update README with new installation steps`

**Commits not following this specification will be rejected.**

Ensure that commit types are chosen carefully, as they directly impact the software 
version; only use `fix` or `feat` for changes that affect user-facing behavior, 
and all commits must strictly follow the conventional commit format.

**Review-feedback fixes**

When a change only corrects mistakes introduced by the **same logical unit of work**, **amend** the original commit and **force-push** instead of adding a new commit.  
If rewriting history is impossible, and you must create a follow-up commit, use **`chore`** or **`refactor`**.  
Never use **`feat`** or **`fix`** for these clean-up commits as they are user-facing and appear in the changelog.

### Test Coverage Requirements (STRICT)

**100% test coverage is mandatory - this is the bare minimum.**

- Write tests for ALL new functionality
- Modify existing tests when changing behavior
- Use meaningful test descriptions and assertions
- Follow existing test patterns (gradletest)
- **NEVER remove, skip, or disable tests without explicit clarification from the team**

If you think a test needs to be removed or disabled, stop and ask for guidance first.

### Code Quality and Architecture

#### Software Hygiene
- **Boy Scout Rule**: Leave code cleaner than you found it
- Clear separation of concerns
- Meaningful variable and function names
- Proper error handling
- No magic numbers or hardcoded values
- Follow existing patterns and conventions

### Documentation

- Document all new features and changes by modifying the `docs/docs` folder's relevant files
- Update README.md when adding new functionality
- Maintain consistent language and style

## When in Doubt

**DO NOT make assumptions or guess.** Instead:

1. Research the existing codebase for similar patterns
2. Check the ADR documentation in `docs/docs/`
3. Review the README.md and CONTRIBUTING.md
4. Ask for clarification from the team

**Never make things up or implement solutions without understanding the requirements.**

## Testing Guidelines

## Development Workflow

1. **Write tests first**: Follow TDD principles where possible
2. **Implement changes**: Make minimal, focused changes
3. **Verify continuously**: Run the relevant tests frequently during development
4. **Commit with conventional messages**: Follow the commit format strictly
5. **Final verification**: Ensure the gradle `build` passes before submitting

## Quality Gates

Before any pull request:
- [ ] Ensure build (`./gradlew build`)
- [ ] Conventional commit format used
- [ ] Documentation updated if needed

**Remember: These are not suggestions - they are requirements. Adherence to these standards is mandatory for all contributions.**
