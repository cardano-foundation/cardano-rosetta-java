package org.cardanofoundation.rosetta.crawler.model;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@AllArgsConstructor
@RequiredArgsConstructor
public class PublicRoot {
  private List<AccessPoint> accessPoints;
}
