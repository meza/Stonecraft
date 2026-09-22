# Stonecraft

## Read before working

Before repository work, read:

- [README.md](./README.md) for the project, its current direction, and the workspace map.
- [CONTRIBUTING.md](./CONTRIBUTING.md) for the project specific development workflow.

Before changing or running code in an application, package, or module, read its nearest `README.md`.
Read the local guide for every affected workspace. A local guide governs its workspace when it is
more specific, unless it conflicts with a repository-wide requirement.

## Operator facing documentation

- [Full Documentation](./docs/docs/)

## Running the tests

Running the full test suite via the `build` or any `test` commands is extremely time consuming.
There is only one instance that you're expected to run them automatically and that's before committing to git.
Otherwise despite the rules of the project, you should always ask the user if they want the full run or not.

## Expanding the e2e test suite

The e2e test suite is a critical part of the Stonecraft project. It ensures that the project works as expected in a real-world scenario.
HOWEVER! They are extremely expensive to run in time.
Before adding an e2e scenario, please exhaust all other options with faster tests. E2e should only contain things that cannot be verified with faster methods
