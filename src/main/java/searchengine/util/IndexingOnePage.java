package searchengine.util;

import lombok.RequiredArgsConstructor;
import org.jsoup.Connection;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Component;
import searchengine.config.JsoupConnection;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.model.*;
import searchengine.model.repository.IndexRepository;
import searchengine.model.repository.LemmaRepository;
import searchengine.model.repository.PageRepository;
import searchengine.model.repository.SiteRepository;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
// индексация одной страницы
@RequiredArgsConstructor
@Component
public class IndexingOnePage {


    private final SiteRepository siteRepository;
    private final PageRepository pageRepository;
    private final LemmaRepository lemmaRepository;
    private final IndexRepository indexRepository;
    private final SitesList sitesList;
    private final PathFromUrl pathFromUrl;
    private final FindLemmas lemmaFinder;
    private final JsoupConnection connection; //

    public void start(String page) {
        String path =  page.endsWith("/") ? pathFromUrl.getPathToPage(page) : pathFromUrl.getPathToPage(page) + "/";
        String hostSite = pathFromUrl.getHostFromPage(page);
        DBSites siteEntity;

        if (siteRepository.findByUrlLike("%" + hostSite + "%") == null) {
            String name = getNameSite(page);
            String url = getUrlSite(page);
            siteEntity = new DBSites();
            siteEntity.setStatus(StatusIndex.INDEXING);
            siteEntity.setStatusTime(LocalDateTime.now());
            siteEntity.setUrl(url);
            siteEntity.setName(name);
            siteRepository.save(siteEntity);
        } else {
            siteEntity = siteRepository.findByUrlLike("%" + hostSite + "%");
        }

        if (pageRepository.findByPath(path) != null) {
            DBPages pageEntity = pageRepository.findByPath(path);

            pageRepository.deleteById(pageEntity.getId());
        }

        DBPages pageEntity;

        Document document = connection.getConnection(page);
        if (document == null) {
            pageEntity= createPages(siteEntity, path, 504, "Gateway timeout" );
        } else {
            Connection.Response response = document.connection().response();
            int code = response.statusCode();
            String htmlContent = document.outerHtml();
            pageEntity=createPages(siteEntity, path, code, htmlContent);
        }
        pageRepository.save(pageEntity);
        addLemmas(pageEntity, siteEntity);

    }



    private void addLemmas(DBPages pageEntity, DBSites siteEntity) {
        String content = pageEntity.getContent();
        String clearContent = lemmaFinder.removeHtmlTags(content);
        HashMap<String, Integer> lemmasMap = lemmaFinder.collectLemmas(clearContent);
        Set<String> lemmasSet = new HashSet<>(lemmasMap.keySet());

        for (String lemma : lemmasSet) {
            float rank = lemmasMap.get(lemma);
            if (lemmaRepository.findLemmaByLemmaAndSite(lemma, siteEntity) != null) {
                DBLemmas lemmaEntity = lemmaRepository.findLemmaByLemmaAndSite(lemma, siteEntity);
                lemmaEntity.setFrequency(lemmaEntity.getFrequency() + 1);
                lemmaRepository.save(lemmaEntity);

                addIndex(pageEntity, lemmaEntity, rank);

                siteEntity.setStatus(StatusIndex.INDEXED);
                siteRepository.save(siteEntity);
            } else {
                DBLemmas lemmaEntity = new DBLemmas();
                lemmaEntity.setSiteId(siteEntity);
                lemmaEntity.setLemma(lemma);
                lemmaEntity.setFrequency(1);
                lemmaRepository.save(lemmaEntity);

                addIndex(pageEntity, lemmaEntity, rank);

                siteEntity.setStatus(StatusIndex.INDEXED);
                siteRepository.save(siteEntity);
            }
        }
    }

    private DBPages createPages(DBSites siteEntity, String path, int code, String content){
        DBPages pageEntity = new DBPages();
        pageEntity.setSiteId(siteEntity);
        pageEntity.setPath(path);
        pageEntity.setCode(code);
        pageEntity.setContent(content);
        return pageEntity;

    }

    private void addIndex(DBPages pageEntity, DBLemmas lemmaEntity, float rank) {
        DBIndexes indexEntity = new DBIndexes();
        indexEntity.setPageId(pageEntity);
        indexEntity.setLemmaId(lemmaEntity);
        indexEntity.setRank(rank);
        indexRepository.save(indexEntity);
    }

    private String getUrlSite(String url) {
        for (Site site : sitesList.getSites()) {
            String pageHost = pathFromUrl.getHostFromPage(url);
            if (site.getUrl().contains(pageHost)) {
                return site.getUrl();
            }
        }
        return "";
    }

    private String getNameSite(String url) {
        for (Site site : sitesList.getSites()) {
            String pageHost = pathFromUrl.getHostFromPage(url);
            if (site.getUrl().contains(pageHost)) {
                return site.getName();
            }
        }
        return "";
    }

}
