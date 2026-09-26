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

import org.opencv.core.Point;

/** A known real-world size in millimeters, independent of any particular pixel orientation. */
public record PhysicalSize(double widthMm, double heightMm) {

  /** Short/long edge ratio in {@code (0, 1]}. */
  public double shortOverLong() {
    return Math.min(widthMm, heightMm) / Math.max(widthMm, heightMm);
  }

  /** Returns this size laid out to match the given quad's orientation (landscape vs. portrait). */
  public PhysicalSize orientedFor(Point[] corners) {
    double longMm = Math.max(widthMm, heightMm);
    double shortMm = Math.min(widthMm, heightMm);
    return OpenCVUtils.isLandscapeQuad(corners)
        ? new PhysicalSize(longMm, shortMm)
        : new PhysicalSize(shortMm, longMm);
  }

  /** Returns this size with width/height swapped for a 90/270 rotation, unchanged for 0/180. */
  public PhysicalSize rotated(int rotationDeg) {
    int deg = ((rotationDeg % 360) + 360) % 360;
    return (deg == 90 || deg == 270) ? new PhysicalSize(heightMm, widthMm) : this;
  }
}
