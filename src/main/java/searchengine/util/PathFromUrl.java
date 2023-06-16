package searchengine.util;

import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Component;

import java.net.MalformedURLException;
import java.net.URL;
// получение пути страницы и текста заголовка
@Component
@RequiredArgsConstructor
public class PathFromUrl {
    public String getPathToPage(String page) {
        try {
            URL url = new URL(page);
            return url.getPath();
        } catch (MalformedURLException e) {
            return "";
        }
    }

    public String getHostFromPage(String page) {
        try {
            URL url = new URL(page);
            return url.getHost();
        } catch (MalformedURLException e) {
            return "";
        }
    }

    public String getTitleFromHtml(String content) {
        Document doc = Jsoup.parse(content);
        return doc.title();
    }
}
