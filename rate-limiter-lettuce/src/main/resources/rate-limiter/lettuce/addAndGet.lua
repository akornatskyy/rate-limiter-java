local key = KEYS[1]

local delta = ARGV[1]
local start = tonumber(ARGV[2])
local now = ARGV[3]
local ms = ARGV[4]

redis.call("hincrby", key, now, delta)
redis.call("pexpire", key, ms)
local all = redis.call("hgetall", key)
local oldest = tonumber(now)
local sum = -1
local removed = {}
local entryKey
for i, v in ipairs(all) do
  if i % 2 == 1 then
    entryKey = v
  else
    local ts = tonumber(entryKey)
    if ts < start then
      removed[#removed+1] = entryKey
    else
      if ts < oldest then
        oldest = ts
      end

      sum = sum + tonumber(v)
    end
  end
end

if #removed > 0 then
  redis.call("hdel", key, unpack(removed))
end

return {sum, oldest}
