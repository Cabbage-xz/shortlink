package org.cabbage.shortlink.admin.config;

import org.cabbage.shortlink.admin.common.biz.user.UserFlowRiskControlFilter;
import org.cabbage.shortlink.admin.common.biz.user.UserTransmitFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * @author xzcabbage
 * @since 2025/9/21
 */
@Configuration
public class UserConfiguration {

    @Bean
    public FilterRegistrationBean<UserTransmitFilter> globalUserTransmitFilter() {
        FilterRegistrationBean<UserTransmitFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new UserTransmitFilter());
        registration.addUrlPatterns("/*");
        registration.setOrder(0);
        return registration;
    }

    @Bean
    public FilterRegistrationBean<UserFlowRiskControlFilter> globalUserFlowRiskControlFilter(StringRedisTemplate stringRedisTemplate,
                                                                                             UserFlowRiskControlConfiguration userFlowRiskControlConfiguration) {
        FilterRegistrationBean<UserFlowRiskControlFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new UserFlowRiskControlFilter(stringRedisTemplate, userFlowRiskControlConfiguration));
        registration.addUrlPatterns("/*");
        registration.setOrder(10);
        return registration;
    }
}
