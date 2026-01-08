package dev.mlml.matrix.gui;

import dev.mlml.matrix.MatrixMod;
import dev.mlml.matrix.mixin.INativeImageMixin;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.nbt.NbtByteArray;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtIo;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.client.render.RenderLayer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class ImageToMapartScreen extends Screen {

    private static final Map<String, int[]> ALL_MATERIALS = new HashMap<>();
    private static final Map<String, double[]> LAB_CACHE = new HashMap<>();

    static {
        ALL_MATERIALS.put("white", new int[]{220, 220, 220});
        ALL_MATERIALS.put("orange", new int[]{186, 109, 44});
        ALL_MATERIALS.put("magenta", new int[]{153, 66, 153});
        ALL_MATERIALS.put("light_blue", new int[]{88, 132, 186});
        ALL_MATERIALS.put("yellow", new int[]{197, 197, 44});
        ALL_MATERIALS.put("lime", new int[]{109, 176, 22});
        ALL_MATERIALS.put("pink", new int[]{208, 109, 142});
        ALL_MATERIALS.put("gray", new int[]{66, 66, 66});
        ALL_MATERIALS.put("light_gray", new int[]{132, 132, 132});
        ALL_MATERIALS.put("cyan", new int[]{66, 109, 132});
        ALL_MATERIALS.put("purple", new int[]{109, 54, 153});
        ALL_MATERIALS.put("blue", new int[]{44, 66, 153});
        ALL_MATERIALS.put("brown", new int[]{88, 66, 44});
        ALL_MATERIALS.put("green", new int[]{88, 109, 44});
        ALL_MATERIALS.put("red", new int[]{132, 44, 44});
        ALL_MATERIALS.put("black", new int[]{22, 22, 22});

        // Precompute LAB values
        for (Map.Entry<String, int[]> entry : ALL_MATERIALS.entrySet()) {
            LAB_CACHE.put(entry.getKey(), rgbToLab(entry.getValue()[0], entry.getValue()[1], entry.getValue()[2]));
        }
    }

    private NativeImage originalImage;
    private NativeImage processedImage;
    private Identifier previewTextureId;
    private NativeImageBackedTexture previewTexture;
    
    private TextFieldWidget widthField;
    private TextFieldWidget heightField;
    
    private double saturation = 1.0;
    private double contrast = 1.0;
    private double brightness = 1.0;
    private boolean dither = true;

    private ControlSlider satSlider;
    private ControlSlider conSlider;
    private ControlSlider briSlider;
    
    private String originalFileName = "mapart";
    private String[][] carpetGrid; // [y][x] -> material name

    public ImageToMapartScreen() {
        super(Text.literal("MapArt Studio"));
    }

    @Override
    public void close() {
        if (originalImage != null) originalImage.close();
        if (previewTexture != null) previewTexture.close();
        super.close();
    }

    @Override
    protected void init() {
        int y = 40;
        int x = 10;
        
        // Dims
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Load Image"), button -> loadRandomImage()).dimensions(x, y, 100, 20).build());
        y += 25;
        
        this.addDrawable(new net.minecraft.client.gui.widget.TextWidget(x, y, 100, 10, Text.literal("Width (Maps):"), textRenderer));
        widthField = new TextFieldWidget(textRenderer, x + 60, y - 2, 40, 15, Text.literal("Width"));
        widthField.setText("1");
        widthField.setChangedListener(s -> processImageThreaded());
        this.addDrawableChild(widthField);
        y += 20;

        this.addDrawable(new net.minecraft.client.gui.widget.TextWidget(x, y, 100, 10, Text.literal("Height (Maps):"), textRenderer));
        heightField = new TextFieldWidget(textRenderer, x + 60, y - 2, 40, 15, Text.literal("Height"));
        heightField.setText("1");
        heightField.setChangedListener(s -> processImageThreaded());
        this.addDrawableChild(heightField);
        y += 25;
        
        // Color Controls - Sliders
        satSlider = new ControlSlider(x, y, 100, 20, "Saturation", saturation, 0.0, 3.0, val -> {
            saturation = val;
            processImageThreaded();
        });
        this.addDrawableChild(satSlider);
        y += 22;
        
        conSlider = new ControlSlider(x, y, 100, 20, "Contrast", contrast, 0.5, 2.0, val -> {
            contrast = val;
            processImageThreaded();
        });
        this.addDrawableChild(conSlider);
        y += 22;

        briSlider = new ControlSlider(x, y, 100, 20, "Brightness", brightness, 0.5, 2.0, val -> {
            brightness = val;
            processImageThreaded();
        });
        this.addDrawableChild(briSlider);
        y += 22;

        // Auto-adjust button
        this.addDrawableChild(ButtonWidget.builder(Text.literal("✨ Auto-adjust"), b -> {
            saturation = 1.8;
            contrast = 1.3;
            brightness = 1.0;
            satSlider.setActualValue(saturation);
            conSlider.setActualValue(contrast);
            briSlider.setActualValue(brightness);
            processImageThreaded();
            ChatHelper.message("Applied auto-adjust presets.");
        }).dimensions(x, y, 100, 20).build());
        y += 25;

        this.addDrawableChild(ButtonWidget.builder(Text.literal("Dither: " + dither), b -> {
            dither = !dither;
            b.setMessage(Text.literal("Dither: " + dither));
            processImageThreaded();
        }).dimensions(x, y, 100, 20).build());
        y += 25;

        this.addDrawableChild(ButtonWidget.builder(Text.literal("Export .schem"), b -> exportSchematic()).dimensions(x, y, 100, 20).build());
    }

    private class ControlSlider extends net.minecraft.client.gui.widget.SliderWidget {
        private final String label;
        private final double min;
        private final double max;
        private final java.util.function.Consumer<Double> update;

        public ControlSlider(int x, int y, int width, int height, String label, double current, double min, double max, java.util.function.Consumer<Double> update) {
            super(x, y, width, height, Text.literal(label + ": " + String.format("%.1f", current)), (current - min) / (max - min));
            this.label = label;
            this.min = min;
            this.max = max;
            this.update = update;
        }

        public void setActualValue(double actual) {
            this.value = (actual - min) / (max - min);
            this.updateMessage();
        }

        @Override
        protected void updateMessage() {
            double val = min + (value * (max - min));
            setMessage(Text.literal(label + ": " + String.format("%.1f", val)));
        }

        @Override
        protected void applyValue() {
            update.accept(min + (value * (max - min)));
        }
    }
    
    private void loadRandomImage() {
         // Placeholder for button action if needed
    }

    public void onFilesDropped(String[] paths) {
        MatrixMod.LOGGER.info("Screen received files: " + java.util.Arrays.toString(paths));
        if (paths.length > 0) {
            try {
                File file = new File(paths[0]);
                MatrixMod.LOGGER.info("Reading image from: " + file.getAbsolutePath());
                ChatHelper.message("Reading image: " + file.getName());
                
                // Store file name without extension
                String name = file.getName();
                int lastDot = name.lastIndexOf('.');
                if (lastDot > 0) {
                    originalFileName = name.substring(0, lastDot);
                } else {
                    originalFileName = name;
                }
                
                // Use ImageIO for robust reading
                java.awt.image.BufferedImage bImg = javax.imageio.ImageIO.read(file);
                
                if (bImg != null) {
                    int w = bImg.getWidth();
                    int h = bImg.getHeight();
                    ChatHelper.message("Image loaded: " + w + "x" + h);
                    
                    if (originalImage != null) originalImage.close();
                    originalImage = new NativeImage(w, h, false);
                    
                    // Copy pixels from BufferedImage (ARGB) to NativeImage (ABGR)
                    for (int y = 0; y < h; y++) {
                        for (int x = 0; x < w; x++) {
                            int argb = bImg.getRGB(x, y);
                            
                            int a = (argb >> 24) & 0xFF;
                            int r = (argb >> 16) & 0xFF;
                            int g = (argb >> 8) & 0xFF;
                            int b = (argb >> 0) & 0xFF;
                            
                            // NativeImage expects ABGR on Little Endian (Win32)
                            int abgr = (a << 24) | (b << 16) | (g << 8) | r;
                            
                            ((INativeImageMixin) (Object) originalImage).invokeSetColor(x, y, abgr);
                        }
                    }
                    
                    MatrixMod.LOGGER.info("Image conversion complete.");
                    processImageThreaded();
                } else {
                    MatrixMod.LOGGER.error("ImageIO returned null. Format not supported?");
                    ChatHelper.message("Failed to read image (Format not supported?)");
                    
                    // Debug: Read header anyway to show user
                    try {
                        byte[] header = new byte[8];
                        java.io.FileInputStream fis = new java.io.FileInputStream(file);
                        fis.read(header);
                        fis.close();
                        StringBuilder sb = new StringBuilder("Header: ");
                        for (byte b : header) sb.append(String.format("%02X ", b));
                        ChatHelper.message(sb.toString());
                    } catch (Exception ignored) {}
                }
            } catch (Exception e) {
                MatrixMod.LOGGER.error("Error reading image file", e);
                ChatHelper.message("Error reading file: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void processImageThreaded() {
        if (originalImage == null) return;
        
        CompletableFuture.runAsync(() -> {
            try {
                int mapW = 1;
                int mapH = 1;
                try {
                    mapW = Integer.parseInt(widthField.getText());
                    mapH = Integer.parseInt(heightField.getText());
                } catch (Exception ignored) {}
                
                int w = mapW * 128;
                int h = mapH * 128;
                
                int origW = originalImage.getWidth();
                int origH = originalImage.getHeight();
                
                if (origW <= 0 || origH <= 0) {
                    throw new IOException("Invalid image dimensions: " + origW + "x" + origH);
                }
                
                carpetGrid = new String[h][w];
                // New processed image
                NativeImage output = new NativeImage(w, h, false);
                
                // Working buffer for error diffusion (float RGB)
                // [y][x][3] (0=R, 1=G, 2=B)
                float[][][] workBuffer = new float[h][w][3];

                // 1. Resample and fill buffer with adjustments
                for (int y = 0; y < h; y++) {
                    for (int x = 0; x < w; x++) {
                        int srcX = (x * origW) / w;
                        int srcY = (y * origH) / h;
                        
                        // Use Mixin for private access
                        int color = ((INativeImageMixin) (Object) originalImage).invokeGetColor(srcX, srcY);
                        
                        // ABGR
                        float r = (color >> 0) & 0xFF; // R
                        float g = (color >> 8) & 0xFF; // G
                        float b = (color >> 16) & 0xFF; // B

                        // --- Apply Adjustments ---
                        // Brightness
                        r *= (float) brightness;
                        g *= (float) brightness;
                        b *= (float) brightness;

                        // Saturation (Interp between grayscale and color)
                        float luma = 0.299f * r + 0.587f * g + 0.114f * b;
                        r = luma + (float) saturation * (r - luma);
                        g = luma + (float) saturation * (g - luma);
                        b = luma + (float) saturation * (b - luma);

                        // Contrast
                        r = 128.0f + (float) contrast * (r - 128.0f);
                        g = 128.0f + (float) contrast * (g - 128.0f);
                        b = 128.0f + (float) contrast * (b - 128.0f);

                        workBuffer[y][x][0] = Math.max(0, Math.min(255, r));
                        workBuffer[y][x][1] = Math.max(0, Math.min(255, g));
                        workBuffer[y][x][2] = Math.max(0, Math.min(255, b));
                    }
                }

                // 2. Dither and Map
                for (int y = 0; y < h; y++) {
                    for (int x = 0; x < w; x++) {
                        float r = Math.max(0, Math.min(255, workBuffer[y][x][0]));
                        float g = Math.max(0, Math.min(255, workBuffer[y][x][1]));
                        float b = Math.max(0, Math.min(255, workBuffer[y][x][2]));

                        // Find closest material using LAB
                        String bestMat = "white";
                        double minDest = Double.MAX_VALUE;
                        double[] currentLab = rgbToLab((int)r, (int)g, (int)b);

                        for (Map.Entry<String, double[]> entry : LAB_CACHE.entrySet()) {
                            double[] mLab = entry.getValue();
                            // Euclidean distance in Lab space (Delta E 76 approx)
                            double dist = Math.pow(currentLab[0] - mLab[0], 2) + 
                                          Math.pow(currentLab[1] - mLab[1], 2) + 
                                          Math.pow(currentLab[2] - mLab[2], 2);
                            if (dist < minDest) {
                                minDest = dist;
                                bestMat = entry.getKey();
                            }
                        }

                        carpetGrid[y][x] = bestMat;
                        int[] bestRgb = ALL_MATERIALS.get(bestMat);
                        
                        // Set pixel in output
                        int outColor = (255 << 24) | (bestRgb[2] << 16) | (bestRgb[1] << 8) | bestRgb[0];
                        ((INativeImageMixin) (Object) output).invokeSetColor(x, y, outColor); // Mixin call

                        // Error diffusion (Floyd-Steinberg)
                        if (dither) {
                            float errR = r - bestRgb[0];
                            float errG = g - bestRgb[1];
                            float errB = b - bestRgb[2];

                            distributeError(workBuffer, x + 1, y, errR, errG, errB, 7.0f / 16.0f, w, h);
                            distributeError(workBuffer, x - 1, y + 1, errR, errG, errB, 3.0f / 16.0f, w, h);
                            distributeError(workBuffer, x, y + 1, errR, errG, errB, 5.0f / 16.0f, w, h);
                            distributeError(workBuffer, x + 1, y + 1, errR, errG, errB, 1.0f / 16.0f, w, h);
                        }
                    }
                }
                
                this.processedImage = output;
                updatePreviewTexture();
                
            } catch (Throwable e) {
                MatrixMod.LOGGER.error("Error in processing thread", e);
            }
        });
    }

    private void distributeError(float[][][] buffer, int x, int y, float eR, float eG, float eB, float factor, int w, int h) {
        if (x >= 0 && x < w && y >= 0 && y < h) {
            buffer[y][x][0] += eR * factor;
            buffer[y][x][1] += eG * factor;
            buffer[y][x][2] += eB * factor;
        }
    }
    
    private void updatePreviewTexture() {
        if (processedImage == null) return;
        
        client.execute(() -> {
            if (previewTexture != null) {
                previewTexture.close();
            }
            
            previewTexture = new NativeImageBackedTexture(processedImage);
            previewTextureId = Identifier.of("matrix", "mapart_" + System.currentTimeMillis());
            client.getTextureManager().registerTexture(previewTextureId, previewTexture);
            previewTexture.upload();
        });
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);
        
        context.drawText(textRenderer, "Drop image here!", this.width / 2, 10, 0xFFFFFF, true);
        
        if (previewTextureId != null && processedImage != null) {
            int previewX = 150;
            int previewY = 40;
            int maxWidth = this.width - 170;
            int maxHeight = this.height - 60;
            
            float aspect = (float) processedImage.getWidth() / processedImage.getHeight();
            int drawW = maxWidth;
            int drawH = (int) (maxWidth / aspect);
            
            if (drawH > maxHeight) {
                drawH = maxHeight;
                drawW = (int) (maxHeight * aspect);
            }

            // Use matrix scaling
            context.getMatrices().push();
            context.getMatrices().translate(previewX, previewY, 0);
            float scaleX = (float) drawW / processedImage.getWidth();
            float scaleY = (float) drawH / processedImage.getHeight();
            context.getMatrices().scale(scaleX, scaleY, 1f);
            
            // Draw texture using Function overload
            context.drawTexture(RenderLayer::getGuiTextured, previewTextureId, 0, 0, 0.0F, 0.0F, processedImage.getWidth(), processedImage.getHeight(), processedImage.getWidth(), processedImage.getHeight());
            
            context.getMatrices().pop();
        }
    }
    
    // RGB to LAB conversion
    private static double[] rgbToLab(int r, int g, int b) {
        double R = r / 255.0;
        double G = g / 255.0;
        double B = b / 255.0;
        
        R = (R > 0.04045) ? Math.pow((R + 0.055) / 1.055, 2.4) : (R / 12.92);
        G = (G > 0.04045) ? Math.pow((G + 0.055) / 1.055, 2.4) : (G / 12.92);
        B = (B > 0.04045) ? Math.pow((B + 0.055) / 1.055, 2.4) : (B / 12.92);
        
        double X = R * 0.4124 + G * 0.3576 + B * 0.1805;
        double Y = R * 0.2126 + G * 0.7152 + B * 0.0722;
        double Z = R * 0.0193 + G * 0.1192 + B * 0.9505;
        
        X /= 0.95047;
        Y /= 1.00000;
        Z /= 1.08883;
        
        X = (X > 0.008856) ? Math.pow(X, 1.0/3.0) : (7.787 * X) + (16.0/116.0);
        Y = (Y > 0.008856) ? Math.pow(Y, 1.0/3.0) : (7.787 * Y) + (16.0/116.0);
        Z = (Z > 0.008856) ? Math.pow(Z, 1.0/3.0) : (7.787 * Z) + (16.0/116.0);
        
        return new double[] {(116.0 * Y) - 16.0, 500.0 * (X - Y), 200.0 * (Y - Z)};
    }
    
    private void exportSchematic() {
        if (carpetGrid == null) {
            ChatHelper.message("No image processed to export!");
            return;
        }
        
        try {
            int w = carpetGrid[0].length;
            int h = carpetGrid.length;
            
            // Sponge Schematic v2 Structure: Root compound contains data directly
            NbtCompound root = new NbtCompound();
            root.putInt("Version", 2);
            root.putInt("DataVersion", 4189); // 1.21.4 DataVersion
            root.putString("Author", "Matrix");
            root.putShort("Width", (short) w);
            root.putShort("Height", (short) 1);
            root.putShort("Length", (short) h);
            
            NbtCompound palette = new NbtCompound();
            List<String> paletteList = new ArrayList<>();
            paletteList.add("minecraft:air");
            
            List<Byte> blockData = new ArrayList<>();
            
            for (int z = 0; z < h; z++) {
                for (int x = 0; x < w; x++) {
                    String mat = carpetGrid[z][x];
                    String fullName = "minecraft:" + mat + "_carpet";
                    
                    if (!paletteList.contains(fullName)) {
                        paletteList.add(fullName);
                    }
                    
                    int stateId = paletteList.indexOf(fullName);
                    while ((stateId & -128) != 0) {
                        blockData.add((byte) (stateId & 127 | 128));
                        stateId >>>= 7;
                    }
                    blockData.add((byte) stateId);
                }
            }

            for (int i = 0; i < paletteList.size(); i++) {
                palette.putInt(paletteList.get(i), i);
            }
            
            root.put("Palette", palette);
            
            byte[] dataArr = new byte[blockData.size()];
            for(int i=0; i<blockData.size(); i++) dataArr[i] = blockData.get(i);
            
            root.put("BlockData", new NbtByteArray(dataArr));
            // No nested "Schematic" tag - root contains everything
            
            File outputDir = new File(MatrixMod.mc.runDirectory, "schematics");
            if (!outputDir.exists()) outputDir.mkdirs();
            
            File outFile = new File(outputDir, originalFileName + ".schem");
            
            NbtIo.writeCompressed(root, outFile.toPath());
            ChatHelper.message("Schematic saved to: " + outFile.getName());

        } catch (Exception e) {
            e.printStackTrace();
            ChatHelper.message("Error exporting schematic: " + e.getMessage());
        }
    }
}