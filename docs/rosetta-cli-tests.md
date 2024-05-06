# `rosetta-cli` checks

## `data`
The verification
``` console
./rosetta-cli check:data --configuration-file ./configuration/data/byron_sample.json
```
occurs at the stage of [integration tests](/.github/workflows/integration-test.yaml)  only against the first 200 blocks within the devkit.