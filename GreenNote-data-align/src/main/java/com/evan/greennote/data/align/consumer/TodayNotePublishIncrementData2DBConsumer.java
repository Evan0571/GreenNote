package com.evan.greennote.data.align.consumer;

import com.evan.framework.common.util.JsonUtils;
import com.evan.greennote.data.align.constant.MQConstants;
import com.evan.greennote.data.align.constant.RedisKeyConstants;
import com.evan.greennote.data.align.constant.TableConstants;
import com.evan.greennote.data.align.domain.mapper.InsertMapper;
import com.evan.greennote.data.align.model.dto.NoteOperateMqDTO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;

//日增量数据库：笔记发布/删除
@Component
@RocketMQMessageListener(
        consumerGroup = "greennote_group_data_align_" + MQConstants.TOPIC_NOTE_OPERATE,
        topic = MQConstants.TOPIC_NOTE_OPERATE
)
@Slf4j
public class TodayNotePublishIncrementData2DBConsumer implements RocketMQListener<String> {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private InsertMapper insertMapper;

    @Value("${table.shards}")
    private int tableShards;

    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("yyyyMMdd");

    @Override
    public void onMessage(String body) {
        log.info("## TodayNotePublishIncrementData2DBConsumer 消费到了MQ: {}", body);

        NoteOperateMqDTO dto = JsonUtils.parseObject(body, NoteOperateMqDTO.class);
        if (dto == null) {
            log.warn("## 反序列化结果为空, body={}", body);
            return;
        }

        // 发布 / 删除的笔记发布者 ID
        Long noteCreatorId = dto.getCreatorId();
        if (noteCreatorId == null) {
            log.warn("## creatorId 为空, dto={}", dto);
            return;
        }

        // 今日日期
        String date = LocalDate.now().format(DATE_FMT);

        String bloomKey = RedisKeyConstants.buildBloomUserNoteOperateListKey(date);

        // 1. 布隆过滤器判断该日增量数据是否已经记录
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setScriptSource(new ResourceScriptSource(
                new ClassPathResource("/lua/bloom_today_user_note_publish_check.lua")));
        script.setResultType(Long.class);

        Long result = redisTemplate.execute(
                script,
                Collections.singletonList(bloomKey),
                noteCreatorId
        );
        log.info("## 发布布隆检查结果, key={}, userId={}, result={}",
                bloomKey, noteCreatorId, result);

        // ====== 调试期：只要不是“确定已经存在”(1)，一律当成需要插库 ======
        if (result == null || result <= 0L) {
            long userIdHashKey = Math.floorMod(noteCreatorId, tableShards);
            String tableNameSuffix = TableConstants.buildTableNameSuffix(date, userIdHashKey);

            log.info("## 准备写入发布日增量表, suffix={}, userId={}",
                    tableNameSuffix, noteCreatorId);

            insertMapper.insert2DataAlignUserNotePublishCountTempTable(
                    tableNameSuffix, noteCreatorId);

            // 写库成功后，再把 userId 加到布隆过滤器
            RedisScript<Long> bloomAddScript = RedisScript.of(
                    "return redis.call('BF.ADD', KEYS[1], ARGV[1])",
                    Long.class);
            redisTemplate.execute(bloomAddScript,
                    Collections.singletonList(bloomKey),
                    noteCreatorId);

            log.info("## 发布日增量表写入完成, suffix={}, userId={}",
                    tableNameSuffix, noteCreatorId);
        } else {
            log.info("## 布隆判断已存在, 本次不再写库, userId={}", noteCreatorId);
        }
    }
}

