# Contributing to Stonecraft

## Core Development Principles

Stonecraft is a complex project due to the wild nature of Minecraft modding and the need to support multiple versions and loaders. To maintain a high standard of quality and ensure the project remains maintainable, we have established the following core development principles that all contributors must adhere to:

### Test Coverage Requirements (STRICT)

**100% test coverage is mandatory - this is the bare minimum.**

- Write tests for ALL new functionality
- Modify existing tests when changing behavior
- Use meaningful test descriptions and assertions
- Follow existing test patterns (gradletest)
- **NEVER remove, skip, or disable tests without explicit clarification from the team**

If you think a test needs to be removed or disabled, stop and ask for guidance first.

### Test Types

Most code of Stonecraft can be tested via unit tests and gradletests. However, due to the nature of Minecraft modding, some features require more complex testing.

This is why we have the `e2e/testmod` project, which is a real mod that is built and run with Stonecraft. This allows us to test that Stonecraft projects can be built, tested and run without issues, and to be a place to test new features in Stonecraft itself.

When working with the `e2e/testmod` project, please refer to the 
[e2e/testmod/README.md](./e2e/testmod/README.md) and the [e2e/testmod/CONTRIBUTING.md](./e2e/testmod/CONTRIBUTING.md) files for specific guidelines on how to work with that project.

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

## Quality Gates

Before any pull request:
- [ ] Ensure build (`./gradlew build`)
- [ ] Documentation updated if needed
