package searchengine.dto;


import lombok.*;



@Getter
@Setter
@AllArgsConstructor
public class IndexesData {
    private int pageId;
    private int lemmaId;
    private float rank;
}
