package searchengine.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import searchengine.dto.BadRequest;
import searchengine.dto.OkResponse;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.services.IndexingService;
import searchengine.services.SearchService;
import searchengine.services.StatisticsService;
import searchengine.util.ErrorsCode;




@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class ApiController {

    private final StatisticsService statisticsService;
    private final IndexingService indexingService;
    private final SearchService searchService;



    @GetMapping("/statistics")
    public ResponseEntity<StatisticsResponse> statistics() {
        return ResponseEntity.ok(statisticsService.getStatistics());
    }

        @GetMapping("/startIndexing")
    public ResponseEntity startIndexing() {
        if (indexingService.startIndexing()) {
            return new ResponseEntity<>(new OkResponse(true), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(new BadRequest(false, ErrorsCode.INDEXING_STARTED),
                    HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/stopIndexing")
    public ResponseEntity stopIndexing() {
        if (indexingService.stopIndexing()) {
            return new ResponseEntity(new OkResponse(true), HttpStatus.OK);
        } else {
            return new ResponseEntity(new BadRequest(false, ErrorsCode.INDEXING_NOT_STARTED),
                    HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/indexPage")
    public ResponseEntity indexPage (@RequestParam(name = "url") String url) {
        if (url.isEmpty()) {
            return new ResponseEntity(new BadRequest(false, ErrorsCode.EMPTY_PAGE),
                    HttpStatus.BAD_REQUEST);
        } else {
            if (indexingService.indexingPage(url)) {
                return new ResponseEntity(new OkResponse(true), HttpStatus.OK);
            } else {
                return new ResponseEntity(new BadRequest(false, ErrorsCode.NOT_AVAILABLE_PAGE),
                        HttpStatus.BAD_REQUEST);
            }
        }
    }

    @GetMapping("/search")
    public ResponseEntity search(@RequestParam(name = "query", required = false, defaultValue = "")
                                         String query,
                                         @RequestParam(name = "site", required = false, defaultValue = "")
                                         String site,
                                         @RequestParam(name = "offset", required = false, defaultValue = "0")
                                         int offset,
                                         @RequestParam(name = "limit", required = false, defaultValue = "20")
                                         int limit) {

        return searchService.search(query, site, offset, limit);
    }

}
