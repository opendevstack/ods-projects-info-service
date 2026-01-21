package org.opendevstack.projects_info_service.server.model;

import org.opendevstack.projects_info_service.server.dto.Section;

import java.util.List;

public class PlatformSectionMother {

    public static PlatformSection of() {
        return PlatformSection.builder()
                .section("section")
                .links(List.of(PlatformLinkMother.of()))
                .build();
    }
}
