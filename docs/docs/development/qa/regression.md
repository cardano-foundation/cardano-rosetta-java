---
sidebar_position: 1
title: Regression Testing
description: Regression testing methodologies and procedures
---

1. Check End 2 End tests testing the whole flow on pre-prod
1. Artillerly tests for /data/\* plus /construction/metadata /search as a rule of thumb and check if performance / scalability did not deteriorate (all online endpoints). ONLY MAINNET.
1. Check single docker image and docker compose on mainnet and preprod
