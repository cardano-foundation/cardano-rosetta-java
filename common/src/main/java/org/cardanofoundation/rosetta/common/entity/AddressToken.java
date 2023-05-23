package org.cardanofoundation.rosetta.common.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Digits;
import org.cardanofoundation.rosetta.common.validation.Word128Type;
import org.hibernate.Hibernate;

import java.math.BigInteger;
import java.util.Objects;

@Entity
@Table(
    name = "address_token"
)
public class AddressToken extends BaseEntity {
    @ManyToOne(
        fetch = FetchType.LAZY,
        optional = false
    )
    @JoinColumn(
        name = "address_id",
        nullable = false,
        foreignKey = @ForeignKey(
    name = "none",
    value = ConstraintMode.NO_CONSTRAINT
)
    )
    private Address address;
    @ManyToOne(
        fetch = FetchType.LAZY,
        optional = false
    )
    @JoinColumn(
        name = "tx_id",
        nullable = false,
        foreignKey = @ForeignKey(
    name = "none",
    value = ConstraintMode.NO_CONSTRAINT
)
    )
    private Tx tx;
    @ManyToOne(
        fetch = FetchType.LAZY,
        optional = false
    )
    @JoinColumn(
        name = "ident",
        nullable = false,
        foreignKey = @ForeignKey(
    name = "none",
    value = ConstraintMode.NO_CONSTRAINT
)
    )
    private MultiAsset multiAsset;
    @Column(
        name = "ident",
        updatable = false,
        insertable = false
    )
    private Long multiAssetId;
    @Column(
        name = "tx_id",
        updatable = false,
        insertable = false
    )
    private Long txId;
    @Column(
        name = "address_id",
        updatable = false,
        insertable = false
    )
    private Long addressId;
    @Column(
        name = "balance",
        nullable = false,
        precision = 39
    )
    @Word128Type
    private @Digits(
    integer = 39,
    fraction = 0
) BigInteger balance;

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o != null && Hibernate.getClass(this) == Hibernate.getClass(o)) {
            AddressToken that = (AddressToken)o;
            return this.id != null && Objects.equals(this.id, that.id);
        } else {
            return false;
        }
    }

    public int hashCode() {
        return this.getClass().hashCode();
    }

    protected AddressToken(AddressTokenBuilder<?, ?> b) {
        super(b);
        this.address = b.address;
        this.tx = b.tx;
        this.multiAsset = b.multiAsset;
        this.multiAssetId = b.multiAssetId;
        this.txId = b.txId;
        this.addressId = b.addressId;
        this.balance = b.balance;
    }

    public static AddressTokenBuilder<?, ?> builder() {
        return new AddressTokenBuilderImpl();
    }

    public AddressTokenBuilder<?, ?> toBuilder() {
        return (new AddressTokenBuilderImpl()).$fillValuesFrom(this);
    }

    public Address getAddress() {
        return this.address;
    }

    public Tx getTx() {
        return this.tx;
    }

    public MultiAsset getMultiAsset() {
        return this.multiAsset;
    }

    public Long getMultiAssetId() {
        return this.multiAssetId;
    }

    public Long getTxId() {
        return this.txId;
    }

    public Long getAddressId() {
        return this.addressId;
    }

    public BigInteger getBalance() {
        return this.balance;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public void setTx(Tx tx) {
        this.tx = tx;
    }

    public void setMultiAsset(MultiAsset multiAsset) {
        this.multiAsset = multiAsset;
    }

    public void setMultiAssetId(Long multiAssetId) {
        this.multiAssetId = multiAssetId;
    }

    public void setTxId(Long txId) {
        this.txId = txId;
    }

    public void setAddressId(Long addressId) {
        this.addressId = addressId;
    }

    public void setBalance(BigInteger balance) {
        this.balance = balance;
    }

    public AddressToken() {
    }

    public AddressToken(Address address, Tx tx, MultiAsset multiAsset, Long multiAssetId, Long txId, Long addressId, BigInteger balance) {
        this.address = address;
        this.tx = tx;
        this.multiAsset = multiAsset;
        this.multiAssetId = multiAssetId;
        this.txId = txId;
        this.addressId = addressId;
        this.balance = balance;
    }

    public abstract static class AddressTokenBuilder<C extends AddressToken, B extends AddressTokenBuilder<C, B>> extends BaseEntity.BaseEntityBuilder<C, B> {
        private Address address;
        private Tx tx;
        private MultiAsset multiAsset;
        private Long multiAssetId;
        private Long txId;
        private Long addressId;
        private BigInteger balance;

        public AddressTokenBuilder() {
        }

        protected B $fillValuesFrom(C instance) {
            super.$fillValuesFrom(instance);
            $fillValuesFromInstanceIntoBuilder(instance, this);
            return this.self();
        }

        private static void $fillValuesFromInstanceIntoBuilder(AddressToken instance, AddressTokenBuilder<?, ?> b) {
            b.address(instance.address);
            b.tx(instance.tx);
            b.multiAsset(instance.multiAsset);
            b.multiAssetId(instance.multiAssetId);
            b.txId(instance.txId);
            b.addressId(instance.addressId);
            b.balance(instance.balance);
        }

        public B address(Address address) {
            this.address = address;
            return this.self();
        }

        public B tx(Tx tx) {
            this.tx = tx;
            return this.self();
        }

        public B multiAsset(MultiAsset multiAsset) {
            this.multiAsset = multiAsset;
            return this.self();
        }

        public B multiAssetId(Long multiAssetId) {
            this.multiAssetId = multiAssetId;
            return this.self();
        }

        public B txId(Long txId) {
            this.txId = txId;
            return this.self();
        }

        public B addressId(Long addressId) {
            this.addressId = addressId;
            return this.self();
        }

        public B balance(BigInteger balance) {
            this.balance = balance;
            return this.self();
        }

        protected abstract B self();

        public abstract C build();

        public String toString() {
            String var10000 = super.toString();
            return "AddressToken.AddressTokenBuilder(super=" + var10000 + ", address=" + this.address + ", tx=" + this.tx + ", multiAsset=" + this.multiAsset + ", multiAssetId=" + this.multiAssetId + ", txId=" + this.txId + ", addressId=" + this.addressId + ", balance=" + this.balance + ")";
        }
    }

    private static final class AddressTokenBuilderImpl extends AddressTokenBuilder<AddressToken, AddressTokenBuilderImpl> {
        private AddressTokenBuilderImpl() {
        }

        protected AddressTokenBuilderImpl self() {
            return this;
        }

        public AddressToken build() {
            return new AddressToken(this);
        }
    }
}