package com.yunkesoftware.www.utils;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;

import java.util.Comparator;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * @author zhaom
 */
public class QueryTreeUtils {

    /**
     * <p>树形查询工具
     * <p/>
     *
     * @param list       包含树形关系的list
     * @param findParent 父级元素标志
     * @param findThis   当前元素标志
     * @param addAttr    添加额外的属性
     * @param parent     是否需要指定父级节点
     * @param <T>        需要操作的对象类型
     * @return JSONArray
     */
    public static <T> JSONArray queryTree(
            List<T> list,
            Function<T, String> findParent,
            Function<T, String> findThis,
            BiFunction<T, JSONObject, JSONObject> addAttr,
            String parent
    ) {
        JSONArray result = new JSONArray(5);
        //如果传入的父级标志为空，则先找出该列表中的顶层
        if (ObjectUtil.isEmpty(parent)) {
            list.stream()
                    //查找顶级元素
                    .filter(item -> findParent.apply(item) == null || findParent.apply(item).isEmpty())
                    //查找顶级元素的下一层元素
                    .forEach(item -> {
                        JSONObject tem = JSONObject.parseObject(JSON.toJSONString(item));
                        JSONObject json = addAttr.apply(item, tem);
                        JSONArray tempChildren = queryTree(list, findParent, findThis, addAttr, findThis.apply(item));
                        if (tempChildren.size() > 0) {
                            json.put("children", tempChildren);
                        }
                        result.add(json);
                    });
            return result;
        }

        list.stream().filter(item -> findParent.apply(item) != null && findParent.apply(item).equals(parent)).forEach(item -> {
            JSONObject tem = JSONObject.parseObject(JSON.toJSONString(item));
            JSONObject json = addAttr.apply(item, tem);
            JSONArray tempChildren = queryTree(list, findParent, findThis, addAttr, findThis.apply(item));
            if (tempChildren.size() > 0) {
                json.put("children", tempChildren);
            }
            result.add(json);
        });

        return result;
    }

    /**
     * 树形查询工具
     *
     * @param list       包含树形关系的list
     * @param findParent 父级元素标志
     * @param findThis   当前元素标志
     * @param addAttr    添加额外的属性
     * @param sort       排序标志
     * @param parent     是否需要指定父级节点
     * @param <T>        需要操作的对象类型
     * @return JSONArray
     */
    public static <T> JSONArray queryTree(
            List<T> list,
            Function<T, String> findParent,
            Function<T, String> findThis,
            Function<T, Integer> sort,
            BiFunction<T, JSONObject, JSONObject> addAttr,
            String parent
    ) {
        JSONArray result = new JSONArray(5);
        //如果传入的父级标志为空，则先找出该列表中的顶层
        if (parent.isEmpty()) {
            list.stream()
                    //查找顶级元素
                    .filter(item -> findParent.apply(item) == null || findParent.apply(item).isEmpty())
                    //查找顶级元素的下一层元素
                    .sorted(Comparator.comparing(sort))
                    .forEach(item -> {
                        JSONObject tem = JSONObject.parseObject(JSON.toJSONString(item));
                        JSONObject json = addAttr.apply(item, tem);
                        JSONArray tempChildren = queryTree(list, findParent, findThis, sort, addAttr, findThis.apply(item));
                        if (!tempChildren.isEmpty()) {
                            json.put("children", tempChildren);
                        }
                        result.add(json);
                    });
            return result;
        }

        list.stream().filter(item -> findParent.apply(item) != null && findParent.apply(item).equals(parent))
                .sorted(Comparator.comparing(sort))
                .forEach(item -> {
                    JSONObject tem = JSONObject.parseObject(JSON.toJSONString(item));
                    JSONObject json = addAttr.apply(item, tem);
                    JSONArray tempChildren = queryTree(list, findParent, findThis, sort, addAttr, findThis.apply(item));
                    if (!tempChildren.isEmpty()) {
                        json.put("children", tempChildren);
                    }
                    result.add(json);
                });

        return result;
    }


}
