package com.turbospaces.protoc.gen;

import static com.turbospaces.protoc.gen.GenException.check;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.turbospaces.protoc.EnumDescriptor;
import com.turbospaces.protoc.InitializingBean;
import com.turbospaces.protoc.MessageDescriptor;
import com.turbospaces.protoc.MessageDescriptor.FieldDescriptor;
import com.turbospaces.protoc.ProtoContainer;
import com.turbospaces.protoc.ServiceDescriptor;
import com.turbospaces.protoc.ServiceDescriptor.MethodDescriptor;
import com.turbospaces.protoc.types.FieldType;
import com.turbospaces.protoc.types.MapMessageType;
import com.turbospaces.protoc.types.MessageType;
import com.turbospaces.protoc.types.ObjectMessageType;

public class ProtoGenerationContext implements InitializingBean {
    public Set<ProtoContainer> containers = Sets.newLinkedHashSet();
    public Set<ProtoContainer> imports = Sets.newLinkedHashSet();
    //
    private Map<ProtoContainer, Map<String, String>> qualifiedMessages = Maps.newHashMap();
    private Map<ProtoContainer, Map<String, String>> qualifiedEnums = Maps.newHashMap();
    private Map<ProtoContainer, Map<String, String>> qualifiedServices = Maps.newHashMap();
    private Map<String, MessageDescriptor> allMessages = Maps.newHashMap();

    public String qualifiedMessageReference(String ref) {
        Collection<Map<String, String>> messages = qualifiedMessages.values();
        for ( Map<String, String> m : messages ) {
            String q = m.get( ref );
            if ( q != null ) {
                return q;
            }
        }
        return null;
    }
    public String qualifiedEnumReference(String ref) {
        Collection<Map<String, String>> messages = qualifiedEnums.values();
        for ( Map<String, String> m : messages ) {
            String q = m.get( ref );
            if ( q != null ) {
                return q;
            }
        }
        return null;
    }

