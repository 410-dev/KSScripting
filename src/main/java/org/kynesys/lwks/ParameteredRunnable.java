package org.kynesys.lwks;

import java.io.Serializable;

@FunctionalInterface
public interface ParameteredRunnable extends Serializable {
    void run(Object... args);
}