package com.honeycomb.colorphone.wallpaper.livewallpaper;

import android.support.v4.os.TraceCompat;
import android.text.TextUtils;

import com.honeycomb.colorphone.wallpaper.util.CommonUtils;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.utils.HSLog;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class LiveWallpaper {

    private static final String TAG = "LiveWallpaper";

    private String wallpaperName;
    private boolean success = true;

    int type = LiveWallpaperConsts.TYPE_SHADER_AND_CONFETTI;
    boolean isLocal;
    String shaderName;
    String baseUrl;
    public String shader;
    public ArrayList<String> shaderTexTureUrls = new ArrayList<>();
    public ArrayList<LiveConfettiSource> confettiSources = new ArrayList<>();
    public ArrayList<HashMap<String, Object>> confettiBgSettings = new ArrayList<>();
    public ArrayList<HashMap<String, Object>> confettiTouchSettings = new ArrayList<>();
    public ArrayList<HashMap<String, Object>> confettiClickSettings = new ArrayList<>();

    public LiveWallpaper(String wallpaperName, String baseUrl) {
        this.baseUrl = baseUrl;
        this.wallpaperName = wallpaperName;
        TraceCompat.beginSection(TAG + "#parseMetadata");
        try {
            parseMetadata();
        } catch (Exception e) {
            HSLog.e(TAG, "Error parsing wallpaper XML: " + wallpaperName + ", e: " + e);
            success = false;
        } finally {
            TraceCompat.endSection();
        }
    }
    public boolean touchable() {
        return type == LiveWallpaperConsts.TYPE_SHADER_AND_CONFETTI
                && (!confettiTouchSettings.isEmpty() || !confettiClickSettings.isEmpty());
    }

    public boolean isRipple() {
        return (shaderName != null && shaderName.contains("ripple"));
    }

    public boolean is3D() {
        return (shaderName != null && shaderName.contains("3d"));
    }

    public boolean successFlag() {
        return success;
    }

    private void parseMetadata()
            throws ParserConfigurationException, SAXException, IOException {
        if (parseMetadataFromAssets()) {
            // First, try parse metadata XML from assets.
            return;
        }
        if (parseMetadataFromInternalStorage()) {
            // Metadata is not placed in APK assets directory. It should have been downloaded
            // from remote and copied to internal storage by now.
            return;
        }
        throw new RuntimeException("Meta-data XML not found in assets and not downloaded from remote.");
    }

    private boolean parseMetadataFromAssets() throws IOException, ParserConfigurationException, SAXException {
        InputStream is = null;
        try {
            is = HSApplication.getContext().getAssets().open(
                    LiveWallpaperConsts.DIRECTORY + wallpaperName + ".xml");
        } catch (FileNotFoundException ignored) {
        }
        if (is != null) {
            try {
                parseMetadata(is);
            } finally {
                is.close();
            }
            return true;
        }
        return false;
    }

    private boolean parseMetadataFromInternalStorage() throws ParserConfigurationException, SAXException, IOException {
        InputStream is;
        File baseDirectory = CommonUtils.getDirectory(
                LiveWallpaperConsts.Files.LIVE_DIRECTORY + File.separator + wallpaperName);
        File file = new File(baseDirectory, wallpaperName + ".xml");
        if (file.exists()) {
            is = new FileInputStream(file);
            try {
                parseMetadata(is);
            } finally {
                is.close();
            }
            return true;
        }
        return false;
    }

    private void parseMetadata(InputStream is) throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(is);

        parseMetadata(doc);
        switch (type) {
            case LiveWallpaperConsts.TYPE_SHADER_AND_CONFETTI:
                parseShader(doc);
                parseConfetti(doc);
                break;
            case LiveWallpaperConsts.TYPE_VIDEO:
                break;
        }
    }

    private void parseMetadata(Document doc) {
        NamedNodeMap attributes = doc.getDocumentElement().getAttributes();

        // Type
        Node attribute = attributes.getNamedItem("type");
        if (attribute == null) {
            type = LiveWallpaperConsts.TYPE_SHADER_AND_CONFETTI; // Default
        } else {
            String typeString = attribute.getNodeValue();
            if ("video".equals(typeString)) {
                type = LiveWallpaperConsts.TYPE_VIDEO;
            } else {
                type = LiveWallpaperConsts.TYPE_SHADER_AND_CONFETTI;
            }
        }

        // Resource source: local (assets) / remote
        attribute = attributes.getNamedItem("localResource");
        if (attribute == null) {
            isLocal = false; // Default
        } else {
            String attribString = attribute.getNodeValue();
            isLocal = "true".equalsIgnoreCase(attribString);
        }
    }

    private void parseShader(Document doc) {
        NodeList root = doc.getElementsByTagName("shader");
        for (int index = 0; index < root.getLength(); index++) {
            if (root.item(index).getNodeType() == Node.ELEMENT_NODE) {
                NamedNodeMap attributes = root.item(index).getAttributes();
                for (int i = 0; i < attributes.getLength(); i++) {
                    if ("name".equals(attributes.item(i).getNodeName())) {
                        shaderName = attributes.item(i).getNodeValue();
                        shader = loadShader(shaderName);
                    }
                }

                NodeList children = root.item(index).getChildNodes();
                for (int i = 0; i < children.getLength(); i++) {
                    if (children.item(i).getNodeType() == Node.ELEMENT_NODE) {
                        if ("image".equals(children.item(i).getNodeName())) {
                            NamedNodeMap val = children.item(i).getAttributes();
                            for (int j = 0; j < val.getLength(); j++) {
                                if ("path".equals(val.item(j).getNodeName())) {
                                    shaderTexTureUrls.add(baseUrl + val.item(j).getNodeValue());
                                }
                            }
                        }
                    }
                }
            }

        }
    }

    private void parseConfetti(Document doc) {
        NodeList particles = doc.getElementsByTagName("particles");
        for (int index = 0; index < particles.getLength(); index++) {
            NodeList particle = particles.item(index).getChildNodes();
            for (int ri = 0; ri < particle.getLength(); ri++) {
                Node root = particle.item(ri);
                if (root.getNodeType() == Node.ELEMENT_NODE) {
                    if (root.getNodeName().equals("background")) {
                        parseAttrsNode(root, LiveWallpaperConsts.BACKGROUND);
                    } else if (root.getNodeName().equals("touch")) {
                        parseAttrsNode(root, LiveWallpaperConsts.TOUCH);
                    } else if (root.getNodeName().equals("click")) {
                        parseAttrsNode(root, LiveWallpaperConsts.CLICK);
                    } else if (root.getNodeName().equals("image")) {
                        NamedNodeMap val = root.getAttributes();
                        String url = "";
                        float ratio = 0f;
                        for (int j = 0; j < val.getLength(); j++) {
                            if ("path".equals(val.item(j).getNodeName())) {
                                url = baseUrl + val.item(j).getNodeValue();
                            } else if ("ratio".equals(val.item(j).getNodeName())) {
                                ratio = Float.valueOf(val.item(j).getNodeValue());
                            }
                        }

                        LiveConfettiSource source = new LiveConfettiSource(url, ratio, LiveWallpaperConsts.COMMON);
                        if (!confettiSources.contains(source)) {
                            confettiSources.add(source);
                        }
                    }
                }
            }

        }
    }

    private void parseAttrsNode(Node root, long category) {
        NamedNodeMap attrs = root.getAttributes();
        HashMap<String, Object> setting = new HashMap<>();
        for (int i = 0; i < attrs.getLength(); i++) {
            setting.put(attrs.item(i).getNodeName(), attrs.item(i).getNodeValue());
        }
        int id = 0;
        if (LiveWallpaperConsts.BACKGROUND == category) {
            confettiBgSettings.add(setting);
            id = confettiBgSettings.size() - 1;
        } else if (LiveWallpaperConsts.TOUCH == category) {
            confettiTouchSettings.add(setting);
        } else if (LiveWallpaperConsts.CLICK == category) {
            confettiClickSettings.add(setting);
        }

        NodeList children = root.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                if ("image".equals(child.getNodeName())) {
                    NamedNodeMap val = child.getAttributes();
                    String url = "";
                    float ratio = 0f;
                    for (int j = 0; j < val.getLength(); j++) {
                        if ("path".equals(val.item(j).getNodeName())) {
                            url = baseUrl + val.item(j).getNodeValue();
                        } else if ("ratio".equals(val.item(j).getNodeName())) {
                            ratio = Float.valueOf(val.item(j).getNodeValue());
                        }
                    }
                    LiveConfettiSource source = new LiveConfettiSource(url, ratio, id, category);
                    if (!confettiSources.contains(source)) {
                        confettiSources.add(source);
                    }
                }
            }
        }
    }

    String loadShader(String shaderName) {
        String shader = null;
        try {
            shader = Program.loadShaderResource(HSApplication.getContext(), shaderName);
        } catch (IOException | ArrayIndexOutOfBoundsException e) {
            HSLog.e(TAG, "Error parsing shader for wallpaper " + wallpaperName
                    + ", shaderName: " + shaderName);
        }
        return shader == null ? "" : shader;
    }

    public static class LiveConfettiSource {
        private long category;
        private String url;
        private float ratio;
        private int id = 0;

        LiveConfettiSource(String url, float ratio, int id, long category) {
            this.url = url;
            this.ratio = ratio;
            this.id = id;
            this.category = category;
        }

        LiveConfettiSource(String url, float ratio, long category) {
            this(url, ratio, 0, category);
        }

        public String getUrl() {
            if (url == null)
                return "";
            return url;
        }

        public long getCategory() {
            return category;
        }

        public float getRatio() {
            return ratio;
        }

        public int getId() {
            return id;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof LiveConfettiSource)) {
                return false;
            }

            LiveConfettiSource source = (LiveConfettiSource) obj;
            return ratio == source.ratio
                    && category == source.category
                    && id == source.id
                    && TextUtils.equals(url, source.getUrl());
        }
    }
}
