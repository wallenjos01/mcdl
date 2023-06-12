package org.wallentines.mcdl;

public class MinecraftVersion {

    private final String versionId;
    private final boolean snapshot;
    private final String definitionUrl;

    public MinecraftVersion(String versionId, boolean snapshot, String definitionUrl) {
        this.versionId = versionId;
        this.snapshot = snapshot;
        this.definitionUrl = definitionUrl;
    }

    public String getVersionId() {
        return versionId;
    }

    public boolean isSnapshot() {
        return snapshot;
    }

    public String getDefinitionUrl() {
        return definitionUrl;
    }
}
