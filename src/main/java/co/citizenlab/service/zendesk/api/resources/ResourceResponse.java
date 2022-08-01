package co.citizenlab.service.zendesk.api.resources;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ResourceResponse extends BasicResponse<Resource> {

    @JsonCreator
    public ResourceResponse(@JsonProperty("items") List<Item<Resource>> items) {

        super(items);
    }
}
