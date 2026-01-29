# 📱 Android Application Development Portfolio (2025)

2025학년도 2학기 **모바일응용** 수업을 통해 개발한 안드로이드 프로젝트 저장소입니다. 기초적인 XML 파싱부터 네트워크(Retrofit), 로컬 DB(Room), 위치 기반 서비스(LBS) 및 멀티미디어 활용까지의 학습 과정을 담고 있습니다.

## 🛠 Tech Stack
* **Language**: Kotlin
* **Architecture**: MVVM 패턴 지향
* [cite_start]**Library**: Retrofit2, GSON, Glide, Room, Google Play Services [cite: 256, 557, 821, 930]
* **Asynchronous**: Coroutines, Flow

---

## 📂 Project Directory

| 구분 | 프로젝트명 | 핵심 기술 및 특징 | 주요 기능 |
| :--- | :--- | :--- | :--- |
| **Week 04** | Naver Search (XML) | `XmlPullParser`, `HttpsURLConnection` | 네이버 도서 검색 API 결과 파싱 및 리스트 출력 |
| **Week 07** | Naver Search (Retrofit) | `Retrofit2`, `Internal Storage`, `Glide` | API 통신 고도화 및 도서 이미지 내부 저장소 관리 |
| **Week 10** | LBS & Geocoding | `FusedLocation`, `Geocoder` | 실시간 위도/경도 좌표 획득 및 주소 변환 |
| **Week 12** | Place Record (Mini) | `Google Maps`, `Room DB` | 지도 위 장소 지정 및 로컬 DB 기반 장소 정보 CRUD |
| **Final** | **MusicSpot (음악 일기)** | `Music API`, `Camera`, `Room Flow` | **최종 프로젝트**: 장소와 함께 음악/사진을 기록하는 위치 기반 일기 앱 |

---

## 🌟 Highlight: MusicSpot (Final Project)
사용자의 현재 위치를 기반으로 그 순간 들었던 음악과 사진을 함께 기록하는 애플리케이션입니다.

### Key Features
* **실시간 음악 검색**: iTunes API와 Retrofit2를 연동하여 곡 정보 및 앨범 아트 워크 로드
* **위치 기반 기록**: Google Maps API를 통해 장소를 시각화하고 마커로 관리
* **데이터 영속성**: Room Database와 Flow를 활용하여 저장된 일기 목록을 실시간으로 동기화 및 관리
* **멀티미디어 활용**: 카메라 연동을 통한 현장 사진 촬영 및 저장 기능



---

## ⚠️ Requirements
* **Google Maps API Key**: `AndroidManifest.xml`에 본인의 API Key 설정이 필요합니다.
* **Permissions**: 위치 정보(`ACCESS_FINE_LOCATION`), 인터넷(`INTERNET`), 카메라(`CAMERA`) 권한 승인이 필요합니다.
