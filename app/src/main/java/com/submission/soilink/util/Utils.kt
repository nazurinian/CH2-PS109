package com.submission.soilink.util

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.location.Address
import android.location.Geocoder
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.ImageView
import android.widget.Toast
import androidx.camera.core.ImageCapture
import androidx.exifinterface.media.ExifInterface
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.submission.soilink.R
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private const val FORMAT_NAME = "yyyyMMdd_HHmmss"
private const val MAXIMAL_SIZE = 1000000
var accountName: String? = null

fun showToast(context: Context, message: String) {
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
}

fun generateFileName(accountName: String? = null): String {
    val dateFormat = SimpleDateFormat(FORMAT_NAME, Locale.getDefault())
    val currentDateAndTime: String = dateFormat.format(Date())

    val firstChar = accountName?.firstOrNull() ?: 'x'
    val lastChar = accountName?.lastOrNull() ?: 'x'

    val fileName = if (accountName != null) {
        currentDateAndTime + "_$firstChar$lastChar"
    } else {
        currentDateAndTime
    }

    return fileName
}

fun getCurrentDate(): String {
    val calendar = Calendar.getInstance()
    val formatter = SimpleDateFormat("dd, MMMM yyyy", Locale.getDefault())
    return formatter.format(calendar.time)
}

fun createCustomTempFile(context: Context): ImageCapture.OutputFileOptions.Builder {
    val resolver = context.contentResolver
    val contentValues = ContentValues().apply {
        put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        put(MediaStore.Images.Media.DISPLAY_NAME, generateFileName())
        put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis())
    }

    val relativePath = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        contentValues.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/Soilink")
        MediaStore.Images.Media.EXTERNAL_CONTENT_URI
    } else {
        val externalStorageDirectory = Environment.getExternalStorageDirectory()
        val directory = File(externalStorageDirectory, "Pictures/Soilink")
        val file = File(directory, generateFileName(accountName) + ".jpg")

        return ImageCapture.OutputFileOptions.Builder(file)
            .setMetadata(ImageCapture.Metadata().apply {
                isReversedHorizontal = false
                isReversedVertical = false
            })
    }

    return ImageCapture.OutputFileOptions.Builder(resolver, relativePath, contentValues)
}

fun uriToFile(imageUri: Uri, context: Context): File {
    val myFile = File(context.externalMediaDirs.first(), generateFileName(accountName) + ".jpg")
    val inputStream = context.contentResolver.openInputStream(imageUri) as InputStream
    val outputStream = FileOutputStream(myFile)
    val buffer = ByteArray(1024)
    var length: Int
    while (inputStream.read(buffer).also { length = it } > 0) outputStream.write(buffer, 0, length)
    outputStream.close()
    inputStream.close()
    return myFile
}

fun File.reduceFileImage(): File {
    val file = this
    val bitmap = BitmapFactory.decodeFile(file.path).getRotatedBitmap(file)

    var compressQuality = 100
    var streamLength: Int
    do {
        val bmpStream = ByteArrayOutputStream()
        bitmap?.compress(Bitmap.CompressFormat.JPEG, compressQuality, bmpStream)
        val bmpPicByteArray = bmpStream.toByteArray()
        streamLength = bmpPicByteArray.size
        compressQuality -= 5
    } while (streamLength > MAXIMAL_SIZE)
    bitmap?.compress(Bitmap.CompressFormat.JPEG, compressQuality, FileOutputStream(file))
    return file
}

fun Bitmap.getRotatedBitmap(file: File): Bitmap? {
    val orientation = ExifInterface(file).getAttributeInt(
        ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED
    )
    return when (orientation) {
        ExifInterface.ORIENTATION_ROTATE_90 -> rotateImage(this, 90F)
        ExifInterface.ORIENTATION_ROTATE_180 -> rotateImage(this, 180F)
        ExifInterface.ORIENTATION_ROTATE_270 -> rotateImage(this, 270F)
        ExifInterface.ORIENTATION_NORMAL -> this
        else -> this
    }
}

fun rotateImage(source: Bitmap, angle: Float): Bitmap? {
    val matrix = Matrix()
    matrix.postRotate(angle)
    return Bitmap.createBitmap(
        source, 0, 0, source.width, source.height, matrix, true
    )
}

fun showLocation(context: Context, latitude: Double?, longitude: Double?): String {
    val geocoder = Geocoder(context, Locale.getDefault())
    var kecamatan: String? = null
    var kota: String? = null
    var posisi: String? = null
    try {
        if (latitude != null && longitude != null) {
            val addresses = geocoder.getFromLocation(latitude, longitude, 1)!!
            if (addresses.isNotEmpty()) {
                val address: Address = addresses[0]
                val addressDetails = "Address: ${address.getAddressLine(0)}"
                kecamatan = address.locality
                kota = address.subAdminArea
            }
            posisi = "$kecamatan, $kota"
        } else {
            kecamatan = ""
            kota = ""

            posisi = "$kecamatan, $kota"
        }
    } catch (e: IOException) {
        e.printStackTrace()
    }
    return posisi.toString()
}

fun ImageView.loadImage(url: String) {
    val requestOptions = RequestOptions()
        .error(R.drawable.ic_disconnected)
    Glide.with(this.context)
        .setDefaultRequestOptions(requestOptions)
        .load(url)
        .into(this)
}