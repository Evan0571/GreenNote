# GreenNote

GreenNote 是一个基于 **Spring Boot 3 + Spring Cloud Alibaba** 的微服务后端项目。系统通过 **Nacos** 做注册发现，**Gateway** 统一入口，覆盖认证、用户、笔记、关系、评论、搜索、计数、对象存储等能力。

## 技术栈

- Java 17
- Spring Boot 3.0.2
- Spring Cloud 2022.0.0 / Spring Cloud Alibaba 2022.0.0.0
- MyBatis + MySQL + Druid
- Redis
- RocketMQ
- Elasticsearch + Canal
- MinIO / Aliyun OSS
- Sa-Token

## 项目结构

```text
GreenNote
├── GreenNote-auth                    # 认证服务
├── GreenNote-gateway                 # API 网关
├── GreenNote-user                    # 用户服务（api + biz）
├── GreenNote-note                    # 笔记服务（api + biz）
├── GreenNote-user-relation           # 关注/粉丝关系服务（api + biz）
├── GreenNote-comment                 # 评论服务（api + biz）
├── GreenNote-count                   # 计数服务（api + biz）
├── GreenNote-search                  # 搜索服务（api + biz）
├── GreenNote-oss                     # 文件/对象存储服务（api + biz）
├── GreenNote-kv                      # KV 服务（api + biz）
├── GreenNote-distributed-id-generator# 分布式 ID 服务（api + biz）
├── GreenNote-data-align              # 数据对齐任务服务
└── GreenNote-framework               # 公共模块与自定义 starter
```

## 服务端口（默认）

| 服务 | 端口 |
| --- | --- |
| GreenNote-gateway | 8000 |
| GreenNote-auth | 8080 |
| GreenNote-oss | 8081 |
| GreenNote-user | 8082 |
| GreenNote-kv | 8084 |
| GreenNote-distributed-id-generator | 8085 |
| GreenNote-note | 8086 |
| GreenNote-user-relation | 8087 |
| GreenNote-count | 8090 |
| GreenNote-data-align | 8091 |
| GreenNote-search | 8092 |
| GreenNote-comment | 8093 |

## 网关路由（统一入口）

启动 `GreenNote-gateway` 后，请求前缀如下：

- `/auth/**` → `GreenNote-auth`
- `/user/**` → `GreenNote-user`
- `/note/**` → `GreenNote-note`
- `/relation/**` → `GreenNote-user-relation`
- `/comment/**` → `GreenNote-comment`
- `/search/**` → `GreenNote-search`
- `/count/**` → `GreenNote-count`
- `/file/**` → `GreenNote-oss`
- `/kv/**` → `GreenNote-kv`

---

## 本地运行必须中间件

> 建议全部通过 Docker 起，避免本机污染。

| 中间件 | 默认端口 | 用途 |
| --- | --- | --- |
| Nacos | 8848 | 服务注册/配置中心 |
| MySQL | 3306 | 业务主库（`greennote`） |
| Redis | 6379 | 缓存、会话、分布式能力 |
| RocketMQ NameServer/Broker | 9876 / 10911 | 消息队列 |
| Elasticsearch | 9200 | 搜索引擎 |
| Canal Server | 11111 | 订阅 MySQL Binlog 做索引同步 |
| MinIO | 9000 / 9001 | 对象存储 |
| XXL-Job Admin（可选） | 7777 | 数据对齐任务调度 |

---

## 用 Docker 一次性启动依赖

### 1）创建 Docker 网络

```bash
docker network create greennote-net
```

### 2）创建 `docker-compose.middleware.yml`

把下面文件保存到仓库根目录（用于本地开发）：

