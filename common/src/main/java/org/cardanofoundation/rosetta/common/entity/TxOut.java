package org.cardanofoundation.rosetta.common.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Digits;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.cardanofoundation.rosetta.common.enumeration.TokenType;
import org.cardanofoundation.rosetta.common.validation.Hash28Type;
import org.cardanofoundation.rosetta.common.validation.Hash32Type;
import org.cardanofoundation.rosetta.common.validation.Lovelace;
import org.cardanofoundation.rosetta.common.validation.TxIndex;
import org.hibernate.Hibernate;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Entity
@Table(
        name = "tx_out",
        uniqueConstraints = {@UniqueConstraint(
                name = "unique_txout",
                columnNames = {"tx_id", "index"}
        )}
)
public class TxOut extends BaseEntity {
  @ManyToOne(
          fetch = FetchType.LAZY,
          optional = false
  )
  @JoinColumn(
          name = "tx_id",
          nullable = false,
          foreignKey = @ForeignKey(
                  value = ConstraintMode.NO_CONSTRAINT,
                  name = "none"
          )
  )
  private Tx tx;
  @Column(
          name = "tx_id",
          updatable = false,
          insertable = false
  )
  private Long txId;
  @Column(
          name = "index",
          nullable = false
  )
  @TxIndex
  private Short index;
  @Column(
          name = "address",
          nullable = false,
          length = 65535
  )
  private String address;
  @Column(
          name = "address_raw",
          nullable = false
  )
  private byte[] addressRaw;
  @Column(
          name = "address_has_script",
          nullable = false
  )
  private Boolean addressHasScript;
  @Column(
          name = "payment_cred",
          length = 56
  )
  @Hash28Type
  private String paymentCred;
  @ManyToOne(
          fetch = FetchType.LAZY
  )
  @JoinColumn(
          name = "stake_address_id",
          foreignKey = @ForeignKey(
                  value = ConstraintMode.NO_CONSTRAINT,
                  name = "none"
          )
  )
  private StakeAddress stakeAddress;
  @Column(
          name = "value",
          nullable = false,
          precision = 20
  )
  @Lovelace
  private @Digits(
          integer = 20,
          fraction = 0
  ) BigInteger value;
  @Column(
          name = "token_type",
          nullable = false
  )
  private TokenType tokenType;
  @Column(
          name = "data_hash",
          length = 64
  )
  @Hash32Type
  private String dataHash;
  @ManyToOne(
          fetch = FetchType.LAZY
  )
  @JoinColumn(
          name = "inline_datum_id",
          foreignKey = @ForeignKey(
                  value = ConstraintMode.NO_CONSTRAINT,
                  name = "none"
          )
  )
  private Datum inlineDatum;
  @ManyToOne(
          fetch = FetchType.LAZY
  )
  @JoinColumn(
          name = "reference_script_id",
          foreignKey = @ForeignKey(
                  value = ConstraintMode.NO_CONSTRAINT,
                  name = "none"
          )
  )
  private Script referenceScript;
  @OneToMany(
          mappedBy = "txOut"
  )
  private List<MaTxOut> maTxOuts;

  public boolean equals(Object o) {
    if (this == o) {
      return true;
    } else if (o != null && Hibernate.getClass(this) == Hibernate.getClass(o)) {
      TxOut txOut = (TxOut)o;
      return this.id != null && Objects.equals(this.id, txOut.id);
    } else {
      return false;
    }
  }

  public int hashCode() {
    return this.getClass().hashCode();
  }

  protected TxOut(TxOutBuilder<?, ?> b) {
    super(b);
    this.tx = b.tx;
    this.txId = b.txId;
    this.index = b.index;
    this.address = b.address;
    this.addressRaw = b.addressRaw;
    this.addressHasScript = b.addressHasScript;
    this.paymentCred = b.paymentCred;
    this.stakeAddress = b.stakeAddress;
    this.value = b.value;
    this.tokenType = b.tokenType;
    this.dataHash = b.dataHash;
    this.inlineDatum = b.inlineDatum;
    this.referenceScript = b.referenceScript;
    this.maTxOuts = b.maTxOuts;
  }

  public static TxOutBuilder<?, ?> builder() {
    return new TxOutBuilderImpl();
  }

  public TxOutBuilder<?, ?> toBuilder() {
    return (new TxOutBuilderImpl()).$fillValuesFrom(this);
  }

  public Tx getTx() {
    return this.tx;
  }

  public Long getTxId() {
    return this.txId;
  }

  public Short getIndex() {
    return this.index;
  }

  public String getAddress() {
    return this.address;
  }

  public byte[] getAddressRaw() {
    return this.addressRaw;
  }

  public Boolean getAddressHasScript() {
    return this.addressHasScript;
  }

  public String getPaymentCred() {
    return this.paymentCred;
  }

  public StakeAddress getStakeAddress() {
    return this.stakeAddress;
  }

  public BigInteger getValue() {
    return this.value;
  }

  public TokenType getTokenType() {
    return this.tokenType;
  }

  public String getDataHash() {
    return this.dataHash;
  }

  public Datum getInlineDatum() {
    return this.inlineDatum;
  }

  public Script getReferenceScript() {
    return this.referenceScript;
  }

  public List<MaTxOut> getMaTxOuts() {
    return this.maTxOuts;
  }

  public void setTx(Tx tx) {
    this.tx = tx;
  }

  public void setTxId(Long txId) {
    this.txId = txId;
  }

  public void setIndex(Short index) {
    this.index = index;
  }

  public void setAddress(String address) {
    this.address = address;
  }

  public void setAddressRaw(byte[] addressRaw) {
    this.addressRaw = addressRaw;
  }

