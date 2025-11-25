-- 操作的 Key
local key = KEYS[1]
local noteId = ARGV[1] -- 笔记ID

if not noteId then
    return 0
end

redis.call("BF.ADD", key, noteId)
return 0