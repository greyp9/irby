package io.github.greyp9.irby.core.naming;

import io.github.greyp9.arwo.core.vm.mutex.MapU;

import javax.naming.Binding;
import javax.naming.Name;
import javax.naming.NameAlreadyBoundException;
import javax.naming.NameClassPair;
import javax.naming.NameParser;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import java.util.Hashtable;
import java.util.Map;
import java.util.TreeMap;

// stackoverflow 10045466 20359483
@SuppressWarnings("PMD.TooManyMethods")
public class IrbyContext implements javax.naming.Context {
    private final IrbyContext parentContext;
    private final Map<String, Binding> bindings;

    public final IrbyContext getParentContext() {
        return parentContext;
    }

    @Override
    public final String toString() {
        return String.format("%s/%s/%s", hashCode(), parentContext.hashCode(), bindings.size());
    }

    public IrbyContext(final IrbyContext parentContext) {
        this.parentContext = parentContext;
        this.bindings = new TreeMap<String, Binding>();
    }

    @Override
    public final Object lookup(final Name name) throws NamingException {
        throw new IllegalStateException();
    }

    @Override
    public final Object lookup(final String name) throws NamingException {
        Object result;
        final Object value = MapU.get(bindings, name);
        if (value instanceof Binding) {
            result = ((Binding) value).getObject();
        } else {
            result = null;
            //throw new NameNotFoundException(name);
        }
        return result;
    }

    @Override
    public final void bind(final Name name, final Object obj) throws NamingException {
        throw new IllegalStateException();
    }

    @Override
    public final void bind(final String name, final Object obj) throws NamingException {
        MapU.put(bindings, name, new Binding(name, obj));
    }

    @Override
    public final void rebind(final Name name, final Object obj) throws NamingException {
        throw new IllegalStateException();
    }

    @Override
    public final void rebind(final String name, final Object obj) throws NamingException {
        throw new IllegalStateException();
    }

    @Override
    public final void unbind(final Name name) throws NamingException {
        throw new IllegalStateException();
    }

    @Override
    public final void unbind(final String name) throws NamingException {
        MapU.put(bindings, name, null);
    }

    @Override
    public final void rename(final Name oldName, final Name newName) throws NamingException {
        throw new IllegalStateException();
    }

    @Override
    public final void rename(final String oldName, final String newName) throws NamingException {
        throw new IllegalStateException();
    }

    @Override
    public final NamingEnumeration<NameClassPair> list(final Name name) throws NamingException {
        throw new IllegalStateException();
    }

    @Override
    public final NamingEnumeration<NameClassPair> list(final String name) throws NamingException {
        throw new IllegalStateException();
    }

    @Override
    public final NamingEnumeration<Binding> listBindings(final Name name) throws NamingException {
        throw new IllegalStateException();
    }

    @Override
    public final NamingEnumeration<Binding> listBindings(final String name) throws NamingException {
        throw new IllegalStateException();
    }

    @Override
    public final void destroySubcontext(final Name name) throws NamingException {
        throw new IllegalStateException();
    }

    @Override
    public final void destroySubcontext(final String name) throws NamingException {
        // empty method
    }

    @Override
    public final javax.naming.Context createSubcontext(final Name name) throws NamingException {
        throw new IllegalStateException();
    }

    @Override
    public final javax.naming.Context createSubcontext(final String name) throws NamingException {
        if (MapU.get(bindings, name) == null) {
            throw new NameAlreadyBoundException(name);
        } else {
            final IrbyContext irbyContext = new IrbyContext(this);
            final Binding binding = new Binding(name, irbyContext);
            MapU.put(bindings, name, binding);
            return irbyContext;
        }
    }

    @Override
    public final Object lookupLink(final Name name) throws NamingException {
        throw new IllegalStateException();
    }

    @Override
    public final Object lookupLink(final String name) throws NamingException {
        throw new IllegalStateException();
    }

    @Override
    public final NameParser getNameParser(final Name name) throws NamingException {
        throw new IllegalStateException();
    }

    @Override
    public final NameParser getNameParser(final String name) throws NamingException {
        throw new IllegalStateException();
    }

    @Override
    public final Name composeName(final Name name, final Name prefix) throws NamingException {
        throw new IllegalStateException();
    }

    @Override
    public final String composeName(final String name, final String prefix) throws NamingException {
        throw new IllegalStateException();
    }

    @Override
    public final Object addToEnvironment(final String propName, final Object propVal) throws NamingException {
        throw new IllegalStateException();
    }

    @Override
    public final Object removeFromEnvironment(final String propName) throws NamingException {
        throw new IllegalStateException();
    }

    @SuppressWarnings("PMD.ReplaceHashtableWithMap")
    @Override
    public final Hashtable<?, ?> getEnvironment() throws NamingException {
        throw new IllegalStateException();
    }

    @Override
    public final void close() throws NamingException {
        throw new IllegalStateException();
    }

    @Override
    public final String getNameInNamespace() throws NamingException {
        throw new IllegalStateException();
    }
}
