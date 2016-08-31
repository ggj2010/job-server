package com.weimob.jobserver.core.zk.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author: kevin
 * @date 2016/08/20.
 */
public class ZkPathUtil {
    public static List<String> parseZkPath(String path) {
        Pattern pattern = Pattern.compile("\\/([A-Za-z0-9-_\\.]+)");
        Matcher matcher = pattern.matcher(path);
        List<String> parserList = new ArrayList<>();
        while (matcher.find()) {
            parserList.add(matcher.group(1));
        }
        return parserList;
    }
}
