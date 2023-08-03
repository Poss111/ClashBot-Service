package com.poss.clash.bot.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ArchiveStatus {

  IN_PROGRESS("IN_PROGRESS"),
  SUCCESSFUL("SUCCESSFUL"),
  FAILED("FAILED");

  private final String value;

  @Override
  public String toString() {
    return String.valueOf(value);
  }

  public static ArchiveStatus fromValue(String value) {
    for (ArchiveStatus b : ArchiveStatus.values()) {
      if (b.value.equals(value)) {
        return b;
      }
    }
    throw new IllegalArgumentException("Unexpected value '" + value + "'");
  }

}
