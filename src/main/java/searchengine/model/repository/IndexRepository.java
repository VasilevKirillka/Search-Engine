package searchengine.model.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import searchengine.model.DBIndexes;
import searchengine.model.DBLemmas;
import searchengine.model.DBPages;


import java.util.List;

@Repository
public interface IndexRepository extends JpaRepository<DBIndexes, Integer> {
    @Query(value = "SELECT i.* FROM search_index i WHERE i.lemma_id IN :lemmas AND i.page_id IN :pages",
            nativeQuery = true)
    List<DBIndexes> findByLemmasAndPages(@Param("lemmas") List<DBLemmas> lemmas,
                                           @Param("pages") List<DBPages> pages);
}
