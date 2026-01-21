package org.opendevstack.projects_info_service.server.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class PlatformLink {
    private String label;
    private String url;
    private String type;
    private String tooltip;
}