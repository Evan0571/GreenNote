package com.evan.greennote.data.align.job;

import com.evan.greennote.data.align.constant.TableConstants;
import com.evan.greennote.data.align.domain.mapper.CreateTableMapper;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

//定时任务：自动创建/清理日增量计数变更表
@Component
@RefreshScope
public class CreateTableXxlJob {

    // 表总分片数
    @Value("${table.shards}")
    private int tableShards;

    @Resource
    private CreateTableMapper createTableMapper;

    // 统一的日期格式：yyyyMMdd
    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyyMMdd");

    //创建日增量表的定时任务
    @XxlJob("createTableJobHandler")
    public void createTableJobHandler() throws Exception {

        // 从 XXL-Job 控制台拿参数（可选）
        String param = XxlJobHelper.getJobParam();

        LocalDate targetDate;
        if (param != null && !param.isBlank()) {
            try {
                targetDate = LocalDate.parse(param, FORMATTER);
            } catch (Exception e) {
                XxlJobHelper.log("任务参数 [{}] 不是合法日期，改用明天日期", param);
                targetDate = LocalDate.now().plusDays(1);
            }
        } else {
            targetDate = LocalDate.now().plusDays(1);
        }

        String date = targetDate.format(FORMATTER);
        XxlJobHelper.log("## 开始创建日增量数据表，日期: {}...", date);

        if (tableShards > 0) {
            for (int hashKey = 0; hashKey < tableShards; hashKey++) {
                // 表名后缀：yyyyMMdd_分片号
                String tableNameSuffix = TableConstants.buildTableNameSuffix(date, hashKey);
                // 创建各类日增量临时表
                createTableMapper.createDataAlignFollowingCountTempTable(tableNameSuffix);
                createTableMapper.createDataAlignFansCountTempTable(tableNameSuffix);
                createTableMapper.createDataAlignNoteCollectCountTempTable(tableNameSuffix);
                createTableMapper.createDataAlignUserCollectCountTempTable(tableNameSuffix);
                createTableMapper.createDataAlignUserLikeCountTempTable(tableNameSuffix);
                createTableMapper.createDataAlignNoteLikeCountTempTable(tableNameSuffix);
                createTableMapper.createDataAlignNotePublishCountTempTable(tableNameSuffix);
            }
        }

        XxlJobHelper.log("## 结束创建日增量数据表，日期: {}...", date);
    }

    //清理过期日增量临时表的定时任务
    @XxlJob("cleanTempTableJobHandler")
    public void cleanTempTableJobHandler() throws Exception {
        int reserveDays = 7; // 只保留最近 7 天

        // 需要清理的那一天
        LocalDate expireDate = LocalDate.now().minusDays(reserveDays);
        String date = expireDate.format(FORMATTER);

        XxlJobHelper.log("## 开始清理过期日增量临时表，日期: {}...", date);

        if (tableShards > 0) {
            for (int hashKey = 0; hashKey < tableShards; hashKey++) {
                String tableNameSuffix = TableConstants.buildTableNameSuffix(date, hashKey);

                // 删除对应分片的各类临时表
                createTableMapper.dropDataAlignFollowingCountTempTable(tableNameSuffix);
                createTableMapper.dropDataAlignFansCountTempTable(tableNameSuffix);
                createTableMapper.dropDataAlignNoteCollectCountTempTable(tableNameSuffix);
                createTableMapper.dropDataAlignUserCollectCountTempTable(tableNameSuffix);
                createTableMapper.dropDataAlignUserLikeCountTempTable(tableNameSuffix);
                createTableMapper.dropDataAlignNoteLikeCountTempTable(tableNameSuffix);
                createTableMapper.dropDataAlignNotePublishCountTempTable(tableNameSuffix);
            }
        }

        XxlJobHelper.log("## 结束清理过期日增量临时表，日期: {}...", date);
    }
}

