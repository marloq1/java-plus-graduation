package ru.yandex.practicum;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import ru.yandex.practicum.grpc.telemetry.user.InteractionsCountRequestProto;
import ru.yandex.practicum.grpc.telemetry.user.RecommendedEventProto;
import ru.yandex.practicum.grpc.telemetry.user.SimilarEventsRequestProto;
import ru.yandex.practicum.grpc.telemetry.user.UserPredictionsRequestProto;
import stats.service.analyzer.RecommendationsControllerGrpc;


@GrpcService
@RequiredArgsConstructor
@Slf4j
public class AnalyzerController extends RecommendationsControllerGrpc.RecommendationsControllerImplBase {

    private final AnalyzerService analyzerService;

    @Override
    public void getRecommendationsForUser(UserPredictionsRequestProto request, StreamObserver<RecommendedEventProto> responseObserver) {
        log.info("getRecommendationsForUser");
        try {
            for (RecommendedEventProto recommendedEventProto: analyzerService.getRecommendationsForUser(request)) {
                responseObserver.onNext(recommendedEventProto);
            }
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(new StatusRuntimeException(
                    Status.INTERNAL
                            .withDescription(e.getLocalizedMessage())
                            .withCause(e)
            ));
        }
    }

    @Override
    public void getSimilarEvents(SimilarEventsRequestProto request, StreamObserver<RecommendedEventProto> responseObserver) {
        log.info("getSimilarEvents");
        try {
            for (RecommendedEventProto recommendedEventProto: analyzerService.getSimilarEvents(request)) {
                responseObserver.onNext(recommendedEventProto);
            }
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(new StatusRuntimeException(
                    Status.INTERNAL
                            .withDescription(e.getLocalizedMessage())
                            .withCause(e)
            ));
        }
    }

    @Override
    public void getInteractionsCount(InteractionsCountRequestProto request, StreamObserver<RecommendedEventProto> responseObserver) {
        log.info("getInteractionsCount");
        try {
            for (RecommendedEventProto recommendedEventProto: analyzerService.getInteractionsCount(request)) {
                responseObserver.onNext(recommendedEventProto);
            }
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
