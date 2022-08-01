package co.citizenlab.service.zendesk.api;

import co.citizenlab.service.FileResolver;
import co.citizenlab.service.zendesk.api.resources.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ZendeskApiService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ZendeskApiService.class);

    private static final Logger MISSING_DOCS_LOGGER = LoggerFactory.getLogger("missing.docs");

    private static final Logger MISSING_CALLS_LOGGER = LoggerFactory.getLogger("missing.calls");

    private final String accessToken;

    private final String urlDeals;

    private final String urlLeads;

    private final String urlContacts;

    private final String urlDocuments;

    private final String urlCalls;

    private final int pageSize;

    private final FileResolver fileResolver;

    private final ObjectMapper objectMapper;

    @Autowired
    public ZendeskApiService(
            @Value("${zendesk.access.token}") String accessToken,
            @Value("${zendesk.url.deals}") String urlDeals,
            @Value("${zendesk.url.leads}") String urlLeads,
            @Value("${zendesk.url.contacts}") String urlContacts,
            @Value("${zendesk.url.documents}") String urlDocuments,
            @Value("${zendesk.url.calls}") String urlCalls,
            @Value("${zendesk.resource.page-size}") int pageSize,
            FileResolver fileResolver,
            ObjectMapper objectMapper) {

        this.accessToken = accessToken;
        this.urlDeals = urlDeals;
        this.urlLeads = urlLeads;
        this.urlContacts = urlContacts;
        this.urlDocuments = urlDocuments;
        this.urlCalls = urlCalls;
        this.pageSize = pageSize;
        this.fileResolver = fileResolver;
        this.objectMapper = objectMapper;
    }

    public void downloadAllDocuments() throws Exception {

        try (CloseableHttpClient client = HttpClientBuilder.create().build()) {

            fetchDocumentsForResource(client, urlLeads, "lead");
            fetchDocumentsForResource(client, urlContacts, "contact");
            fetchDocumentsForResource(client, urlDeals, "deal");

            fetchAllCalls(client);
        }
    }

    private void fetchDocumentsForResource(
            CloseableHttpClient client, String resourceUrl, String resourceType) throws Exception {

        boolean hasMore = true;

        for (int i = 1; hasMore; i++) {

            LOGGER.info("Fetching page '{}' for resource: {}", i, resourceType);

            String requestUrl = resourceUrl + "?page=" + i + "&per_page=" + pageSize;

            ResourceResponse resourceResponse = executeGetRequest(client, requestUrl, ResourceResponse.class);

            hasMore = resourceResponse.items.size() == pageSize;

            fetchDocuments(client, resourceType, resourceResponse);
        }
    }

    private <T>T executeGetRequest(
            CloseableHttpClient client, String requestUrl, Class<T> responseType) throws Exception {

        HttpGet request = prepareGetRequest(requestUrl);

        try (CloseableHttpResponse response = client.execute(request)) {

            validateResponse(response);

            return objectMapper.readValue(response.getEntity().getContent(), responseType);
        }
    }

    private <T>T executeGetRequest(
            CloseableHttpClient client, String requestUrl, TypeReference<T> typeReference) throws Exception {

        HttpGet request = prepareGetRequest(requestUrl);

        try (CloseableHttpResponse response = client.execute(request)) {

            validateResponse(response);

            return objectMapper.readValue(response.getEntity().getContent(), typeReference);
        }
    }

    private HttpGet prepareGetRequest(String requestUrl) {

        HttpGet request = new HttpGet(requestUrl);
        request.setHeader("Accept", "application/json");
        request.setHeader("Authorization", "Bearer " + accessToken);
        request.setHeader("Connection", "Keep-Alive");
        request.setHeader("Keep-Alive", "timeout=10, max=1000");

        return request;
    }

    private void validateResponse(CloseableHttpResponse response) throws Exception {

        StatusLine statusLine = response.getStatusLine();

        if (statusLine.getStatusCode() != 200) {

            String body = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);

            throw new RuntimeException("Response returned bad status code: " + statusLine + "\n" + body);
        }
    }

    public void fetchDocuments(
            CloseableHttpClient client, String resourceType, ResourceResponse resourceResponse) throws Exception {

        List<String> resourceIds = resourceResponse.items.stream().map(
                e -> String.valueOf(e.data.id)).collect(Collectors.toList());

        boolean hasMore = true;

        for (int i = 1; hasMore; i++) {

            String url = urlDocuments
                    + "?page=" + i
                    + "&per_page=" + pageSize
                    + "&resource_type=" + resourceType
                    + "&resource_id=" + String.join(",", resourceIds);

            DocumentResponse documentResponse = executeGetRequest(client, url, DocumentResponse.class);

            hasMore = documentResponse.items.size() == pageSize;

            downloadDocumentsToFolder(client, resourceType, documentResponse, resourceResponse);
        }
    }

    private void downloadDocumentsToFolder(
            CloseableHttpClient client,
            String resourceType,
            DocumentResponse documentResponse,
            ResourceResponse resourceResponse) throws Exception {

        LOGGER.info("Found '{}' documents for current page", documentResponse.items.size());

        for (Item<Document> item : documentResponse.items) {

            Document document = item.data;

            HttpGet request = new HttpGet(document.downloadUrl);

            try (CloseableHttpResponse response = client.execute(request)) {

                attemptToDownload(response, document, resourceType, resourceResponse);
            }
        }
    }

    private void attemptToDownload(
            CloseableHttpResponse response,
            Document document,
            String resourceType,
            ResourceResponse resourceResponse) throws Exception {

        String resourceName = findResourceName(document, resourceResponse);

        StatusLine statusLine = response.getStatusLine();

        if (statusLine.getStatusCode() == 404) {

            MISSING_DOCS_LOGGER.info(
                    "{},\"{}\",\"{}\",{}", document.createdAt, document.name, resourceName, document.downloadUrl);
        }
        else {

            validateResponse(response);

            try (FileOutputStream fos = new FileOutputStream(
                    fileResolver.resolveDocName(document, resourceType, resourceName))) {

                IOUtils.copy(response.getEntity().getContent(), fos);
            }
        }
    }

    private String findResourceName(Document document, ResourceResponse resourceResponse) {

        @SuppressWarnings("OptionalGetWithoutIsPresent")
        Resource resource = resourceResponse.items.stream().filter(
                e -> e.data.id == document.resourceId).findFirst().get().data;

        return assembleResourceName(document.resourceType, resource);
    }

    private String assembleResourceName(String resourceName, Resource resource) {

        String result;

        switch (resourceName) {

            case "deal":
                result = resource.name.replaceAll("/", ""); // Replace the 'dir-separator' char that the guys placed
                break;

            case "lead":
                result = (resource.firstName + "-" + resource.lastName + "-" + resource.organizationName).replaceAll("/", "");
                break;

            case "contact":
                result = (resource.firstName + "-" + resource.lastName + "-" + resource.name).replaceAll("/", "");
                break;

            default:
                throw new RuntimeException("Unreachable code! Resource type should be known: " + resourceName);
        }

        return result.length() > 180 ? result.substring(0, 180) : result;
    }

    private void fetchAllCalls(CloseableHttpClient client) throws Exception {

        boolean hasMore = true;

        for (int i = 1; hasMore; i++) {

            LOGGER.info("Fetching page '{}' for 'calls'", i);

            String requestUrl = urlCalls + "?page=" + i + "&per_page=" + pageSize;

            CallResponse callResponse = executeGetRequest(client, requestUrl, CallResponse.class);

            hasMore = callResponse.items.size() == pageSize;

            downloadCalls(client, callResponse);
        }
    }

    private void downloadCalls(CloseableHttpClient client, CallResponse callResponse) throws Exception {

        for (Item<Call> callItem : callResponse.items)  {

            Call call = callItem.data;

            if (call.recordingUrl != null) {

                if (call.resourceType == null) {

                    downloadCall(client, call, "00_UNKNOWN");
                }
                else {

                    // resource type can only be either 'lead' or 'contact'
                    String resourceUrl = "lead".equals(call.resourceType) ? urlLeads : urlContacts;

                    Item<Resource> resourceItem = executeGetRequest(
                            client, resourceUrl + "/" + call.resourceId, new TypeReference<Item<Resource>>() {});

                    String resourceName = assembleResourceName(call.resourceType, resourceItem.data);

                    downloadCall(client, call, resourceName);
                }
            }
        }
    }

    private void downloadCall(CloseableHttpClient client, Call call, String resourceName) throws Exception {

        LOGGER.info("Downloading call-recording to: {}", resourceName);

        HttpGet request = new HttpGet(call.recordingUrl);

        try (CloseableHttpResponse response = client.execute(request)) {

            if (response.getStatusLine().getStatusCode() == 404) {

                MISSING_CALLS_LOGGER.info(
                        "{},\"{}\",\"{}\",{}", call.madeAt, call.summary, resourceName, call.recordingUrl);
            }
            else {

                validateResponse(response);

                String callName = fileResolver.resolveCallName(call, resourceName);

                try (FileOutputStream fos = new FileOutputStream(callName + ".wav")) {

                    IOUtils.copy(response.getEntity().getContent(), fos);
                }

                if (call.summary != null && call.summary.length() > 0) {

                    FileUtils.writeStringToFile(new File(callName + "-summary.txt"), call.summary, StandardCharsets.UTF_8);
                }
            }
        }
    }
}
