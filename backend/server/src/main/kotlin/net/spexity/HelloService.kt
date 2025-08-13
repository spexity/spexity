package net.spexity

import net.spexity.proto.Greeter
import net.spexity.proto.HelloReply
import net.spexity.proto.HelloRequest
import io.quarkus.grpc.GrpcService
import io.smallrye.mutiny.Uni

@GrpcService
class HelloService : Greeter {

    override fun sayHello(request: HelloRequest?): Uni<HelloReply> {
        return Uni.createFrom().item(HelloReply.newBuilder().setMessage("Hello ${request?.name}").build())
    }

}