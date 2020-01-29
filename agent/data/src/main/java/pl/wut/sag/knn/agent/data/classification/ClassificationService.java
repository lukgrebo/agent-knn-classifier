package pl.wut.sag.knn.agent.data.classification;

import pl.wut.sag.knn.protocol.classy.model.TrainingRequest;

public interface ClassificationService {
    void processRequest(final TrainingRequest trainingRequest);
}

