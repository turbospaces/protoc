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
    <#assign v = entry.value>
    <#assign k = entry.key>
    public static final int FIELD_${v.name?upper_case} = ${k};
    </#list>
    
    private static final SortedMap<Integer, FieldDescriptor> DESCRIPTORS = new TreeMap<Integer, FieldDescriptor>();
    
    static {
    <#list fields.entrySet() as entry>
    <#assign v = entry.value>
    <#assign k = entry.key>
        <#if v.isMap()>DESCRIPTORS.put(${k}, new FieldDescriptor(${v.tag}, "${v.name}", new MessageType("${v.type.typeRef}", "${v.type.valueRef}")));<#rt>
        <#elseif v.isCollection()>DESCRIPTORS.put(${k}, new FieldDescriptor(${v.tag}, "${v.name}", new MessageType("${v.type.typeRef}", CollectionType.${v.type.collectionType})));<#rt>
        <#else>
        </#if>
    </#list>
    }
    
    <#list fields.entrySet() as entry>
    <#assign v = entry.value>
    private ${v.javaTypeAsString()} ${v.name};
    </#list>
    
    <#list fields.entrySet() as entry>
    <#assign v = entry.value>
    public ${clazz.name} set${v.name?capitalize}(${v.javaTypeAsString()} val) {
       this.${v.name} = val;
       return this;
    }
    public ${v.javaTypeAsString()} get${v.name?capitalize}() {
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
           case ${k} : { this.${v.name} = (${v.javaTypeAsString()}) value; break; }
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
    public int hashCode() {
       <#if fields.entrySet()?has_content>
       return Objects.hashCode(
       <#list fields.entrySet() as entry>
       <#assign v = entry.value>
       this.${v.name}<#if entry_has_next>,</#if>
       </#list>
       );
       <#else>
       return super.hashCode();
       </#if>
    }
    @Override
    public boolean equals(Object obj) {
       boolean equals = false;
       if(obj instanceof ${clazz.name}) {
         if (obj == this) {return true;}
         // all field to be eqauls
         ${clazz.name} other = (${clazz.name}) obj;
         <#if fields.entrySet()?has_content>
         return
         <#list fields.entrySet() as entry>
         <#assign v = entry.value>
            Objects.equal(other.${v.name}, this.${v.name})<#if entry_has_next> &&<#else>;</#if>
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
        <#assign v = entry.value>
           .add("${v.name}", this.${v.name})
        </#list>.toString();
    }
}