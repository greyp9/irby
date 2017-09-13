package io.github.greyp9.irby.core.naming;

import javax.naming.Binding;
import javax.naming.InitialContext;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;

public class IrbyContextU {

    public static String enumerate(final IrbyContext context) {
        StringBuilder buffer = new StringBuilder();
        try {
            final NamingEnumeration<Binding> bindings = ((context == null) ?
                    new InitialContext().listBindings(".*") : context.listBindings(".*"));
            while (bindings.hasMore()) {
                final Binding binding = bindings.next();
                buffer.append(String.format("\n[%s][%s]=[%s]",
                        binding.getName(), binding.getClassName(), binding.getObject().toString()));
                final Object object = binding.getObject();
                if (object instanceof IrbyContext) {
                    final IrbyContext subcontext = (IrbyContext) object;
                    buffer.append(enumerate(subcontext));
                }
            }
        } catch (NamingException e) {
            buffer.append(e.getMessage());
        }
        return buffer.toString();
    }
}
