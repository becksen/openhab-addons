package org.openhab.binding.linktap.internal.update;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.linktap.internal.data.TopLevelData;
import org.openhab.binding.linktap.internal.data.linktapIdentifiable;
import org.openhab.binding.linktap.internal.listener.linktapThingDataListener;

/**
 * Handles all linktap data updates through delegation to the {@link LinktapUpdateHandler} for the respective data type.
 *
 * @author Timmy Becker - Initial contribution
 */
@NonNullByDefault
public class LinktapCompositeUpdateHandler {
    private final Supplier<Set<String>> presentLinktapIdsSupplier;
    private final Map<Class<?>, @Nullable LinktapUpdateHandler<?>> updateHandlersMap = new ConcurrentHashMap<>();

    public LinktapCompositeUpdateHandler(Supplier<Set<String>> presentNestIdsSupplier) {
        this.presentLinktapIdsSupplier = presentNestIdsSupplier;
    }

    public <T> boolean addListener(Class<T> dataClass, linktapThingDataListener<T> listener) {
        return getOrCreateUpdateHandler(dataClass).addListener(listener);
    }

    public <T> boolean addListener(Class<T> dataClass, String nestId, linktapThingDataListener<T> listener) {
        return getOrCreateUpdateHandler(dataClass).addListener(nestId, listener);
    }

    private Set<String> findMissingLinktapIds(Set<linktapIdentifiable> updates) {
        Set<String> nestIds = updates.stream().map(u -> u.getId()).collect(Collectors.toSet());
        Set<String> missingNestIds = presentLinktapIdsSupplier.get();
        missingNestIds.removeAll(nestIds);
        return missingNestIds;
    }

    public @Nullable <T> T getLastUpdate(Class<T> dataClass, String nestId) {
        return getOrCreateUpdateHandler(dataClass).getLastUpdate(nestId);
    }

    public <T> List<T> getLastUpdates(Class<T> dataClass) {
        return getOrCreateUpdateHandler(dataClass).getLastUpdates();
    }

    private Set<linktapIdentifiable> getLinktapUpdates(TopLevelData data) {
        Set<linktapIdentifiable> updates = new HashSet<>();
        if (data.getDevices() != null) {
            if (data.getDevices().getWatertimers() != null) {
                updates.addAll(data.getDevices().getWatertimers().values());
            }
            /*
             * if (data.getDevices().getSmokeCoAlarms() != null) {
             * updates.addAll(data.getDevices().getSmokeCoAlarms().values());
             * }
             * if (data.getDevices().getThermostats() != null) {
             * updates.addAll(data.getDevices().getThermostats().values());
             * }
             */
        }
        if (data.getStructures() != null) {
            updates.addAll(data.getStructures().values());
        }
        return updates;
    }

    @SuppressWarnings("unchecked")
    private <T> LinktapUpdateHandler<T> getOrCreateUpdateHandler(Class<T> dataClass) {
        LinktapUpdateHandler<T> handler = (LinktapUpdateHandler<T>) updateHandlersMap.get(dataClass);
        if (handler == null) {
            handler = new LinktapUpdateHandler<>();
            updateHandlersMap.put(dataClass, handler);
        }
        return handler;
    }

    @SuppressWarnings("unchecked")
    public void handleUpdate(TopLevelData data) {
        Set<linktapIdentifiable> updates = getLinktapUpdates(data);
        updates.forEach(update -> {
            Class<linktapIdentifiable> updateClass = (Class<linktapIdentifiable>) update.getClass();
            getOrCreateUpdateHandler(updateClass).handleUpdate(updateClass, update.getId(), update);
        });

        Set<String> missingNestIds = findMissingLinktapIds(updates);
        if (!missingNestIds.isEmpty()) {
            updateHandlersMap.values().forEach(handler -> {
                if (handler != null) {
                    handler.handleMissingNestIds(missingNestIds);
                }
            });
        }
    }

    public <T> boolean removeListener(Class<T> dataClass, linktapThingDataListener<T> listener) {
        return getOrCreateUpdateHandler(dataClass).removeListener(listener);
    }

    public <T> boolean removeListener(Class<T> dataClass, String nestId, linktapThingDataListener<T> listener) {
        return getOrCreateUpdateHandler(dataClass).removeListener(nestId, listener);
    }

    public void resendLastUpdates() {
        updateHandlersMap.values().forEach(handler -> {
            if (handler != null) {
                handler.resendLastUpdates();
            }
        });
    }

}
