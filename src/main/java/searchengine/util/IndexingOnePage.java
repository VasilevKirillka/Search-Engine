package searchengine.util;

import lombok.RequiredArgsConstructor;
import org.jsoup.Connection;
import org.jsoup.nodes.Document;
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
public class IndexingOnePage implements Runnable {


    private final SiteRepository siteRepository;
    private final PageRepository pageRepository;
    private final LemmaRepository lemmaRepository;
    private final IndexRepository indexRepository;
    private final SitesList sitesList;
    private final PathFromUrl pathFromUrl;
    private final FindLemmas lemmaFinder;
    private final JsoupConnection connection;
    private final String url;

    @Override
    public void run() {
        String hostSite = pathFromUrl.getHostFromPage(url);
        String path =  url.endsWith("/") ? hostSite : hostSite + "/";
        DBSites siteEntity = siteRepository.findByUrlContainingIgnoreCase(hostSite);

        if (siteEntity == null) {
            String name = getNameSite(url);
            String url = getUrlSite(this.url);
            siteEntity = new DBSites();
            siteEntity.setStatus(StatusIndex.INDEXING);
            siteEntity.setStatusTime(LocalDateTime.now());
            siteEntity.setUrl(url);
            siteEntity.setName(name);
            siteRepository.save(siteEntity);
        }

        DBPages pageEntity = pageRepository.findByPath(path);
        if (pageEntity != null) {
            pageRepository.deleteById(pageEntity.getId());
        }

        Document document = connection.getConnection(url);
        if (document == null) {
            pageEntity = createPages(siteEntity, path, 504, "Gateway timeout");
        } else {
            Connection.Response response = document.connection().response();
            int code = response.statusCode();
            String htmlContent = document.outerHtml();
            pageEntity = createPages(siteEntity, path, code, htmlContent);
        }
        pageRepository.save(pageEntity);
        addLemmas(pageEntity, siteEntity);
    }


//    public void start(String page) {
//        String hostSite = pathFromUrl.getHostFromPage(page);
//        String path =  page.endsWith("/") ? hostSite : hostSite + "/";
//        DBSites siteEntity = siteRepository.findByUrlContainingIgnoreCase(hostSite);
//
//        if (siteEntity == null) {
//            String name = getNameSite(page);
//            String url = getUrlSite(page);
//            siteEntity = new DBSites();
//            siteEntity.setStatus(StatusIndex.INDEXING);
//            siteEntity.setStatusTime(LocalDateTime.now());
//            siteEntity.setUrl(url);
//            siteEntity.setName(name);
//            siteRepository.save(siteEntity);
//        }
//
//        DBPages pageEntity = pageRepository.findByPath(path);
//        if (pageEntity != null) {
//            pageRepository.deleteById(pageEntity.getId());
//        }
//
//        Document document = connection.getConnection(page);
//        if (document == null) {
//            pageEntity = createPages(siteEntity, path, 504, "Gateway timeout");
//        } else {
//            Connection.Response response = document.connection().response();
//            int code = response.statusCode();
//            String htmlContent = document.outerHtml();
//            pageEntity = createPages(siteEntity, path, code, htmlContent);
//        }
//        pageRepository.save(pageEntity);
//        addLemmas(pageEntity, siteEntity);
//    }



    private void addLemmas(DBPages pageEntity, DBSites siteEntity) {
        String content = pageEntity.getContent();
        String clearContent = lemmaFinder.removeHtmlTags(content);
        HashMap<String, Integer> lemmasMap = lemmaFinder.collectLemmas(clearContent);
        Set<String> lemmasSet = new HashSet<>(lemmasMap.keySet());

        for (String lemma : lemmasSet) {
            float rank = lemmasMap.get(lemma);
            DBLemmas lemmaEntity = lemmaRepository.findLemmaByLemmaAndSite(lemma, siteEntity);
            if (lemmaEntity != null) {
                lemmaEntity.setFrequency(lemmaEntity.getFrequency() + 1);
                lemmaRepository.save(lemmaEntity);

                addIndex(pageEntity, lemmaEntity, rank);

                siteEntity.setStatus(StatusIndex.INDEXED);
                siteRepository.save(siteEntity);
            } else {
                lemmaEntity = new DBLemmas();
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
