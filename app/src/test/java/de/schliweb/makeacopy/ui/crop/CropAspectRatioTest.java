/*
 * Copyright (c) 2024-2025 Christian Kierdorf
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 */
package de.schliweb.makeacopy.ui.crop;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/** JVM unit tests for {@link CropAspectRatio}. See docs/aspect_ratio_concept_v3.8.0.md (§8.1). */
public class CropAspectRatioTest {

  private static final double EPS = 1e-6;

  @Test
  public void shortOverLong_dinASeries_isOneOverSqrt2() {
    double expected = 1.0 / Math.sqrt(2.0);
    Double a3 = CropAspectRatio.A3.shortOverLong();
    Double a4 = CropAspectRatio.A4.shortOverLong();
    Double a5 = CropAspectRatio.A5.shortOverLong();
    assertNotNull(a3);
    assertNotNull(a4);
    assertNotNull(a5);
    assertEquals(expected, a3, EPS);
    assertEquals(expected, a4, EPS);
    assertEquals(expected, a5, EPS);
    // All three DIN-A entries must yield the exact same numeric ratio.
    assertEquals(a4, a3, 0.0);
    assertEquals(a4, a5, 0.0);
  }

  @Test
  public void shortOverLong_letterAndLegal() {
    assertEquals(8.5 / 11.0, CropAspectRatio.US_LETTER.shortOverLong(), EPS);
    assertEquals(8.5 / 14.0, CropAspectRatio.LEGAL.shortOverLong(), EPS);
  }

  @Test
  public void shortOverLong_autoOriginalCustom_returnNull() {
    assertNull(CropAspectRatio.AUTO.shortOverLong());
    assertNull(CropAspectRatio.ORIGINAL.shortOverLong());
    assertNull(CropAspectRatio.CUSTOM.shortOverLong());
  }

  @Test
  public void fromName_validNamesAreParsed() {
    for (CropAspectRatio v : CropAspectRatio.values()) {
      assertSame(v, CropAspectRatio.fromName(v.name(), CropAspectRatio.AUTO));
    }
  }

  @Test
  public void fromName_nullOrUnknown_returnsDefault() {
    assertSame(CropAspectRatio.AUTO, CropAspectRatio.fromName(null, CropAspectRatio.AUTO));
    assertSame(CropAspectRatio.AUTO, CropAspectRatio.fromName("garbage", CropAspectRatio.AUTO));
    assertSame(CropAspectRatio.A4, CropAspectRatio.fromName("garbage", CropAspectRatio.A4));
    // valueOf is case sensitive — lower-case names must not match
    assertSame(CropAspectRatio.AUTO, CropAspectRatio.fromName("a4", CropAspectRatio.AUTO));
  }

  @Test
  public void shortOverLong_isWithinUnitInterval() {
    for (CropAspectRatio v : CropAspectRatio.values()) {
      Double r = v.shortOverLong();
      if (r != null) {
        org.junit.Assert.assertTrue("ratio out of range for " + v + ": " + r, r > 0.0 && r <= 1.0);
      }
    }
  }

  @Test
  public void physicalSizeMm_knownDocumentPresets_returnExactMm() {
    assertEquals(85.60, CropAspectRatio.ID1_CARD.physicalSizeMm().widthMm(), EPS);
    assertEquals(53.98, CropAspectRatio.ID1_CARD.physicalSizeMm().heightMm(), EPS);
    assertEquals(89.0, CropAspectRatio.BUSINESS_CARD.physicalSizeMm().widthMm(), EPS);
    assertEquals(51.0, CropAspectRatio.BUSINESS_CARD.physicalSizeMm().heightMm(), EPS);
    assertEquals(125.0, CropAspectRatio.PASSPORT_TD3.physicalSizeMm().widthMm(), EPS);
    assertEquals(88.0, CropAspectRatio.PASSPORT_TD3.physicalSizeMm().heightMm(), EPS);
  }

  @Test
  public void physicalSizeMm_paperFormats_returnExactMm() {
    assertEquals(297.0, CropAspectRatio.A3.physicalSizeMm().widthMm(), EPS);
    assertEquals(420.0, CropAspectRatio.A3.physicalSizeMm().heightMm(), EPS);
    assertEquals(210.0, CropAspectRatio.A4.physicalSizeMm().widthMm(), EPS);
    assertEquals(297.0, CropAspectRatio.A4.physicalSizeMm().heightMm(), EPS);
    assertEquals(148.0, CropAspectRatio.A5.physicalSizeMm().widthMm(), EPS);
    assertEquals(210.0, CropAspectRatio.A5.physicalSizeMm().heightMm(), EPS);
    assertEquals(215.9, CropAspectRatio.US_LETTER.physicalSizeMm().widthMm(), EPS);
    assertEquals(279.4, CropAspectRatio.US_LETTER.physicalSizeMm().heightMm(), EPS);
    assertEquals(215.9, CropAspectRatio.LEGAL.physicalSizeMm().widthMm(), EPS);
    assertEquals(355.6, CropAspectRatio.LEGAL.physicalSizeMm().heightMm(), EPS);
  }

  @Test
  public void physicalSizeMm_everyOtherEntry_returnsNull() {
    for (CropAspectRatio v : CropAspectRatio.values()) {
      switch (v) {
        case ID1_CARD:
        case BUSINESS_CARD:
        case PASSPORT_TD3:
        case A3:
        case A4:
        case A5:
        case US_LETTER:
        case LEGAL:
          break;
        default:
          assertNull("expected null physicalSizeMm() for " + v, v.physicalSizeMm());
      }
    }
  }

  @Test
  public void shortOverLong_paperFormats_stillExactLegacyConstant() {
    double dinA = 1.0 / Math.sqrt(2.0);
    assertEquals(dinA, CropAspectRatio.A4.shortOverLong(), 0.0);
    assertNotEquals(dinA, CropAspectRatio.A4.physicalSizeMm().shortOverLong(), 0.0);
  }

  @Test
  public void isRigidPhysicalSize_onlyTheThreeCardPresets() {
    assertTrue(CropAspectRatio.ID1_CARD.isRigidPhysicalSize());
    assertTrue(CropAspectRatio.BUSINESS_CARD.isRigidPhysicalSize());
    assertTrue(CropAspectRatio.PASSPORT_TD3.isRigidPhysicalSize());
    for (CropAspectRatio v : CropAspectRatio.values()) {
      if (v != CropAspectRatio.ID1_CARD
          && v != CropAspectRatio.BUSINESS_CARD
          && v != CropAspectRatio.PASSPORT_TD3) {
        assertFalse("expected non-rigid for " + v, v.isRigidPhysicalSize());
      }
    }
  }

  @Test
  public void cornerRadiusMm_onlyId1Card_isNonZero() {
    assertEquals(3.18, CropAspectRatio.ID1_CARD.cornerRadiusMm(), EPS);
    for (CropAspectRatio v : CropAspectRatio.values()) {
      if (v != CropAspectRatio.ID1_CARD) {
        assertEquals("expected square corners for " + v, 0.0, v.cornerRadiusMm(), EPS);
      }
    }
  }

  @Test
  public void shortOverLong_physicalSizePresets_derivedFromMmPair() {
    assertEquals(53.98 / 85.60, CropAspectRatio.ID1_CARD.shortOverLong(), EPS);
    assertEquals(51.0 / 89.0, CropAspectRatio.BUSINESS_CARD.shortOverLong(), EPS);
    assertEquals(88.0 / 125.0, CropAspectRatio.PASSPORT_TD3.shortOverLong(), EPS);
  }
}
