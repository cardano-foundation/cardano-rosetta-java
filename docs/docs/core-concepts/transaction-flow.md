---
title: Mesh API Transaction Flow
sidebar_label: Transaction Flow
sidebar_position: 3
---

## The Transaction Lifecycle

Creating and submitting a transaction on Cardano using the Mesh API involves several steps and coordinating different components. A key part of this process is the **signing service**, which the client integrates to handle the secure signing of transaction data.

The diagram below walks through the typical sequence of interactions:

```mermaid
sequenceDiagram
    participant Client
    participant "Signing Service"
    participant "Rosetta API"
    participant "Construction Service"
    participant "Cardano Node"

    Client->>+"Rosetta API": /construction/preprocess
    "Rosetta API"->>+"Construction Service": Calculate transaction parameters
    "Construction Service"-->>-"Rosetta API": Processing information
    "Rosetta API"-->>-Client: Options

    Client->>+"Rosetta API": /construction/metadata
    "Rosetta API"->>+"Construction Service": Retrieve network parameters & fees
    "Construction Service"-->>-"Rosetta API": Network data
    "Rosetta API"-->>-Client: Metadata

    Client->>+"Rosetta API": /construction/payloads
    "Rosetta API"->>+"Construction Service": Prepare transaction elements
    "Construction Service"-->>-"Rosetta API": Transaction data for signing
    "Rosetta API"-->>-Client: Unsigned transaction & payloads

    Client->>+"Signing Service": Process payloads
    "Signing Service"-->>-Client: Signed payloads

    Client->>+"Rosetta API": /construction/combine
    "Rosetta API"->>+"Construction Service": Assemble signed transaction
    "Construction Service"-->>-"Rosetta API": Complete transaction
    "Rosetta API"-->>-Client: Signed transaction

    Client->>+"Rosetta API": /construction/submit
    "Rosetta API"->>+"Construction Service": Submit to blockchain
    "Construction Service"->>+"Cardano Node": Broadcast to network
    "Cardano Node"-->>-"Construction Service": Transaction hash
    "Construction Service"-->>-"Rosetta API": Confirmation hash
    "Rosetta API"-->>-Client: Transaction hash
```

As you can see in the diagram, the flow covers everything from preparing the transaction details to the final submission. It also highlights where your custom signing service fits in, taking the unsigned payloads from the API, signing them securely, and returning them to be combined and submitted to the network.
