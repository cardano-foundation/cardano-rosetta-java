package org.cardanofoundation.rosetta.common.ledgersync;


public class Relay {
  private Integer port;
  private String ipv4;
  private String ipv6;
  private String dnsName;
  private RelayType relayType;

  public static RelayBuilder builder() {
    return new RelayBuilder();
  }

  public Integer getPort() {
    return this.port;
  }

  public String getIpv4() {
    return this.ipv4;
  }

  public String getIpv6() {
    return this.ipv6;
  }

  public String getDnsName() {
    return this.dnsName;
  }

  public RelayType getRelayType() {
    return this.relayType;
  }

  public Relay(Integer port, String ipv4, String ipv6, String dnsName, RelayType relayType) {
    this.port = port;
    this.ipv4 = ipv4;
    this.ipv6 = ipv6;
    this.dnsName = dnsName;
    this.relayType = relayType;
  }

  public Relay() {
  }

  public boolean equals(Object o) {
    if (o == this) {
      return true;
    } else if (!(o instanceof Relay)) {
      return false;
    } else {
      Relay other = (Relay)o;
      if (!other.canEqual(this)) {
        return false;
      } else {
        label71: {
          Object this$port = this.getPort();
          Object other$port = other.getPort();
          if (this$port == null) {
            if (other$port == null) {
              break label71;
            }
          } else if (this$port.equals(other$port)) {
            break label71;
          }

          return false;
        }

        Object this$ipv4 = this.getIpv4();
        Object other$ipv4 = other.getIpv4();
        if (this$ipv4 == null) {
          if (other$ipv4 != null) {
            return false;
          }
        } else if (!this$ipv4.equals(other$ipv4)) {
          return false;
        }

        label57: {
          Object this$ipv6 = this.getIpv6();
          Object other$ipv6 = other.getIpv6();
          if (this$ipv6 == null) {
            if (other$ipv6 == null) {
              break label57;
            }
          } else if (this$ipv6.equals(other$ipv6)) {
            break label57;
          }

          return false;
        }

        Object this$dnsName = this.getDnsName();
        Object other$dnsName = other.getDnsName();
        if (this$dnsName == null) {
          if (other$dnsName != null) {
            return false;
          }
        } else if (!this$dnsName.equals(other$dnsName)) {
          return false;
        }

        Object this$relayType = this.getRelayType();
        Object other$relayType = other.getRelayType();
        if (this$relayType == null) {
          if (other$relayType == null) {
            return true;
          }
        } else if (this$relayType.equals(other$relayType)) {
          return true;
        }

        return false;
      }
    }
  }

  protected boolean canEqual(Object other) {
    return other instanceof Relay;
  }

  public int hashCode() {
    int result = 1;
    Object $port = this.getPort();
    result = result * 59 + ($port == null ? 43 : $port.hashCode());
    Object $ipv4 = this.getIpv4();
    result = result * 59 + ($ipv4 == null ? 43 : $ipv4.hashCode());
    Object $ipv6 = this.getIpv6();
    result = result * 59 + ($ipv6 == null ? 43 : $ipv6.hashCode());
    Object $dnsName = this.getDnsName();
    result = result * 59 + ($dnsName == null ? 43 : $dnsName.hashCode());
    Object $relayType = this.getRelayType();
    result = result * 59 + ($relayType == null ? 43 : $relayType.hashCode());
    return result;
  }

  public String toString() {
    Integer var10000 = this.getPort();
    return "Relay(port=" + var10000 + ", ipv4=" + this.getIpv4() + ", ipv6=" + this.getIpv6() + ", dnsName=" + this.getDnsName() + ", relayType=" + this.getRelayType() + ")";
  }

  public static class RelayBuilder {
    private Integer port;
    private String ipv4;
    private String ipv6;
    private String dnsName;
    private RelayType relayType;

    RelayBuilder() {
    }

    public RelayBuilder port(Integer port) {
      this.port = port;
      return this;
    }

    public RelayBuilder ipv4(String ipv4) {
      this.ipv4 = ipv4;
      return this;
    }

    public RelayBuilder ipv6(String ipv6) {
      this.ipv6 = ipv6;
      return this;
    }

    public RelayBuilder dnsName(String dnsName) {
      this.dnsName = dnsName;
      return this;
    }

    public RelayBuilder relayType(RelayType relayType) {
      this.relayType = relayType;
      return this;
    }

    public Relay build() {
      return new Relay(this.port, this.ipv4, this.ipv6, this.dnsName, this.relayType);
    }

    public String toString() {
      return "Relay.RelayBuilder(port=" + this.port + ", ipv4=" + this.ipv4 + ", ipv6=" + this.ipv6 + ", dnsName=" + this.dnsName + ", relayType=" + this.relayType + ")";
    }
  }
}
