package com.skycanvas.video;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 视频Provider工厂（策略模式）
 */
@Component
public class VideoProviderFactory {

    @Autowired
    private Map<String, VideoGenerationService> providers;

    @Value("${video.provider.default}")
    private String defaultProvider;

    /**
     * 获取当前激活的provider
     */
    public VideoGenerationService getProvider() {
        return getProvider(defaultProvider);
    }

    /**
     * 获取指定的provider（支持动态切换）
     */
    public VideoGenerationService getProvider(String providerName) {
        String beanName = providerName + "Provider";
        VideoGenerationService provider = providers.get(beanName);
        if (provider == null) {
            throw new RuntimeException("Provider not found: " + providerName);
        }
        return provider;
    }

    /**
     * 获取所有可用provider
     */
    public List<String> getAvailableProviders() {
        return providers.keySet().stream()
                .map(key -> key.replace("Provider", ""))
                .collect(Collectors.toList());
    }

    /**
     * 检查provider是否可用
     */
    public boolean isProviderAvailable(String providerName) {
        try {
            VideoGenerationService provider = getProvider(providerName);
            return provider.healthCheck();
        } catch (Exception e) {
            return false;
        }
    }
}

