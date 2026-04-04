package pl.kuba6000.ae2webintegration.altasgenerator;

import com.google.gson.Gson;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.item.ItemStack;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.IntBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.stream.Stream;

public class ItemBatchRenderer {

    private final Minecraft mc = Minecraft.getMinecraft();

    private final Queue<ItemStack> queue = new LinkedList<>();
    private final List<String> processedIds = new ArrayList<>();
    private final Map<String, Integer> processedIndex = new HashMap<>();
    private final Path imageExportPath;

    private Framebuffer fbo;
    private IntBuffer pixelBuffer;

    private final int size;

    private int total = 0;
    private int processed = 0;

    private boolean running = false;

    // Callback
    public interface Listener {
        void onProgress(int current, int total);
        void onFinished(int total);
        void onException(Exception ex);
    }

    private final Listener listener;

    public ItemBatchRenderer(int size, Listener listener) {
        this.size = size;
        this.listener = listener;

        imageExportPath = Minecraft.getMinecraft().gameDir.toPath().resolve("atlas_export");
    }

    private void init() throws Exception {
        fbo = new Framebuffer(size, size, true);
        pixelBuffer = BufferUtils.createIntBuffer(size * size);

        if (Files.exists(imageExportPath)) {
            try (Stream<Path> walk = Files.walk(imageExportPath)){
                walk.sorted(Comparator.reverseOrder())
                        .map(Path::toFile)
                        .forEach(File::delete);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        Files.createDirectories(imageExportPath);


    }

    private void cleanup() {
        if (fbo != null) {
            fbo.deleteFramebuffer();
            fbo = null;
        }
    }

    public void start(Collection<ItemStack> items) {
        if (running) return;

        queue.clear();
        processedIds.clear();

        for (ItemStack stack : items) {
            if (!stack.isEmpty()) {
                queue.add(stack.copy());
            }
        }

        total = queue.size();
        processed = 0;

        try {
            init();
        } catch (Exception e) {
            AE2WebIntegration.LOG.atError().withThrowable(e).log("Error while initializing");
            if (listener != null) {
                listener.onException(e);
            }
            return;
        }

        AE2WebIntegration.LOG.atInfo().log("Exporting assets to {}", imageExportPath.toFile().getAbsolutePath());
        running = true;
    }

    public boolean isRunning() {
        return running;
    }

    public void tick() {
        if (!running) return;

        int batchSize = 5;

        for (int i = 0; i < batchSize && !queue.isEmpty(); i++) {
            ItemStack stack = queue.poll();
            String itemStringId = getKey(stack);

            try {
                if (!processedIds.contains(itemStringId)) {
                    byte[] png = renderItem(stack);
                    processedIds.add(itemStringId);
                    processedIndex.put(itemStringId, processed);
                    Path outPath = imageExportPath.resolve(processed + ".png");
                    try (OutputStream os = Files.newOutputStream(outPath, StandardOpenOption.CREATE)) {
                        os.write(png);
                    }
                }
            } catch (Exception e) {
                AE2WebIntegration.LOG.atError().withThrowable(e).log("Error while generating texture for {}:{}", stack.getItem().getRegistryName(), stack.getItemDamage());
                if (listener != null) {
                    listener.onException(e);
                }
                queue.clear();
                cleanup();
                running = false;
                return;
            }

            processed++;

            if (listener != null) {
                listener.onProgress(processed, total);
            }
        }

        if (queue.isEmpty()) {
            running = false;
            cleanup();

            Path jsonOut = imageExportPath.resolve("info.json");
            String json = new Gson().toJson(processedIndex);
            try {
                Files.write(jsonOut, json.getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE);
            } catch (Exception ex) {
                AE2WebIntegration.LOG.atError().withThrowable(ex).log("Error while writing json atlas map");
                if (listener != null) {
                    listener.onException(ex);
                }
            }

            if (listener != null) {
                listener.onFinished(total);
            }
        }
    }

    private byte[] renderItem(ItemStack stack) throws Exception {

        fbo.bindFramebuffer(true);

        // Viewport
        GlStateManager.viewport(0, 0, size, size);

        // Clear background
        GlStateManager.clearColor(0f, 0f, 0f, 0f);
        GlStateManager.clear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

        // GUI Projection
        GlStateManager.matrixMode(GL11.GL_PROJECTION);
        GlStateManager.loadIdentity();
        GlStateManager.ortho(0, size, size, 0, 1000, 3000);

        GlStateManager.matrixMode(GL11.GL_MODELVIEW);
        GlStateManager.loadIdentity();
        GlStateManager.translate(0, 0, -2000);

        GlStateManager.pushMatrix();

        // Clean rendering
        GlStateManager.enableRescaleNormal();
        GlStateManager.enableAlpha();
        GlStateManager.enableBlend();
        GlStateManager.enableTexture2D();
        GlStateManager.enableCull();
        GlStateManager.enableLighting();

        RenderHelper.enableGUIStandardItemLighting();

        RenderItem renderItem = mc.getRenderItem();

        // Center element
        float scale = size / 16f;

        GlStateManager.translate((size - 16 * scale) / 2f, (size - 16 * scale) / 2f, 0);
        GlStateManager.scale(scale, scale, 1f);

        // Render element
        renderItem.renderItemAndEffectIntoGUI(stack, 0, 0);
        renderItem.renderItemOverlays(mc.fontRenderer, stack, 0, 0);

        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableCull();

        GlStateManager.popMatrix();

        // =========================

        // Read pixels
        pixelBuffer.clear();

        GL11.glReadPixels(
                0, 0,
                size, size,
                GL11.GL_RGBA,
                GL11.GL_UNSIGNED_BYTE,
                pixelBuffer
        );

        return convertToPNG(pixelBuffer);
    }

    private byte[] convertToPNG(IntBuffer buffer) throws Exception {

        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);

        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {

                int i = x + (size * (size - y - 1));
                int rgba = buffer.get(i);

                int r = (rgba) & 0xFF;
                int g = (rgba >> 8) & 0xFF;
                int b = (rgba >> 16) & 0xFF;
                int a = (rgba >> 24) & 0xFF;

                int argb =
                        (a << 24) |
                        (r << 16) |
                        (g << 8) |
                        (b);

                image.setRGB(x, y, argb);
            }
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "png", baos);

        return baos.toByteArray();
    }

    private String getKey(ItemStack stack) {
        return stack.getItem().getRegistryName() + ":" + stack.getItemDamage();
    }
}
