package searchengine.dto.statistics;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class DetailedStatisticsItem {
    private String url;
    private String name;
    private String status;
    private LocalDateTime statusTime;
    private String error;
    private int pages;
    private int lemmas;
}
