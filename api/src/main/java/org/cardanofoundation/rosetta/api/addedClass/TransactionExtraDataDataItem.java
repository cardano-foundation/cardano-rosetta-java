package org.cardanofoundation.rosetta.api.addedClass;

import co.nstant.in.cbor.model.DataItem;
import co.nstant.in.cbor.model.MajorType;
import lombok.Getter;
import lombok.Setter;
import org.cardanofoundation.rosetta.api.model.TransactionExtraData;

@Getter
@Setter
public class TransactionExtraDataDataItem extends DataItem {

    private TransactionExtraData transactionExtraData;

    public TransactionExtraDataDataItem(MajorType majorType, TransactionExtraData transactionExtraData) {
        super(majorType);
        this.transactionExtraData=transactionExtraData;
    }
}
