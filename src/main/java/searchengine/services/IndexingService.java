package searchengine.services;

public interface IndexingService {
    boolean startIndexing();
    boolean stopIndexing();
    boolean indexingPage(String url);
}
