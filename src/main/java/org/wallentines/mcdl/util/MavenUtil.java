package org.wallentines.mcdl.util;

import org.jetbrains.annotations.Nullable;
import org.wallentines.mcdl.Task;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;
import java.util.Stack;

public class MavenUtil {

    public static String getLatestVersion(String repo, ArtifactSpec spec) {

        String url = repo + "/" + spec.getMetadataPath();

        try {
            URL actualUrl = new URL(url);
            URLConnection conn = actualUrl.openConnection();

            SAXParser parser = SAXParserFactory.newDefaultInstance().newSAXParser();
            LatestVersionHandler handler = new LatestVersionHandler();
            parser.parse(conn.getInputStream(), handler);

            return handler.latestVersion;

        } catch (IOException | ParserConfigurationException | SAXException ex) {
            return null;
        }
    }

    public static Task.Result downloadArtifact(String repo, ArtifactSpec spec, File output) {
        String actualUrl = spec.getArtifactPath(repo);
        if(!DownloadUtil.downloadBytes(actualUrl, output)) {
            return Task.Result.error("Unable to download Fabric installer!");
        }
        return Task.Result.success();
    }


    public static class ArtifactSpec {
        private final String namespace;
        private final String id;
        private final @Nullable String version;

        public ArtifactSpec(String namespace, String id, @Nullable String version) {
            this.namespace = namespace;
            this.id = id;
            this.version = version;
        }

        public String getNamespace() {
            return namespace;
        }

        public String getId() {
            return id;
        }

        public @Nullable String getVersion() {
            return version;
        }

        public String getArtifactPath(String version) {
            return namespace.replace(".", "/") + "/%s/%s/%s-%s.jar".formatted(id, version, id, version);
        }

        public String getMetadataPath() {
            return namespace.replace(".", "/") + "/" + id + "/maven-metadata.xml";
        }

        public static ArtifactSpec parse(String input) throws ParseException {
            String[] parts = input.split(":");
            if(parts.length == 2) {
                return new ArtifactSpec(parts[0], parts[1], null);
            } else if(parts.length == 3) {
                return new ArtifactSpec(parts[0], parts[1], parts[2]);
            } else {
                String data = "Artifact spec needs 2 or 3 parts!";
                throw new ParseException(data, data.length() + input.length() - 1);
            }
        }
    }

    private static class LatestVersionHandler extends DefaultHandler {
        private String latestVersion = null;
        private final Stack<String> tags = new Stack<>();
        private final StringBuilder currentValue = new StringBuilder();

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) {

            tags.push(qName);
            currentValue.setLength(0);
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {

            String tag = tags.pop();
            if(!tag.equals(qName)) throw new SAXException("Corrupted stack!");

            if(tag.equals("latest") && tags.peek().equals("versioning") && tags.size() == 2) {
                latestVersion = currentValue.toString();
            }
        }

        @Override
        public void characters(char[] ch, int start, int length) {

            currentValue.append(ch, start, length);
        }
    }

}