```yaml
version: "3.9"

services:
  mysql:
    image: mysql:8.0
    container_name: greennote-mysql
    restart: unless-stopped
    environment:
      MYSQL_ROOT_PASSWORD: root
      MYSQL_DATABASE: greennote
      TZ: Asia/Shanghai
    ports:
      - "3306:3306"
    command:
      --default-authentication-plugin=mysql_native_password
      --character-set-server=utf8mb4
      --collation-server=utf8mb4_unicode_ci
    volumes:
      - ./docker-data/mysql:/var/lib/mysql
    networks:
      - greennote-net

  redis:
    image: redis:7
    container_name: greennote-redis
    restart: unless-stopped
    command: ["redis-server", "--appendonly", "yes", "--requirepass", "123456"]
    ports:
      - "6379:6379"
    volumes:
      - ./docker-data/redis:/data
    networks:
      - greennote-net

  nacos:
    image: nacos/nacos-server:v2.2.3
    container_name: greennote-nacos
    restart: unless-stopped
    environment:
      - MODE=standalone
      - SPRING_DATASOURCE_PLATFORM=mysql
      - MYSQL_SERVICE_HOST=mysql
      - MYSQL_SERVICE_PORT=3306
      - MYSQL_SERVICE_DB_NAME=nacos
      - MYSQL_SERVICE_USER=root
      - MYSQL_SERVICE_PASSWORD=root
      - NACOS_AUTH_ENABLE=true
      - NACOS_AUTH_IDENTITY_KEY=greennote
      - NACOS_AUTH_IDENTITY_VALUE=greennote
      - NACOS_AUTH_TOKEN=greennote-token
    ports:
      - "8848:8848"
      - "9848:9848"
    depends_on:
      - mysql
    networks:
      - greennote-net

  rocketmq-namesrv:
    image: apache/rocketmq:5.1.4
    container_name: greennote-rmq-namesrv
    restart: unless-stopped
    command: sh mqnamesrv
    ports:
      - "9876:9876"
    networks:
      - greennote-net

  rocketmq-broker:
    image: apache/rocketmq:5.1.4
    container_name: greennote-rmq-broker
    restart: unless-stopped
    command: sh mqbroker -n rocketmq-namesrv:9876 --enable-proxy
    ports:
      - "10911:10911"
      - "10909:10909"
    depends_on:
      - rocketmq-namesrv
    networks:
      - greennote-net

  elasticsearch:
    image: elasticsearch:7.17.17
    container_name: greennote-es
    restart: unless-stopped
    environment:
      - discovery.type=single-node
      - ES_JAVA_OPTS=-Xms512m -Xmx512m
      - xpack.security.enabled=false
    ports:
      - "9200:9200"
    volumes:
      - ./docker-data/es:/usr/share/elasticsearch/data
    networks:
      - greennote-net

  canal:
    image: canal/canal-server:v1.1.7
    container_name: greennote-canal
    restart: unless-stopped
    ports:
      - "11111:11111"
    environment:
      - canal.auto.scan=false
      - canal.destinations=example
      - canal.instance.master.address=mysql:3306
      - canal.instance.dbUsername=root
      - canal.instance.dbPassword=root
      - canal.instance.filter.regex=greennote\\..*
    depends_on:
      - mysql
    networks:
      - greennote-net

  minio:
    image: minio/minio:latest
    container_name: greennote-minio
    restart: unless-stopped
    environment:
      MINIO_ROOT_USER: minioadmin
      MINIO_ROOT_PASSWORD: minioadmin
    command: server /data --console-address ":9001"
    ports:
      - "9000:9000"
      - "9001:9001"
    volumes:
      - ./docker-data/minio:/data
    networks:
      - greennote-net

networks:
  greennote-net:
    external: true
```

### 3）启动 / 停止依赖容器

```bash
# 启动
docker compose -f docker-compose.middleware.yml up -d

# 查看状态
docker compose -f docker-compose.middleware.yml ps

# 停止
docker compose -f docker-compose.middleware.yml down
```

### 4）初始化配置建议（第一次启动后）

1. 在 MySQL 中创建业务库表结构（项目 SQL 脚本如果在其他仓库，请先导入）。
2. Nacos 中创建命名空间 `greennote`，并按各服务 `bootstrap.yml` / `application-*.yml` 填入配置。
3. 若启用搜索增量同步，请确认：
   - MySQL 已开启 binlog（ROW 模式）
   - Canal `destination=example`
   - `GreenNote-search` 的 `canal.*` 配置与容器一致。
4. 若启用文件上传，请将 OSS 服务配置改成你实际的 MinIO/OSS 凭证。

---

## 服务配置怎么改

配置文件主要在：

- 大多数服务：`<module>/src/main/resources/config/`
- 网关服务：`GreenNote-gateway/src/main/resources/`

重点检查：

- `spring.datasource.*`（MySQL 地址、账号、密码）
- `spring.data.redis.*`（Redis 地址、密码）
- `spring.cloud.nacos.*`（Nacos 地址与命名空间）
- `rocketmq.name-server`
- `elasticsearch.address`、`canal.*`（搜索服务）
- `storage.minio.*` 或阿里云 OSS 配置（对象存储）

---

## 项目构建与启动

### 1）编译

```bash
mvn clean install -DskipTests
```

### 2）推荐启动顺序

1. `GreenNote-distributed-id-generator`
2. `GreenNote-kv`
3. `GreenNote-user`
4. `GreenNote-auth`
5. `GreenNote-note`
6. `GreenNote-user-relation`
7. `GreenNote-comment`
8. `GreenNote-count`
9. `GreenNote-oss`
10. `GreenNote-search`
11. `GreenNote-data-align`（按需）
12. `GreenNote-gateway`

---

## 说明

- 本仓库是后端微服务，不包含前端工程。
- 生产环境请不要直接使用示例密码，建议通过配置中心和密钥管理统一治理。
