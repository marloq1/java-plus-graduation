package stats.service.collector;

import com.google.protobuf.Empty;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import stats.service.collector.handler.hub.grpc.GrpcUserActionHandler;
import ru.yandex.practicum.grpc.telemetry.user.UserActionProto;


@GrpcService
public class UserActionController extends UserActionControllerGrpc.UserActionControllerImplBase {


    private final GrpcUserActionHandler userActionHandler;

    public UserActionController(GrpcUserActionHandler userActionHandler) {
        this.userActionHandler = userActionHandler;
    }


    @Override
    public void collectUserAction(UserActionProto request, StreamObserver<Empty> responseObserver) {
        try {
                userActionHandler.handle(request);

            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(new StatusRuntimeException(
                    Status.INTERNAL
                            .withDescription(e.getLocalizedMessage())
                            .withCause(e)
            ));
        }
    }


}