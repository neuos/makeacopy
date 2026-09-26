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
  /**
   * ISO/IEC 7810 ID-1 (EU ID cards, driving licences, payment cards since ~2013). 85.60 x
   * 53.98mm, rounded corners. Unlike the other fixed entries this also carries an absolute
   * physical size (see {@link #physicalSizeMm()}) — the crop warps to this exact size in pixels
   * at {@code OpenCVUtils.PHYSICAL_SIZE_DPI}, not just this ratio.
   */
  ID1_CARD,
  /** Common business card size, 89 x 51mm. Square corners (unlike {@link #ID1_CARD}). */
  BUSINESS_CARD,
  /** ICAO Doc 9303 TD3 passport bio page, 125 x 88mm. Square corners. */
  PASSPORT_TD3,
  /** User-defined ratio; the actual numbers are stored via {@link CropPrefsHelper}. */
  CUSTOM;

  private static final double DIN_A = 1.0 / Math.sqrt(2.0); // ≈ 0.7071067811865476
  private static final double LETTER = 8.5 / 11.0; // ≈ 0.7727272727272727
  private static final double LEGAL_R = 8.5 / 14.0; // ≈ 0.6071428571428571

  private static final double[] ID1_CARD_MM = {85.60, 53.98};
  private static final double ID1_CARD_CORNER_RADIUS_MM = 3.18; // mid of ISO spec's 2.88-3.48mm
  private static final double[] BUSINESS_CARD_MM = {89.0, 51.0};
  private static final double[] PASSPORT_TD3_MM = {125.0, 88.0};
  // ISO 216 / ANSI paper sizes. Their rounded mm dimensions give a short/long ratio that is
  // extremely close to but not bit-identical to the exact irrational constants above (e.g.
  // 210/297 ≈ 0.7070707 vs. the true 1/sqrt(2) ≈ 0.7071068) — shortOverLong() below deliberately
  // keeps returning the exact constant for these entries; physicalSizeMm() is only consulted for
  // the *absolute*-size warp/export path, not to redefine the long-standing ratio contract.
  private static final double[] A3_MM = {297.0, 420.0};
  private static final double[] A4_MM = {210.0, 297.0};
  private static final double[] A5_MM = {148.0, 210.0};
  private static final double[] US_LETTER_MM = {215.9, 279.4}; // 8.5 x 11in
  private static final double[] LEGAL_MM = {215.9, 355.6}; // 8.5 x 14in

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
    double[] mm = physicalSizeMm();
    if (mm != null) {
      return Math.min(mm[0], mm[1]) / Math.max(mm[0], mm[1]);
    }
    return null; // AUTO, ORIGINAL, CUSTOM
  }

  /**
   * Returns the {@code {widthMm, heightMm}} absolute physical size for every entry that has one
   * — the rigid known-document presets ({@link #ID1_CARD}, {@link #BUSINESS_CARD}, {@link
   * #PASSPORT_TD3}) and the fixed paper formats ({@link #A3}, {@link #A4}, {@link #A5}, {@link
   * #US_LETTER}, {@link #LEGAL}) — or {@code null} for {@link #AUTO}, {@link #ORIGINAL} and
   * {@link #CUSTOM}, which only ever enforce a ratio.
   */
  @Nullable
  public double[] physicalSizeMm() {
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

  /**
   * True for entries that describe a rigid, always-flat item (never a curved book page), where
   * the crop step's curved-edges/dewarp mode is meaningless and should always be bypassed in
   * favor of a plain flat warp to {@link #physicalSizeMm()}. {@link #A3}/{@link #A4}/{@link
   * #A5}/{@link #US_LETTER}/{@link #LEGAL} are deliberately excluded — a paper document at one of
   * those sizes can still be a curved book page, so those keep respecting dewarp mode.
   */
  public boolean isRigidPhysicalSize() {
    return this == ID1_CARD || this == BUSINESS_CARD || this == PASSPORT_TD3;
  }

  /**
   * Returns the corner radius in mm to cosmetically round the warped output to, or {@code 0} for
   * entries with square corners (including every non-physical-size entry).
   */
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
