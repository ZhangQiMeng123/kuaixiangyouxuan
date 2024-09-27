package com.hmdp.config;

import com.hmdp.properties.AliossProperties;
import com.hmdp.utils.AliOssUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 配置类，用于创建AliOssUtil对象
 */
@Configuration
@Slf4j
public class OssConfiguration {
    @Bean
    @ConditionalOnMissingBean
    public AliOssUtil aliOssUtil(AliossProperties aliossProperties){
        log.info("开始创建阿里云文件上传工具类对象：{}",aliossProperties);
        return new AliOssUtil(aliossProperties.getEndpoint(),
                aliossProperties.getAccessKeyId(),
                aliossProperties.getAccessKeySecret(),
                aliossProperties.getBucketName());
    }
}
