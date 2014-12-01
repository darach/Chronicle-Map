package net.openhft.chronicle.hash;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * @author Rob Austin.
 */
public interface FindByName {

    /**
     * @param name the name of the map or set
     * @param <T>  the type returned
     * @return a chronicle map or set
     * @throws IllegalArgumentException              if a map with this name can not be found
     * @throws IOException                           if it not possible to create the map or set
     * @throws java.util.concurrent.TimeoutException if the call times out
     * @throws java.util.concurrent.TimeoutException if interrupted by another thread
     */
    <T extends ChronicleHash> T from(String name) throws IllegalArgumentException,
            IOException, TimeoutException, InterruptedException;
}