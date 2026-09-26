/*
 * Copyright 2025 Christian Kierdorf
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 */
package de.schliweb.makeacopy.utils.image;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.opencv.core.Point;

/** Unit tests for {@link PhysicalSize}. */
public class PhysicalSizeTest {

  private static final double EPS = 1e-9;

  @Test
  public void shortOverLong_matchesShortDividedByLong() {
    assertEquals(53.98 / 85.60, new PhysicalSize(85.60, 53.98).shortOverLong(), EPS);
    assertEquals(53.98 / 85.60, new PhysicalSize(53.98, 85.60).shortOverLong(), EPS);
  }

  @Test
  public void orientedFor_landscapeQuad_putsLongEdgeInWidth() {
    Point[] landscape =
        new Point[] {new Point(0, 0), new Point(1200, 0), new Point(1200, 700), new Point(0, 700)};
    PhysicalSize oriented = new PhysicalSize(51.0, 89.0).orientedFor(landscape);
    assertEquals(89.0, oriented.widthMm(), EPS);
    assertEquals(51.0, oriented.heightMm(), EPS);
  }

  @Test
  public void orientedFor_portraitQuad_putsLongEdgeInHeight() {
    Point[] portrait =
        new Point[] {new Point(0, 0), new Point(700, 0), new Point(700, 1200), new Point(0, 1200)};
    PhysicalSize oriented = new PhysicalSize(89.0, 51.0).orientedFor(portrait);
    assertEquals(51.0, oriented.widthMm(), EPS);
    assertEquals(89.0, oriented.heightMm(), EPS);
  }

  @Test
  public void rotated_90Or270_swapsWidthAndHeight() {
    PhysicalSize size = new PhysicalSize(85.60, 53.98);
    assertEquals(53.98, size.rotated(90).widthMm(), EPS);
    assertEquals(85.60, size.rotated(90).heightMm(), EPS);
    assertEquals(53.98, size.rotated(270).widthMm(), EPS);
    assertEquals(53.98, size.rotated(-90).widthMm(), EPS); // normalizes to 270
  }

  @Test
  public void rotated_0Or180_leavesSizeUnchanged() {
    PhysicalSize size = new PhysicalSize(85.60, 53.98);
    assertEquals(size, size.rotated(0));
    assertEquals(size, size.rotated(180));
    assertEquals(size, size.rotated(360));
  }
}
