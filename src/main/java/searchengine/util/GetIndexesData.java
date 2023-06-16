package searchengine.util;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import searchengine.dto.IndexesData;
import searchengine.model.DBLemmas;
import searchengine.model.DBPages;
import searchengine.model.DBSites;
import searchengine.model.repository.LemmaRepository;
import searchengine.model.repository.PageRepository;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
@RequiredArgsConstructor

public class GetIndexesData {
    private final PageRepository pageRepository;
    private final LemmaRepository lemmaRepository;
    private final FindLemmas lemmaFinder;

    private CopyOnWriteArrayList<IndexesData> indexesData;

    public CopyOnWriteArrayList<IndexesData> getIndexesData(DBSites siteEntity) {
        indexesData = new CopyOnWriteArrayList<>();

        List<DBPages> pages = pageRepository.findBySiteId(siteEntity);
        List<DBLemmas> lemmas = lemmaRepository.findBySiteId(siteEntity);

        for (DBPages page : pages) {
            if (page.getCode() < 400) {
                int pageId = page.getId();
                String content = page.getContent();
                String clearContent = lemmaFinder.removeHtmlTags(content);
                HashMap<String, Integer> indexMap = lemmaFinder.collectLemmas(clearContent);
                for (DBLemmas lemmaEntity : lemmas) {
                    int lemmaId = lemmaEntity.getId();
                    String lemmaWord = lemmaEntity.getLemma();
                    if (indexMap.containsKey(lemmaWord)) {
                        float rank = indexMap.get(lemmaWord);
                        indexesData.add(new IndexesData(pageId, lemmaId, rank));
                    }

                }
            }
        }
        return indexesData;
    }
}
