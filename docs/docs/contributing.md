---
sidebar_position: 8
title: Contributing
description: How to contribute to the Cardano Rosetta Java project
---

# Contributing to this project

Thanks for considering contributing and help us on building this project!

The best way to contribute right now is to try things out and provide feedback, but we also accept contributions to the documentation and obviously to the code itself.

This document contains guidelines to help you get started and how to make sure your contribution gets accepted, making you our newest contributor!

## Communication channels

Should you have any questions or need some help in getting set up, you can use these communication channels to reach the team and get answers in a way where others can benefit from it as well:

- Github [Discussions](https://github.com/cardano-foundation/cardano-rosetta-java/discussions)
- Cardano [StackExchange](https://cardano.stackexchange.com/) using the `rosetta` tag

## Your first contribution

Contributing to the documentation, its translation, reporting bugs or proposing features are awesome ways to get started.

Also, take a look at the tests. Making sure we have the best high quality test suite is vital for this project.

### Documentation + translations

We host our documentation / user manual in the [Wiki](https://github.com/cardano-foundation/cardano-rosetta-java/wiki) and [README](https://github.com/cardano-foundation/cardano-rosetta-java/blob/main/README.md).

### Bug reports

[Submit an issue](https://github.com/cardano-foundation/cardano-rosetta-java/issues/new).

For bug reports, it's very important to explain

- what version you used,
- steps to reproduce (or steps you took),
- what behavior you saw (ideally supported by logs), and
- what behavior you expected.

### Feature ideas

Feature ideas are precursors to high-level features items, which will be discussed and fleshed out to ideally become items on our feature roadmap.

We use the [Ideas discussions category](https://github.com/cardano-foundation/cardano-rosetta-java/discussions/categories/ideas) to discuss and vote on feature ideas, but you can also [submit an issue](https://github.com/cardano-foundation/cardano-rosetta-java/issues/new) using the "Feature idea :thought_balloon:" template and we convert that to a discussion.

We expect a description of

- why you (or the user) need/want something (e.g. problem, challenge, pain, benefit), and
- what this is roughly about (e.g. description of a new API endpoint or message format).

Note that we do NOT require a detailed technical description, but are much more interested in _why_ a feature is needed. This also helps in understanding the relevance and ultimately the priority of such an item.

## Making changes

When contributing code, it helps to have discussed the rationale and (ideally) how something is implemented in a feature idea or bug ticket beforehand.

### Building & Testing

- Build with _Maven_
- Make sure **all** unit tests are successful
- Make sure the [Postman collection](https://github.com/cardano-foundation/cardano-rosetta-java/blob/main/postmanTests/rosetta-java.postman_collection.json) and defined tests are successful
- Check the output of [PMD](https://pmd.github.io/) and [CPD](https://pmd.github.io/latest/pmd_userdocs_cpd.html) for any code quality issues with a priority higher than **4**

### Coding standards

Make sure to follow the [Google style guide for Java](https://google.github.io/styleguide/javaguide.html) but more important check if the coding style you find is consistent and report or fix any inconsistencies by filing an issue or a pull request. Make sure to file a separate pull request.

In general regarding code style, just take a look at the existing sources and make your code look like them.

### Creating a pull request

Thank you for contributing your changes by opening a pull requests! To get something merged we usually require:

- Make sure you follow [conventional commits](https://www.conventionalcommits.org/en/v1.0.0/) for commit messages
- Description of the changes - if your commit messages are great, this is less important
- Quality of changes is ensured - through new or updated automated tests
- Change is related to an issue, feature (idea) or bug report - ideally discussed beforehand
- Well-scoped - we prefer multiple PRs, rather than a big one

### Versioning & Changelog

To be defined but mostly based on conventional commits

### Releasing

To be defined but mostly based on conventional commits
