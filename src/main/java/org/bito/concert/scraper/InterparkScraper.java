package org.bito.concert.scraper;

import org.bito.concert.model.Concert;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Component
public class InterparkScraper {

    private static final Logger logger = LoggerFactory.getLogger(InterparkScraper.class);
    private static final String INTERPARK_CONCERT_URL = "http://ticket.interpark.com/TPGoodsList.asp?Ca=Liv";
    private static final String MOBILE_CONCERT_URL = "https://mticket.interpark.com/Genre/ConcertMain?invisible=N";

    /**
     * 인터파크 티켓에서 콘서트 정보 크롤링
     */
    public List<Concert> scrapeConcerts() {
        logger.info("Starting Interpark concert scraping...");

        WebDriver driver = null;
        List<Concert> concerts = new ArrayList<>();

        try {
            driver = createWebDriver();

            // 모바일 페이지 크롤링 시도 (구조가 더 간단함)
            concerts.addAll(scrapeMobilePage(driver));

            logger.info("Successfully scraped {} concerts from Interpark", concerts.size());

        } catch (Exception e) {
            logger.error("Error scraping Interpark concerts", e);
        } finally {
            if (driver != null) {
                driver.quit();
            }
        }

        return concerts;
    }

    /**
     * Chrome WebDriver 생성 (헤드리스 모드)
     */
    private WebDriver createWebDriver() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new");
        options.addArguments("--disable-gpu");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--window-size=1920,1080");
        options.addArguments("--user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");

