package searchengine.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

// данные лемм(лемма, кол-во страниц )
@Getter
@Setter
@AllArgsConstructor
public class LemmasData {
    private String lemma;
    private int frequency;

}
