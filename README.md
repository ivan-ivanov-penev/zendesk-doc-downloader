# Introduction

The `zendesk-doc-downloader` application is used for downloading all 
[documents](https://developer.zendesk.com/api-reference/sales-crm/resources/documents) from the Zendesk REST API (which 
is currently 'v2'). This is done by searching all 
[lead](https://developer.zendesk.com/api-reference/sales-crm/resources/leads), 
[contact](https://developer.zendesk.com/api-reference/sales-crm/resources/contacts) and 
[deal](https://developer.zendesk.com/api-reference/sales-crm/resources/deals) resources for the corresponding documents. 
The full documentation can be found [here](https://developer.zendesk.com/documentation "Zendesk API documentation") 

---

# Authentication

The Zendesk API implements the OAuth 2.0 protocol and the way this application authorizes itself is via 'Access Token'
which can be created by any user with sufficient permission via the WEB UI of Zendesk from the profile menu - in the URL 
ending in `https://...com/settings/oauth/tokens` - more info can be found
[here](https://developer.zendesk.com/documentation/ticketing/working-with-oauth/creating-and-using-oauth-tokens-with-the-api/)

---

# Configuration

There are two fields that need setting up in the configuration file 
[application.properties](src/main/resources/application.properties):
1. `zendesk.access.token` which as specified in the above 'Authentication' section can be created via the WEB UI
2. `docs.dir.base` which is the local output directory in which all downloaded documents will be stored

---

# Document structure

The document structure in the configurable output directory will be crated as follows:
* All documents belonging to the 'deal' resource type are placed under 
  `${docs.dir.base}/deal/${NAME-OF-DEAL}${ORIGINAL-DOC-NAME}` where the `NAME-OF-DEAL` is obtained from the Zendesk API 
  and corresponds to the deal of the according document.
* All documents belonging to the 'lead' resource type are placed under
  `${docs.dir.base}/lead/${FULL-NAME-AND-ORGANIZATION-NAME-OF-LEAD}${ORIGINAL-DOC-NAME}` where the
  `FULL-NAME-AND-ORGANIZATION-NAME-OF-LEAD` is obtained from the Zendesk API and corresponds to the lead of the 
  according document.
* All documents belonging to the 'contact' resource type are placed under
  `${docs.dir.base}/contact/${FULL-NAME-AND-NAME-OF-LEAD}/${ORIGINAL-DOC-NAME}` where the `FULL-NAME-AND-NAME-OF-LEAD` 
  is obtained from the Zendesk API and corresponds to the contact of the according document.

In all of the above cases the `${ORIGINAL-DOC-NAME}` is the original name of the document (with extension) with which it 
was stored when uploaded to Zendesk. Naturally it is obtained through the Zendesk API.

This will result in the following example structure:

```
${docs.dir.base}
│
├── contact
│   ├── Elon-Musk-Tesla
│   │   └── bank-account-details.docx
│   └── Ivan-Penev-Documaster
│       └── CV.pdf
├── deal
│   ├── Awesome Deal with Client
│   │   └── contract.pdf
│   └── Facebook deal for API integration
│       ├── contract-02-15.pdf
│       └── REST_API_documentation.pdf
└── lead
    ├── Bill-Gates-Microsoft
    │   └── Funding.xlsx
    └── Christian-Bale-Hollywood
        └── New car - FvsF.pdf
```

---

# Broken document links

Performing the document download consists of 2 phases - first is fetching the information for the documents which 
includes 'file_name', 'file_size', 'download_url' etc., and second is using the 'download_url' to download the document
itself. For some documents the 'download_url' returns HTTP status 404 'Not Found' (which means that either the URL is 
broken or the document is lost). For such documents there is little the application can do - this is an issue which can 
be resolved only by Zendesk. Information about such documents is logged in a CSV format into a separate log file called 
`missing-documents.csv` in the configurable root logging directory (check the full logging configuration in
[log4j2.xml](src/main/resources/log4j2.xml) configuration file).

---

# Note

The application was created for specific client - CitizenLab - to cover their use case. From this point of view it
has more in common with a one-time execution script than an actual service. It doesn't have a lot of safety-checks, and
it can be broken relatively easily. The reason for not making it more robust is to reduce costs from the client's
perspective - the app simply had to cover the 'happy' case and do this with minimal coding time.
With that being said use it with caution and preferably more as a guideline rather than a production tool.
