/*
 * Copyright 2025 Christian Kierdorf
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 */
package de.schliweb.makeacopy.ui.export;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import android.graphics.Bitmap;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ViewModelProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import de.schliweb.makeacopy.R;
import de.schliweb.makeacopy.testutil.HiltFragmentScenario;
import de.schliweb.makeacopy.ui.crop.CropAspectRatio;
import de.schliweb.makeacopy.ui.crop.CropPrefsHelper;
import de.schliweb.makeacopy.ui.crop.CropViewModel;
import de.schliweb.makeacopy.ui.export.session.CompletedScan;
import de.schliweb.makeacopy.ui.export.session.ExportSessionViewModel;
import de.schliweb.makeacopy.utils.image.PhysicalSize;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Regression test for the {@code CropViewModel} → {@code ExportFragment} physical-size handoff.
 *
 * <p>{@code CropFragment} orients a known-document preset's physical size to the actual selection
 * quad and stores the result in {@link CropViewModel#getLastAcceptedPhysicalSizeMm()}, which may
 * differ from the preset's own canonical width/height order. {@code ExportFragment} must attach
 * that oriented value to the new page — re-deriving the preset's raw size from {@link
 * CropPrefsHelper} instead would silently place the true-size content with the wrong aspect.
 */
@RunWith(AndroidJUnit4.class)
public class ExportFragmentPhysicalSizeWiringTest {

  @Test
  public void seedsNewPage_withCropViewModelsOrientedSize_notThePresetsRawSize() {
    HiltFragmentScenario<ExportFragment> scenario =
        HiltFragmentScenario.launchInHiltContainer(
            ExportFragment.class, null, R.style.Theme_MakeACopy, Lifecycle.State.RESUMED);

    // Swapped relative to BUSINESS_CARD.physicalSizeMm() (89 x 51) — simulates a card framed
    // portrait, where CropFragment's orientedFor() re-orients the warp before this size is stored.
    PhysicalSize orientedByQuad = new PhysicalSize(51.0, 89.0);

    scenario.onFragment(
        fragment -> {
          CropViewModel cropVm =
              new ViewModelProvider(fragment.requireActivity()).get(CropViewModel.class);
          // The preset's own canonical size, which a re-derive-from-preset bug would return
          // instead of the oriented value below.
          CropPrefsHelper.setLastAspect(fragment.requireContext(), CropAspectRatio.BUSINESS_CARD);
          cropVm.setLastAcceptedPhysicalSizeMm(orientedByQuad);
          cropVm.setImageBitmap(Bitmap.createBitmap(140, 240, Bitmap.Config.ARGB_8888));
        });

    // Fragment view is torn down and recreated; onViewCreated re-runs seedOrAppendCurrentPage
    // with the CropViewModel state set above (the activity-scoped ViewModelStore survives).
    scenario.recreate();

    scenario.onFragment(
        fragment -> {
          ExportSessionViewModel sessionVm =
              new ViewModelProvider(fragment.requireActivity()).get(ExportSessionViewModel.class);
          List<CompletedScan> pages = sessionVm.getPages().getValue();
          assertNotNull(pages);
          assertEquals(1, pages.size());
          assertEquals(orientedByQuad, pages.get(0).physicalSize());
        });
  }
}
