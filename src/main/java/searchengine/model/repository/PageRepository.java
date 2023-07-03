package searchengine.model.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import searchengine.model.DBLemmas;
import searchengine.model.DBPages;
import searchengine.model.DBSites;


import java.util.Collection;
import java.util.List;

@Repository
public interface PageRepository extends JpaRepository<DBPages, Integer> {

    List<DBPages> findBySiteId(DBSites siteEntity);

 //   DBPages findFirstByPath(String path);  // First  findByPath
    DBPages findByPath(String path);
    int countBySiteId(DBSites siteId);

    @Query(value = "SELECT p.* FROM page p JOIN search_index i ON p.id = i.page_id WHERE i.lemma_id IN :lemmas",  // DISTINCT
            nativeQuery = true)
    List<DBPages> findByLemmas(@Param("lemmas") Collection<DBLemmas> lemmas);
}
