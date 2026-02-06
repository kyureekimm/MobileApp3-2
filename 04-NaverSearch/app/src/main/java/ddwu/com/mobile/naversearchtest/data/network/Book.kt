package ddwu.com.mobile.naversearchtest.data.network

/*BOOK DTO -> XML의 item*/
data class Book(
    var title : String?,
    var author : String?,
    var publisher : String?,
    var pubdate : Int?
) {
    override fun toString()
            = "$title($author)-$publisher (출간일:$pubdate)"

}