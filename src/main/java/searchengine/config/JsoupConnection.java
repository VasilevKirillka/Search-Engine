package searchengine.config;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

// получение данный со страницы
@Component
@Getter
@Setter
@RequiredArgsConstructor
@ConfigurationProperties(prefix = "jsoup-connection")
public class JsoupConnection {
    private String userAgent;
    private String referrer;

    public Document getConnection(String url) {
        try {
            return Jsoup.connect(url)
                    .userAgent(userAgent)
                    .referrer(referrer)
                    .get();
        } catch (Exception e) {
            return null;
        }
    }
}
