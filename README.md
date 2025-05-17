# Real-time_Books_Recommendation_System
## Prerequisites (cluster setup, dependencies)

Most of the components used in the project, such as Kafka, Redis, MongoDB, Azkaban, Spark, and Flume, are containerized and deployed using Docker. Please refer to the docker-compose file below for specific configurations. 

During actual deployment, some image versions may need to be adjusted based on the architecture of the physical machine(For example, choose a different version of the image depending on the window or mac chip.).

In addition, you need to pay attention to the port configured in dockercompose, whether it is occupied, if it is occupied, the components in the container will not be successfully mapped to the host machine

The last, java version is 1.8 and the scala version is 2.11.8.



## Build & run commands

First, execute the docker-compose.yml file located in the project root directory. The frontend and backend components (businessServer) and their dependencies will be automatically built and deployed as container.

Before running the recommendation module, a user and the recommender database need to be created inside the MongoDB container. Use the following commands:

```
mongo -u root -p example --authenticationDatabase admin
use recommender
switched to db recommender
> db.createUser({
...   user: "root",
...   pwd: "example",
...   roles: [{ role: "readWrite", db: "recommender" }]
... })
```



### Offline Modules

Next, run the **DataLoader** module to load user ratings and book datasets into MongoDB. Then, execute the four offline recommendation modules one by one: **StatisticsRecommender**, **ContentRecommender**, **ItemCFRecommender**, and **OfflineRecommender**, to generate the corresponding recommendation data from different algorithms.![img](https://lh7-rt.googleusercontent.com/docsz/AD_4nXeZIe91GLD8hQrqsJqORhC1e167M3hB8gRhZszYFDNT9j_wPtZSyEnyTgLV40c7wbGPuQJICCvjZiI0IVdr_VpynRjLl9O3xSjjKHmefFu0Y27K9PRYeXuJwWix6hWzNqWC4QyJyA?key=uUGYfvp8LzmhFXRkoX8qG9pB)

Among them, **OfflineRecommender** provides personalized recommendations based on user rating data. If the currently logged-in user has no historical ratings, no recommendations will be shown. To automatically refresh personalized recommendation data, **Azkaban** is used to schedule the **OfflineRecommender** module as a periodic task. The configuration steps are as follows:

Create project:

![img](https://lh7-rt.googleusercontent.com/docsz/AD_4nXfGiHdq4HkoKiFhhPGyF4N3cFU2QaRQNiqCHdO-qH5DIxqQ_erWMlPiXwHxl07huGO4NlK-2XrtFFw997KJ7XI0JQGEft2wkhVoCho4EPDp3U5sLkgfsJE4HoCzSWNf_u_biHSo?key=uUGYfvp8LzmhFXRkoX8qG9pB)

Create 2 job files as following

```
Azkaban-stat.job:

type=command****command=/home/bigdata/cluster/spark-2.1.1-bin-hadoop2.7/bin/spark-submit --class com.atguigu.offline.RecommenderTrainerApp** **offlineRecommender-1.0-SNAPSHOT.jar

Azkaban-offline.job:

type=command****command=/home/bigdata/cluster/spark-2.1.1-bin-hadoop2.7/bin/spark-submit --class com.atguigu.statisticsRecommender.StatisticsApp** **statisticsRecommender-1.0-SNAPSHOT.jar
```

Packaging files for uploading azkaban![img](https://lh7-rt.googleusercontent.com/docsz/AD_4nXf4VUhhtfW3alZGQACiHt-apTBWYY5CUQEpHzQOFhvfpGf7QfEdKjPNOynhQLMBCQQ9lkrOTejy0UNBhh5otixQq5BiN8hcV_oQGdSsupRM8-j_OMxB2T-buiuI5A7Ua1E2ViCgXg?key=uUGYfvp8LzmhFXRkoX8qG9pB)

![img](https://lh7-rt.googleusercontent.com/docsz/AD_4nXd6h8wIfTQvXEfdrjwc_6H0wKiK_1lAtsGlCoRj9mH_mDe9JzUHLsdV7VovWcGRGnlMSdTZAzPTuoBH5Vxk7rFvCkjalrfrZDZstvBIQ0KY6Oq7X6u9tTPs2iyUiAhSInuUudbYMg?key=uUGYfvp8LzmhFXRkoX8qG9pB)

Setting the time for each timed task![img](https://lh7-rt.googleusercontent.com/docsz/AD_4nXfdqX59Dep1jDuVRJqByP1W98ThfbGCuaYZIBzdn7dwLLv-HjpHv761q5wPobQAlnkSJv04glkwtOimX7W1mlCJ_waLsUqE58Y_cGHpFur7uChKDiB7g0jRdivH50EGf5M5JYE6KA?key=uUGYfvp8LzmhFXRkoX8qG9pB)



### Online Modules

Run KafkaStreaming, OnlineRecommender modules respectively and user's implementation actions can be captured. Subsequently the recommended products are calculated and StreamRecs table is generated.



### Remark

The data for real-time recommendations and offline recommendations requires that the logged in user has previously scored books in order to generate recommendation data. You can log out and log in again to refresh the display data



## Directory structure

```Shell
.
├── LICENSE
├── README.md
├── businessServer
│   ├── Dockerfile
│   ├── pom.xml
│   └── src
│       └── main
│           ├── java
│           │   └── com
│           │       └── iss
│           │           └── business
│           │               ├── Application.java
│           │               ├── model
│           │               │   ├── domain
│           │               │   │   ├── Product.java
│           │               │   │   ├── Rating.java
│           │               │   │   └── User.java
│           │               │   ├── recom
│           │               │   │   └── Recommendation.java
│           │               │   └── request
│           │               │       ├── ContentBasedRecommendationRequest.java
│           │               │       ├── HotRecommendationRequest.java
│           │               │       ├── ItemCFRecommendationRequest.java
│           │               │       ├── LoginUserRequest.java
│           │               │       ├── ProductRatingRequest.java
│           │               │       ├── ProductRecommendationRequest.java
│           │               │       ├── RateMoreRecommendationRequest.java
│           │               │       ├── RegisterUserRequest.java
│           │               │       └── UserRecommendationRequest.java
│           │               ├── package-info.java
│           │               ├── rest
│           │               │   ├── ProductRestApi.java
│           │               │   └── UserRestApi.java
│           │               ├── service
│           │               │   ├── ProductService.java
│           │               │   ├── RatingService.java
│           │               │   ├── RecommenderService.java
│           │               │   └── UserService.java
│           │               └── utils
│           │                   ├── Configure.java
│           │                   └── Constant.java
│           ├── log
│           │   └── agent.log
│           ├── resources
│           │   ├── agent.log
│           │   ├── application.xml
│           │   ├── log4j.properties
│           │   ├── log4j2.xml
│           │   └── recommend.properties
│           └── webapp	# Packaged Service
├── docker-compose.yml	# Configuration and startup file
├── flume-custom
│   ├── Dockerfile
│   ├── flume.conf
│   └── log4j.properties
├── pom.xml
├── recommender	# Big Data Module
│   ├── ContentRecommender
│   │   ├── pom.xml
│   │   ├── src
│   │   │   └── main
│   │   │       ├── resources
│   │   │       │   └── log4j.properties
│   │   │       └── scala
│   │   │           └── com
│   │   │               └── iss
│   │   │                   └── content
│   │   │                       └── ContentRecommender.scala
│   ├── DataLoader	# First boot to load data
│   │   ├── pom.xml
│   │   ├── src
│   │   │   └── main
│   │   │       ├── resources
│   │   │       │   ├── log4j.properties
│   │   │       │   ├── products_cleaned.csv
│   │   │       │   └── ratings.csv
│   │   │       └── scala
│   │   │           └── com
│   │   │               └── iss
│   │   │                   └── recommender
│   │   │                       └── DataLoader.scala
│   ├── ItemCFRecommender
│   │   ├── pom.xml
│   │   ├── src
│   │   │   └── main
│   │   │       └── scala
│   │   │           └── com
│   │   │               └── iss
│   │   │                   └── itemcf
│   │   │                       └── ItemCFRecommender.scala
│   ├── KafkaStreaming
│   │   ├── pom.xml
│   │   ├── src
│   │   │   └── main
│   │   │       ├── java
│   │   │       │   └── com.iss.kafkastream
│   │   │       │       ├── Application.java
│   │   │       │       └── LogProcessor.java
│   │   │       └── resources
│   │   │           └── log4j.properties
│   ├── OfflineRecommender
│   │   ├── pom.xml
│   │   ├── src
│   │   │   └── main
│   │   │       ├── resources
│   │   │       │   └── log4j.properties
│   │   │       └── scala
│   │   │           └── com
│   │   │               └── iss
│   │   │                   └── offline
│   │   │                       ├── ALSTrainer.scala
│   │   │                       └── OfflineRecommender.scala
│   ├── OnlineRecommender
│   │   ├── pom.xml
│   │   ├── src
│   │   │   └── main
│   │   │       ├── resources
│   │   │       │   └── log4j.properties
│   │   │       └── scala
│   │   │           └── com
│   │   │               └── iss
│   │   │                   └── online
│   │   │                       └── OnlineRecommender.scala
│   ├── StatisticsRecommender
│   │   ├── pom.xml
│   │   ├── src
│   │   │   └── main
│   │   │       ├── resources
│   │   │       │   └── log4j.properties
│   │   │       └── scala
│   │   │           └── com
│   │   │               └── iss
│   │   │                   └── statistics
│   │   │                       └── StatisticsRecommender.scala
│   └── pom.xml
└── spark-warehouse
```



## Contact (team’s email)

**Hao Dawei      e1349303@u.nus.edu**
**Zhou Kangkai   e1349686@u.nus.edu**
**Zhu Chengxuan  e1351252@u.nus.edu**
**Zeng Guang    e1349186@u.nus.edu**
