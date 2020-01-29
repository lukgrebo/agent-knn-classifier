package pl.wut.sag.classification.protocol.common;

import jade.domain.FIPAAgentManagement.ServiceDescription;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

public class TargetedStep<T extends Protocol, C> extends ProtocolStep<T, C> {

    @Getter
    private final ServiceDescription targetService;

    @Builder(builderMethodName = "targetedBuilder")
    protected TargetedStep(@NonNull final String stepName,
                           @NonNull final int performative,
                           @NonNull final Class<C> messageClass,
                           @NonNull final T protocol,
                           @NonNull final ServiceDescription targetService) {
        super(stepName, performative, messageClass, protocol);
        this.targetService = targetService;
    }
}
