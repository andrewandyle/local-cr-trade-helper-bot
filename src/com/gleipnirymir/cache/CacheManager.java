package com.gleipnirymir.cache;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class CacheManager {

//    private static final Logger logger = LogManager.getLogger(CacheManager.class);

    private static final int DEFAULT_ALIVE_MINUTES = 15;
    private static final long CACHE_PURGE_FRECUENCY_MILLISECONDS = 60000;
    private static CacheManager instance = null;
    private Map<String, CacheResource> cacheResources = new ConcurrentHashMap<>();

    public static CacheManager getInstance() {
        if (instance == null) {
            instance = new CacheManager();
        }
        return instance;
    }

    private CacheManager() {

        Thread t = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(CACHE_PURGE_FRECUENCY_MILLISECONDS);
                } catch (InterruptedException ex) {
                }
//                logger.info("Purging cache...");
                purge();
            }
        });

        t.setDaemon(true);
        t.start();

    }

    private void purge() {
//        int purgeCounter = 0;
//        int cacheTotal;
        synchronized (cacheResources){
            Set<String> keys = cacheResources.keySet();
//            cacheTotal = keys.size();
            for (String key : keys){
                if (!cacheResources.get(key).isValid()){
//                    purgeCounter++;
                    cacheResources.remove(key);
                }
            }
        }
//        StringBuilder stringBuilder = new StringBuilder();
//        stringBuilder.append("Purged ")
//                .append(purgeCounter)
//                .append(" of ")
//                .append(cacheTotal);
//        logger.info(stringBuilder.toString());
//        stringBuilder.setLength(0);

    }

    public String retrieve(String method, String key) {
        if (isValid(method, key)) {
            return cacheResources.get(buildKey(method, key)).getSavedResource();
        }
        return "";
    }

    public boolean isValid(String method, String key) {
        String cacheKey = buildKey(method, key);
        if (!cacheResources.containsKey(cacheKey)) {
            return false;
        }
        return cacheResources.get(cacheKey).isValid();
    }

    public void save(String method, String key, String resource) {
        save(method, key, resource, DEFAULT_ALIVE_MINUTES);
    }

    public void save(String method, String key, String resource, int minutesAlive) {
        cacheResources.put(buildKey(method, key), new CacheResource(method, key, resource, minutesAlive));
    }

    private String buildKey(String method, String key) {
        return method + "_" + key;
    }


}
