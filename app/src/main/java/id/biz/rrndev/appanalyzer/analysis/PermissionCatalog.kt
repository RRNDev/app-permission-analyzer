package id.biz.rrndev.appanalyzer.analysis

import android.Manifest
import android.content.Context
import androidx.annotation.StringRes
import id.biz.rrndev.appanalyzer.R
import id.biz.rrndev.appanalyzer.model.PermissionCategory
import id.biz.rrndev.appanalyzer.model.PermissionItem
import id.biz.rrndev.appanalyzer.model.WarningLevel
import java.util.Locale

object PermissionCatalog {
    private data class PermissionTemplate(
        @param:StringRes val displayNameRes: Int,
        val category: PermissionCategory,
        @param:StringRes val explanationRes: Int,
        @param:StringRes val privacyImpactRes: Int,
        @param:StringRes val legitimateUseCaseRes: Int,
        val warningLevel: WarningLevel
    )

    private val knownPermissions = mapOf(
        Manifest.permission.CAMERA to PermissionTemplate(
            displayNameRes = R.string.permission_camera_display_name,
            category = PermissionCategory.Privacy,
            explanationRes = R.string.permission_camera_explanation,
            privacyImpactRes = R.string.permission_camera_privacy_impact,
            legitimateUseCaseRes = R.string.permission_camera_legitimate_use,
            warningLevel = WarningLevel.High
        ),
        Manifest.permission.RECORD_AUDIO to PermissionTemplate(
            displayNameRes = R.string.permission_microphone_display_name,
            category = PermissionCategory.Privacy,
            explanationRes = R.string.permission_microphone_explanation,
            privacyImpactRes = R.string.permission_microphone_privacy_impact,
            legitimateUseCaseRes = R.string.permission_microphone_legitimate_use,
            warningLevel = WarningLevel.High
        ),
        Manifest.permission.ACCESS_FINE_LOCATION to PermissionTemplate(
            displayNameRes = R.string.permission_precise_location_display_name,
            category = PermissionCategory.Privacy,
            explanationRes = R.string.permission_precise_location_explanation,
            privacyImpactRes = R.string.permission_precise_location_privacy_impact,
            legitimateUseCaseRes = R.string.permission_precise_location_legitimate_use,
            warningLevel = WarningLevel.High
        ),
        Manifest.permission.ACCESS_COARSE_LOCATION to PermissionTemplate(
            displayNameRes = R.string.permission_approximate_location_display_name,
            category = PermissionCategory.Privacy,
            explanationRes = R.string.permission_approximate_location_explanation,
            privacyImpactRes = R.string.permission_approximate_location_privacy_impact,
            legitimateUseCaseRes = R.string.permission_approximate_location_legitimate_use,
            warningLevel = WarningLevel.Medium
        ),
        Manifest.permission.ACCESS_BACKGROUND_LOCATION to PermissionTemplate(
            displayNameRes = R.string.permission_background_location_display_name,
            category = PermissionCategory.BackgroundAccess,
            explanationRes = R.string.permission_background_location_explanation,
            privacyImpactRes = R.string.permission_background_location_privacy_impact,
            legitimateUseCaseRes = R.string.permission_background_location_legitimate_use,
            warningLevel = WarningLevel.Critical
        ),
        Manifest.permission.READ_CONTACTS to PermissionTemplate(
            displayNameRes = R.string.permission_read_contacts_display_name,
            category = PermissionCategory.Privacy,
            explanationRes = R.string.permission_read_contacts_explanation,
            privacyImpactRes = R.string.permission_read_contacts_privacy_impact,
            legitimateUseCaseRes = R.string.permission_read_contacts_legitimate_use,
            warningLevel = WarningLevel.High
        ),
        Manifest.permission.WRITE_CONTACTS to PermissionTemplate(
            displayNameRes = R.string.permission_edit_contacts_display_name,
            category = PermissionCategory.Privacy,
            explanationRes = R.string.permission_edit_contacts_explanation,
            privacyImpactRes = R.string.permission_edit_contacts_privacy_impact,
            legitimateUseCaseRes = R.string.permission_edit_contacts_legitimate_use,
            warningLevel = WarningLevel.High
        ),
        Manifest.permission.GET_ACCOUNTS to PermissionTemplate(
            displayNameRes = R.string.permission_device_accounts_display_name,
            category = PermissionCategory.Tracking,
            explanationRes = R.string.permission_device_accounts_explanation,
            privacyImpactRes = R.string.permission_device_accounts_privacy_impact,
            legitimateUseCaseRes = R.string.permission_device_accounts_legitimate_use,
            warningLevel = WarningLevel.Medium
        ),
        Manifest.permission.READ_CALENDAR to PermissionTemplate(
            displayNameRes = R.string.permission_read_calendar_display_name,
            category = PermissionCategory.Privacy,
            explanationRes = R.string.permission_read_calendar_explanation,
            privacyImpactRes = R.string.permission_read_calendar_privacy_impact,
            legitimateUseCaseRes = R.string.permission_read_calendar_legitimate_use,
            warningLevel = WarningLevel.High
        ),
        Manifest.permission.WRITE_CALENDAR to PermissionTemplate(
            displayNameRes = R.string.permission_edit_calendar_display_name,
            category = PermissionCategory.Privacy,
            explanationRes = R.string.permission_edit_calendar_explanation,
            privacyImpactRes = R.string.permission_edit_calendar_privacy_impact,
            legitimateUseCaseRes = R.string.permission_edit_calendar_legitimate_use,
            warningLevel = WarningLevel.Medium
        ),
        Manifest.permission.READ_SMS to PermissionTemplate(
            displayNameRes = R.string.permission_read_sms_display_name,
            category = PermissionCategory.Communication,
            explanationRes = R.string.permission_read_sms_explanation,
            privacyImpactRes = R.string.permission_read_sms_privacy_impact,
            legitimateUseCaseRes = R.string.permission_read_sms_legitimate_use,
            warningLevel = WarningLevel.Critical
        ),
        Manifest.permission.SEND_SMS to PermissionTemplate(
            displayNameRes = R.string.permission_send_sms_display_name,
            category = PermissionCategory.Communication,
            explanationRes = R.string.permission_send_sms_explanation,
            privacyImpactRes = R.string.permission_send_sms_privacy_impact,
            legitimateUseCaseRes = R.string.permission_send_sms_legitimate_use,
            warningLevel = WarningLevel.Critical
        ),
        Manifest.permission.RECEIVE_SMS to PermissionTemplate(
            displayNameRes = R.string.permission_receive_sms_display_name,
            category = PermissionCategory.Communication,
            explanationRes = R.string.permission_receive_sms_explanation,
            privacyImpactRes = R.string.permission_receive_sms_privacy_impact,
            legitimateUseCaseRes = R.string.permission_receive_sms_legitimate_use,
            warningLevel = WarningLevel.Critical
        ),
        Manifest.permission.READ_PHONE_STATE to PermissionTemplate(
            displayNameRes = R.string.permission_phone_state_display_name,
            category = PermissionCategory.Communication,
            explanationRes = R.string.permission_phone_state_explanation,
            privacyImpactRes = R.string.permission_phone_state_privacy_impact,
            legitimateUseCaseRes = R.string.permission_phone_state_legitimate_use,
            warningLevel = WarningLevel.Medium
        ),
        Manifest.permission.CALL_PHONE to PermissionTemplate(
            displayNameRes = R.string.permission_place_phone_calls_display_name,
            category = PermissionCategory.Communication,
            explanationRes = R.string.permission_place_phone_calls_explanation,
            privacyImpactRes = R.string.permission_place_phone_calls_privacy_impact,
            legitimateUseCaseRes = R.string.permission_place_phone_calls_legitimate_use,
            warningLevel = WarningLevel.High
        ),
        Manifest.permission.READ_CALL_LOG to PermissionTemplate(
            displayNameRes = R.string.permission_read_call_log_display_name,
            category = PermissionCategory.Communication,
            explanationRes = R.string.permission_read_call_log_explanation,
            privacyImpactRes = R.string.permission_read_call_log_privacy_impact,
            legitimateUseCaseRes = R.string.permission_read_call_log_legitimate_use,
            warningLevel = WarningLevel.Critical
        ),
        Manifest.permission.WRITE_CALL_LOG to PermissionTemplate(
            displayNameRes = R.string.permission_edit_call_log_display_name,
            category = PermissionCategory.Communication,
            explanationRes = R.string.permission_edit_call_log_explanation,
            privacyImpactRes = R.string.permission_edit_call_log_privacy_impact,
            legitimateUseCaseRes = R.string.permission_edit_call_log_legitimate_use,
            warningLevel = WarningLevel.High
        ),
        Manifest.permission.READ_EXTERNAL_STORAGE to PermissionTemplate(
            displayNameRes = R.string.permission_read_shared_storage_display_name,
            category = PermissionCategory.Storage,
            explanationRes = R.string.permission_read_shared_storage_explanation,
            privacyImpactRes = R.string.permission_read_shared_storage_privacy_impact,
            legitimateUseCaseRes = R.string.permission_read_shared_storage_legitimate_use,
            warningLevel = WarningLevel.High
        ),
        Manifest.permission.WRITE_EXTERNAL_STORAGE to PermissionTemplate(
            displayNameRes = R.string.permission_write_shared_storage_display_name,
            category = PermissionCategory.Storage,
            explanationRes = R.string.permission_write_shared_storage_explanation,
            privacyImpactRes = R.string.permission_write_shared_storage_privacy_impact,
            legitimateUseCaseRes = R.string.permission_write_shared_storage_legitimate_use,
            warningLevel = WarningLevel.Medium
        ),
        Manifest.permission.MANAGE_EXTERNAL_STORAGE to PermissionTemplate(
            displayNameRes = R.string.permission_all_files_access_display_name,
            category = PermissionCategory.Storage,
            explanationRes = R.string.permission_all_files_access_explanation,
            privacyImpactRes = R.string.permission_all_files_access_privacy_impact,
            legitimateUseCaseRes = R.string.permission_all_files_access_legitimate_use,
            warningLevel = WarningLevel.Critical
        ),
        Manifest.permission.READ_MEDIA_IMAGES to PermissionTemplate(
            displayNameRes = R.string.permission_photos_and_images_display_name,
            category = PermissionCategory.Storage,
            explanationRes = R.string.permission_photos_and_images_explanation,
            privacyImpactRes = R.string.permission_photos_and_images_privacy_impact,
            legitimateUseCaseRes = R.string.permission_photos_and_images_legitimate_use,
            warningLevel = WarningLevel.High
        ),
        Manifest.permission.READ_MEDIA_VIDEO to PermissionTemplate(
            displayNameRes = R.string.permission_videos_display_name,
            category = PermissionCategory.Storage,
            explanationRes = R.string.permission_videos_explanation,
            privacyImpactRes = R.string.permission_videos_privacy_impact,
            legitimateUseCaseRes = R.string.permission_videos_legitimate_use,
            warningLevel = WarningLevel.High
        ),
        Manifest.permission.READ_MEDIA_AUDIO to PermissionTemplate(
            displayNameRes = R.string.permission_audio_files_display_name,
            category = PermissionCategory.Storage,
            explanationRes = R.string.permission_audio_files_explanation,
            privacyImpactRes = R.string.permission_audio_files_privacy_impact,
            legitimateUseCaseRes = R.string.permission_audio_files_legitimate_use,
            warningLevel = WarningLevel.Medium
        ),
        Manifest.permission.SYSTEM_ALERT_WINDOW to PermissionTemplate(
            displayNameRes = R.string.permission_display_over_other_apps_display_name,
            category = PermissionCategory.DeviceAccess,
            explanationRes = R.string.permission_display_over_other_apps_explanation,
            privacyImpactRes = R.string.permission_display_over_other_apps_privacy_impact,
            legitimateUseCaseRes = R.string.permission_display_over_other_apps_legitimate_use,
            warningLevel = WarningLevel.Critical
        ),
        Manifest.permission.REQUEST_INSTALL_PACKAGES to PermissionTemplate(
            displayNameRes = R.string.permission_install_unknown_apps_display_name,
            category = PermissionCategory.DeviceAccess,
            explanationRes = R.string.permission_install_unknown_apps_explanation,
            privacyImpactRes = R.string.permission_install_unknown_apps_privacy_impact,
            legitimateUseCaseRes = R.string.permission_install_unknown_apps_legitimate_use,
            warningLevel = WarningLevel.High
        ),
        Manifest.permission.BLUETOOTH_CONNECT to PermissionTemplate(
            displayNameRes = R.string.permission_bluetooth_devices_display_name,
            category = PermissionCategory.DeviceAccess,
            explanationRes = R.string.permission_bluetooth_devices_explanation,
            privacyImpactRes = R.string.permission_bluetooth_devices_privacy_impact,
            legitimateUseCaseRes = R.string.permission_bluetooth_devices_legitimate_use,
            warningLevel = WarningLevel.Medium
        ),
        Manifest.permission.BODY_SENSORS to PermissionTemplate(
            displayNameRes = R.string.permission_body_sensors_display_name,
            category = PermissionCategory.Privacy,
            explanationRes = R.string.permission_body_sensors_explanation,
            privacyImpactRes = R.string.permission_body_sensors_privacy_impact,
            legitimateUseCaseRes = R.string.permission_body_sensors_legitimate_use,
            warningLevel = WarningLevel.High
        ),
        Manifest.permission.ACTIVITY_RECOGNITION to PermissionTemplate(
            displayNameRes = R.string.permission_physical_activity_display_name,
            category = PermissionCategory.Privacy,
            explanationRes = R.string.permission_physical_activity_explanation,
            privacyImpactRes = R.string.permission_physical_activity_privacy_impact,
            legitimateUseCaseRes = R.string.permission_physical_activity_legitimate_use,
            warningLevel = WarningLevel.Medium
        ),
        Manifest.permission.POST_NOTIFICATIONS to PermissionTemplate(
            displayNameRes = R.string.permission_notifications_display_name,
            category = PermissionCategory.DeviceAccess,
            explanationRes = R.string.permission_notifications_explanation,
            privacyImpactRes = R.string.permission_notifications_privacy_impact,
            legitimateUseCaseRes = R.string.permission_notifications_legitimate_use,
            warningLevel = WarningLevel.Low
        ),
        Manifest.permission.RECEIVE_BOOT_COMPLETED to PermissionTemplate(
            displayNameRes = R.string.permission_start_after_reboot_display_name,
            category = PermissionCategory.BackgroundAccess,
            explanationRes = R.string.permission_start_after_reboot_explanation,
            privacyImpactRes = R.string.permission_start_after_reboot_privacy_impact,
            legitimateUseCaseRes = R.string.permission_start_after_reboot_legitimate_use,
            warningLevel = WarningLevel.Medium
        ),
        Manifest.permission.FOREGROUND_SERVICE to PermissionTemplate(
            displayNameRes = R.string.permission_foreground_service_display_name,
            category = PermissionCategory.BackgroundAccess,
            explanationRes = R.string.permission_foreground_service_explanation,
            privacyImpactRes = R.string.permission_foreground_service_privacy_impact,
            legitimateUseCaseRes = R.string.permission_foreground_service_legitimate_use,
            warningLevel = WarningLevel.Low
        ),
        Manifest.permission.WAKE_LOCK to PermissionTemplate(
            displayNameRes = R.string.permission_keep_device_awake_display_name,
            category = PermissionCategory.BackgroundAccess,
            explanationRes = R.string.permission_keep_device_awake_explanation,
            privacyImpactRes = R.string.permission_keep_device_awake_privacy_impact,
            legitimateUseCaseRes = R.string.permission_keep_device_awake_legitimate_use,
            warningLevel = WarningLevel.Low
        ),
        "com.google.android.gms.permission.AD_ID" to PermissionTemplate(
            displayNameRes = R.string.permission_advertising_id_display_name,
            category = PermissionCategory.Tracking,
            explanationRes = R.string.permission_advertising_id_explanation,
            privacyImpactRes = R.string.permission_advertising_id_privacy_impact,
            legitimateUseCaseRes = R.string.permission_advertising_id_legitimate_use,
            warningLevel = WarningLevel.Medium
        ),
        "android.permission.SCHEDULE_EXACT_ALARM" to PermissionTemplate(
            displayNameRes = R.string.permission_exact_alarms_display_name,
            category = PermissionCategory.BackgroundAccess,
            explanationRes = R.string.permission_exact_alarms_explanation,
            privacyImpactRes = R.string.permission_exact_alarms_privacy_impact,
            legitimateUseCaseRes = R.string.permission_exact_alarms_legitimate_use,
            warningLevel = WarningLevel.Low
        )
    )

