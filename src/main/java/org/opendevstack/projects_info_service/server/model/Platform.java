package org.opendevstack.projects_info_service.server.model;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Platform {
    private String id;
    private String label;
    private String url;
    private String abbreviation;
}