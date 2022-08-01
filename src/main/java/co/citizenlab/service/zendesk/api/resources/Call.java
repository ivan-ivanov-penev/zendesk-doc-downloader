package co.citizenlab.service.zendesk.api.resources;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Call {

    public final String madeAt;

    public final String recordingUrl;

    public final String summary;

    public final String resourceType;

    public final long resourceId;

    @JsonCreator
    public Call(
            @JsonProperty("made_at") String madeAt,
            @JsonProperty("recording_url") String recordingUrl,
            @JsonProperty("summary") String summary,
            @JsonProperty("resource_type") String resourceType,
            @JsonProperty("resource_id") long resourceId) {

        this.madeAt = madeAt;
        this.recordingUrl = recordingUrl;
        this.summary = summary;
        this.resourceType = resourceType;
        this.resourceId = resourceId;
    }

    @Override
    public String toString() {
        return "Call{" +
                "madeAt='" + madeAt + '\'' +
                "recordingUrl='" + recordingUrl + '\'' +
                ", summary='" + summary + '\'' +
                ", resourceType='" + resourceType + '\'' +
                ", resourceId='" + resourceId + '\'' +
                '}';
    }
}
