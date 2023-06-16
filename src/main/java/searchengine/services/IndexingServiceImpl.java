package searchengine.services;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;
import searchengine.config.JsoupConnection;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.model.StatusIndex;
import searchengine.model.repository.IndexRepository;
import searchengine.model.repository.LemmaRepository;
import searchengine.model.repository.PageRepository;
import searchengine.model.repository.SiteRepository;
import searchengine.util.*;


import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


@Service
@RequiredArgsConstructor
public class IndexingServiceImpl implements IndexingService {
    private static final int CORE_COUNT = Runtime.getRuntime().availableProcessors();
    private ExecutorService executorService;
    private final IndexingOnePage indexingOnePage;

    private final SiteRepository siteRepository;
    private final PageRepository pageRepository;
    private final LemmaRepository lemmaRepository;
    private final IndexRepository indexRepository;
    private final GetLemmasData getLemmasData;
    private final GetIndexesData getIndexesData;
    private final SitesList sitesList;
    private final PathFromUrl pathFromUrl;
    private final JsoupConnection connection;

    @SneakyThrows
    @Override
    public boolean startIndexing() {
        // старт индексирования, если статус INDEXING - false, иначе создается пул потоков, для каждого сайта
        // запускается индексация всез страниц, после потоки закрываются
        if (isIndexing()) {
            return false;
        } else {
            executorService = Executors.newCachedThreadPool();
            for (Site site : sitesList.getSites()) {
                executorService.submit(new IndexingAllPages(siteRepository, pageRepository,
                        lemmaRepository, indexRepository, getLemmasData, getIndexesData, site, pathFromUrl, connection));
            }
            executorService.shutdown();
            return true;
        }
    }

    // прерывание индексации
    @SneakyThrows
    @Override
    public boolean stopIndexing() {
        if (isIndexing()) {
            executorService.shutdown();
            return true;
        } else {
            return false;
        }
    }

    // индексация страницы, если страница доступна и url не отсутствует.
    @Override
    public boolean indexingPage(String url) {
        if (isPageAvailable(url) && !url.isEmpty()) {
            indexingOnePage.start(url);
            return true;
        } else {
            return false;
        }
    }

//    @Override
//    public boolean indexingPage(String urlPage) {
//        if (isPageAvailable(urlPage)) {
//            executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
//            executorService.submit(new IndexingAllPages(siteRepository, pageRepository,
//                    lemmaRepository, indexRepository, getLemmasData, getIndexesData, site, pathFromUrl, connection));
//            executorService.shutdown();
//            return true;
//        } else {
//            return false;
//        }
//
//    }


    private boolean isIndexing() {

        return siteRepository.existsByStatus(StatusIndex.INDEXING);
    }

    public static int getCoreCounts() {

        return CORE_COUNT;
    }

    private boolean isPageAvailable(String url) {
        for (Site site : sitesList.getSites()) {
            String hostPage = pathFromUrl.getHostFromPage(url);
            if (site.getUrl().contains(hostPage)) {
                return true;
            }
        }
        return false;
    }

}
