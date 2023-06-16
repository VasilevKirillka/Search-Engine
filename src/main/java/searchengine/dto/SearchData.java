package searchengine.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
// данные поиска(сайт, имя сайта, путь, релевантность, текст для выделения)
@Getter
@Setter
@AllArgsConstructor
public class SearchData {
    private String site;
    private String siteName;
    private String uri;
    private String title;
    private String snippet;
    private float relevance;
}
