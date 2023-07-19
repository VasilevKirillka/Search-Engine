package searchengine.model;

import lombok.*;

import javax.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Entity
@Getter
@Setter
@Table(name = "site")
public class DBSites {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "ENUM ('INDEXING', 'INDEXED', 'FAILED')", nullable = false)
    @Basic(optional = false)
    private StatusIndex status;

    @Column(name = "status_time", nullable = false)
    private LocalDateTime statusTime;

    @Column(name = "last_error", columnDefinition = "TEXT")
    private String lastError;

    @Column(nullable = false, columnDefinition = "VARCHAR(515)")
    private String url;

    @Column(columnDefinition = "VARCHAR(515)", nullable = false)
    private String name;


    @OneToMany(mappedBy = "siteId", cascade = CascadeType.ALL)
    private List<DBPages> pagesList = new ArrayList<>();

    @OneToMany(mappedBy = "siteId", cascade = CascadeType.ALL)
    List<DBLemmas> lemmasList = new ArrayList<>();

}
