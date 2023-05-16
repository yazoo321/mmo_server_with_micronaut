package server;

import io.micronaut.runtime.Micronaut;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;

@OpenAPIDefinition(
        info =
                @Info(
                        title = "Micronaut MMO server project",
                        version = "0.1",
                        description = "Hobby MMO server, integrated with Unreal Engine",
                        contact =
                                @Contact(
                                        url = "https://unreal-mmo-dev.com/",
                                        name = "Yaroslav Lazarev",
                                        email = "y.lazarev@hotmail.com")))
public class Application {

    public static void main(String[] args) {
        Micronaut.run(Application.class, args);
    }
}
