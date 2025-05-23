package com.alibaba.datax.core.container.util;

/**
 * Created by xiafei.qiuxf on 14/12/17.
 */

import com.alibaba.datax.common.exception.CommonErrorCode;
import com.alibaba.datax.common.exception.DataXException;
import com.alibaba.datax.common.spi.Hook;
import com.alibaba.datax.common.util.Configuration;
import com.alibaba.datax.core.util.FrameworkErrorCode;
import com.alibaba.datax.core.util.container.JarLoader;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FilenameFilter;
import java.util.*;

/**
 * 扫描给定目录的所有一级子目录，每个子目录当作一个Hook的目录。
 * 对于每个子目录，必须符合ServiceLoader的标准目录格式，见http://docs.oracle.com/javase/6/docs/api/java/util/ServiceLoader.html。
 * 加载里头的jar，使用ServiceLoader机制调用。
 */
public class HookInvoker {

    private static final Logger LOG = LoggerFactory.getLogger(HookInvoker.class);
    private final Map<String, Number> msg;
    private final Configuration conf;

    private File baseDir;

    public HookInvoker(String baseDirName, Configuration conf, Map<String, Number> msg) {
        this.baseDir = new File(baseDirName);
        this.conf = conf;
        this.msg = msg;
    }

    public void invokeAll() {
        if (!baseDir.exists() || baseDir.isFile()) {
            LOG.info("No hook invoked, because base dir not exists or is a file: " + baseDir.getAbsolutePath());
            return;
        }

        String[] subDirs = baseDir.list(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return new File(dir, name).isDirectory();
            }
        });

        if (subDirs == null) {
            throw DataXException.asDataXException(FrameworkErrorCode.HOOK_LOAD_ERROR, "获取HOOK子目录返回null");
        }

        String hooks = conf.getString("job.hooks");
        if (StringUtils.isEmpty(hooks)) {
            return;
        }

        JSONArray hookList = JSON.parseArray(hooks);
        List<String> hookKeys = new ArrayList<>();
        for (int i=0; i<hookList.size(); i++) {
            List<String> hookKey = new ArrayList<>(hookList.getJSONObject(i).keySet());
            hookKeys.addAll(hookKey);
        }

        List<String> subDirList = new ArrayList<>();
        Collections.addAll(subDirList, subDirs);

        // 根据json中hooks参数配置顺序执行hook
        for (String hookKey : hookKeys) {
            if (subDirList.contains(hookKey.split("_")[0])) {
                doInvoke(new File(baseDir, hookKey.split("_")[0]).getAbsolutePath(), hookKey);
            } else {
                LOG.error("{} hook file doesn't exist!", hookKey);
                throw DataXException.asDataXException(FrameworkErrorCode.HOOK_LOAD_ERROR, "请检查Hooks内参数名称和DataX hook文件目录");
            }
        }
    }

    private void doInvoke(String path, String hookKey) {
        ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            JarLoader jarLoader = new JarLoader(new String[]{path});
            Thread.currentThread().setContextClassLoader(jarLoader);
            Iterator<Hook> hookIt = ServiceLoader.load(Hook.class).iterator();
            if (!hookIt.hasNext()) {
                LOG.warn("No hook defined under path: " + path);
            } else {
                Hook hook = hookIt.next();
                LOG.info("Invoke hook [{}], path: {}", hook.getName(), path);
                conf.set("hookKey", hookKey);
                hook.invoke(conf, msg);
            }
        } catch (Exception e) {
            LOG.error("Exception when invoke hook", e);
            throw DataXException.asDataXException(
                    CommonErrorCode.HOOK_INTERNAL_ERROR, "Exception when invoke hook", e);
        } finally {
            Thread.currentThread().setContextClassLoader(oldClassLoader);
        }
    }

    public static void main(String[] args) {
        new HookInvoker("/Users/xiafei/workspace/datax3/target/datax/datax/hook",
                null, new HashMap<String, Number>()).invokeAll();
    }
}
