package ddwu.com.mobile.cameratest

import android.content.Context
import android.net.Uri
import android.os.Environment
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date

class FileUtil {
    companion object {
        private const val TAG = "FileUtil"
        private const val FILENAME_FORMAT = "yyyyMMdd_HHmmss"

        /*현재 시간 정보로 이미지 파일에 사용할 파일 명(절대경로명) 생성*/
        private fun getFileName(context: Context) : String {
            val timeStamp: String = SimpleDateFormat(FILENAME_FORMAT).format(Date())
            // 파일 저장 위치를 바꿔야 할 경우 수정 필요
            val storageDir: File? = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            return "${storageDir?.path}/${timeStamp}.jpg"    // 시간정보를 사용한 파일명의 파일 생성
        }

        /*현재 시간을 파일명으로 새로운 파일 생성*/
        @Throws(IOException::class)
        fun createNewFile(context: Context): File {
            val file = File (getFileName(context))    // 시간정보를 사용한 파일명의 파일 생성
            return file
        }

        /*uri 에 해당하는 이미지를 외부저장소 지정 위치에 파일로 저장*/
        fun saveFileToExtStorage(context: Context, sourceUri: Uri?) : String? {
            if (sourceUri == null) {
                Log.e(TAG, "sourceUri is null")
                return null
            }

            val saveTargetFile = File(getFileName(context))  // 이미지를 저장할 목표 파일

            return try {
                  // 원본 URI로부터 InputStream 열기
                context.contentResolver.openInputStream(sourceUri)?.use { input ->
                    FileOutputStream(saveTargetFile).use { output ->
                        input.copyTo(output)
                    }
                } ?: null   // input(InputStream) 이 null 일 경우 null 반환
                Log.d(TAG, "사진 저장 완료: ${saveTargetFile.absolutePath}")
                saveTargetFile.absolutePath
            } catch (e: Exception) {
                Log.e(TAG, "파일 저장 오류: ${e.message}", e)
                return null
            }
        }


        /*filePath의 파일 삭제*/
        @Throws(IOException::class)
        fun deleteFile(filePath: String?) : Boolean {
            if (filePath.isNullOrEmpty()) return false

            val file = File(filePath)
            return if (file.exists()) {
                val deleted = file.delete()     // 삭제 실행 결과 반환
                Log.d(TAG, "$filePath 삭제 $deleted")
                deleted
            } else {
                Log.d(TAG, "$filePath 삭제 실패")
                false
            }
        }
    }
}