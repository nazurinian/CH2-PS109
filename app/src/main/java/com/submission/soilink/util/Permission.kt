package com.submission.soilink.util

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.submission.soilink.R

class Permission(private val fragmentActivity: FragmentActivity,  private val permissionCallback: () -> Unit) {
    val requestPermissionLauncher =
        fragmentActivity.registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            when {
                permissions[REQUIRED_FINE_LOCATION_PERMISSION] ?: false -> {
                    checkAllPermission()
                }

                permissions[REQUIRED_WRITE_STORAGE_PERMISSION] ?: false -> {
                    checkAllPermission()
                }

                permissions[REQUIRED_COARSE_LOCATION_PERMISSION] ?: false -> {
                    checkAllPermission()
                }

                permissions[REQUIRED_CAMERA_PERMISSION] ?: false -> {
                    checkAllPermission()
                }

                else -> {}
            }
        }
    private fun checkAllPermission() {
        val requiredPermissions = arrayOf(
            REQUIRED_FINE_LOCATION_PERMISSION,
            REQUIRED_WRITE_STORAGE_PERMISSION,
            REQUIRED_COARSE_LOCATION_PERMISSION,
            REQUIRED_CAMERA_PERMISSION
        )

        if (!requiredPermissions.all { checkPermission(it) }) {
            requiredPermissions.forEach { permission ->
                if (!checkPermission(permission)) {
                    when (permission) {
                        REQUIRED_FINE_LOCATION_PERMISSION -> showToast(
                            fragmentActivity,
                            fragmentActivity.getString(R.string.fine_gps_granted)
                        )

                        REQUIRED_WRITE_STORAGE_PERMISSION -> showToast(
                            fragmentActivity,
                            fragmentActivity.getString(R.string.write_storage_granted)
                        )

                        REQUIRED_COARSE_LOCATION_PERMISSION -> {
                            showToast(
                                fragmentActivity,
                                fragmentActivity.getString(R.string.coarse_gps_granted)
                            )
                        }

                        REQUIRED_CAMERA_PERMISSION -> showToast(
                            fragmentActivity,
                            fragmentActivity.getString(R.string.camera_permission_granted)
                        )
                    }
                    requestPermissionLauncher.launch(arrayOf(permission))
                }
            }
        }
    }

    fun checkPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(
            fragmentActivity,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }

    companion object {
        fun handlePermissionFlow(permission: Permission) {
            when {
                !permission.checkPermission(REQUIRED_FINE_LOCATION_PERMISSION) -> {
                    permission.requestPermissionLauncher.launch(arrayOf(REQUIRED_FINE_LOCATION_PERMISSION))
                }
                !permission.checkPermission(REQUIRED_WRITE_STORAGE_PERMISSION) -> {
                    permission.requestPermissionLauncher.launch(arrayOf(REQUIRED_WRITE_STORAGE_PERMISSION))
                }
                !permission.checkPermission(REQUIRED_COARSE_LOCATION_PERMISSION) -> {
                    permission.requestPermissionLauncher.launch(arrayOf(REQUIRED_COARSE_LOCATION_PERMISSION))
                }
                !permission.checkPermission(REQUIRED_CAMERA_PERMISSION) -> {
                    permission.requestPermissionLauncher.launch(arrayOf(REQUIRED_CAMERA_PERMISSION))
                }
                else -> {
                    permission.permissionCallback.invoke()
                }
            }
        }
    }
}