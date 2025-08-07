package ewm.src.main.java.ru.practicum;


import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.grpc.telemetry.user.ActionTypeProto;
import ru.yandex.practicum.grpc.telemetry.user.UserActionProto;
import stats.service.collector.UserActionControllerGrpc;

@Component
public class CollectorClient {


    @GrpcClient("collector")
    private UserActionControllerGrpc.UserActionControllerBlockingStub stub;


    public void collectUserAction(long userId, long eventId, String actionType) {
        stub.collectUserAction(UserActionProto.newBuilder()
                .setUserId(userId)
                .setEventId(eventId)
                .setActionType(ActionTypeProto.valueOf(actionType)).build());
    }




}
