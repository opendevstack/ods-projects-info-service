package org.opendevstack.projects_info_service.server.model;

public class OpenshiftProjectClusterMother {

    public static OpenshiftProjectCluster of() {
        return OpenshiftProjectCluster.builder()
                .project("mother-project-key")
                .cluster("mother-cluster")
                .build();
    }
}
