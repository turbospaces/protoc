// 
// auto-generated class, don't edit (protoc generator version ${version})
//
package ${pkg};

import java.util.List;
import java.util.Set;
import java.util.Map;
import java.util.HashMap;
import java.math.BigDecimal;
import java.math.BigInteger;
import com.turbospaces.protoc.GeneratedMessage;
import com.turbospaces.protoc.MessageDescriptor.*;
import com.turbospaces.protoc.MessageType.*;
import com.turbospaces.protoc.MessageType;

<#assign fields = clazz.fieldDescriptors>
public class ${clazz.name}<#if clazz.parent??> extends ${clazz.parent}</#if> implements GeneratedMessage {
    <#list fields.entrySet() as entry>
    public static final int FIELD_${entry.value.name?upper_case} = ${entry.key};
    </#list>
    
    private static final Map<Integer, FieldDescriptor> DESCRIPTORS = new HashMap<Integer, FieldDescriptor>();
    
    static {
    <#list fields.entrySet() as entry>
        <#if entry.value.type.isMap()>DESCRIPTORS.put(${entry.key}, new FieldDescriptor(${entry.value.tag}, "${entry.value.name}", new MessageType("${entry.value.type.typeRef}", "${entry.value.type.valueRef}")));<#rt>
                               <#else>DESCRIPTORS.put(${entry.key}, new FieldDescriptor(${entry.value.tag}, "${entry.value.name}", new MessageType("${entry.value.type.typeRef}", CollectionType.${entry.value.type.collectionType})));</#if>
    </#list>
    }
    
    <#list fields.entrySet() as entry>
    private ${entry.value.type.genericJavaTypeAsString()} ${entry.value.name};
    </#list>
    
    <#list fields.entrySet() as entry>
    public ${clazz.name} set${entry.value.name?capitalize}(${entry.value.type.genericJavaTypeAsString()} val) {
       this.${entry.value.name} = val;
       return this;
    }
    public ${entry.value.type.genericJavaTypeAsString()} get${entry.value.name?capitalize}() {
       return this.${entry.value.name};
    }
    </#list>
    @Override
    public Object getFieldValue(int tag) {
        switch(tag) {
           <#list fields.entrySet() as entry>
           case ${entry.key} : return this.${entry.value.name};
           </#list>
           default : throw new RuntimeException("there is no such field with tag = " + tag);
        }
    }
    @Override
    public FieldDescriptor getFieldDescriptor(int tag) {
        return DESCRIPTORS.get(tag);
    }
}