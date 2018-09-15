package com.my.edge.common.resolver;

import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.fasterxml.jackson.databind.DatabindContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.jsontype.impl.TypeIdResolverBase;
import com.my.edge.common.control.NodeMetadata;

import java.io.IOException;

/**
 * Creator: Beefman
 * Date: 2018/9/15
 */
public class NodeMetadataResolver extends TypeIdResolverBase {

    private JavaType superType;

    @Override
    public void init(JavaType baseType) {
        superType = baseType;
    }

    @Override
    public Id getMechanism() {
        return Id.CLASS;
    }

    @Override
    public String idFromValue(Object value) {
        return idFromValueAndType(value, value.getClass());
    }

    @Override
    public String idFromValueAndType(Object value, Class<?> suggestedType) {
        return suggestedType.getCanonicalName();
    }

    @Override
    public JavaType typeFromId(DatabindContext context, String id) throws IOException {
        try {
            ClassLoader classLoader = this.getClass().getClassLoader();
            Class<?> clazz = classLoader.loadClass(id);
            return context.constructSpecializedType(superType, clazz);
        } catch (Exception e) {
            throw new RuntimeException("Resolving subtype of NodeMetadata failed. ", e);
        }
    }
}
