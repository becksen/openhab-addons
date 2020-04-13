package org.openhab.binding.linktap.internal.data;

public class ErrorData {
    private String error;
    private String type;
    private String message;
    private String instance;

    public String getError() {
        return error;
    }

    public String getType() {
        return type;
    }

    public String getMessage() {
        return message;
    }

    public String getInstance() {
        return instance;
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
        ErrorData other = (ErrorData) obj;
        if (error == null) {
            if (other.error != null) {
                return false;
            }
        } else if (!error.equals(other.error)) {
            return false;
        }
        if (instance == null) {
            if (other.instance != null) {
                return false;
            }
        } else if (!instance.equals(other.instance)) {
            return false;
        }
        if (message == null) {
            if (other.message != null) {
                return false;
            }
        } else if (!message.equals(other.message)) {
            return false;
        }
        if (type == null) {
            if (other.type != null) {
                return false;
            }
        } else if (!type.equals(other.type)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((error == null) ? 0 : error.hashCode());
        result = prime * result + ((instance == null) ? 0 : instance.hashCode());
        result = prime * result + ((message == null) ? 0 : message.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("ErrorData [error=").append(error).append(", type=").append(type).append(", message=")
                .append(message).append(", instance=").append(instance).append("]");
        return builder.toString();
    }

}
