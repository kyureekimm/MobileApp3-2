package ddwu.com.mobile.miniproject3.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/*Memo Dao 구현*/

@Dao
interface MemoDao {
    // 1. 메모 추가 (Insert)
    // suspend: 코루틴을 사용하여 백그라운드에서 실행 (UI 멈춤 방지)
    @Insert
    suspend fun insertMemo(memo: Memo)

    // 2. 메모 수정 (Update)
    @Update
    suspend fun updateMemo(memo: Memo)

    // 3. 메모 삭제 (Delete)
    @Delete
    suspend fun deleteMemo(memo: Memo)

    // 4. 전체 메모 조회 (Read All)
    // Flow를 반환하면 DB 데이터가 변경될 때마다 자동으로 UI에 업데이트를 알려줍니다.
    @Query("SELECT * FROM memo_table")
    fun getAllMemo(): Flow<List<Memo>>

    // 5. 특정 ID로 메모 조회 (Read One)
    // 수정 화면 등으로 이동할 때 특정 메모의 정보를 가져오기 위해 필요합니다.
//    @Query("SELECT * FROM memo_table WHERE _id = :id")
//    suspend fun getMemoById(id: Int): Memo

    // (선택 사항) 제목으로 검색 기능
//    @Query("SELECT * FROM memo_table WHERE memo_title LIKE '%' || :keyword || '%'")
//    fun searchMemos(keyword: String): Flow<List<Memo>>
}