package ddwu.com.mobile.naversearchtest.data.network

import android.util.Xml
import ddwu.com.mobile.naversearchtest.data.network.Book
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException
import java.io.InputStream


class BookParser {
    private val ns: String? = null

    /*목표태그 상수 선언*/
    //Parsing을 진행하며 확인해야할 목표 태그를 companion으로 지정
    companion object {
        val CHANNEL_TAG = "channel"  //목록태그 channel
        val ITEM_TAG = "item"  //항목태그 item
        val TITLE_TAG = "title"
        val AUTHOR_TAG = "author"
        val PUBLISHER_TAG = "publisher"
        val PUBDATE_TAG = "pubdate"
    }


    @Throws(XmlPullParserException::class, IOException::class)
    fun parse(inputStream: InputStream?) : List<Book> {

        inputStream.use { inputStream ->
            val parser : XmlPullParser = Xml.newPullParser()
            /*Parser 의 동작 정의, next() 호출 전 반드시 호출 필요*/
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
            parser.setInput(inputStream, null)   // Paring 입력 inputStream 설정
            while (parser.name != CHANNEL_TAG/*목록태그명*/) {  // 항목태그를 갖고 있는 목록태그까지 이동
                parser.next()
            }
            val books : List<Book> = readChannel(parser) /*태그파싱 함수 호출 및 결과 저장*/
            return books
        }
    }

    /*목록파싱 함수*/
    @Throws(XmlPullParserException::class, IOException::class)
    private fun readChannel(parser: XmlPullParser) : List<Book/*DTO클래스*/> {
        val books = mutableListOf<Book>()    // 목표 항목을 보관할 리스트 선언

        parser.require(XmlPullParser.START_TAG, ns, CHANNEL_TAG/*목록태그명*/)  // 대상태그 상위태그인지 확인, 아닐 경우 예외 발생

        // 대상태그들을 포함하고 있는 상위태그가 닫힐 때까지 반복
        while(parser.next() != XmlPullParser.END_TAG/*닫는태그 이벤트*/) {
            if (parser.eventType != XmlPullParser.START_TAG) {  // 시작태그 유형 이외 skip
                continue
            }
            if (parser.name == ITEM_TAG/*항목태그명*/) {      // 대상태그(item) 확인
                books.add( readItem(parser) )    // 필요태그 Parsing 결과를 리스트에 추가
            } else {
                skip(parser)    // 항목태그가 아닐 경우 skip
            }
        }

        return books
    }


    @Throws(XmlPullParserException::class, IOException::class)
    private fun readItem(parser: XmlPullParser) : Book/*DTO클래스*/ {
        parser.require(XmlPullParser.START_TAG, ns, ITEM_TAG/*항목태그명*/)   // 항목태그인지 확인
        /*관심태그 값을 저장할 변수 선언
        * title, author, publisher, image*/
        var title : String? = null
        var author : String? = null
        var publisher : String? = null
        var pubdate : Int? = null

        while (parser.next() != XmlPullParser.END_TAG) {    // 닫는 항목태그가 나올 때까지 반복
            if (parser.eventType != XmlPullParser.START_TAG) {
                continue
            }
            when (parser.name) {
                /*태그명 확인 후 관심태그일 경우 값추출 함수 호출 및 및 결과값 저장*/
                TITLE_TAG -> title = readTextInTag(parser, TITLE_TAG)
                AUTHOR_TAG -> author = readTextInTag(parser, AUTHOR_TAG)
                PUBLISHER_TAG -> publisher = readTextInTag(parser, PUBLISHER_TAG)
                PUBDATE_TAG -> pubdate = readTextInTag(parser, PUBDATE_TAG).toInt()
                else -> skip(parser)
            }
        }
        return Book(title, author, publisher, pubdate)/*관심태그 변수값으로 DTO 생성*/   // DTO에 관심태그 값 보관 및 반환
    }


    @Throws(IOException::class, XmlPullParserException::class)
    private fun readTextInTag (parser: XmlPullParser, tag: String): String {
        parser.require(XmlPullParser.START_TAG, ns, tag)    // 현재 태그 확인
        var text = ""

        /*Parsing 이벤트가 TEXT 일 경우 값 추출 및 다음으로 이동*/
        if (parser.next() == XmlPullParser.TEXT) {
            text = parser.text
            parser.nextTag()
        }

        parser.require(XmlPullParser.END_TAG, ns, tag)  // 현재 태그의 닫는 태그 확인
        return text
    }


    @Throws(XmlPullParserException::class, IOException::class)
    private fun skip(parser: XmlPullParser) {
        if (parser.eventType != XmlPullParser.START_TAG) {
            throw IllegalStateException()
        }
        var depth = 1
        while (depth != 0) {    // 현재태그 하위의 태그들을 모두 skip
            when (parser.next()) {
                XmlPullParser.END_TAG -> depth--
                XmlPullParser.START_TAG -> depth++
            }
        }
    }
}