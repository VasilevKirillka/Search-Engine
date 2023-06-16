package searchengine.model.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import searchengine.model.DBSites;
import searchengine.model.StatusIndex;


import java.util.List;

@Repository
public interface SiteRepository extends JpaRepository<DBSites, Integer> {
    DBSites findByUrl(String url);
    DBSites findByUrlLike(String url);
    List<DBSites> findByStatus(StatusIndex status);
    boolean existsByStatus(StatusIndex status);
}