    fun describe(context: Context, permissionName: String): PermissionItem {
        val template = knownPermissions[permissionName]
        return if (template != null) {
            PermissionItem(
                name = permissionName,
                displayName = context.getString(template.displayNameRes),
                category = template.category,
                explanation = context.getString(template.explanationRes),
                privacyImpact = context.getString(template.privacyImpactRes),
                legitimateUseCase = context.getString(template.legitimateUseCaseRes),
                warningLevel = template.warningLevel
            )
        } else {
            inferPermission(context, permissionName)
        }
    }

    private fun inferPermission(context: Context, permissionName: String): PermissionItem {
        val upper = permissionName.uppercase()
        val display = humanizePermissionName(permissionName)
        val category = when {
            upper.contains("LOCATION") || upper.contains("CONTACT") ||
                upper.contains("CAMERA") || upper.contains("AUDIO") ||
                upper.contains("SENSOR") -> PermissionCategory.Privacy
            upper.contains("SMS") || upper.contains("CALL") || upper.contains("PHONE") ->
                PermissionCategory.Communication
            upper.contains("STORAGE") || upper.contains("MEDIA") || upper.contains("FILE") ->
                PermissionCategory.Storage
            upper.contains("AD_ID") || upper.contains("ADVERT") || upper.contains("ANALYTIC") ->
                PermissionCategory.Tracking
            upper.contains("BOOT") || upper.contains("BACKGROUND") || upper.contains("ALARM") ||
                upper.contains("SERVICE") -> PermissionCategory.BackgroundAccess
            upper.contains("BLUETOOTH") || upper.contains("NFC") || upper.contains("OVERLAY") ||
                upper.contains("WINDOW") -> PermissionCategory.DeviceAccess
            else -> PermissionCategory.Other
        }
        val level = when (category) {
            PermissionCategory.Privacy,
            PermissionCategory.Communication,
            PermissionCategory.Storage -> WarningLevel.Medium
            PermissionCategory.Tracking,
            PermissionCategory.BackgroundAccess,
            PermissionCategory.DeviceAccess,
            PermissionCategory.Other -> WarningLevel.Low
        }
        return PermissionItem(
            name = permissionName,
            displayName = display,
            category = category,
            explanation = context.getString(
                R.string.permission_inferred_explanation,
                display.lowercase(Locale.getDefault())
            ),
            privacyImpact = context.getString(R.string.permission_inferred_privacy_impact),
            legitimateUseCase = context.getString(R.string.permission_inferred_legitimate_use),
            warningLevel = level
        )
    }

    fun humanizePermissionName(permissionName: String): String {
        return permissionName
            .substringAfterLast('.')
            .lowercase()
            .split('_')
            .filter { it.isNotBlank() }
            .joinToString(" ") { word -> word.replaceFirstChar { it.titlecase() } }
            .ifBlank { permissionName }
    }
}
