// 
// auto-generated class, don't edit (protoc generator version ${version})
//
package ${pkg};

import com.turbospaces.protoc.gen.*;

<#assign members = enum.members>
public enum ${enum.name} implements GeneratedEnum<${enum.name}> {
    <#list members.entrySet() as entry>
    ${entry.value} (${entry.key})<#if entry_has_next>,<#else>;</#if>
    </#list>

    private int tag;
    private ${enum.name}(int tag) {
       this.tag = tag;
    }
    @Override
    public int tag() {return tag;}
    @Override
    public ${enum.name} valueOf(int tag) {
        ${enum.name} item = null;
        for ( ${enum.name} next : values() ) {
            if ( next.tag == tag ) {
                item = next;
                break;
            }
        }
        return item;
    }
}