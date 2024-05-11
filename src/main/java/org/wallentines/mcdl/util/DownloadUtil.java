package org.wallentines.mcdl.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wallentines.mdcfg.ConfigObject;
import org.wallentines.mdcfg.codec.JSONCodec;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

public class DownloadUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger("DownloadUtil");

    public static ConfigObject downloadJSON(String url) {

        try {
            URL actualUrl = new URL(url);
            LOGGER.info("Downloading JSON from " + actualUrl);

            URLConnection conn = actualUrl.openConnection();

            return JSONCodec.loadConfig(new BufferedInputStream(conn.getInputStream()));

        } catch (IOException ex) {
            LOGGER.error("An error occurred while downloading a JSON file!", ex);
            return null;
        }
    }

    public static boolean downloadBytes(String url, File output) {

        try {
            URL actualUrl = new URL(url);
            URLConnection conn = actualUrl.openConnection();
            FileOutputStream outputStream = new FileOutputStream(output);

            int bytesRead;
            byte[] buffer = new byte[1024];
            while((bytesRead = conn.getInputStream().read(buffer, 0, 1024)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            outputStream.close();

            if(!output.setExecutable(true)) {
                LOGGER.warn("Unable to set executable bit on downloaded file " + output.getName() + "!");
            }

            return true;

        } catch (IOException ex) {

            LOGGER.error("An error occurred while downloading a file!", ex);
            return false;
        }

    }
}
