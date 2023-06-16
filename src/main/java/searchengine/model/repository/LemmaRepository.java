package searchengine.model.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import searchengine.model.DBLemmas;
import searchengine.model.DBSites;

import java.util.List;

@Repository
public interface LemmaRepository extends JpaRepository<DBLemmas, Integer> {
    List<DBLemmas> findBySiteId(DBSites siteEntity);

    List<DBLemmas> findByLemma(String lemma);

    @Query(value = "SELECT l.* FROM Lemma l WHERE l.lemma = :lemma AND l.site_id = :site", nativeQuery = true)
    DBLemmas findLemmaByLemmaAndSite(@Param("lemma") String lemma, @Param("site") DBSites siteEntity);  // indexing one page

    @Query(value = "SELECT l.* FROM Lemma l WHERE l.lemma IN :lemmas AND l.site_id = :site", nativeQuery = true)
    List<DBLemmas> findLemmasBySite(@Param("lemmas") List<String> lemmas, @Param("site") DBSites site); // search service

    int countBySiteId(DBSites site);
}
