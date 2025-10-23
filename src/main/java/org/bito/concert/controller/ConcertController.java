package org.bito.concert.controller;

import org.bito.concert.model.Concert;
import org.bito.concert.service.ConcertScraperService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/concerts")
@CrossOrigin(origins = "*")
public class ConcertController {

    private final ConcertScraperService scraperService;

    public ConcertController(ConcertScraperService scraperService) {
        this.scraperService = scraperService;
    }

    /**
     * 인터파크 티켓 크롤링 실행 (실제 크롤링)
     * GET /api/concerts/scrape
     *
     * 주의: Chrome과 ChromeDriver 필요, 5-10초 소요
     */
    @GetMapping("/scrape")
    public ResponseEntity<ScrapeResponse> scrapeConcerts() {
        List<Concert> concerts = scraperService.scrapeConcerts();
        return ResponseEntity.ok(new ScrapeResponse(
            "크롤링 완료",
            concerts.size(),
            concerts
        ));
    }

    /**
     * 샘플 데이터 로드 (테스트용)
     * GET /api/concerts/scrape/sample
     */
    @GetMapping("/scrape/sample")
    public ResponseEntity<ScrapeResponse> scrapeSampleConcerts() {
        List<Concert> concerts = scraperService.scrapeSampleConcerts();
        return ResponseEntity.ok(new ScrapeResponse(
            "샘플 데이터 로드 완료",
            concerts.size(),
            concerts
        ));
    }

    /**
     * 저렴한 콘서트 조회 (10,000원 이하)
     * GET /api/concerts/cheap
     */
    @GetMapping("/cheap")
    public ResponseEntity<List<Concert>> getCheapConcerts() {
        List<Concert> concerts = scraperService.getCheapConcerts();
        return ResponseEntity.ok(concerts);
    }

    /**
     * 모든 콘서트 조회
     * GET /api/concerts
     */
    @GetMapping
    public ResponseEntity<List<Concert>> getAllConcerts() {
        List<Concert> concerts = scraperService.getAllConcerts();
        return ResponseEntity.ok(concerts);
    }

    // Response DTO
    public static class ScrapeResponse {
        private String message;
        private int count;
        private List<Concert> concerts;

        public ScrapeResponse(String message, int count, List<Concert> concerts) {
            this.message = message;
            this.count = count;
            this.concerts = concerts;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public int getCount() {
            return count;
        }

        public void setCount(int count) {
            this.count = count;
        }

        public List<Concert> getConcerts() {
            return concerts;
        }

        public void setConcerts(List<Concert> concerts) {
            this.concerts = concerts;
        }
    }
}