        return new ChromeDriver(options);
    }

    /**
     * 모바일 페이지 크롤링
     */
    private List<Concert> scrapeMobilePage(WebDriver driver) {
        List<Concert> concerts = new ArrayList<>();

        try {
            logger.info("Accessing Interpark mobile page: {}", MOBILE_CONCERT_URL);
            driver.get(MOBILE_CONCERT_URL);

            // 페이지 로딩 대기 (최대 10초)
            Thread.sleep(3000);

            // 페이지 소스 가져오기
            String pageSource = driver.getPageSource();
            Document doc = Jsoup.parse(pageSource);

            // 콘서트 목록 추출 시도
            // 모바일 페이지의 실제 구조에 맞게 선택자를 조정해야 합니다

            // 방법 1: 상품 리스트 찾기
            Elements goodsItems = doc.select(".goodsItem, .goods-item, .product-item, [class*='goods']");
            logger.info("Found {} goods items", goodsItems.size());

            if (goodsItems.isEmpty()) {
                // 방법 2: 링크 기반으로 찾기
                Elements links = doc.select("a[href*='GoodsCode'], a[href*='goodsCode']");
                logger.info("Found {} links with goods code", links.size());

                for (Element link : links) {
                    try {
                        Concert concert = extractConcertFromLink(link);
                        if (concert != null) {
                            concerts.add(concert);
                        }
                    } catch (Exception e) {
                        logger.debug("Error extracting concert from link", e);
                    }
                }
            } else {
                // 상품 아이템에서 정보 추출
                for (Element item : goodsItems) {
                    try {
                        Concert concert = extractConcertFromElement(item);
                        if (concert != null) {
                            concerts.add(concert);
                        }
                    } catch (Exception e) {
                        logger.debug("Error extracting concert from item", e);
                    }
                }
            }

            // 제한: 최대 20개만
            if (concerts.size() > 20) {
                concerts = concerts.subList(0, 20);
            }

        } catch (Exception e) {
            logger.error("Error scraping mobile page", e);
        }

        return concerts;
    }

    /**
     * 링크 요소에서 콘서트 정보 추출
     */
    private Concert extractConcertFromLink(Element link) {
        String url = link.attr("abs:href");
        String title = link.text();

        if (title.isEmpty()) {
            title = link.attr("title");
        }

        if (title.isEmpty() || url.isEmpty()) {
            return null;
        }

        // 이미지 찾기
        Element img = link.select("img").first();

        // 부모 요소에서 추가 정보 찾기
        Element parent = link.parent();
        String venue = "";
        String dateStr = "";
        Integer price = null;

        if (parent != null) {
            // 날짜 정보 찾기
            Elements dateElements = parent.select("[class*='date'], [class*='period'], .play-date");
            if (!dateElements.isEmpty()) {
                dateStr = dateElements.first().text();
            }

            // 장소 정보 찾기
            Elements venueElements = parent.select("[class*='place'], [class*='venue'], .play-place");
            if (!venueElements.isEmpty()) {
                venue = venueElements.first().text();
            }

            // 가격 정보 찾기
            Elements priceElements = parent.select("[class*='price']");
            if (!priceElements.isEmpty()) {
                String priceStr = priceElements.first().text();
                price = parsePrice(priceStr);
            }
        }

        LocalDate date = parseDate(dateStr);

        Concert concert = new Concert(
            title,
            "Various", // 아티스트 정보가 없는 경우
            venue.isEmpty() ? "인터파크 티켓" : venue,
            date,
            price != null ? price : 0,
            url,
            "Interpark"
        );

        return concert;
    }

    /**
     * 요소에서 콘서트 정보 추출
     */
    private Concert extractConcertFromElement(Element element) {
        // 제목
        String title = element.select(".title, .goods-title, .name").text();
        if (title.isEmpty()) {
            title = element.select("a").first() != null ? element.select("a").first().text() : "";
        }

        // URL
        String url = "";
        Element link = element.select("a[href*='Goods']").first();
        if (link != null) {
            url = link.attr("abs:href");
        }

        // 날짜
        String dateStr = element.select(".date, .period, .play-date").text();
        LocalDate date = parseDate(dateStr);

        // 장소
        String venue = element.select(".place, .venue, .play-place").text();
        if (venue.isEmpty()) {
            venue = "인터파크 티켓";
        }

        // 가격
        String priceStr = element.select(".price").text();
        Integer price = parsePrice(priceStr);

        if (title.isEmpty()) {
            return null;
        }

        return new Concert(
            title,
            "Various",
            venue,
            date,
            price != null ? price : 0,
            url,
            "Interpark"
        );
    }

    /**
     * 가격 문자열 파싱
     */
    private Integer parsePrice(String priceStr) {
        try {
            if (priceStr == null || priceStr.isEmpty()) {
                return 0;
            }

            if (priceStr.contains("무료") || priceStr.toLowerCase().contains("free")) {
                return 0;
            }

            // 숫자만 추출
            String digits = priceStr.replaceAll("[^0-9]", "");
            return digits.isEmpty() ? 0 : Integer.parseInt(digits);
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * 날짜 문자열 파싱
     */
    private LocalDate parseDate(String dateStr) {
        try {
            if (dateStr == null || dateStr.isEmpty()) {
                return LocalDate.now();
            }

            // "2025.10.31~2025.11.01" 형식 처리
            if (dateStr.contains("~")) {
                dateStr = dateStr.split("~")[0].trim();
            }

            // 여러 날짜 형식 시도
            DateTimeFormatter[] formatters = {
                DateTimeFormatter.ofPattern("yyyy.MM.dd"),
                DateTimeFormatter.ofPattern("yyyy-MM-dd"),
                DateTimeFormatter.ofPattern("yyyy/MM/dd"),
                DateTimeFormatter.ofPattern("MM.dd")
            };

            for (DateTimeFormatter formatter : formatters) {
                try {
                    if (dateStr.matches("\\d{2}\\.\\d{2}")) {
                        // MM.dd 형식인 경우 현재 연도 추가
                        dateStr = LocalDate.now().getYear() + "." + dateStr;
                        return LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("yyyy.MM.dd"));
                    }
                    return LocalDate.parse(dateStr, formatter);
                } catch (Exception ignored) {
                }
            }

            return LocalDate.now();
        } catch (Exception e) {
            return LocalDate.now();
        }
    }
}
