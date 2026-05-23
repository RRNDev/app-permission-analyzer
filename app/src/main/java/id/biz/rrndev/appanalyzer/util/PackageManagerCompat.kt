package id.biz.rrndev.appanalyzer.util

import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build

object PackageManagerCompat {
    fun PackageManager.getInstalledPackagesCompat(flags: Int): List<PackageInfo> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            getInstalledPackages(PackageManager.PackageInfoFlags.of(flags.toLong()))
        } else {
            @Suppress("DEPRECATION")
            getInstalledPackages(flags)
        }
    }

    fun PackageManager.getPackageArchiveInfoCompat(path: String, flags: Int): PackageInfo? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            getPackageArchiveInfo(path, PackageManager.PackageInfoFlags.of(flags.toLong()))
        } else {
            @Suppress("DEPRECATION")
            getPackageArchiveInfo(path, flags)
        }
    }

    fun PackageInfo.versionCodeCompat(): Long {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            longVersionCode
        } else {
            @Suppress("DEPRECATION")
            versionCode.toLong()
        }
    }
}

