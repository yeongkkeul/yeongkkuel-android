package com.example.yeongkkuel.presentation.my.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Environment
import java.io.File
import java.io.FileOutputStream

object FileUtil {
    // 임시 파일 생성
    fun createTempFile(context: Context, fileName: String): File {
        val storageDir: File? = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File(storageDir, fileName)
    }

    // 압축 함수 추가
    fun compressAndSave(context: Context, uri: Uri, file: File) {
        val inputStream = context.contentResolver.openInputStream(uri)
        val bitmap = BitmapFactory.decodeStream(inputStream)
        val outputStream = FileOutputStream(file)

        // 압축할 품질과 함께 Bitmap을 압축하여 저장
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, outputStream)

        outputStream.flush()
        outputStream.close()
    }

}