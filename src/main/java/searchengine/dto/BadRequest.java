package searchengine.dto;

import lombok.Value;

@Value
public class BadRequest {
    boolean result;
    String error;
}
