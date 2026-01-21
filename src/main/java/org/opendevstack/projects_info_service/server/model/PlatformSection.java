package org.opendevstack.projects_info_service.server.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class PlatformSection {
    private String section;
    private String tooltip;
    private List<PlatformLink> links;
}