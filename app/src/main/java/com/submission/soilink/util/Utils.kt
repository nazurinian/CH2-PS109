package com.submission.soilink.util

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.camera.core.ImageCapture
import androidx.exifinterface.media.ExifInterface
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private const val FORMAT_NAME = "yyyyMMdd_HHmmss"
//private val timeStamp: String = SimpleDateFormat(FORMAT_NAME, Locale.US).format(Date())
private const val MAXIMAL_SIZE = 1000000
var accountName: String? = null

fun showToast(context: Context, message: String) {
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
}

fun generateFileName(accountName: String?): String {
    val dateFormat = SimpleDateFormat(FORMAT_NAME, Locale.getDefault())
    val currentDateAndTime: String = dateFormat.format(Date())

    val firstChar = accountName?.firstOrNull() ?: 'x'
    val lastChar = accountName?.lastOrNull() ?: 'x'

    return currentDateAndTime + "_$firstChar$lastChar"
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
        put(MediaStore.Images.Media.DISPLAY_NAME, generateFileName(accountName))
        put(MediaStore.Images.Media.DATE_TAKEN, generateFileName(accountName))
        put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/Soilink")
    }
    return ImageCapture.OutputFileOptions.Builder(
        resolver,
        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
        contentValues
    )
}

@SuppressLint("Range")
fun getFileNameFromUri(uri: Uri, context: Context): String? {
    var fileName: String? = null
    val cursor: Cursor? = context.contentResolver.query(uri, null, null, null, null)
    cursor?.use {
        it.moveToFirst()
        fileName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
    }
    return fileName
}

fun uriToFile(imageUri: Uri, context: Context): File {
//    val myFile = File(context.externalMediaDirs.first(), "my_temp_image.jpg")
    val fileName: String? = getFileNameFromUri(imageUri, context)
    val myFile = File(context.externalMediaDirs.first(), fileName ?: "my_temp_image.jpg")

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
