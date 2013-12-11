// 
// auto-generated class, don't edit (protoc generator version ${version})
//
package ${pkg};

import java.util.*;
import com.turbospaces.protoc.MessageDescriptor;

public class ${proto.name} {
   public static Collection<MessageDescriptor> DESCRIPTORS = new HashSet<MessageDescriptor>();

   <#list proto.messages as m>
   public static final MessageDescriptor ${m.name?upper_case} = new MessageDescriptor("${m.name}", <#if m.parent??>"${m.parent}"<#else>null</#if>, "${m.pkg}");
   </#list>
   
   static {
       <#list proto.messages as m>
       DESCRIPTORS.add(${m.name?upper_case});
       </#list>
       DESCRIPTORS = Collections.unmodifiableCollection(DESCRIPTORS);
   }
   
   <#list proto.constants as c>
   public static final ${c.type.javaTypeAsString()} ${c.name?upper_case} = ${c.value.toString()};
   </#list>
}