package com.example.propertyrentalmanagement.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GenericResponse {
    private String message;
    private Object data;
    // Avoid including resourceId in the response, because this will shows
    // as part of the Location header
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private UUID resourceId;

    @Builder.Default
    @JsonIgnore
    private HttpStatus status = HttpStatus.OK;

    public ResponseEntity<GenericResponse> buildResponse() {
        // Include all required response fields here
        ResponseEntity.BodyBuilder response = ResponseEntity.status(status);

        // Add all optional fields
        if (this.resourceId != null) {
            HttpHeaders locationHeader = new HttpHeaders();
            URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                    .path("/{id}")
                    .buildAndExpand(this.resourceId)
                    .toUri();

            locationHeader.setLocation(location);
            response.headers(locationHeader);
        }

        // Confirm response structure
        return response.body(this);
    }
}