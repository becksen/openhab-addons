package org.openhab.binding.linktap.internal.data;

import java.util.Map;

public class linktapDevices {

    // private Map<String, Thermostat> thermostats;
    // private Map<String, SmokeDetector> smokeCoAlarms;
    // private Map<String, TP1B> cameras;
    private Map<String, TP1B> watertimers;

    /**
     * Id to thermostat mapping
     * public Map<String, Thermostat> getThermostats() {
     * return thermostats;
     * }
     */

    /** Id to TP1B mapping */
    // public Map<String, TP1B> getCameras() {
    public Map<String, TP1B> getWatertimers() {
        return watertimers;
    }

    /**
     * Not required
     * Id to smoke detector
     * public Map<String, SmokeDetector> getSmokeCoAlarms() {
     * return smokeCoAlarms;
     * }
     */

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        linktapDevices other = (linktapDevices) obj;
        if (watertimers == null) {
            if (other.watertimers != null) {
                return false;
            }
        } else if (!watertimers.equals(other.watertimers)) {
            return false;
        }
        /*
         * if (smokeCoAlarms == null) {
         * if (other.smokeCoAlarms != null) {
         * return false;
         * }
         * } else if (!smokeCoAlarms.equals(other.smokeCoAlarms)) {
         * return false;
         * }
         * if (thermostats == null) {
         * if (other.thermostats != null) {
         * return false;
         * }
         * } else if (!thermostats.equals(other.thermostats)) {
         * return false;
         * }
         */
        return true;

    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((watertimers == null) ? 0 : watertimers.hashCode());
        // result = prime * result + ((smokeCoAlarms == null) ? 0 : smokeCoAlarms.hashCode());
        // result = prime * result + ((thermostats == null) ? 0 : thermostats.hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        /*
         * builder.append("linktapDevices [thermostats=").append(thermostats).append(", smokeCoAlarms=").append(
         * smokeCoAlarms)
         * .append(", watertimers=").append(watertimers).append("]");
         */

        builder.append("linktapDevices [watertimers=").append(watertimers).append("]");

        return builder.toString();
    }
}
