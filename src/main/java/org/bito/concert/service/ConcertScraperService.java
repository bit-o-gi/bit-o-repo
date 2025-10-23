package org.bito.concert.service;

import org.bito.concert.model.Concert;
import org.bito.concert.repository.ConcertRepository;
import org.bito.concert.scraper.InterparkScraper;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class ConcertScraperService {

    private static final Logger logger = LoggerFactory.getLogger(ConcertScraperService.class);
    private final ConcertRepository concertRepository;
    private final InterparkScraper interparkScraper;

    public ConcertScraperService(ConcertRepository concertRepository, InterparkScraper interparkScraper) {
        this.concertRepository = concertRepository;
        this.interparkScraper = interparkScraper;
    }

    /**
     * 인터파크 티켓에서 콘서트 크롤링
     *
     * 주의: Chrome 브라우저와 ChromeDriver가 설치되어 있어야 합니다.
     * 크롤링은 시간이 걸릴 수 있습니다 (5-10초).
     */
    public List<Concert> scrapeConcerts() {
        logger.info("Starting concert scraping...");

        List<Concert> concerts = new ArrayList<>();

        try {
            // 인터파크에서 실제 크롤링
            concerts.addAll(interparkScraper.scrapeConcerts());

            // 크롤링 결과가 없으면 샘플 데이터 사용
            if (concerts.isEmpty()) {
                logger.warn("No concerts scraped, using sample data");
                concerts.addAll(generateSampleConcerts());
            }

        } catch (Exception e) {
            logger.error("Error during scraping, falling back to sample data", e);
            concerts.addAll(generateSampleConcerts());
        }

        // 데이터베이스에 저장
        concertRepository.deleteAll();
        concertRepository.saveAll(concerts);

        logger.info("Scraped {} concerts", concerts.size());
        return concerts;
    }

    /**
     * 샘플 데이터만 사용 (테스트용)
     */
    public List<Concert> scrapeSampleConcerts() {
        logger.info("Using sample concert data");

        List<Concert> concerts = generateSampleConcerts();

        // 데이터베이스에 저장
        concertRepository.deleteAll();
        concertRepository.saveAll(concerts);

        logger.info("Loaded {} sample concerts", concerts.size());
        return concerts;
    }

    /**
     * 실제 웹사이트에서 크롤링하는 예제 메서드
     * 사용하려는 사이트의 HTML 구조에 맞게 수정 필요
     */
    private List<Concert> scrapeFromWebsite(String url) {
        List<Concert> concerts = new ArrayList<>();

        try {
            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    .timeout(5000)
                    .get();

            // 예시: 콘서트 목록을 담고 있는 요소 선택
            // 실제 사이트 구조에 맞게 CSS selector를 수정해야 합니다
            Elements concertElements = doc.select(".concert-item");

            for (Element element : concertElements) {
                try {
                    String title = element.select(".title").text();
                    String artist = element.select(".artist").text();
                    String venue = element.select(".venue").text();
                    String dateStr = element.select(".date").text();
                    String priceStr = element.select(".price").text();
                    String concertUrl = element.select("a").attr("href");

                    // 가격 파싱 (예: "10,000원" -> 10000)
                    Integer price = parsePrice(priceStr);

                    // 날짜 파싱
                    LocalDate date = parseDate(dateStr);

                    Concert concert = new Concert(title, artist, venue, date, price, concertUrl, "ExampleSite");
                    concerts.add(concert);
                } catch (Exception e) {
                    logger.error("Error parsing concert element", e);
                }
            }
        } catch (Exception e) {
            logger.error("Error scraping website: " + url, e);
        }

        return concerts;
    }

    /**
     * 가격 문자열을 숫자로 변환
     */
    private Integer parsePrice(String priceStr) {
        try {
            if (priceStr == null || priceStr.isEmpty()) {
                return 0;
            }

            if (priceStr.contains("무료") || priceStr.contains("FREE")) {
                return 0;
            }

            // 숫자만 추출
            String digits = priceStr.replaceAll("[^0-9]", "");
            return digits.isEmpty() ? null : Integer.parseInt(digits);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 날짜 문자열을 LocalDate로 변환
     */
    private LocalDate parseDate(String dateStr) {
        try {
            // 여러 날짜 형식 지원
            DateTimeFormatter[] formatters = {
                DateTimeFormatter.ofPattern("yyyy.MM.dd"),
                DateTimeFormatter.ofPattern("yyyy-MM-dd"),
                DateTimeFormatter.ofPattern("yyyy/MM/dd")
            };

            for (DateTimeFormatter formatter : formatters) {
                try {
                    return LocalDate.parse(dateStr, formatter);
                } catch (Exception ignored) {
                }
            }

            return LocalDate.now();
        } catch (Exception e) {
            return LocalDate.now();
        }
    }

    /**
     * 샘플 콘서트 데이터 생성
     */
    private List<Concert> generateSampleConcerts() {
        List<Concert> concerts = new ArrayList<>();

        concerts.add(new Concert(
            "서울 재즈 페스티벌",
            "Various Artists",
            "올림픽공원",
            LocalDate.now().plusDays(10),
            0,
            "https://example.com/jazz-festival",
            "Sample"
        ));

        concerts.add(new Concert(
            "인디 밴드의 밤",
            "The Local Band",
            "홍대 라이브홀",
            LocalDate.now().plusDays(15),
            5000,
            "https://example.com/indie-night",
            "Sample"
        ));

        concerts.add(new Concert(
            "클래식 피아노 리사이틀",
            "김예진",
            "예술의전당 콘서트홀",
            LocalDate.now().plusDays(20),
            8000,
            "https://example.com/piano-recital",
            "Sample"
        ));

        concerts.add(new Concert(
            "거리 공연",
            "버스킹 크루",
            "이태원 거리",
            LocalDate.now().plusDays(7),
            0,
            "https://example.com/busking",
            "Sample"
        ));

        concerts.add(new Concert(
            "K-POP 콘서트",
            "신예 아이돌",
            "잠실 실내체육관",
            LocalDate.now().plusDays(30),
            50000,
            "https://example.com/kpop",
            "Sample"
        ));

        concerts.add(new Concert(
            "아마추어 밴드 경연",
            "여러 팀",
            "강남 클럽",
            LocalDate.now().plusDays(5),
            3000,
            "https://example.com/band-battle",
            "Sample"
        ));

        return concerts;
    }

    /**
     * 저렴한 콘서트만 조회 (10,000원 이하)
     */
    public List<Concert> getCheapConcerts() {
        return concertRepository.findCheapConcerts(10000);
    }

    /**
     * 모든 콘서트 조회 (날짜순)
     */
    public List<Concert> getAllConcerts() {
        return concertRepository.findAllOrderByDate();
    }
}
