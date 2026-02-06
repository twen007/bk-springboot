/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package gov.nist.oism.asd.empbc.util;

/**
 *
 * @author xinweiw
 */
import com.google.common.cache.*;
import gov.nist.oism.asd.empbc.v1.UserService;
import gov.nist.oism.asd.empbc.model.User;
import gov.nist.oism.asd.empbc.model.UserDetailedPrivilege;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CacheManager {

    private static final Logger LOG = Logger.getLogger(CacheManager.class.getSimpleName());

    /**
     * Maximum number of cache entries
     */
    private static final int GUAVA_CACHE_SIZE = 1000;
    
    /**
     * Cache time: minutes
     */
    private static final int GUAVA_CACHE_MINUTES = 10;
    
    /**
     * user role info in NIST Org do not change that often but to be safe, set to 5min
     */
    private static final int USER_ROLE_CACHE_MINUTES = 5;
    
    /**
     * Maximum number of user cache entries; 
     */
    private static final int USER_CACHE_SIZE = 500;
    
    /**
     * don't expect user info in CPR to change that often, so set to 30min
     */
    private static final int USER_CACHE_MINUTES = 30;
    
    /**
     * Cache operation object
     */
    private LoadingCache<String, List<UserService.Role>> userRoleCache = null;
    private LoadingCache<String, User> userCache = null;
    private LoadingCache<Integer, List<UserDetailedPrivilege>> userDetailedCache = null;
    
    public static volatile CacheManager instance;

    private CacheManager() {
        try {
            userRoleCache = loadCache(new CacheLoader<String, List<UserService.Role>>() {
                @Override
                public List<UserService.Role> load(String url) throws StatusCodeException {
                    // Processing logic when the cache key does not have a cache value
                    UserService us = new UserService();
                    return us.getUserRoles(url);
                }
            });
            
            userCache = loadUserCache(new CacheLoader<String, User>() {
                @Override
                public User load(String key) throws StatusCodeException {
                    // processing logic when the cache key does not have a cache value
                    UserService userService = new UserService();
                    return userService.getUserByUsernameFromDatabase(key);
                }
            });
            
            userDetailedCache = loadUserDetailedCache(new CacheLoader<Integer, List<UserDetailedPrivilege>>() {
                @Override
                public List<UserDetailedPrivilege> load(Integer peopleId) throws StatusCodeException {

                    UserService userService = new UserService();
                    return userService.getUserDetailedPrivilegeByIdFromDatabase(peopleId);
                }
            });


        } catch (Exception e) {
            LOG.log(Level.SEVERE, "error initializing guava cache", e);
        }
    }

    public static CacheManager getInstance() {
        if (instance == null) {
            synchronized (CacheManager.class) {
                if (instance == null) {
                    instance = new CacheManager();
                }
            }
        }
        return instance;
    }

    public LoadingCache<String, List<UserService.Role>> getUserRoleCache() {
        return userRoleCache;
    }
    
    public LoadingCache<String, User> getUserCache() {
        return userCache;
    }

    
    public LoadingCache<Integer, List<UserDetailedPrivilege>> getUserDetailedCache() {
        return userDetailedCache;
    }
    
    /**
     * Global cache settings
     *
     * @param cacheLoader
     * @return
     * @throws Exception
     */
    private LoadingCache<String, List<UserService.Role>> loadCache(CacheLoader<String, List<UserService.Role>> cacheLoader) {
        LoadingCache<String, List<UserService.Role>> cache = CacheBuilder.newBuilder()
                // The size of the cache pool. When the cache item approaches this size, guava
                // begins to recycle the old cache item
                .maximumSize(GUAVA_CACHE_SIZE)
                // If the time object is not accessed by read / write, the object will be
                // deleted from memory (maintained irregularly in another thread)
                .expireAfterAccess(USER_ROLE_CACHE_MINUTES, TimeUnit.MINUTES)
                // The set cache becomes invalid after a set time after writing
                .expireAfterWrite(USER_ROLE_CACHE_MINUTES, TimeUnit.MINUTES)
                .removalListener(new RemovalListener<String, List<UserService.Role>>() {
                    @Override
                    public void onRemoval(RemovalNotification<String, List<UserService.Role>> notification) {
                        LOG.info(String.format("#### Key - %s  removed due to %s", notification.getKey(),
                                notification.getCause()));
                    }
                })
                // Enable the statistical function of guava cache
                // .recordStats()
                .build(cacheLoader);

        return cache;
    }
    
    /**
     * 
     * @param cacheLoader
     * @return 
     */
    private LoadingCache<String, User> loadUserCache(CacheLoader<String, User> cacheLoader) {
        LoadingCache<String, User> cache = CacheBuilder.newBuilder()
            // The size of the cache pool. When the cache item approaches this size, guava
            // begins to recycle the old cache item
            .maximumSize(USER_CACHE_SIZE)
            // If the time object is not accessed by read / write, the object will be
            // deleted from memory (maintained irregularly in another thread)
            .expireAfterAccess(USER_CACHE_MINUTES, TimeUnit.HOURS)
            // The set cache becomes invalid after a set time after writing
            .expireAfterWrite(USER_CACHE_MINUTES, TimeUnit.HOURS)
            .removalListener(new RemovalListener<String, User>() {
                @Override
                public void onRemoval(RemovalNotification<String, User> notification) {
                    LOG.info(String.format("#### Key - %d  removed due to %s", notification.getKey(),
                        notification.getCause()));
                }
            })
            // Enable the statistical function of guava cache
            // .recordStats()
            .build(cacheLoader);

        return cache;
    }

    private LoadingCache<Integer, List<UserDetailedPrivilege>> loadUserDetailedCache(CacheLoader<Integer, List<UserDetailedPrivilege>> cacheLoader) {
        LoadingCache<Integer, List<UserDetailedPrivilege>> cache = CacheBuilder.newBuilder()
                // The size of the cache pool. When the cache item approaches this size, guava
                // begins to recycle the old cache item
                .maximumSize(GUAVA_CACHE_SIZE)
                // If the time object is not accessed by read / write, the object will be
                // deleted from memory (maintained irregularly in another thread)
                .expireAfterAccess(USER_ROLE_CACHE_MINUTES, TimeUnit.MINUTES)
                // The set cache becomes invalid after a set time after writing
                .expireAfterWrite(USER_ROLE_CACHE_MINUTES, TimeUnit.MINUTES)
                .removalListener(new RemovalListener<Integer, List<UserDetailedPrivilege>>() {
                    @Override
                    public void onRemoval(RemovalNotification<Integer, List<UserDetailedPrivilege>> notification) {
                        LOG.info(String.format("#### Key - %d  removed due to %s", notification.getKey(),
                                notification.getCause()));
                    }
                })
                // Enable the statistical function of guava cache
                // .recordStats()
                .build(cacheLoader);

        return cache;
    }

}
