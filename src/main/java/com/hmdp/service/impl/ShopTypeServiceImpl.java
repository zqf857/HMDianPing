package com.hmdp.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.entity.ShopType;
import com.hmdp.mapper.ShopTypeMapper;
import com.hmdp.service.IShopTypeService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

import static com.hmdp.constant.RedisConstants.SHOP_TYPE_LIST;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
public class ShopTypeServiceImpl extends ServiceImpl<ShopTypeMapper, ShopType> implements IShopTypeService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public List<ShopType> queryTypeList() {
        String shopTypeListJson = stringRedisTemplate.opsForValue().get(SHOP_TYPE_LIST);
        if (StrUtil.isNotBlank(shopTypeListJson)) {
            return JSONUtil.toList(shopTypeListJson, ShopType.class);
        }
        List<ShopType> typeList = this.query().orderByAsc("sort").list();
        stringRedisTemplate.opsForValue().set(SHOP_TYPE_LIST, JSONUtil.toJsonStr(typeList));
        return typeList;
    }
}
