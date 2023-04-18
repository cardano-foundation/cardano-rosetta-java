package org.cardanofoundation.rosetta.api.addedFunctionalInterface;

@FunctionalInterface
public interface Functional<In1, Out>{ // (In1) -> Out
    public Out callback(In1 in1);
}


