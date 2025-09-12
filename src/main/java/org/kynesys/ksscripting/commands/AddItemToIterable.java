package org.kynesys.ksscripting.commands;

import org.kynesys.ksscripting.objects.KSScriptingNull;
import org.kynesys.lwks.KSExecutionSession;
import org.kynesys.lwks.KSScriptingExecutable;

import java.util.Collection;
import java.util.List;

public class AddItemToIterable implements KSScriptingExecutable {
    @Override
    public String returnType() {
        return Object.class.getName();
    }

    @Override
    public Object execute(Object[] args, KSExecutionSession session) throws Exception {
        // Usage: AddItemToIterable <iterable> <item>
        if (args.length != 2) {
            throw new IllegalArgumentException("AddItemToIterable requires exactly two arguments.");
        }

        Object iterable = args[0];
        Object item = args[1];
        if (KSScriptingNull.isNull(iterable)) {
            throw new IllegalArgumentException("The first argument cannot be null.");
        }

        if (iterable instanceof List<?>) {
            // Make a copy of the list to avoid modifying the original
            List<Object> newList = ((List<Object>) iterable).getClass().newInstance();
            newList.addAll((List<Object>) iterable);
            newList.add(item);
            return newList; // Return the modified list
        } else if (iterable instanceof Collection<?>) {
            // Make a copy of the collection to avoid modifying the original
            Collection<Object> newCollection = ((Collection<Object>) iterable).getClass().newInstance();
            newCollection.addAll((Collection<Object>) iterable);
            newCollection.add(item);
            return newCollection; // Return the modified collection
        } else {
            throw new IllegalArgumentException("The first argument must be a Collection or List.");
        }
    }
}
