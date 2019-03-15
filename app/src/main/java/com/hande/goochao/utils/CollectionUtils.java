package com.hande.goochao.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by yanshen on 2015/11/3.
 */
public class CollectionUtils {

    /**
     * 过滤List对象，返回过滤后的新的List对象
     * @param collection
     * @param filter
     * @param <T>
     * @return
     */
    public static <T> List<T> filter(List<T> collection, CollectionFilter<T> filter) {
        try {
            return CollectionUtils.<T, ArrayList>filter(collection, filter, ArrayList.class);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 过滤Set对象，返回过滤后的新的Set对象
     * @param collection
     * @param filter
     * @param <T>
     * @return
     */
    public static <T> Set<T> filter(Set<T> collection, CollectionFilter<T> filter) {
        try {
            return CollectionUtils.<T, HashSet>filter(collection, filter, HashSet.class);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 过滤
     * @param collection
     * @param filter
     * @param cz
     * @param <T>
     * @param <C>
     * @return
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    public static <T, C extends Collection> C filter(Collection<T> collection, CollectionFilter<T> filter, Class<C> cz) throws IllegalAccessException, InstantiationException {
        if (collection == null || filter == null)
            return (C)collection;
        Collection<T> newCollection = (Collection<T>)cz.newInstance();
        for (T t : collection) {
            if (filter.accept(t)) {
                newCollection.add(t);
            }
        }
        return (C)newCollection;
    }

    /**
     * 计数
     * @param collection
     * @param filter
     * @param <T>
     * @return
     */
    public static <T> int count(Collection<T> collection, CollectionFilter<T> filter) {
        if (collection == null || filter == null)
            return 0;
        int c = 0;
        for (T t : collection) {
            if (filter.accept(t)) {
                c++;
            }
        }
        return c;
    }

    /**
     * 过来接口
     * accept返回true表示没有被过滤，返回false表示已被过滤
     * @param <E>
     */
    public interface CollectionFilter<E>{
        boolean accept(E e);
    }
}
