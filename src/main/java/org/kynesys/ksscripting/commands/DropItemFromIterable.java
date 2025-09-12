package org.kynesys.ksscripting.commands;

import org.kynesys.ksscripting.objects.KSScriptingNull;
import org.kynesys.lwks.KSExecutionSession;
import org.kynesys.lwks.KSScriptingExecutable;

import java.util.Collection;
import java.util.List;

public class DropItemFromIterable implements KSScriptingExecutable {
    @Override
    public String returnType() {
        return Object.class.getName();
    }

    @Override
    public Object execute(Object[] args, KSExecutionSession session) throws Exception {
        // Usage: DropItemFromIterable <iterable> <index>
        if (args.length != 2) {
            throw new IllegalArgumentException("DropItemFromIterable requires exactly two arguments.");
        }

        Object iterable = args[0];
        Object index = args[1];
        if (KSScriptingNull.isNull(iterable)) {
            throw new IllegalArgumentException("The iterable cannot be null.");
        }
        if (KSScriptingNull.isNull(index)) {
            throw new IllegalArgumentException("The index cannot be null.");
        }

        int indexValue;
        if (index instanceof Number) {
            indexValue = ((Number) index).intValue();
        } else if (index instanceof String) {
            try {
                indexValue = Integer.parseInt((String) index);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("The index must be a number or a string that can be parsed to an integer.");
            }
        } else {
            throw new IllegalArgumentException("The index must be a number or a string that can be parsed to an integer.");
        }

        if (iterable instanceof List<?>) {
            // Make a copy of the list to avoid modifying the original
            List<Object> newList = ((List<Object>) iterable).getClass().newInstance();
            newList.addAll((List<Object>) iterable);
            if (indexValue < 0 || indexValue >= newList.size()) {
                throw new IndexOutOfBoundsException("Index out of bounds: " + indexValue);
            }
            newList.remove(indexValue);
            // Remove the item at the specified index
            return newList; // Return the modified list
        } else if (iterable instanceof Collection<?>) {
            // Make a copy of the collection to avoid modifying the original
            Collection<Object> newCollection = ((Collection<Object>) iterable).getClass().newInstance();
            newCollection.addAll((Collection<Object>) iterable);
            if (indexValue < 0 || indexValue >= newCollection.size()) {
                throw new IndexOutOfBoundsException("Index out of bounds: " + indexValue);
            }
            // Remove the item at the specified index
            int i = 0;
            for (Object item : newCollection) {
                if (i == indexValue) {
                    newCollection.remove(item);
                    break;
                }
                i++;
            }
            return newCollection; // Return the modified collection
        } else {
            throw new IllegalArgumentException("The first argument must be a Collection or List.");
        }
    }
}
