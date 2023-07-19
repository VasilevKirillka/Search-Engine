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
import java.util.concurrent.TimeUnit;


@Service
@RequiredArgsConstructor
public class IndexingServiceImpl implements IndexingService {
    private static final int CORE_COUNT = Runtime.getRuntime().availableProcessors();
    private ExecutorService executorService;
    private final SiteRepository siteRepository;
    private final PageRepository pageRepository;
    private final LemmaRepository lemmaRepository;
    private final IndexRepository indexRepository;
    private final GetLemmasData getLemmasData;
    private final GetIndexesData getIndexesData;
    private final SitesList sitesList;
    private final PathFromUrl pathFromUrl;
    private final JsoupConnection connection;
    private final FindLemmas lemmaFinder;

    @SneakyThrows
    @Override
    public boolean startIndexing() {
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


    @Override
    @SneakyThrows
    public boolean indexingPage(String urlPage) {
        if (isPageAvailable(urlPage)) {
            executorService = Executors.newSingleThreadExecutor();
            executorService.submit(new IndexingOnePage(siteRepository, pageRepository,
                    lemmaRepository, indexRepository, sitesList, pathFromUrl, lemmaFinder, connection, urlPage));
            executorService.awaitTermination(10, TimeUnit.MINUTES);
            return true;
        } else {
            return false;
        }
    }

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
