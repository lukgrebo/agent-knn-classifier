package pl.wut.sag.classification.agent.user.interfaces.web.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CheckObjectResponse {
    private String message;
    private UUID objectId;

    public static CheckObjectResponse valid(final UUID objectId) {
        return new CheckObjectResponse("Poprawnie zlecono zadanie, o rezultat możesz odpytać dzięki podanemu id", objectId);
    }
}
