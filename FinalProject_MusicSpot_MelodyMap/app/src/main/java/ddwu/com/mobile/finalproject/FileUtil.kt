package ddwu.com.mobile.finalproject

import android.content.Context
import android.os.Environment
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FileUtil(val context: Context) {

    fun getFilePath() : String? {
        val fileName = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File? = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val file = File.createTempFile(fileName, ".jpg", storageDir)
        return file.absolutePath
    }


    fun deleteFile(fileName: String?) {
        if (fileName != null) {
            val file = File(fileName)
            if (file.exists()) file.delete()
        }
    }
}