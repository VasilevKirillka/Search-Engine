package searchengine.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
// результат поискового запроса
@Getter
@Setter
@AllArgsConstructor
public class SearchResponse {
    private boolean result;
    private int count;
    List<SearchData> data;
}
