package au.alabri.hisham

import au.alabri.hisham.proto.Greeter
import au.alabri.hisham.proto.HelloReply
import au.alabri.hisham.proto.HelloRequest
import io.quarkus.grpc.GrpcService
import io.smallrye.mutiny.Uni

@GrpcService
class HelloService : Greeter {

    override fun sayHello(request: HelloRequest?): Uni<HelloReply> {
        return Uni.createFrom().item(HelloReply.newBuilder().setMessage("Hello ${request?.name}").build())
    }

}