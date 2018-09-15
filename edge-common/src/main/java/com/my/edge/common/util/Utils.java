package com.my.edge.common.util;

import java.util.Collection;
import java.util.Iterator;

/**
 * Creator: Beefman
 * Date: 2018/8/4
 */
public class Utils {
    public static String makeString(Collection<?> collection, char separator) {
        if (collection == null) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        Iterator<?> iterator = collection.iterator();
        while (iterator.hasNext()) {
            Object object = iterator.next();
            if (object == null) {
                builder.append("null").append(separator);
            } else {
                builder.append(object.toString()).append(separator);
            }
        }
        if (builder.length() > 0 && builder.charAt(builder.length() - 1) == separator) {
            builder.deleteCharAt(builder.length() - 1);
        }
        return builder.toString();
    }
}
