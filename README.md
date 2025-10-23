# bit-o-repo

인터파크 티켓에서 콘서트 정보를 크롤링하여 저렴하거나 무료인 콘서트를 찾아주는 서비스입니다.

## 주요 기능

- 인터파크 티켓 실시간 크롤링
- 저렴한 콘서트 (10,000원 이하) 필터링
- REST API 제공

## 기술 스택

- Java 21
- Spring Boot 3.5.6
- Selenium WebDriver (웹 크롤링)
- Jsoup (HTML 파싱)
- H2 Database (인메모리)
- Gradle

## 사전 요구사항

### Chrome 브라우저 설치
Selenium을 사용하기 위해 Chrome 브라우저가 설치되어 있어야 합니다.

### ChromeDriver 자동 관리
Selenium 4.x 버전은 WebDriver Manager를 내장하고 있어 ChromeDriver를 자동으로 다운로드합니다.

## 실행 방법

### 1. 빌드
```bash
./gradlew build
```

Windows:
```bash
gradlew.bat build
```

### 2. 애플리케이션 실행
```bash
./gradlew bootRun
```

Windows:
```bash
gradlew.bat bootRun
```

서버는 `http://localhost:8080`에서 실행됩니다.

## API 사용법

### 1. 인터파크 티켓 크롤링 (실제 크롤링)
```bash
curl http://localhost:8080/api/concerts/scrape
```

- 인터파크 티켓에서 실시간으로 콘서트 정보를 가져옵니다
- Chrome 브라우저가 필요합니다
- 5-10초 정도 소요됩니다

### 2. 샘플 데이터 로드 (테스트용)
```bash
curl http://localhost:8080/api/concerts/scrape/sample
```

- 실제 크롤링 없이 샘플 데이터를 사용합니다
- 빠른 테스트에 유용합니다

### 3. 저렴한 콘서트 조회 (10,000원 이하)
```bash
curl http://localhost:8080/api/concerts/cheap
```

### 4. 전체 콘서트 조회
```bash
curl http://localhost:8080/api/concerts
```

## 응답 예시

```json
{
  "message": "크롤링 완료",
  "count": 6,
  "concerts": [
    {
      "id": 1,
      "title": "서울 재즈 페스티벌",
      "artist": "Various Artists",
      "venue": "올림픽공원",
      "date": "2025-11-02",
      "price": 0,
      "url": "https://example.com/jazz-festival",
      "source": "Sample"
    },
    ...
  ]
}
```

## 프로젝트 구조

```
src/main/java/org/bito/concert/
├── model/
│   └── Concert.java              # 콘서트 엔티티
├── repository/
│   └── ConcertRepository.java    # JPA 레포지토리
├── service/
│   └── ConcertScraperService.java # 크롤링 서비스
├── scraper/
│   └── InterparkScraper.java     # 인터파크 크롤러
├── controller/
│   └── ConcertController.java    # REST API 컨트롤러
└── ConcertApplication.java       # 메인 애플리케이션
```

## 데이터베이스

- H2 인메모리 데이터베이스 사용
- 애플리케이션 재시작 시 데이터 초기화
- H2 Console: `http://localhost:8080/h2-console`
  - JDBC URL: `jdbc:h2:mem:concertdb`
  - Username: `sa`
  - Password: (비어있음)

## 주의사항

### 웹 크롤링 관련
- 인터파크 티켓의 이용약관을 준수하세요
- 과도한 요청은 서버에 부담을 줄 수 있습니다
- 개인 학습 목적으로만 사용하세요
- 상업적 용도로 사용하지 마세요

### 기술적 제한사항
- 인터파크 티켓의 HTML 구조가 변경되면 크롤러 수정이 필요합니다
- 헤드리스 Chrome을 사용하므로 시스템 리소스를 사용합니다
- Windows 환경에서 ChromeDriver 경로 문제가 발생할 수 있습니다

## 문제 해결

### ChromeDriver 에러
```
Could not start a new session. Selenium Manager failed
```

**해결 방법:**
1. Chrome 브라우저를 최신 버전으로 업데이트
2. 수동으로 ChromeDriver 다운로드: https://chromedriver.chromium.org/
3. PATH에 ChromeDriver 경로 추가

### 크롤링 실패
크롤링이 실패하면 자동으로 샘플 데이터를 사용합니다.

## 테스트

```bash
./gradlew test
```

## 라이센스

MIT