    @Override
    public void init(ProtoGenerationContext ctx) throws Exception {
        Set<ProtoContainer> all = Sets.newHashSet();
        all.addAll( containers );
        all.addAll( imports );

        for ( ProtoContainer c : all ) {
            Map<String, String> qMessages = Maps.newHashMap(); // short=package+short
            Map<String, String> qEnums = Maps.newHashMap(); // short=package+short
            Map<String, String> qServices = Maps.newHashMap(); // short=package+short

            qualifiedMessages.put( c, qMessages );
            qualifiedEnums.put( c, qEnums );
            qualifiedServices.put( c, qServices );

            String pkg = c.pkg;
            Collection<MessageDescriptor> messages = c.messages.values();
            Collection<EnumDescriptor> enums = c.enums.values();
            Collection<String> aliases = c.aliases.values();
            Collection<ServiceDescriptor> services = c.services.values();

            for ( MessageDescriptor m : messages ) {
                String q = pkg + "." + m.getName();
                ensureUnique( q );
                allMessages.put( m.getName(), m );
                qMessages.put( m.getName(), q );
            }
            for ( EnumDescriptor e : enums ) {
                String q = pkg + "." + e.getName();
                ensureUnique( q );
                qEnums.put( e.getName(), q );
            }
            for ( String a : aliases ) {
                String q = pkg + "." + a;
                ensureUnique( q );
                qMessages.put( a, q );
            }
            for ( ServiceDescriptor s : services ) {
                String q = pkg + "." + s.getName();
                ensureUnique( q );
                qServices.put( s.getName(), q );
            }
        }

        for ( ProtoContainer c : all ) {
            Collection<MessageDescriptor> messages = c.messages.values();
            for ( MessageDescriptor m : messages ) {
                Collection<FieldDescriptor> values = m.getFieldDescriptors().values();

                // check for unique tag numbers
                Collection<FieldDescriptor> allHierarchyFields = Sets.newHashSet();
                String parent = m.getParent();
                while ( parent != null ) {
                    String e = qualifiedEnumReference( parent );
                    check( e == null, "parent can't extend enum=%s", e );
                    MessageDescriptor pdescriptor = allMessages.get( parent );
                    check( pdescriptor != null, "there is no such parent class=%s defined", parent );
                    allHierarchyFields.addAll( pdescriptor.getFieldDescriptors().values() );
                    parent = pdescriptor.getParent();
                }
                Set<Integer> uniqueTags = Sets.newHashSet();
                for ( FieldDescriptor hf : allHierarchyFields ) {
                    check(
                            !uniqueTags.contains( hf.getTag() ),
                            "hierarchy is not consistent, field with tag=%s already defined",
                            hf.getTag() );
                    uniqueTags.add( hf.getTag() );
                }

                for ( FieldDescriptor f : values ) {
                    MessageType type = f.getType();
                    if ( type instanceof ObjectMessageType ) {
                        ObjectMessageType omt = (ObjectMessageType) type;
                        String qualified = m.getPkg() + "." + omt.getTypeReference();
                        ensureReferenceResolved( omt.getTypeReference(), qualified );
                    }
                    else if ( type instanceof MapMessageType ) {
                        MapMessageType mmt = (MapMessageType) type;
                        String kqualified = m.getPkg() + "." + mmt.getKeyTypeReference();
                        String vqualified = m.getPkg() + "." + mmt.getValueTypeReference();
                        ensureReferenceResolved( mmt.getKeyTypeReference(), kqualified );
                        ensureReferenceResolved( mmt.getValueTypeReference(), vqualified );
                    }
                }
            }
        }

        for ( ProtoContainer c : all ) {
            Collection<MessageDescriptor> messages = c.messages.values();
            for ( MessageDescriptor m : messages ) {
                Collection<FieldDescriptor> values = m.getFieldDescriptors().values();
                for ( FieldDescriptor f : values ) {
                    f.init( this );
                }
            }
            Collection<ServiceDescriptor> services = c.services.values();
            for ( ServiceDescriptor s : services ) {
                Collection<MethodDescriptor> methods = s.getMethods().values();
                for ( MethodDescriptor m : methods ) {
                    Set<String> exceptions = m.getExceptions();
                    for ( String exception : exceptions ) {
                        MessageDescriptor mdesc = allMessages.get( exception );
                        check( mdesc != null, "there is no such exception=%s defined", exception );
                        mdesc.setException( true );
                    }
                    m.init( this );
                }
            }
        }
    }
    private void ensureUnique(String q) throws GenException {
        for ( Map<String, String> m : qualifiedMessages.values() ) {
            Collection<String> qualified = m.values();
            check( !qualified.contains( q ), "Message/Alias with name=%s already defined", q );
        }
        for ( Map<String, String> m : qualifiedEnums.values() ) {
            Collection<String> qualified = m.values();
            check( !qualified.contains( q ), "Enum with name=%s already defined", q );
        }
        for ( Map<String, String> m : qualifiedServices.values() ) {
            Collection<String> qualified = m.values();
            check( !qualified.contains( q ), "Service with name=%s already defined", q );
        }
    }
    private void ensureReferenceResolved(String uq, String q) {
        FieldType f = null;

        // try primitives
        for ( FieldType next : FieldType.values() ) {
            if ( next.name().equals( uq.toUpperCase() ) ) {
                f = next;
                break;
            }
        }

        if ( f == null ) {
            boolean exists = false;
            for ( Map<String, String> m : qualifiedMessages.values() ) {
                if ( m.values().contains( q ) ) {
                    exists = true;
                    break;
                }
            }
            for ( Map<String, String> m : qualifiedEnums.values() ) {
                if ( m.values().contains( q ) ) {
                    exists = true;
                    break;
                }
            }

            if ( !exists ) {
                throw new GenException( String.format(
                        "Reference type %s is not defined in %s and imported %s",
                        q,
                        containers,
                        imports ) );
            }
        }
    }
}
