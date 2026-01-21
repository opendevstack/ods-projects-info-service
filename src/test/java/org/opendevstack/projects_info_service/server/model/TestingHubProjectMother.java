package org.opendevstack.projects_info_service.server.model;

public class TestingHubProjectMother {
    public static TestingHubProject of(String key, String id) {
        return new TestingHubProject(id, key);
    }
}
