package co.citizenlab.service.zendesk.api.resources;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Document {

    public final long resourceId;

    public final String resourceType;

    public final String name;

    public final String downloadUrl;

    public final String createdAt;

    @JsonCreator
    public Document(
            @JsonProperty("resource_id") long resourceId,
            @JsonProperty("name") String name,
            @JsonProperty("resource_type") String resourceType,
            @JsonProperty("download_url") String downloadUrl,
            @JsonProperty("created_at") String createdAt) {

        this.resourceId = resourceId;
        this.name = name;
        this.resourceType = resourceType;
        this.downloadUrl = downloadUrl;
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "Document{" +
                "name='" + name + '\'' +
                ", downloadUrl='" + downloadUrl + '\'' +
                ", createdAt='" + createdAt + '\'' +
                '}';
    }
}
