package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import searchengine.dto.BadRequest;
import searchengine.dto.SearchData;
import searchengine.dto.SearchResponse;
import searchengine.model.DBIndexes;
import searchengine.model.DBLemmas;
import searchengine.model.DBPages;
import searchengine.model.DBSites;
import searchengine.model.repository.IndexRepository;
import searchengine.model.repository.LemmaRepository;
import searchengine.model.repository.PageRepository;
import searchengine.model.repository.SiteRepository;
import searchengine.util.ErrorsCode;
import searchengine.util.FindLemmas;
import searchengine.util.PathFromUrl;


import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {

    private final SiteRepository siteRepository;
    private final PageRepository pageRepository;
    private final LemmaRepository lemmaRepository;
    private final IndexRepository indexRepository;
    private final FindLemmas lemmaFinder;
    private final PathFromUrl pathFromUrl;
    private final int MAX_SNIPPET_LENGTH = 3;

    @Override
    public ResponseEntity search(String query, String url, int offset, int limit) {
        if (query.isEmpty()) {
            return new ResponseEntity(new BadRequest(false, ErrorsCode.EMPTY_QUERY),
                    HttpStatus.BAD_REQUEST);
        } else {
            List<SearchData> searchData;
            if (!url.isEmpty()) {
                if (siteRepository.findByUrl(url) == null) {
                    return new ResponseEntity(new BadRequest(false, ErrorsCode.NOT_AVAILABLE_PAGE),
                            HttpStatus.BAD_REQUEST);
                } else {
                    searchData = searchOneSite(query, url, offset, limit);
                }
            } else {
                searchData = searchAllSites(query, offset, limit);
            }
            if (searchData == null) {
                return new ResponseEntity(new BadRequest(false, ErrorsCode.NOT_FOUND),
                        HttpStatus.BAD_REQUEST);
            }
            return new ResponseEntity(new SearchResponse(true, searchData.size(), searchData), HttpStatus.OK);
        }
    }

    @Override
    public List<SearchData> searchAllSites(String query, int offset, int limit) {
        List<DBSites> sites = siteRepository.findAll();
        List<SearchData> searchDataList = new ArrayList<>();
        List<DBLemmas> lemmasPerSite = new ArrayList<>();

        List<String> lemmasFromQuery = getQueryIntoLemma(query);
        for (DBSites siteEntity : sites) {
            lemmasPerSite.addAll(getLemmasFromSite(lemmasFromQuery, siteEntity));
        }
        List<SearchData> searchData = null;
        for (DBLemmas lemmaEntity : lemmasPerSite) {
            if (lemmaEntity.getLemma().equals(query)) {
                searchData = new ArrayList<>(getSearchDataList(lemmasPerSite, lemmasFromQuery, offset, limit));
                searchData.sort((o1, o2) -> Float.compare(o2.getRelevance(), o1.getRelevance()));
                if (searchData.size() > limit) {
                    for (int i = offset; i < limit; i++) {
                        searchDataList.add(searchData.get(i));
                    }
                    return searchDataList;
                }
            }
        }
        return searchData;
    }

    @Override
    public List<SearchData> searchOneSite(String query, String url, int offset, int limit) {
        DBSites siteEntity = siteRepository.findByUrl(url);
        List<String> lemmasFromQuery = getQueryIntoLemma(query);
        List<DBLemmas> lemmasFromSite = getLemmasFromSite(lemmasFromQuery, siteEntity);
        return getSearchDataList(lemmasFromSite, lemmasFromQuery, offset, limit);
    }

    private List<String> getQueryIntoLemma(String query) {
        String[] words = query.toLowerCase(Locale.ROOT).split(" ");
        List<String> lemmaList = new ArrayList<>();
        for (String word : words) {
            List<String> lemma = lemmaFinder.getLemma(word);
            lemmaList.addAll(lemma);
        }
        return lemmaList;
    }

    private List<DBLemmas> getLemmasFromSite(List<String> lemmas, DBSites site) {
        List<DBLemmas> lemmaList = lemmaRepository.findLemmasBySite(lemmas, site);
        lemmaList.sort(Comparator.comparingInt(DBLemmas::getFrequency));
        return lemmaList;
    }

    private List<SearchData> getSearchDataList(List<DBLemmas> lemmas, List<String> lemmasFromQuery,
                                               int offset, int limit) {
        List<SearchData> searchDataList = new ArrayList<>();
        if (lemmas.size() >= lemmasFromQuery.size()) {
            List<DBPages> pageList = pageRepository.findByLemmas(lemmas);
            List<DBIndexes> indexList = indexRepository.findByLemmasAndPages(lemmas, pageList);
            Map<DBPages, Float> relevanceMap =
                    getRelevanceFromPage(pageList, indexList);
            List<SearchData> interimDataList = getSearchData((ConcurrentHashMap<DBPages, Float>) relevanceMap, lemmasFromQuery);

            return getResultInterimList(searchDataList, interimDataList, offset, limit);
        } else return searchDataList;
    }

    private List<SearchData> getResultInterimList(List<SearchData> searchDataList, List<SearchData> interimDataList,
                                                  int offset, int limit) {
        if (interimDataList.size() > limit) {
            for (int i = offset; i < limit; i++) {
                searchDataList.add(interimDataList.get(i));
            }
            return searchDataList;
        } else return interimDataList;
    }

    private Map<DBPages, Float> getRelevanceFromPage(List<DBPages> pages,
                                                     List<DBIndexes> indexes) {
        Map<DBPages, Float> relevanceMap = new HashMap<>();
        for (DBPages page : pages) {
            float relevant = 0;
            for (DBIndexes index : indexes) {
                if (index.getPageId().equals(page)) {
                    relevant += index.getRank();
                }
            }
            relevanceMap.put(page, relevant);
        }

        Map<DBPages, Float> allRelevanceMap = new HashMap<>();

        for (DBPages page : relevanceMap.keySet()) {
            float absRelevant = relevanceMap.get(page) / Collections.max(relevanceMap.values());
            allRelevanceMap.put(page, absRelevant);
        }

        List<Map.Entry<DBPages, Float>> sortList = new ArrayList<>(allRelevanceMap.entrySet());
        sortList.sort(Map.Entry.comparingByValue(Comparator.reverseOrder()));
        Map<DBPages, Float> map = new ConcurrentHashMap<>();
        for (Map.Entry<DBPages, Float> pageModelFloatEntry : sortList) {
            map.putIfAbsent(pageModelFloatEntry.getKey(), pageModelFloatEntry.getValue());
        }
        return map;
    }

    private List<SearchData> getSearchData(ConcurrentHashMap<DBPages, Float> sortedPages,
                                           List<String> lemmasFromQuery) {
        List<SearchData> searchData = new ArrayList<>();

        for (DBPages pageEntity : sortedPages.keySet()) {
            String uri = pageEntity.getPath();
            String content = pageEntity.getContent();
            String title = pathFromUrl.getTitleFromHtml(content);
            DBSites siteEntity = pageEntity.getSiteId();
            String site = siteEntity.getUrl();
            String siteName = siteEntity.getName();
            Float absRelevance = sortedPages.get(pageEntity);

            String clearContent = lemmaFinder.removeHtmlTags(content);
            String snippet = getSnippet(clearContent, lemmasFromQuery);

            searchData.add(new SearchData(site, siteName, uri, title, snippet, absRelevance));
        }
        return searchData;
    }


    private String getSnippet(String content, List<String> lemmasFromQuery) {
        List<Integer> lemmaIndex = new ArrayList<>();
        StringBuilder result = new StringBuilder();
        for (String lemma : lemmasFromQuery) {
            lemmaIndex.addAll(lemmaFinder.findLemmaIndexInText(content, lemma));
        }
        Collections.sort(lemmaIndex);
        List<String> wordsList = extractWordsByLemmaIndex(content, lemmaIndex);
//        for (int i = 0; i < wordsList.size(); i++) {
//            result.append(wordsList.get(i)).append("... ");
//            if (i > 3) {
//                break;
//            }
//        }
        for (String word : wordsList) {
            result.append(word).append("... ");
            if (result.length() > MAX_SNIPPET_LENGTH) {
                break;
            }
        }
        return result.toString();
    }

    private List<String> extractWordsByLemmaIndex(String content, List<Integer> lemmaIndex) {
        List<String> result = new ArrayList<>();
        for (int i = 0; i < lemmaIndex.size(); i++) {
            int start = lemmaIndex.get(i);
            int end = content.indexOf(" ", start);
            int step = i + 1;
            while (step < lemmaIndex.size() && lemmaIndex.get(step) - end > 0 && lemmaIndex.get(step) - end < 5) {
                end = content.indexOf(" ", lemmaIndex.get(step));
                step += 1;
            }
            i = step - 1;
            String text = getWordsFromIndexWithHighlighting(start, end, content);
            result.add(text);
        }
        result.sort(Comparator.comparingInt(String::length).reversed());
        return result;
    }

    private String getWordsFromIndexWithHighlighting(int start, int end, String content) {
        String word = content.substring(start, end);
        int prevPoint;
        int lastPoint;
        if (content.lastIndexOf(" ", start) != -1) {
            prevPoint = content.lastIndexOf(" ", start);
        } else prevPoint = start;
        if (content.indexOf(" ", end + 30) != -1) {
            lastPoint = content.indexOf(" ", end + 30);
        } else {
            lastPoint = content.indexOf(" ", end);
        }
        String text = content.substring(prevPoint, lastPoint).replaceAll(word, "<b>" + word + "</b>");
        return text;
    }


}
