package gov.tna.discovery.taxonomy.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@ConfigurationProperties(prefix = "async.executor")
@EnableConfigurationProperties
public class AsyncConfiguration {

    private Integer corePoolSize;

    private Integer maxPoolSize;

    private Integer queueCapacity;

    public Integer getCorePoolSize() {
	return corePoolSize;
    }

    public void setCorePoolSize(Integer corePoolSize) {
	this.corePoolSize = corePoolSize;
    }

    public Integer getMaxPoolSize() {
	return maxPoolSize;
    }

    public void setMaxPoolSize(Integer maxPoolSize) {
	this.maxPoolSize = maxPoolSize;
    }

    public Integer getQueueCapacity() {
	return queueCapacity;
    }

    public void setQueueCapacity(Integer queueCapacity) {
	this.queueCapacity = queueCapacity;
    }

    // TODO think how to use threadPools for categorisation of several docs,
    // running the queries for cat, user requests, etc
    /**
     * See
     * {@link org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor}
     * 
     * @return
     */
    public @Bean ThreadPoolTaskExecutor threadPoolTaskExecutor() {
	ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
	executor.setCorePoolSize(corePoolSize);
	executor.setMaxPoolSize(maxPoolSize);
	executor.setQueueCapacity(queueCapacity);
	executor.setThreadNamePrefix("MyExecutor-");
	executor.initialize();
	return executor;
    }

}