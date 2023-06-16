package searchengine.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
// данные страниц(путь, код, содержание)
@Getter
@Setter
@AllArgsConstructor
public class PagesData {
    private String path;
    private int code;
    private String content;
}
