package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.dto.statistics.DetailedStatisticsItem;
import searchengine.dto.statistics.StatisticsData;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.dto.statistics.TotalStatistics;
import searchengine.model.DBSites;
import searchengine.model.repository.LemmaRepository;
import searchengine.model.repository.PageRepository;
import searchengine.model.repository.SiteRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {

    private final PageRepository pageRepository;
    private final LemmaRepository lemmaRepository;
    private final SiteRepository siteRepository;

    private TotalStatistics getTotal() {
        long sites = siteRepository.count();
        long pages = pageRepository.count();
        long lemmas = lemmaRepository.count();
        return new TotalStatistics((int) sites, (int) pages, (int) lemmas, true);
    }

    private DetailedStatisticsItem getDetailed(DBSites site) {
        String url = site.getUrl();
        String name = site.getName();
        String status = site.getStatus().toString();
        LocalDateTime statusTime = site.getStatusTime();
        String error = site.getLastError();
        int pages = pageRepository.countBySiteId(site);
        int lemmas = lemmaRepository.countBySiteId(site);
        return new DetailedStatisticsItem(url, name, status, statusTime, error, pages, lemmas);
    }

    private List<DetailedStatisticsItem> getStatisticsData() {
        List<DBSites> sites = siteRepository.findAll();
        List<DetailedStatisticsItem> result = new ArrayList<>();
        for (DBSites site : sites) {
            DetailedStatisticsItem item = getDetailed(site);
            result.add(item);
        }
        return result;
    }


    @Override
    public StatisticsResponse getStatistics() {
        TotalStatistics total = getTotal();
        List<DetailedStatisticsItem> list = getStatisticsData();
        StatisticsData statistics = new StatisticsData(total, list);
        return new StatisticsResponse(true, statistics);
    }
}
