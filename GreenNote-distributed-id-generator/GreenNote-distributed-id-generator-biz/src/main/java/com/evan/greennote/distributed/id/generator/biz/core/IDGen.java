package com.evan.greennote.distributed.id.generator.biz.core;

import com.evan.greennote.distributed.id.generator.biz.core.common.Result;

public interface IDGen {
    Result get(String key);
    boolean init();
}
