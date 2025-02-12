package com.example.yeongkkuel.presentation.my.util

import android.content.Context
import android.net.Uri
import java.io.File

object UriUtil {

    // URI -> File
    fun toFile(context: Context, uri: Uri): File {
        val fileName = getFileName(context, uri)

        val file = FileUtil.createTempFile(context, fileName)
        FileUtil.compressAndSave(context, uri, file)
        return File(file.absolutePath)
    }

    // 파일 이름 생성
    private fun getFileName(context: Context, uri: Uri): String {
        val name = uri.toString().split("/").last()
        val ext = context.contentResolver.getType(uri)!!.split("/").last()

        return "$name.$ext"
    }

}