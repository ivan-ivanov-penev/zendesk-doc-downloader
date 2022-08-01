package co.citizenlab.service.zendesk.api.resources;

import java.util.List;

public abstract class BasicResponse<T> {

    public final List<Item<T>> items;

    public BasicResponse(List<Item<T>> items) {

        this.items = items;
    }

    @Override
    public String toString() {
        return "ResponseBody{" +
                "items=" + items +
                '}';
    }
}
