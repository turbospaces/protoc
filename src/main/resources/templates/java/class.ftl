// 
// auto-generated class, don't edit (protoc generator version ${version})
//
package ${pkg};

import java.util.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import com.turbospaces.protoc.MessageDescriptor.*;
import com.turbospaces.protoc.*;
import com.turbospaces.protoc.gen.*;
import com.turbospaces.protoc.types.*;

<#assign fields = clazz.fieldDescriptors>
public class ${clazz.name} extends <#if clazz.parent??>${clazz.parent}<#else>AbstractGeneratedMessage</#if> implements GeneratedMessage {
    <#list fields.entrySet() as entry>
    <#assign v = entry.value>
    <#assign k = entry.key>
    public static final int FIELD_${v.name?upper_case} = ${k};
    </#list>
    
    private static final Collection<FieldDescriptor> DESCRIPTORS = new HashSet<FieldDescriptor>();
    
    <#list fields.entrySet() as entry>
    <#assign v = entry.value>
    <#assign k = entry.key>
    <#assign t = entry.value.type>
    public static final FieldDescriptor FIELD_DESCRIPTOR_${v.name?upper_case} = <#rt> 
        <#lt><#if t.isMap()>new FieldDescriptor(${v.tag}, "${v.name}", new MapMessageType(FieldType.${t.keyType}, "${t.keyTypeReference}", FieldType.${t.valueType}, "${t.valueTypeReference}"));
        <#lt><#elseif t.isCollection()>new FieldDescriptor(${v.tag}, "${v.name}", new CollectionMessageType(FieldType.${t.type}, "${t.typeReference}", ${t.set?c}));
        <#lt><#else>new FieldDescriptor(${v.tag}, "${v.name}", new ObjectMessageType(FieldType.${t.type}, "${t.typeReference}"));
        </#if>
    </#list>
    
    static {
     <#list fields.entrySet() as entry>
     <#assign v = entry.value>
       DESCRIPTORS.add(FIELD_DESCRIPTOR_${v.name?upper_case});
     </#list>
    }
    
    <#list fields.entrySet() as entry>
    <#assign v = entry.value>
    private ${v.type.javaTypeAsString()} ${v.name};
    </#list>
    
    <#list fields.entrySet() as entry>
    <#assign v = entry.value>
    public ${clazz.name} set${v.name?capitalize}(${v.type.javaTypeAsString()} val) {
       this.${v.name} = val;
       return this;
    }
    public ${v.type.javaTypeAsString()} get${v.name?capitalize}() {
       return this.${v.name};
    }
    </#list>
    @Override
    public Object getFieldValue(int tag) {
        switch(tag) {
           <#list fields.entrySet() as entry>
           <#assign v = entry.value>
           <#assign k = entry.key>
           case ${k} : return this.${v.name};
           </#list>
           default : throw new RuntimeException("there is no such field with tag = " + tag);
        }
    }
    @Override
    public void setFieldValue(int tag, Object value) {
        switch(tag) {
           <#list fields.entrySet() as entry>
           <#assign v = entry.value>
           <#assign k = entry.key>
           case ${k} : { this.${v.name} = (${v.type.javaTypeAsString()}) value; break; }
           </#list>
           default : throw new RuntimeException("there is no such field with tag = " + tag);
        }
    }
    @Override
    public FieldDescriptor getFieldDescriptor(int tag) {
        switch(tag) {
           <#list fields.entrySet() as entry>
           <#assign v = entry.value>
           <#assign k = entry.key>
           case ${k} : { return FIELD_DESCRIPTOR_${v.name?upper_case}; }
           </#list>
           default : throw new RuntimeException("there is no such field with tag = " + tag);
        }
    }
    @Override
    public Collection<FieldDescriptor> getAllDescriptors() {
       return DESCRIPTORS;
    }
}