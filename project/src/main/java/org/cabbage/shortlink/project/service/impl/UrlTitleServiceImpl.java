package org.cabbage.shortlink.project.service.impl;

import lombok.SneakyThrows;
import org.cabbage.shortlink.project.service.UrlTitleService;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;

import java.net.HttpURLConnection;
import java.net.URL;

/**
 * @author xzcabbage
 * @since 2025/10/3
 * url标题接口实现层
 */
@Service
public class UrlTitleServiceImpl implements UrlTitleService {

    /**
     * 依据url获取网站标题
     * @param url url
     * @return 网站标题
     */
    @SneakyThrows
    @Override
    public String getTitleByUrl(String url) {
        URL targetUrl  = new URL(url);
        HttpURLConnection httpURLConnection = (HttpURLConnection) targetUrl.openConnection();
        httpURLConnection.setRequestMethod("GET");
        httpURLConnection.connect();
        int responseCode = httpURLConnection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            Document document = Jsoup.connect(url).get();
            return document.title();
        }
        return "Error while fetching title";
    }
}
