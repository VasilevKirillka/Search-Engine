package searchengine.util;

import org.apache.lucene.morphology.russian.RussianLuceneMorphology;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class FindLemmas {
    private static RussianLuceneMorphology russianLuceneMorphology;
    private static final Pattern HTML_TAG = Pattern.compile("<[^>]*>");

    static {
        try {
            russianLuceneMorphology = new RussianLuceneMorphology();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public HashMap<String, Integer> collectLemmas(String text) {
        text = clearText(text);
        HashMap<String, Integer> lemmaList = new HashMap<>();

        String[] elements = text.toLowerCase(Locale.ROOT).split("\\s+");
        for (String element : elements) {
            List<String> wordsList = getLemma(element);
            for (String word : wordsList) {
                int count = lemmaList.getOrDefault(word, 0);
                lemmaList.put(word, count + 1);
            }
        }
        return lemmaList;
    }

    public List<String> getLemma(String word) {
        List<String> lemmaList = new ArrayList<>();
        if (isRussianWord(word)) {
            List<String> lemmaForms = russianLuceneMorphology.getNormalForms(word);
            if (!isServiceWord(word) && !word.isEmpty()) {
                lemmaList.addAll(lemmaForms);
            }
        }
        return lemmaList;
    }

    public List<Integer> findLemmaIndexInText(String text, String lemma) {
        List<Integer> lemmaIndexList = new ArrayList<>();
        String[] elements = text.toLowerCase(Locale.ROOT).split("\\p{Punct}|\\s");
        int index = 0;
        for (String element : elements) {
            List<String> lemmas = getLemma(element);
            for (String lem : lemmas) {
                if (lem.equals(lemma)) {
                    lemmaIndexList.add(index);
                    break;  // ?
                }
            }
            index += element.length() + 1;
        }
        return lemmaIndexList;
    }

    private String clearText(String text) {
        return text.toLowerCase(Locale.ROOT)
                .replaceAll("([^а-я\\s])", " ")
                .trim();
    }

    // проверка на рус.язык
    private boolean isRussianWord(String word) {
        String regex = "[а-яА-Я]+";
        return word.matches(regex);
    }

    private boolean isServiceWord(String word) {
        List<String> morphForm = russianLuceneMorphology.getMorphInfo(word);
        for (String element : morphForm) {
            if (element.contains("ПРЕДЛ")
                    || element.contains("СОЮЗ")
                    || element.contains("МЕЖД")
                    || element.contains("МС")
                    || element.contains("ЧАСТ")
                    || element.length() <= 3) {
                return true;
            }
        }
        return false;
    }

    public String removeHtmlTags(String html) { // очищение от тегов
        Matcher matcher = HTML_TAG.matcher(html);
        StringBuilder plainText = new StringBuilder();
        int last = 0;
        while (matcher.find()) {
            plainText.append(html, last, matcher.start());
            last = matcher.end();
        }
        plainText.append(html.substring(last));
        return plainText.toString();
    }
}
