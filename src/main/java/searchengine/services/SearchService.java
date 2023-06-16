package searchengine.services;

import org.springframework.http.ResponseEntity;
import searchengine.dto.SearchData;

import java.util.List;

public interface SearchService {
    ResponseEntity search(String query, String url, int offset, int limit);
    List<SearchData> searchAllSites(String query, int offset, int limit);
    List<SearchData> searchOneSite(String query, String url, int offset, int limit);

}
