package io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.util;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Base64;

public class ServerIconUtil {
    private String cachedServerIconBase64 = "";
    public static final ServerIconUtil INSTANCE = new ServerIconUtil();
    private ServerIconUtil() {}

    public void loadServerIcon(File iconFile) throws IOException {
        BufferedImage image = ImageIO.read(iconFile);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(image, "PNG", out);

        this.cachedServerIconBase64 = Base64.getEncoder().encodeToString(out.toByteArray());
    }

    public String getServerIconBase64() {
        return this.cachedServerIconBase64;
    }
}
