package org.cardanofoundation.rosetta.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import java.math.BigInteger;
import lombok.Getter;
import lombok.Setter;
import org.cardanofoundation.rosetta.common.validation.Lovelace;

@Getter
@Setter
@Entity
@Table(name = "collateral_tx_out")
public class CollateralTxOut extends BaseEntity {


  @NotNull
  @Column(name = "tx_id", nullable = false)
  private Long txId;

  @NotNull
  @Lob
  @Column(name = "address", nullable = false)
  private String address;

  @NotNull
  @Column(name = "address_raw", nullable = false)
  private byte[] addressRaw;

  @NotNull
  @Column(name = "address_has_script", nullable = false)
  private Boolean addressHasScript = false;

  @NotNull
  @Lob
  @Column(name = "multi_assets_descr", nullable = false)
  private String multiAssetsDescr;

  @Column(name = "stake_address_id")
  private Long stakeAddressId;
  @Column(name = "inline_datum_id")
  private Long inlineDatumId;

  @Column(name = "reference_script_id")
  private Long referenceScriptId;


  @Column(name = "index")
  private Short index;


  @Column(name = "payment_cred")
  private String paymentCred;


  @Column(name = "value", nullable = false, precision = 20)
  @Lovelace
  @Digits(integer = 20, fraction = 0)
  private BigInteger value;

  @Column(name = "data_hash")
  private String dataHash;

}