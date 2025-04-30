FROM probablyfine/flume

# 1）把配置文件复制到镜像里
COPY flume.conf /opt/flume-conf/flume.conf
COPY log4j.properties /opt/flume-conf/log4j.properties

# 2）指定 Agent 名称，必须和 flume.conf 里所有前缀一致
ENV FLUME_AGENT_NAME agent

# 3）Shell 形式启动，保证 $FLUME_AGENT_NAME 被展开
ENTRYPOINT exec /opt/flume/bin/flume-ng agent \
            --conf /opt/flume-conf \
            --conf-file /opt/flume-conf/flume.conf \
            -n $FLUME_AGENT_NAME \
            -Dflume.root.logger=INFO,console \
            -Dorg.apache.kafka=DEBUG