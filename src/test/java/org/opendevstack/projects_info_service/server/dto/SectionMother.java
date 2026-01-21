package org.opendevstack.projects_info_service.server.dto;

import java.util.List;

public class SectionMother {

    public static Section of() {
        return Section.builder()
                .section("section")
                .tooltip("tooltip")
                .links(List.of(LinkMother.of()))
                .build();
    }
}
