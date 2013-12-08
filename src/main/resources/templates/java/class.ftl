// 
// auto-generated class, don't edit (protoc generator version ${version})
//
package ${pkg};

import java.util.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import com.turbospaces.protoc.GeneratedMessage;
import com.turbospaces.protoc.MessageDescriptor.*;
import com.turbospaces.protoc.MessageType.*;
import com.turbospaces.protoc.MessageType;
import com.google.common.base.Objects;

<#assign fields = clazz.fieldDescriptors>
public class ${clazz.name}<#if clazz.parent??> extends ${clazz.parent}</#if> implements GeneratedMessage {
    <#list fields.entrySet() as entry>
    public static final int FIELD_${entry.value.name?upper_case} = ${entry.key};
    </#list>
    
    private static final SortedMap<Integer, FieldDescriptor> DESCRIPTORS = new TreeMap<Integer, FieldDescriptor>();
    
    static {
    <#list fields.entrySet() as entry>
    <#assign v = entry.value>
    <#assign k = entry.key>
        <#if v.type.isMap()>DESCRIPTORS.put(${k}, new FieldDescriptor(${v.tag}, "${v.name}", new MessageType("${v.type.typeRef}", "${v.type.valueRef}")));<#rt>
                     <#else>DESCRIPTORS.put(${k}, new FieldDescriptor(${v.tag}, "${v.name}", new MessageType("${v.type.typeRef}", CollectionType.${v.type.collectionType})));</#if>
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
    public void setFieldValue(int tag, Object value) {
        switch(tag) {
           <#list fields.entrySet() as entry>
           case ${entry.key} : { this.${entry.value.name} = (${entry.value.type.genericJavaTypeAsString()}) value; break; }
           </#list>
           default : throw new RuntimeException("there is no such field with tag = " + tag);
        }
    }
    @Override
    public FieldDescriptor getFieldDescriptor(int tag) {
        return DESCRIPTORS.get(tag);
    }
    @Override
    public SortedMap<Integer, FieldDescriptor> getAllDescriptors() {
       return DESCRIPTORS;
    }
    @Override
    public boolean equals(Object obj) {
       boolean equals = false;
       if(obj instanceof ${clazz.name}) {
         if (obj == this) {return true;}
         ${clazz.name} other = (${clazz.name}) obj;
         <#if fields.entrySet()?has_content>
         return
         <#list fields.entrySet() as entry>
            Objects.equal(other.${entry.value.name}, this.${entry.value.name}) <#if entry_has_next>&&<#else>;</#if>
        </#list>
        <#else> return true;
        </#if>
       }
       return equals;
    }
    @Override
    public String toString() {
       return Objects.toStringHelper(this)
        <#list fields.entrySet() as entry>
           .add("${entry.value.name}", this.${entry.value.name})
        </#list>.toString();
    }
}