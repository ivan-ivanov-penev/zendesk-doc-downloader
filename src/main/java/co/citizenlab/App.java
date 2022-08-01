package co.citizenlab;

import co.citizenlab.service.zendesk.api.ZendeskApiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class App {

    private static final Logger LOGGER = LoggerFactory.getLogger(App.class);

    public static void main( String[] args ) {

        try {

            LOGGER.info("Starting application");

            ApplicationContext applicationContext = new AnnotationConfigApplicationContext("co.citizenlab");
            applicationContext.getBean(ZendeskApiService.class).downloadAllDocuments();

            LOGGER.info("Application finished execution successfully");
        }
        catch (Exception e) {

            LOGGER.error("An error occurred during execution!", e);
        }
    }
}
