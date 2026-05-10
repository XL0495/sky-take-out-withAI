package com.sky.config;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import dev.langchain4j.store.embedding.pinecone.PineconeEmbeddingStore;
import dev.langchain4j.store.embedding.pinecone.PineconeServerlessIndexConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class EmbeddingStoreConfig {

    @Value("${pinecone.enabled:true}")
    private boolean pineconeEnabled;

    @Value("${pinecone.api-key:}")
    private String pineconeApiKey;

    @Value("${pinecone.index-name}")
    private String indexName;

    @Value("${pinecone.cloud}")
    private String cloud;

    @Value("${pinecone.region}")
    private String region;

    /** 控制台已建好索引时设为 false，避免重复调创建接口导致 404；无索引首次部署可设为 true */
    @Value("${pinecone.create-index:true}")
    private boolean pineconeCreateIndex;

    @Bean
    public EmbeddingStore<TextSegment> embeddingStore(
            @Qualifier("qwenEmbeddingModel") EmbeddingModel embeddingModel) {
        if (!pineconeEnabled) {
            log.warn("pinecone.enabled=false，使用内存向量库（重启后清空，仅本地调试用）");
            return new InMemoryEmbeddingStore<>();
        }
        if (!StringUtils.hasText(pineconeApiKey)) {
            throw new IllegalStateException(
                    "已开启 Pinecone（pinecone.enabled=true），但未配置 API Key。"
                            + "请设置环境变量 PINECONE_API_KEY，或在对应 profile 的 yml 中配置 pinecone.api-key（勿提交密钥到版本库）。"
                            + "若已在「系统环境变量」里配置，请完全退出并重新打开 IDE 后再启动应用，否则进程读不到新变量。");
        }
        var builder = PineconeEmbeddingStore.builder()
                .apiKey(pineconeApiKey)
                .nameSpace("default")
                .index(indexName);
        if (pineconeCreateIndex) {
            log.info("Pinecone: 将尝试创建索引（不存在则创建） index={}, cloud={}, region={}, dimension={}",
                    indexName, cloud, region, embeddingModel.dimension());
            builder.createIndex(
                    PineconeServerlessIndexConfig.builder()
                            .cloud(cloud)
                            .region(region)
                            .dimension(embeddingModel.dimension())
                            .build());
        } else {
            log.info("Pinecone: 仅连接已有索引 index={}（pinecone.create-index=false）", indexName);
        }
        return builder.build();
    }
}
