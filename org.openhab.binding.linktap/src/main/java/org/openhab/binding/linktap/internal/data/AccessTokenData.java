package org.openhab.binding.linktap.internal.data;

public class AccessTokenData {
    private String accessToken;
    private Long expiresIn;

    public String getAccessToken() {
        return accessToken;
    }

    public Long getExpiresIn() {
        return expiresIn;
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
        AccessTokenData other = (AccessTokenData) obj;
        if (accessToken == null) {
            if (other.accessToken != null) {
                return false;
            }
        } else if (!accessToken.equals(other.accessToken)) {
            return false;
        }
        if (expiresIn == null) {
            if (other.expiresIn != null) {
                return false;
            }
        } else if (!expiresIn.equals(other.expiresIn)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((accessToken == null) ? 0 : accessToken.hashCode());
        result = prime * result + ((expiresIn == null) ? 0 : expiresIn.hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("AccessTokenData [accessToken=").append(accessToken).append(", expiresIn=").append(expiresIn)
                .append("]");
        return builder.toString();
    }
}
