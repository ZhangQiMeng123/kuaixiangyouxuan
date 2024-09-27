--判断是否可以下订单 1.库存是否充足 2.用户是否重复下单 3.将用户id，优惠卷id，订单id存放入阻塞队列
--优惠卷id
local voucherId=ARGV[1]
--用户id
local userId=ARGV[2]
--订单id
local orderId=ARGV[3]

--库存key
local stockKey='seckill:stock:' .. voucherId
--订单key
local orderKey='seckill:order:' .. voucherId

--脚本业务
if(tonumber(redis.call('get', stockKey))<=0) then
    --库存不足
    return 1;
end
if(redis.call('sismember',orderKey,userId)==1) then
    --存在，重复下单
    return 2
end

--扣库存
redis.call('incrby',stockKey,-1)
--下单（保存用户）
redis.call('sadd',orderKey,userId)
--发送消息到队列
redis.call('xadd','stream.orders','*','userId',userId,'voucherId',voucherId,'id',orderId)
return 0
