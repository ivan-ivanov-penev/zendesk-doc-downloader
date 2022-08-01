package co.citizenlab.service.zendesk.api.resources;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * This is a generic POJO that has all the fields for the 'contact', 'lead' and 'deal' resources.
 *
 * Ideally those resources should correspond each to its own separate class but the current solution although a bit
 * hacky saves a lot of coding. The biggest downside is a huge switch-case statement in the 'findResourceName()' method
 * of the ZendeskApiService class
 *
 * @see co.citizenlab.service.zendesk.api.ZendeskApiService#findResourceName(Document, ResourceResponse)
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Resource {

    public final long id;

    public final String name;

    public final String firstName;

    public final String lastName;

    public final String organizationName;

    @JsonCreator
    public Resource(
            @JsonProperty("id") long id,
            @JsonProperty("name") String name,
            @JsonProperty("first_name") String firstName,
            @JsonProperty("last_name") String lastName,
            @JsonProperty("organization_name") String organizationName) {

        this.id = id;
        this.name = name;
        this.firstName = firstName;
        this.lastName = lastName;
        this.organizationName = organizationName;
    }

    @Override
    public String toString() {
        return "Resource{" +
                "id=" + id +
                '}';
    }
}
