package searchengine.util;

import lombok.RequiredArgsConstructor;
import org.jsoup.Connection;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import searchengine.config.JsoupConnection;
import searchengine.dto.PagesData;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.RecursiveTask;

// получение данный со страниц, рекурсивный обход сайтов
@RequiredArgsConstructor
public class GetPagesData extends RecursiveTask<CopyOnWriteArrayList<PagesData>> {
    private final CopyOnWriteArrayList<String> linksPool;
    private final CopyOnWriteArrayList<PagesData> pagesData;
    private final PathFromUrl pathFromUrl;
    private final String siteUrl;
    private final JsoupConnection connection; //

    @Override
    public CopyOnWriteArrayList<PagesData> compute() {
        linksPool.add(siteUrl);
        try {
            Thread.sleep(100);
            Document document = connection.getConnection(siteUrl);
            Connection.Response response = document.connection().response();
            int code = response.statusCode();
            String htmlContent = document.outerHtml();
            PagesData dtoPage = new PagesData(siteUrl, code, htmlContent);
            pagesData.add(dtoPage);
            List<GetPagesData> tasks = new ArrayList<>();
            Elements elements = document.select("body").select("a");
            for (Element element : elements) {
                String link = element.absUrl("href");
                if (isCorrectLink(link) && link.startsWith(element.baseUri())
                        && !linksPool.contains(link)) {
                    linksPool.add(link);
                    GetPagesData task = new GetPagesData(linksPool, pagesData, pathFromUrl, link, connection); //
                    task.fork();
                    tasks.add(task);
                }
            }
            for (GetPagesData task : tasks) {
                task.join();
            }
        } catch (Exception e) {
            PagesData newPagesData = new PagesData(siteUrl, 500, "INTERNAL SERVER ERROR");
            pagesData.add(newPagesData);
        }
        return pagesData;
    }

    // проверка на корректность
    public static boolean isCorrectLink(String url) {
        return isCorrectUrl(url) && !isFile(url);
    }

    private static boolean isCorrectUrl(String url) {
        return url.matches("^https?://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]");
    }

    private static boolean isFile(String url) {
        return url.contains(".jpg")
                || url.contains(".jpeg")
                || url.contains(".png")
                || url.contains(".gif")
                || url.contains(".webp")
                || url.contains(".pdf")
                || url.contains(".eps")
                || url.contains(".xlsx")
                || url.contains(".doc")
                || url.contains(".pptx")
                || url.contains(".docx")
                || url.contains("#")
                || url.contains("?_ga");
    }

}

