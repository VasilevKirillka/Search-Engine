package searchengine.util;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import searchengine.dto.LemmasData;
import searchengine.model.DBPages;
import searchengine.model.DBSites;
import searchengine.model.repository.PageRepository;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
@RequiredArgsConstructor
public class GetLemmasData {
    private final PageRepository pageRepository;
    private final FindLemmas lemmaFinder;
    private CopyOnWriteArrayList<LemmasData> dtoLemmas;

    public CopyOnWriteArrayList<LemmasData> getLemmas(DBSites site) {
        dtoLemmas = new CopyOnWriteArrayList<>();
        List<DBPages> pages = pageRepository.findBySiteId(site);
        HashMap<String, Integer> lemmasPerPage = new HashMap<>();

        for (DBPages page : pages) {
            String content = page.getContent();
            String clearContent = lemmaFinder.removeHtmlTags(content);
            HashMap<String, Integer> lemmasMap = lemmaFinder.collectLemmas(clearContent);
            Set<String> lemmasSet = new HashSet<>(lemmasMap.keySet());
            for (String lemma : lemmasSet) {
                int frequency = lemmasPerPage.getOrDefault(lemma, 0) + 1;
                lemmasPerPage.put(lemma, frequency);
            }
        }

        for (Map.Entry<String, Integer> entry : lemmasPerPage.entrySet()) {
            String lemma = entry.getKey();
            int frequency = entry.getValue();
            dtoLemmas.add(new LemmasData(lemma, frequency));
        }
        return dtoLemmas;
    }
}
