package ru.yandex.practicum;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.processors.EventsSimilarityProcessor;
import ru.yandex.practicum.processors.UserActionsProcessor;

@RequiredArgsConstructor
@Component
public class AnalyzerRunner implements CommandLineRunner {

    private final EventsSimilarityProcessor eventsSimilarityProcessor;
    private final UserActionsProcessor userActionsProcessor;

    @Override
    public void run(String... args) throws Exception {
        Thread eventSimilarityThread = new Thread(eventsSimilarityProcessor);
        eventSimilarityThread.setName("EventsSimilarityHandlerThread");
        eventSimilarityThread.start();

        userActionsProcessor.start();
    }
}
