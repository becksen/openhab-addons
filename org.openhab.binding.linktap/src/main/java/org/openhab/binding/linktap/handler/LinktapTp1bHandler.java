package org.openhab.binding.linktap.handler;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatusInfo;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerCallback;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.linktap.internal.data.TP1B;

public class LinktapTp1bHandler extends LinktapBaseHandler<TP1B> {

    public LinktapTp1bHandler(Thing thing) {
        super(thing, TP1B.class);
    }

    /*
     * @Override
     * public @NonNull Thing getThing() {
     * // TODO Auto-generated method stub
     * return null;
     * }
     */

    @Override
    public void initialize() {
        // TODO Auto-generated method stub

    }

    @Override
    public void dispose() {
        // TODO Auto-generated method stub

    }

    @Override
    public void setCallback(@Nullable ThingHandlerCallback thingHandlerCallback) {
        // TODO Auto-generated method stub

    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // TODO Auto-generated method stub

    }

    @Override
    public void handleUpdate(ChannelUID channelUID, State newState) {
        // TODO Auto-generated method stub

    }

    @Override
    public void handleConfigurationUpdate(Map<@NonNull String, @NonNull Object> configurationParameters) {
        // TODO Auto-generated method stub

    }

    @Override
    public void thingUpdated(Thing thing) {
        // TODO Auto-generated method stub

    }

    @Override
    public void channelLinked(ChannelUID channelUID) {
        // TODO Auto-generated method stub

    }

    @Override
    public void channelUnlinked(ChannelUID channelUID) {
        // TODO Auto-generated method stub

    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        // TODO Auto-generated method stub

    }

    @Override
    public void handleRemoval() {
        // TODO Auto-generated method stub

    }

    @Override
    protected @NonNull State getChannelState(@NonNull ChannelUID channelUID, TP1B data) {
        // TODO Auto-generated method stub
        return UnDefType.UNDEF;
    }

    @Override
    protected void update(TP1B oldData, TP1B data) {
        // TODO Auto-generated method stub

    }

}
