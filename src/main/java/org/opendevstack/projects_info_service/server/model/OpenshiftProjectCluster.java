package org.opendevstack.projects_info_service.server.model;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@Builder
@EqualsAndHashCode
@ToString
public class OpenshiftProjectCluster {
    String project;
    String cluster;
}
