package gov.nist.emp.bankcard.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.OAuthFlow;
import io.swagger.v3.oas.models.security.OAuthFlows;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.security.Scopes;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        // Register a simple Bearer JWT scheme so the Swagger UI shows an "Authorize" button
        Components components = new Components()
                .addSecuritySchemes("bearerAuth",
                        new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                );

        OpenAPI openAPI = new OpenAPI()
                .components(components)
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));

        // Example: OAuth2 (Authorization Code with PKCE) configuration.
        // If you prefer interactive login from the Swagger UI, you can add an OAuth2 flow here
        // configured to your Okta custom authorization server. Example (uncomment and adjust):
        // components.addSecuritySchemes("oauth2",
        //      new SecurityScheme()
        //          .type(SecurityScheme.Type.OAUTH2)
        //          .flows(new OAuthFlows()
        //              .authorizationCode(new OAuthFlow()
        //                  .authorizationUrl("https://{yourOktaDomain}/oauth2/{authServerId}/v1/authorize")
        //                  .tokenUrl("https://{yourOktaDomain}/oauth2/{authServerId}/v1/token")
        //                  .scopes(new Scopes().addString("openid","OpenID scope").addString("api.read","API read"))
        //              )
        //          )
        // );

        return openAPI;
    }
}
