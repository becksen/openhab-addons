package org.openhab.binding.linktap.internal.data;

public class linktapMetadata {
    private String accessToken;
    private String clientVersion;

    public String getAccessToken() {
        return accessToken;
    }

    public String getClientVersion() {
        return clientVersion;
    }

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
        linktapMetadata other = (linktapMetadata) obj;
        if (accessToken == null) {
            if (other.accessToken != null) {
                return false;
            }
        } else if (!accessToken.equals(other.accessToken)) {
            return false;
        }
        if (clientVersion == null) {
            if (other.clientVersion != null) {
                return false;
            }
        } else if (!clientVersion.equals(other.clientVersion)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((accessToken == null) ? 0 : accessToken.hashCode());
        result = prime * result + ((clientVersion == null) ? 0 : clientVersion.hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("NestMetadata [accessToken=").append(accessToken).append(", clientVersion=")
                .append(clientVersion).append("]");
        return builder.toString();
    }

}
