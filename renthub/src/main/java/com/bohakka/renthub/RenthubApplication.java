package com.bohakka.renthub;

import java.util.Locale;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class RenthubApplication {

	/** All end-user texts are Ukrainian; the default locale also covers non-request threads (async, scheduler). */
	public static final Locale UI_LOCALE = Locale.forLanguageTag("uk");

	public static void main(String[] args) {
		Locale.setDefault(UI_LOCALE);
		SpringApplication.run(RenthubApplication.class, args);
	}

}
