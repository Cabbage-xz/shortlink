package org.cabbage.shortlink.project.service;

/**
 * @author xzcabbage
 * @since 2025/10/3
 * url标题接口层
 */
public interface UrlTitleService {
    /**
     * 依据url获取网站标题
     * @param url url
     * @return 网站标题
     */
    String getTitleByUrl(String url);
}
