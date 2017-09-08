package com.demo.cache;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import javafx.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by shuoshuo on 2017/9/5.
 */
public class RightVerifyCache {
    private static final Logger logger = LoggerFactory.getLogger(RightVerifyCache.class);

    /**
     * build cache
     */
    public static LoadingCache<Pair<String, String>, Pair<Long, List<Map>>> cache =
            CacheBuilder.newBuilder()
            .expireAfterWrite(10, TimeUnit.HOURS)
            .softValues()
            .build(new CacheLoader<Pair<String, String>, Pair<Long, List<Map>>>() {
                @Override
                public Pair<Long, List<Map>> load(Pair<String, String> pair) throws Exception {
                    List<Map> retList = RightVerifyCache.queryRightList(pair.getKey());
                    return new Pair(Long.valueOf(new SimpleDateFormat("yyyyMMddHHmmss").format(new Date())), retList);
                }
            });

    /**
     * 获取员工权限
     */
    public static List<Map> getRightsList(String staffId, Long updateTime) {
        Pair<String, String> cacheKey = getCacheKey(staffId);
        Pair<Long, List<Map>> cacheValues = cache.getUnchecked(cacheKey);
        List<Map> retList = cacheValues.getValue();

        //判断cache的数据是否是最新值
        if (updateTime > cacheValues.getKey()) {
            //更新缓存，重新取数据
            refreshRightList(staffId);
            retList = cache.getUnchecked(cacheKey).getValue();
        }

        return retList;
    }

    /**
     * 获取cache的key值
     * @param staffId
     * @return
     */
    private static Pair<String, String> getCacheKey(String staffId) {
        return new Pair<String, String>(staffId, new SimpleDateFormat("yyyyMMdd").format(new Date()));
    }

    /**
     * 通过DB查询权限
     */
    public static List<Map> queryRightList(String staffId) {
        logger.info("读取员工{}权限开始", staffId);
        List<Map> menuList = null;

        try {
//            menuList = queryFromDB(staffId);
        } catch(Exception e) {
            logger.error("读取员工{}权限失败", staffId);
        }
        logger.info("读取员工{}权限结束", staffId);

        return menuList;
    }

    /**
     * 刷新权限列表
     */
    public static void refreshRightList(String staffId) {
        cache.refresh(new Pair(staffId, new SimpleDateFormat("yyyyMMdd").format(new Date())));
    }
}
