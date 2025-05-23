package com.alibaba.datax.plugin.reader.elasticsearchreader;

import com.alibaba.datax.common.util.Configuration;
import com.alibaba.fastjson2.JSON;
import io.searchbox.params.SearchType;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * @author yangyi5
 * @date 2022/11/8
 */
public final class Key {
    // ----------------------------------------
    //  类型定义 主键字段定义
    // ----------------------------------------

    public static final String SEARCH_KEY = "search";

    public static enum ActionType {
        UNKONW,
        INDEX,
        CREATE,
        DELETE,
        UPDATE
    }

    public static SearchType getSearchType(Configuration conf) {
        String searchType = conf.getString("searchType", SearchType.DFS_QUERY_THEN_FETCH.toString());
        return SearchType.valueOf(searchType.toUpperCase());
    }

    public static ActionType getActionType(Configuration conf) {
        String actionType = conf.getString("actionType", "index");
        if ("index".equals(actionType)) {
            return ActionType.INDEX;
        } else if ("create".equals(actionType)) {
            return ActionType.CREATE;
        } else if ("delete".equals(actionType)) {
            return ActionType.DELETE;
        } else if ("update".equals(actionType)) {
            return ActionType.UPDATE;
        } else {
            return ActionType.UNKONW;
        }
    }


    public static String getEndpoint(Configuration conf) {
        return conf.getNecessaryValue("endpoint", ESReaderErrorCode.BAD_CONFIG_VALUE);
    }

    public static String getAccessID(Configuration conf) {
        return conf.getString("accessId", "");
    }

    public static String getAccessKey(Configuration conf) {
        return conf.getString("accessKey", "");
    }

    public static int getBatchSize(Configuration conf) {
        return conf.getInt("batchSize", 1000);
    }

    public static int getTrySize(Configuration conf) {
        return conf.getInt("trySize", 30);
    }

    public static int getTimeout(Configuration conf) {
        return conf.getInt("timeout", 600000);
    }

    public static boolean isCleanup(Configuration conf) {
        return conf.getBool("cleanup", false);
    }

    public static boolean isDiscovery(Configuration conf) {
        return conf.getBool("discovery", false);
    }

    public static boolean isCompression(Configuration conf) {
        return conf.getBool("compression", true);
    }

    public static boolean isMultiThread(Configuration conf) {
        return conf.getBool("multiThread", true);
    }

    public static String getIndexName(Configuration conf) {
        return conf.getNecessaryValue("index", ESReaderErrorCode.BAD_CONFIG_VALUE);
    }

    public static String getTypeName(Configuration conf) {
        String indexType = conf.getString("indexType");
        if (StringUtils.isBlank(indexType)) {
            indexType = conf.getString("type", getIndexName(conf));
        }
        return indexType;
    }


    public static boolean isIgnoreWriteError(Configuration conf) {
        return conf.getBool("ignoreWriteError", false);
    }

    public static boolean isIgnoreParseError(Configuration conf) {
        return conf.getBool("ignoreParseError", true);
    }


    public static boolean isHighSpeedMode(Configuration conf) {
        if ("highspeed".equals(conf.getString("mode", ""))) {
            return true;
        }
        return false;
    }

    public static String getAlias(Configuration conf) {
        return conf.getString("alias", "");
    }

    public static boolean isNeedCleanAlias(Configuration conf) {
        String mode = conf.getString("aliasMode", "append");
        if ("exclusive".equals(mode)) {
            return true;
        }
        return false;
    }

    public static Map<String, Object> getSettings(Configuration conf) {
        return conf.getMap("settings", new HashMap<String, Object>());
    }

    public static Map<String, Object> getHeaders(Configuration conf) {
        return conf.getMap("headers", new HashMap<>());
    }

    public static String getQuery(Configuration conf) {
        return conf.getConfiguration(Key.SEARCH_KEY).toString();
    }

    public static String getSplitter(Configuration conf) {
        return conf.getString("splitter", "-,-");
    }

    public static boolean getDynamic(Configuration conf) {
        return conf.getBool("dynamic", false);
    }

    public static String getScroll(Configuration conf) {
        return conf.getString("scroll");
    }

    public static EsTable getTable(Configuration conf) {
        String column = conf.getString("table");
        return JSON.parseObject(column, EsTable.class);
    }
}
