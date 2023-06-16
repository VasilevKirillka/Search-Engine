package searchengine.model;

import lombok.*;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;


@Entity
@Getter
@Setter
@Table(name = "page", indexes = {@Index(name = "idx_path", columnList = "path")})
public class DBPages {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    @JoinColumn(name = "site_id", nullable = false, referencedColumnName = "id")
    private DBSites siteId;

    @Column(length = 256, nullable = false, columnDefinition = "VARCHAR(256)")
    private String path;

    @Column(nullable = false)
    private int code;

    @Column(columnDefinition = "MEDIUMTEXT")
    private String content;

    @OneToMany(mappedBy = "pageId", cascade = CascadeType.ALL)
    List<DBIndexes> index=new ArrayList<>();
}
