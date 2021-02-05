package com.rhb.istock;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Ticker;

@SpringBootApplication
@EnableCaching
@EnableScheduling
public class IstockApplication {

	public static void main(String[] args) {
		SpringApplication.run(IstockApplication.class, args);
	}
	
	@Bean
	public TaskScheduler taskScheduler(){
		ThreadPoolTaskScheduler taskScheduer = new ThreadPoolTaskScheduler();
		taskScheduer.setPoolSize(13);
		taskScheduer.setThreadNamePrefix("istock task");
		return taskScheduer;
	}
	
    //@Bean
    public CacheManager cacheManager(Ticker ticker) {
        CaffeineCache tushareDailyKdatasCache = buildCache("tushareDailyKdatas", ticker, 120);

        SimpleCacheManager manager = new SimpleCacheManager();
        manager.setCaches(Arrays.asList(tushareDailyKdatasCache));
        return manager;
    }
    
    private CaffeineCache buildCache(String name, Ticker ticker, int secondsToExpire) {
        return new CaffeineCache(name, Caffeine.newBuilder()
                    .expireAfterAccess(secondsToExpire, TimeUnit.SECONDS)
                    .maximumSize(133)
                    .ticker(ticker)
                    .build());
    }

    @Bean
    public Ticker ticker() {
        return Ticker.systemTicker();
    }

}