  public void setAddressHasScript(Boolean addressHasScript) {
    this.addressHasScript = addressHasScript;
  }

  public void setPaymentCred(String paymentCred) {
    this.paymentCred = paymentCred;
  }

  public void setStakeAddress(StakeAddress stakeAddress) {
    this.stakeAddress = stakeAddress;
  }

  public void setValue(BigInteger value) {
    this.value = value;
  }

  public void setTokenType(TokenType tokenType) {
    this.tokenType = tokenType;
  }

  public void setDataHash(String dataHash) {
    this.dataHash = dataHash;
  }

  public void setInlineDatum(Datum inlineDatum) {
    this.inlineDatum = inlineDatum;
  }

  public void setReferenceScript(Script referenceScript) {
    this.referenceScript = referenceScript;
  }

  public void setMaTxOuts(List<MaTxOut> maTxOuts) {
    this.maTxOuts = maTxOuts;
  }

  public TxOut() {
  }

  public TxOut(Tx tx, Long txId, Short index, String address, byte[] addressRaw, Boolean addressHasScript, String paymentCred, StakeAddress stakeAddress, BigInteger value, TokenType tokenType, String dataHash, Datum inlineDatum, Script referenceScript, List<MaTxOut> maTxOuts) {
    this.tx = tx;
    this.txId = txId;
    this.index = index;
    this.address = address;
    this.addressRaw = addressRaw;
    this.addressHasScript = addressHasScript;
    this.paymentCred = paymentCred;
    this.stakeAddress = stakeAddress;
    this.value = value;
    this.tokenType = tokenType;
    this.dataHash = dataHash;
    this.inlineDatum = inlineDatum;
    this.referenceScript = referenceScript;
    this.maTxOuts = maTxOuts;
  }

  public abstract static class TxOutBuilder<C extends TxOut, B extends TxOutBuilder<C, B>> extends BaseEntity.BaseEntityBuilder<C, B> {
    private Tx tx;
    private Long txId;
    private Short index;
    private String address;
    private byte[] addressRaw;
    private Boolean addressHasScript;
    private String paymentCred;
    private StakeAddress stakeAddress;
    private BigInteger value;
    private TokenType tokenType;
    private String dataHash;
    private Datum inlineDatum;
    private Script referenceScript;
    private List<MaTxOut> maTxOuts;

    public TxOutBuilder() {
    }

    protected B $fillValuesFrom(C instance) {
      super.$fillValuesFrom(instance);
      $fillValuesFromInstanceIntoBuilder(instance, this);
      return this.self();
    }

    private static void $fillValuesFromInstanceIntoBuilder(TxOut instance, TxOutBuilder<?, ?> b) {
      b.tx(instance.tx);
      b.txId(instance.txId);
      b.index(instance.index);
      b.address(instance.address);
      b.addressRaw(instance.addressRaw);
      b.addressHasScript(instance.addressHasScript);
      b.paymentCred(instance.paymentCred);
      b.stakeAddress(instance.stakeAddress);
      b.value(instance.value);
      b.tokenType(instance.tokenType);
      b.dataHash(instance.dataHash);
      b.inlineDatum(instance.inlineDatum);
      b.referenceScript(instance.referenceScript);
      b.maTxOuts(instance.maTxOuts);
    }

    public B tx(Tx tx) {
      this.tx = tx;
      return this.self();
    }

    public B txId(Long txId) {
      this.txId = txId;
      return this.self();
    }

    public B index(Short index) {
      this.index = index;
      return this.self();
    }

    public B address(String address) {
      this.address = address;
      return this.self();
    }

    public B addressRaw(byte[] addressRaw) {
      this.addressRaw = addressRaw;
      return this.self();
    }

    public B addressHasScript(Boolean addressHasScript) {
      this.addressHasScript = addressHasScript;
      return this.self();
    }

    public B paymentCred(String paymentCred) {
      this.paymentCred = paymentCred;
      return this.self();
    }

    public B stakeAddress(StakeAddress stakeAddress) {
      this.stakeAddress = stakeAddress;
      return this.self();
    }

    public B value(BigInteger value) {
      this.value = value;
      return this.self();
    }

    public B tokenType(TokenType tokenType) {
      this.tokenType = tokenType;
      return this.self();
    }

    public B dataHash(String dataHash) {
      this.dataHash = dataHash;
      return this.self();
    }

    public B inlineDatum(Datum inlineDatum) {
      this.inlineDatum = inlineDatum;
      return this.self();
    }

    public B referenceScript(Script referenceScript) {
      this.referenceScript = referenceScript;
      return this.self();
    }

    public B maTxOuts(List<MaTxOut> maTxOuts) {
      this.maTxOuts = maTxOuts;
      return this.self();
    }

    protected abstract B self();

    public abstract C build();

    public String toString() {
      String var10000 = super.toString();
      return "TxOut.TxOutBuilder(super=" + var10000 + ", tx=" + this.tx + ", txId=" + this.txId + ", index=" + this.index + ", address=" + this.address + ", addressRaw=" + Arrays.toString(this.addressRaw) + ", addressHasScript=" + this.addressHasScript + ", paymentCred=" + this.paymentCred + ", stakeAddress=" + this.stakeAddress + ", value=" + this.value + ", tokenType=" + this.tokenType + ", dataHash=" + this.dataHash + ", inlineDatum=" + this.inlineDatum + ", referenceScript=" + this.referenceScript + ", maTxOuts=" + this.maTxOuts + ")";
    }
  }

  private static final class TxOutBuilderImpl extends TxOutBuilder<TxOut, TxOutBuilderImpl> {
    private TxOutBuilderImpl() {
    }

    protected TxOutBuilderImpl self() {
      return this;
    }

    public TxOut build() {
      return new TxOut(this);
    }
  }
}
