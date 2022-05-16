

package com.assesment.mlkit.vision.demo.preference;

import android.hardware.Camera;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import androidx.annotation.StringRes;

import com.assesment.mlkit.vision.demo.CameraSource;
import com.assesment.mlkit.vision.demo.R;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Configures live preview demo settings. */
public class LivePreviewPreferenceFragment extends PreferenceFragment {

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    addPreferencesFromResource(R.xml.preference_live_preview_quickstart);
    setUpCameraPreferences();
  }

  void setUpCameraPreferences() {
    PreferenceCategory cameraPreference =
        (PreferenceCategory) findPreference(getString(R.string.pref_category_key_camera));
    cameraPreference.removePreference(
        findPreference(getString(R.string.pref_key_camerax_rear_camera_target_resolution)));
    cameraPreference.removePreference(
        findPreference(getString(R.string.pref_key_camerax_front_camera_target_resolution)));
    setUpCameraPreviewSizePreference(
        R.string.pref_key_rear_camera_preview_size,
        R.string.pref_key_rear_camera_picture_size,
        CameraSource.CAMERA_FACING_BACK);
    setUpCameraPreviewSizePreference(
        R.string.pref_key_front_camera_preview_size,
        R.string.pref_key_front_camera_picture_size,
        CameraSource.CAMERA_FACING_FRONT);
  }

  private void setUpCameraPreviewSizePreference(
      @StringRes int previewSizePrefKeyId, @StringRes int pictureSizePrefKeyId, int cameraId) {
    ListPreference previewSizePreference =
        (ListPreference) findPreference(getString(previewSizePrefKeyId));

    Camera camera = null;
    try {
      camera = Camera.open(cameraId);

      List<CameraSource.SizePair> previewSizeList = CameraSource.generateValidPreviewSizeList(camera);
      String[] previewSizeStringValues = new String[previewSizeList.size()];
      Map<String, String> previewToPictureSizeStringMap = new HashMap<>();
      for (int i = 0; i < previewSizeList.size(); i++) {
        CameraSource.SizePair sizePair = previewSizeList.get(i);
        previewSizeStringValues[i] = sizePair.preview.toString();
        if (sizePair.picture != null) {
          previewToPictureSizeStringMap.put(
              sizePair.preview.toString(), sizePair.picture.toString());
        }
      }
      previewSizePreference.setEntries(previewSizeStringValues);
      previewSizePreference.setEntryValues(previewSizeStringValues);

      if (previewSizePreference.getEntry() == null) {
        // First time of opening the Settings page.
        CameraSource.SizePair sizePair =
            CameraSource.selectSizePair(
                camera,
                CameraSource.DEFAULT_REQUESTED_CAMERA_PREVIEW_WIDTH,
                CameraSource.DEFAULT_REQUESTED_CAMERA_PREVIEW_HEIGHT);
        String previewSizeString = sizePair.preview.toString();
        previewSizePreference.setValue(previewSizeString);
        previewSizePreference.setSummary(previewSizeString);
        PreferenceUtils.saveString(
            getActivity(),
            pictureSizePrefKeyId,
            sizePair.picture != null ? sizePair.picture.toString() : null);
      } else {
        previewSizePreference.setSummary(previewSizePreference.getEntry());
      }

      previewSizePreference.setOnPreferenceChangeListener(
          (preference, newValue) -> {
            String newPreviewSizeStringValue = (String) newValue;
            previewSizePreference.setSummary(newPreviewSizeStringValue);
            PreferenceUtils.saveString(
                getActivity(),
                pictureSizePrefKeyId,
                previewToPictureSizeStringMap.get(newPreviewSizeStringValue));
            return true;
          });
    } catch (RuntimeException e) {
      // If there's no camera for the given camera id, hide the corresponding preference.
      ((PreferenceCategory) findPreference(getString(R.string.pref_category_key_camera)))
          .removePreference(previewSizePreference);
    } finally {
      if (camera != null) {
        camera.release();
      }
    }
  }
}
