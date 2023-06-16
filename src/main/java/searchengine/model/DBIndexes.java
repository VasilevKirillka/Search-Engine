package searchengine.model;

import lombok.*;
import javax.persistence.*;


@Entity
@Getter
@Setter
@Table(name = "search_index")
public class DBIndexes{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    @JoinColumn(name = "page_id", nullable = false, referencedColumnName = "id")
    private DBPages pageId;

    @ManyToOne
    @JoinColumn(name = "lemma_id", nullable = false, referencedColumnName = "id")
    private DBLemmas lemmaId;

    @Column(name = "lemma_rank")
    private float rank;

}
