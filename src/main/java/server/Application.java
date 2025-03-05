package server;

import io.micronaut.runtime.Micronaut;
import io.micronaut.serde.annotation.SerdeImport;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import server.common.dto.Motion;
import server.motion.model.MotionMessage;
import server.socket.model.SocketMessage;
import server.socket.model.SocketResponse;

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
@SerdeImport(MotionMessage.class)
@SerdeImport(Motion.class)
@SerdeImport(SocketMessage.class)
@SerdeImport(SocketResponse.class)
public class Application {

    public static void main(String[] args) {
        Micronaut.run(Application.class, args);
    }
}
