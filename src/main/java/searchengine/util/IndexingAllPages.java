package searchengine.util;

import lombok.RequiredArgsConstructor;
import searchengine.config.JsoupConnection;
import searchengine.config.Site;
import searchengine.dto.IndexesData;
import searchengine.dto.LemmasData;
import searchengine.dto.PagesData;
import searchengine.model.*;
import searchengine.model.repository.IndexRepository;
import searchengine.model.repository.LemmaRepository;
import searchengine.model.repository.PageRepository;
import searchengine.model.repository.SiteRepository;
import searchengine.services.IndexingServiceImpl;


import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ForkJoinPool;

@RequiredArgsConstructor
public class IndexingAllPages implements Runnable {

    private final SiteRepository siteRepository;
    private final PageRepository pageRepository;
    private final LemmaRepository lemmaRepository;
    private final IndexRepository indexRepository;
    private final GetLemmasData getLemmasData;
    private final GetIndexesData getIndexesData;
    private final Site site;
    private final PathFromUrl pathFromUrl;
    private final JsoupConnection connection;


    @Override
    public void run() {
        long start = System.currentTimeMillis();
        String siteName = site.getName();
        if (siteRepository.findByUrl(site.getUrl()) != null) {
            deleteData(site);
        }
        System.out.println("Начинается индексация сайта: " + siteName);
        try {
            addSiteToTheRepository();
            System.out.println("Добавлен сайт: " + siteName);
            addPagesToTheRepository();
            System.out.println("Добавлены страницы сайта: " + siteName);
            addLemmasToTheRepository();
            System.out.println("Добавлены леммы сайта: " + siteName);
            addIndexToTheRepository();
        } catch (InterruptedException e) {
            List<DBSites> sites = siteRepository.findByStatus(StatusIndex.INDEXING);
            for (DBSites siteEntity : sites) {
                siteEntity.setStatusTime(LocalDateTime.now());
                siteEntity.setStatus(StatusIndex.FAILED);
                siteEntity.setLastError("Индексация остановлена пользователем");
                siteRepository.saveAndFlush(siteEntity);
            }
        }
        System.out.println("Время индексации составило " + (System.currentTimeMillis() - start) + " ms");
    }

    private void addSiteToTheRepository() throws InterruptedException {
        if (!Thread.interrupted()) {
            DBSites siteEntity = new DBSites();
            siteEntity.setStatus(StatusIndex.INDEXING);
            siteEntity.setStatusTime(LocalDateTime.now());
            siteEntity.setUrl(site.getUrl());
            siteEntity.setName(site.getName());
            siteRepository.saveAndFlush(siteEntity);

        } else {
            throw new InterruptedException();
        }
    }

    private synchronized void addPagesToTheRepository() throws InterruptedException {
        if (!Thread.interrupted()) {

            ForkJoinPool forkJoinPool = new ForkJoinPool(IndexingServiceImpl.getCoreCounts());
            List<DBPages> pagesStore = new CopyOnWriteArrayList<>();

            CopyOnWriteArrayList<String> linksPool = new CopyOnWriteArrayList<>();
            CopyOnWriteArrayList<PagesData> pages = new CopyOnWriteArrayList<>();

            String siteUrl = site.getUrl();
            DBSites siteEntity = siteRepository.findByUrl(siteUrl);

            CopyOnWriteArrayList<PagesData> dtoPages = forkJoinPool
                    .invoke(new GetPagesData(linksPool, pages, pathFromUrl, siteUrl, connection));
            for (PagesData dtoPage : dtoPages) {
                String pageUrl = dtoPage.getPath();
                String path = pageUrl.endsWith("/") ? pathFromUrl.getPathToPage(pageUrl)
                        : pathFromUrl.getPathToPage(pageUrl) + "/";

                DBPages pageEntity = new DBPages();
                pageEntity.setSiteId(siteEntity);
                pageEntity.setPath(path);
                pageEntity.setCode(dtoPage.getCode());
                pageEntity.setContent(dtoPage.getContent());
                pagesStore.add(pageEntity);
            }
            pageRepository.saveAll(pagesStore);
        } else {
            throw new InterruptedException();
        }
    }

    private void addLemmasToTheRepository() throws InterruptedException {
        if (!Thread.interrupted()) {
            DBSites siteEntity = siteRepository.findByUrl(site.getUrl());
            List<LemmasData> dtoLemmas = getLemmasData.getLemmas(siteEntity);

            List<DBLemmas> lemmasStore = new CopyOnWriteArrayList<>();
            for (LemmasData dtoLemma : dtoLemmas) {
                DBLemmas lemmaEntity = new DBLemmas();
                lemmaEntity.setSiteId(siteEntity);
                lemmaEntity.setLemma(dtoLemma.getLemma());
                lemmaEntity.setFrequency(dtoLemma.getFrequency());
                lemmasStore.add(lemmaEntity);
            }
            lemmaRepository.saveAll(lemmasStore);
        } else {
            throw new InterruptedException();
        }
    }

    private void addIndexToTheRepository() throws InterruptedException {
        if (!Thread.interrupted()) {
            DBSites siteEntity = siteRepository.findByUrl(site.getUrl());
            List<IndexesData> indexesDataList = getIndexesData.getIndexesData(siteEntity);

            List<DBIndexes> indexStore = new CopyOnWriteArrayList<>();
            for (IndexesData indexesData : indexesDataList) {
                DBPages pageEntity = pageRepository.getById(indexesData.getPageId());
                DBLemmas lemmaEntity = lemmaRepository.getById(indexesData.getLemmaId());

                DBIndexes indexEntity = new DBIndexes();
                indexEntity.setPageId(pageEntity);
                indexEntity.setLemmaId(lemmaEntity);
                indexEntity.setRank(indexesData.getRank());
                indexStore.add(indexEntity);
            }
            indexRepository.saveAll(indexStore);

            siteEntity.setStatus(StatusIndex.INDEXED);
            siteEntity.setStatusTime(LocalDateTime.now());
            siteRepository.save(siteEntity);
            System.out.println("Закончена индексация сайта: " + siteEntity.getName());

        } else {
            throw new InterruptedException();
        }
    }

    private void deleteData(Site site) {
        DBSites siteEntity = siteRepository.findByUrl(site.getUrl());
        siteEntity.setStatus(StatusIndex.INDEXING);
        siteRepository.save(siteEntity);
        siteRepository.delete(siteEntity);

    }

}







