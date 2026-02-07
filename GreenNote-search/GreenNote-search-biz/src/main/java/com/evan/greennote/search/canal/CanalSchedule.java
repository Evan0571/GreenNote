package com.evan.greennote.search.canal;

import com.alibaba.otter.canal.client.CanalConnector;
import com.alibaba.otter.canal.protocol.CanalEntry;
import com.alibaba.otter.canal.protocol.Message;
import com.evan.framework.common.enums.StatusEnum;
import com.evan.greennote.search.domain.mapper.SelectMapper;
import com.evan.greennote.search.enums.NoteStatusEnum;
import com.evan.greennote.search.enums.NoteVisibleEnum;
import com.evan.greennote.search.index.NoteIndex;
import com.evan.greennote.search.index.UserIndex;
import com.google.common.collect.Maps;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

//Canal 数据消费
@Component
@Slf4j
public class CanalSchedule implements Runnable {

    @Resource
    private CanalProperties canalProperties;
    @Resource
    private CanalConnector canalConnector;
    @Resource
    private RestHighLevelClient restHighLevelClient;
    @Resource
    private SelectMapper selectMapper;

    @Override
    @Scheduled(fixedDelay = 100) // 每隔 100ms 被执行一次
    public void run() {
        // 初始化批次 ID，-1 表示未开始或未获取到数据
        long batchId = -1;
        try {
            // 检查连接状态，必要时重连
            checkAndReconnect();
            
            // 从 canalConnector 获取批量消息，返回的数据量由 batchSize 控制，若不足，则拉取已有的
            Message message = canalConnector.getWithoutAck(canalProperties.getBatchSize());

            // 获取当前拉取消息的批次 ID
            batchId = message.getId();

            // 获取当前批次中的数据条数
            long size = message.getEntries().size();
            if (batchId == -1 || size == 0) {
                try {
                    // 拉取数据为空，休眠 1s, 防止频繁拉取
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            } else {
                // 如果当前批次有数据，处理这批次数据
                processEntry(message.getEntries());
            }

            // 对当前批次的消息进行 ack 确认，表示该批次的数据已经被成功消费
            canalConnector.ack(batchId);
        } catch (Exception e) {
            log.error("消费 Canal 批次数据异常, batchId: {}", batchId, e);
            
            // 区分不同类型的异常进行处理
            if (isConnectionException(e)) {
                log.warn("检测到连接异常，将在下次循环中自动重连");
                // 连接异常时不执行rollback，避免进一步的IO异常
            } else {
                try {
                    // 如果出现业务异常，需要进行数据回滚，以便重新消费这批次的数据
                    canalConnector.rollback(batchId);
                } catch (Exception rollbackEx) {
                    log.error("执行rollback时发生异常, batchId: {}", batchId, rollbackEx);
                }
            }
        }
    }

    //检查连接状态并在必要时重连
    private void checkAndReconnect() {
        try {
            // 简单的心跳检查 - 尝试获取少量数据来验证连接
            Message testMessage = canalConnector.getWithoutAck(1);
            if (testMessage.getId() != -1) {
                // 如果获取到了数据，需要ack确认
                canalConnector.ack(testMessage.getId());
            }
        } catch (Exception e) {
            log.warn("Canal连接异常，准备重连: {}", e.getMessage());
            reconnect();
        }
    }
    
    //重连Canal服务
    private synchronized void reconnect() {
        try {
            log.info("开始重新连接Canal服务...");
            
            // 断开现有连接
            try {
                canalConnector.disconnect();
            } catch (Exception e) {
                log.debug("断开现有连接时出现异常: {}", e.getMessage());
            }
            
            // 重新连接
            canalConnector.connect();
            canalConnector.subscribe(canalProperties.getSubscribe());
            canalConnector.rollback();
            
            log.info("Canal服务重连成功");
        } catch (Exception e) {
            log.error("Canal服务重连失败", e);
            // 可以在这里添加告警通知逻辑
        }
    }
    
    //判断是否为连接异常
    private boolean isConnectionException(Exception e) {
        return e instanceof java.io.IOException || 
               e.getCause() instanceof java.io.IOException ||
               e.getMessage() != null && (
                   e.getMessage().contains("连接") || 
                   e.getMessage().contains("connection") ||
                   e.getMessage().contains("网络"));
    }
    
    //处理这一批次数据
    private void processEntry(List<CanalEntry.Entry> entrys) throws Exception {
        // 循环处理批次数据
        for (CanalEntry.Entry entry : entrys) {
            // 只处理 ROWDATA 行数据类型的 Entry，忽略事务等其他类型
            if (entry.getEntryType() == CanalEntry.EntryType.ROWDATA) {
                // 获取事件类型（如：INSERT、UPDATE、DELETE 等等）
                CanalEntry.EventType eventType = entry.getHeader().getEventType();
                // 获取数据库名称
                String database = entry.getHeader().getSchemaName();
                // 获取表名称
                String table = entry.getHeader().getTableName();

                // 解析出 RowChange 对象，包含 RowData 和事件相关信息
                CanalEntry.RowChange rowChange = CanalEntry.RowChange.parseFrom(entry.getStoreValue());

                // 遍历所有行数据（RowData）
                for (CanalEntry.RowData rowData : rowChange.getRowDatasList()) {
                    // 获取行中所有列的最新值（AfterColumns）
                    List<CanalEntry.Column> columns =
                            (eventType == CanalEntry.EventType.DELETE)
                                    ? rowData.getBeforeColumnsList()
                                    : rowData.getAfterColumnsList();

                    // 将列数据解析为 Map，方便后续处理
                    Map<String, Object> columnMap = parseColumns2Map(columns);

                    log.info("EventType: {}, Database: {}, Table: {}, Columns: {}", eventType, database, table, columnMap);

                    // 处理事件
                    processEvent(columnMap, table, eventType);
                }
            }
        }
    }

    //将列数据解析为 Map
    private Map<String, Object> parseColumns2Map(List<CanalEntry.Column> columns) {
        Map<String, Object> map = Maps.newHashMap();
        columns.forEach(column -> {
            if (Objects.isNull(column)) return;
            map.put(column.getName(), column.getValue());
        });
        return map;
    }

    //处理事件
    private void processEvent(Map<String, Object> columnMap, String table, CanalEntry.EventType eventType) throws Exception {
        switch (table) {
            case "t_note" -> handleNoteEvent(columnMap, eventType); // 笔记表
            case "t_user" -> handleUserEvent(columnMap, eventType); // 用户表
            default -> log.warn("Table: {} not support", table);
        }
    }

    //处理笔记表事件
    private void handleNoteEvent(Map<String, Object> columnMap, CanalEntry.EventType eventType) throws Exception {
        Object idObj = columnMap.get("id");
        if (idObj == null) {
            log.warn("t_note event missing id. eventType={}, columnMap={}", eventType, columnMap);
            return;
        }
        Long noteId = Long.parseLong(idObj.toString());

        switch (eventType) {
            case INSERT -> syncNoteIndex(noteId);

            case UPDATE -> {
                Object statusObj = columnMap.get("status");
                Object visibleObj = columnMap.get("visible");
                if (statusObj == null || visibleObj == null) {
                    log.warn("t_note UPDATE missing status/visible. noteId={}, columnMap={}", noteId, columnMap);
                    return;
                }

                Integer status = Integer.parseInt(statusObj.toString());
                Integer visible = Integer.parseInt(visibleObj.toString());

                if (Objects.equals(status, NoteStatusEnum.NORMAL.getCode())
                        && Objects.equals(visible, NoteVisibleEnum.PUBLIC.getCode())) {
                    syncNoteIndex(noteId);
                } else if (Objects.equals(visible, NoteVisibleEnum.PRIVATE.getCode())
                        || Objects.equals(status, NoteStatusEnum.DELETED.getCode())
                        || Objects.equals(status, NoteStatusEnum.DOWNED.getCode())) {
                    deleteNoteDocument(String.valueOf(noteId));
                }
            }

            case DELETE -> deleteNoteDocument(String.valueOf(noteId));

            default -> log.warn("Unhandled event type for t_note: {}", eventType);
        }
    }

    //同步笔记索引
    private void syncNoteIndex(Long noteId) throws Exception {
        // 从数据库查询 Elasticsearch 索引数据
        List<Map<String, Object>> result = selectMapper.selectEsNoteIndexData(noteId,null);

        // 遍历查询结果，将每条记录同步到 Elasticsearch
        for (Map<String, Object> recordMap : result) {
            // 创建索引请求对象，指定索引名称
            IndexRequest indexRequest = new IndexRequest(NoteIndex.NAME);
            // 设置文档的 ID，使用记录中的主键 “id” 字段值
            indexRequest.id((String.valueOf(recordMap.get(NoteIndex.FIELD_NOTE_ID))));
            // 设置文档的内容，使用查询结果的记录数据
            indexRequest.source(recordMap);
            // 将数据写入 Elasticsearch 索引
            restHighLevelClient.index(indexRequest, RequestOptions.DEFAULT);
        }
    }

    //删除指定 ID 的文档
    private void deleteNoteDocument(String documentId) throws Exception {
        // 创建删除请求对象，指定索引名称和文档 ID
        DeleteRequest deleteRequest = new DeleteRequest(NoteIndex.NAME, documentId);
        // 执行删除操作，将指定文档从 Elasticsearch 索引中删除
        restHighLevelClient.delete(deleteRequest, RequestOptions.DEFAULT);
    }

    //处理用户表事件
    private void handleUserEvent(Map<String, Object> columnMap, CanalEntry.EventType eventType) throws Exception {
        // 获取用户 ID
        Long userId = Long.parseLong(columnMap.get("id").toString());

        // 不同的事件，处理逻辑不同
        switch (eventType) {
            case INSERT -> syncUserIndex(userId); // 记录新增事件
            case UPDATE -> { // 记录更新事件
                // 用户变更后的状态
                Integer status = Integer.parseInt(columnMap.get("status").toString());
                // 逻辑删除
                Integer isDeleted = Integer.parseInt(columnMap.get("is_deleted").toString());

                if (Objects.equals(status, StatusEnum.ENABLE.getValue())
                        && Objects.equals(isDeleted, 0)) { // 用户状态为已启用，并且未被逻辑删除
                    // 更新用户索引、笔记索引
                    syncNotesIndexAndUserIndex(userId);
                } else if (Objects.equals(status, StatusEnum.DISABLE.getValue()) // 用户状态为禁用
                        || Objects.equals(isDeleted, 1)) { // 被逻辑删除
                    // 删除用户文档
                    deleteUserDocument(String.valueOf(userId));
                }
            }
            default -> log.warn("Unhandled event type for t_user: {}", eventType);
        }
    }

    //同步用户索引
    private void syncUserIndex(Long userId) throws Exception {
        // 1. 同步用户索引
        List<Map<String, Object>> userResult = selectMapper.selectEsUserIndexData(userId);

        // 遍历查询结果，将每条记录同步到 Elasticsearch
        for (Map<String, Object> recordMap : userResult) {
            // 创建索引请求对象，指定索引名称
            IndexRequest indexRequest = new IndexRequest(UserIndex.NAME);
            // 设置文档的 ID，使用记录中的主键 “id” 字段值
            indexRequest.id((String.valueOf(recordMap.get(UserIndex.FIELD_USER_ID))));
            // 设置文档的内容，使用查询结果的记录数据
            indexRequest.source(recordMap);
            // 将数据写入 Elasticsearch 索引
            restHighLevelClient.index(indexRequest, RequestOptions.DEFAULT);
        }
    }

    //同步用户索引、笔记索引（可能是多条）
    private void syncNotesIndexAndUserIndex(Long userId) throws Exception {
        // 创建一个 BulkRequest
        BulkRequest bulkRequest = new BulkRequest();

        // 1. 用户索引
        List<Map<String, Object>> userResult = selectMapper.selectEsUserIndexData(userId);

        // 遍历查询结果，将每条记录同步到 Elasticsearch
        for (Map<String, Object> recordMap : userResult) {
            // 创建索引请求对象，指定索引名称
            IndexRequest indexRequest = new IndexRequest(UserIndex.NAME);
            // 设置文档的 ID，使用记录中的主键 “id” 字段值
            indexRequest.id((String.valueOf(recordMap.get(UserIndex.FIELD_USER_ID))));
            // 设置文档的内容，使用查询结果的记录数据
            indexRequest.source(recordMap);
            // 将每个 IndexRequest 加入到 BulkRequest
            bulkRequest.add(indexRequest);
        }

        // 2. 笔记索引
        List<Map<String, Object>> noteResult = selectMapper.selectEsNoteIndexData(null, userId);
        for (Map<String, Object> recordMap : noteResult) {
            // 创建索引请求对象，指定索引名称
            IndexRequest indexRequest = new IndexRequest(NoteIndex.NAME);
            // 设置文档的 ID，使用记录中的主键 “id” 字段值
            indexRequest.id((String.valueOf(recordMap.get(NoteIndex.FIELD_NOTE_ID))));
            // 设置文档的内容，使用查询结果的记录数据
            indexRequest.source(recordMap);
            // 将每个 IndexRequest 加入到 BulkRequest
            bulkRequest.add(indexRequest);
        }

        // 执行批量请求
        restHighLevelClient.bulk(bulkRequest, RequestOptions.DEFAULT);
    }

    //删除指定 ID 的用户文档
    private void deleteUserDocument(String documentId) throws Exception {
        // 创建删除请求对象，指定索引名称和文档 ID
        DeleteRequest deleteRequest = new DeleteRequest(UserIndex.NAME, documentId);
        // 执行删除操作，将指定文档从 Elasticsearch 索引中删除
        restHighLevelClient.delete(deleteRequest, RequestOptions.DEFAULT);
    }
}