package com.evan.greennote.kv.biz;

import com.evan.framework.common.util.JsonUtils;
import com.evan.greennote.kv.biz.domain.dataobject.NoteContentDO;
import com.evan.greennote.kv.biz.domain.repository.NoteContentRepository;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;
import java.util.UUID;

@SpringBootTest
@Slf4j
public class CassandraTests {
    @Resource
    private NoteContentRepository noteContentRepository;

    //测试插入数据
    @Test
    void testInsert() {
        NoteContentDO noteContentDO = NoteContentDO.builder()
                .id(UUID.randomUUID())
                .content("测试插入数据")
                .build();
        noteContentRepository.save(noteContentDO);
        log.info("==> 插入数据成功");
    }

    //测试修改数据
    @Test
    void testUpdate() {
        NoteContentDO noteContentDO = NoteContentDO.builder()
                .id(UUID.fromString("16ab5323-58cc-44ec-9b1a-d690d52d73f3"))
                .content("测试数据内容更新")
                .build();
        noteContentRepository.save(noteContentDO);
        log.info("==> 修改数据成功");
    }

    //测试查询数据
    @Test
    void testSelect(){
        Optional<NoteContentDO> optional=noteContentRepository.findById(UUID.fromString("16ab5323-58cc-44ec-9b1a-d690d52d73f3"));
        optional.ifPresent(noteContentDO->log.info("==> 查询结果：{}", JsonUtils.toJsonString(noteContentDO)));
    }

    //测试删除数据
    @Test
    void testDelete(){
        noteContentRepository.deleteById(UUID.fromString("16ab5323-58cc-44ec-9b1a-d690d52d73f3"));
        log.info("==> 删除数据成功");
    }
}
