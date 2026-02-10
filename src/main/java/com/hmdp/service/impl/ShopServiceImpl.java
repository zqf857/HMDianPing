package com.hmdp.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.constant.RedisConstants;
import com.hmdp.entity.Shop;
import com.hmdp.mapper.ShopMapper;
import com.hmdp.service.IShopService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.Serializable;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
public class ShopServiceImpl extends ServiceImpl<ShopMapper, Shop> implements IShopService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 根据id查询商铺信息
     * 添加 redis緩存
     *
     * @param id
     * @return
     */
    @Override
    public Shop getById(Serializable id) {
        // 1. 從 redis查詢商鋪緩存
        String key = RedisConstants.CACHE_SHOP_KEY + id;
        String shopJson = stringRedisTemplate.opsForValue().get(key);
        // 1.2. 判断缓存是否存在
        if (StrUtil.isNotBlank(shopJson)) {
            // 2. 存在, 直接返回
            return JSONUtil.toBean(shopJson, Shop.class);
        }
        // 3. 缓存不存在, 查询 Mysql
        Shop shop = super.getById(id);
        // 4. 缓存结果到 redis
        stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(shop));
        // 5. 返回数据
        return shop;
    }
}
