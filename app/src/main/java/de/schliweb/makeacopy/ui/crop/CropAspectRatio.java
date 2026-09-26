/*
 * Copyright 2025 Christian Kierdorf
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 */
package de.schliweb.makeacopy.ui.crop;

import androidx.annotation.Nullable;
import de.schliweb.makeacopy.utils.image.PhysicalSize;

/**
 * Aspect ratio choices for the crop step (stage B, see {@code
 * docs/aspect_ratio_concept_v3.8.0.md}).
 *
 * <p>Deliberately separated from {@code de.schliweb.makeacopy.utils.export.PageFormat} (PDF page
 * size with letterboxing). This enum models the geometry that is enforced at warp time, before any
 * export step.
 *
 * <p>{@link #AUTO} keeps stage A (projective estimate inside {@code
 * OpenCVUtils.computeWarpTargetSize}) and is the default. {@link #ORIGINAL} forces the legacy
 * pixel-distance heuristic. The fixed entries map to a concrete short/long ratio. {@link #CUSTOM}
 * pulls its actual ratio from {@link CropPrefsHelper}.
 */
public enum CropAspectRatio {
  /** Stage A: projective aspect-ratio estimate (Zhang & He, 2006). Default. */
  AUTO,
  /** Force the legacy pixel-distance heuristic (no projective correction). */
  ORIGINAL,
  A3,
  /** DIN A series, 1 : sqrt(2). */
  A4,
  A5,
  /** US Letter, 8.5 : 11. */
  US_LETTER,
  /** US Legal, 8.5 : 14. */
  LEGAL,
  /** ISO/IEC 7810 ID-1, 85.60 x 53.98mm, rounded corners. */
  ID1_CARD,
  /** Common business card size, 89 x 51mm. */
  BUSINESS_CARD,
  /** ICAO Doc 9303 TD3 passport bio page, 125 x 88mm. */
  PASSPORT_TD3,
  /** User-defined ratio; the actual numbers are stored via {@link CropPrefsHelper}. */
  CUSTOM;

  private static final double DIN_A = 1.0 / Math.sqrt(2.0); // ≈ 0.7071067811865476
  private static final double LETTER = 8.5 / 11.0; // ≈ 0.7727272727272727
  private static final double LEGAL_R = 8.5 / 14.0; // ≈ 0.6071428571428571

  private static final PhysicalSize ID1_CARD_MM = new PhysicalSize(85.60, 53.98);
  private static final double ID1_CARD_CORNER_RADIUS_MM = 3.18; // mid of ISO spec's 2.88-3.48mm
  private static final PhysicalSize BUSINESS_CARD_MM = new PhysicalSize(89.0, 51.0);
  private static final PhysicalSize PASSPORT_TD3_MM = new PhysicalSize(125.0, 88.0);
  private static final PhysicalSize A3_MM = new PhysicalSize(297.0, 420.0);
  private static final PhysicalSize A4_MM = new PhysicalSize(210.0, 297.0);
  private static final PhysicalSize A5_MM = new PhysicalSize(148.0, 210.0);
  private static final PhysicalSize US_LETTER_MM = new PhysicalSize(215.9, 279.4); // 8.5 x 11in
  private static final PhysicalSize LEGAL_MM = new PhysicalSize(215.9, 355.6); // 8.5 x 14in

  /**
   * Returns the short/long edge ratio in {@code (0, 1]} for fixed entries. For {@link #AUTO},
   * {@link #ORIGINAL} and {@link #CUSTOM} the method returns {@code null}; callers must resolve
   * those via {@link CropPrefsHelper}.
   */
  @Nullable
  public Double shortOverLong() {
    switch (this) {
      case A3:
      case A4:
      case A5:
        return DIN_A;
      case US_LETTER:
        return LETTER;
      case LEGAL:
        return LEGAL_R;
      default:
        break;
    }
    PhysicalSize size = physicalSizeMm();
    return size != null ? size.shortOverLong() : null; // AUTO, ORIGINAL, CUSTOM
  }

  /** Returns the absolute physical size, or {@code null} if none. */
  @Nullable
  public PhysicalSize physicalSizeMm() {
    switch (this) {
      case ID1_CARD:
        return ID1_CARD_MM;
      case BUSINESS_CARD:
        return BUSINESS_CARD_MM;
      case PASSPORT_TD3:
        return PASSPORT_TD3_MM;
      case A3:
        return A3_MM;
      case A4:
        return A4_MM;
      case A5:
        return A5_MM;
      case US_LETTER:
        return US_LETTER_MM;
      case LEGAL:
        return LEGAL_MM;
      default:
        return null;
    }
  }

  /** True for rigid, always-flat items where dewarp mode never applies. */
  public boolean isRigidPhysicalSize() {
    return this == ID1_CARD || this == BUSINESS_CARD || this == PASSPORT_TD3;
  }

  /** Returns the corner radius in mm to round the warped output to, or {@code 0} for square. */
  public double cornerRadiusMm() {
    return this == ID1_CARD ? ID1_CARD_CORNER_RADIUS_MM : 0.0;
  }

  /**
   * Parses a stored name into a {@link CropAspectRatio}. Returns {@code def} when the input is
   * {@code null} or not a known constant.
   */
  public static CropAspectRatio fromName(@Nullable String name, CropAspectRatio def) {
    if (name == null) return def;
    try {
      return CropAspectRatio.valueOf(name);
    } catch (IllegalArgumentException e) {
      return def;
    }
  }
}
