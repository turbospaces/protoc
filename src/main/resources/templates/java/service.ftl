// 
// auto-generated class, don't edit (protoc generator version ${version})
//
package ${pkg};

import java.util.*;

<#assign methods = service.methods.values()>
public interface ${service.name}<#if service.parent??> extends ${service.parent}</#if> {
    <#list methods as m>
    ${m.responseType.javaTypeAsString()} ${m.name}(<#if m.requestType??>${m.requestType.javaTypeAsString()} req</#if>)<#rt>
    <#lt><#if m.exceptions?has_content> throws <#list m.exceptions as e>${e}<#if e_has_next>, </#if></#list></#if>;
    </#list>
}