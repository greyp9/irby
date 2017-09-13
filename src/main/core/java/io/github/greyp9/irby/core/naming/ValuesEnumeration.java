package io.github.greyp9.irby.core.naming;

import javax.naming.Binding;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import java.util.Enumeration;

public class ValuesEnumeration implements NamingEnumeration<Binding> {
    final Enumeration<Binding> objects;

    public ValuesEnumeration(final Enumeration<Binding> objects) {
        this.objects = objects;
    }

    @Override
    public Binding next() throws NamingException {
        return objects.nextElement();
    }

    @Override
    public boolean hasMore() throws NamingException {
        return objects.hasMoreElements();
    }

    @Override
    public void close() throws NamingException {
    }

    @Override
    public boolean hasMoreElements() {
        return objects.hasMoreElements();
    }

    @Override
    public Binding nextElement() {
        return objects.nextElement();
    }
}
