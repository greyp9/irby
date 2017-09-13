package io.github.greyp9.irby.core.naming;

import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import java.util.Enumeration;

public class NamesEnumeration implements NamingEnumeration<NameClassPair> {
    final Enumeration<NameClassPair> pairs;

    public NamesEnumeration(final Enumeration<NameClassPair> pairs) {
        this.pairs = pairs;
    }

    @Override
    public NameClassPair next() throws NamingException {
        return pairs.nextElement();
    }

    @Override
    public boolean hasMore() throws NamingException {
        return pairs.hasMoreElements();
    }

    @Override
    public void close() throws NamingException {
    }

    @Override
    public boolean hasMoreElements() {
        return pairs.hasMoreElements();
    }

    @Override
    public NameClassPair nextElement() {
        return pairs.nextElement();
    }
}
