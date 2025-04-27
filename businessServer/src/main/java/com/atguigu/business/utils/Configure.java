package com.atguigu.business.utils;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import redis.clients.jedis.Jedis;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Properties;

@Configuration
public class Configure {

    private String mongoHost;
    private int mongoPort;
    private String esClusterName;
    private String esHost;
    private int esPort;
    private String redisHost;

    public Configure(){
        try{
            Properties properties = new Properties();
            Resource resource = new ClassPathResource("recommend.properties");
            properties.load(new FileInputStream(resource.getFile()));
            // 优先从环境变量获取Mongo配置
            String envMongoHost = System.getenv("SPRING_DATA_MONGODB_HOST");
            String envMongoPort = System.getenv("SPRING_DATA_MONGODB_PORT");
            this.mongoHost = properties.getProperty("mongo.host");
            this.mongoPort = Integer.parseInt(properties.getProperty("mongo.port"));
            // 覆盖为环境变量
            if (envMongoHost != null && !envMongoHost.isEmpty()) this.mongoHost = envMongoHost;
            if (envMongoPort != null && !envMongoPort.isEmpty()) this.mongoPort = Integer.parseInt(envMongoPort);
            // 优先从环境变量获取Redis配置
            String envRedisHost = System.getenv("SPRING_REDIS_HOST");
            this.redisHost = properties.getProperty("redis.host");
            if (envRedisHost != null && !envRedisHost.isEmpty()) this.redisHost = envRedisHost;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.exit(0);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(0);
        }
    }

    @Bean(name = "mongoClient")
    public MongoClient getMongoClient() {
        String uri = String.format(
            "mongodb://root:example@%s:%d/recommender?authSource=admin",
            mongoHost,
            mongoPort
        );
        System.out.println("MongoDB URI: " + uri);
        return new MongoClient(new MongoClientURI(uri));
    }

    @Bean(name = "transportClient")
    public TransportClient getTransportClient() throws UnknownHostException {
        Settings settings = Settings.builder().put("cluster.name",esClusterName).build();
        TransportClient esClient = new PreBuiltTransportClient(settings);
        esClient.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(esHost), esPort));
        return esClient;
    }

    @Bean(name = "jedis")
    public Jedis getRedisClient() {
        Jedis jedis = new Jedis(redisHost);
        return jedis;
    }
}
