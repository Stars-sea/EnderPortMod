package com.github.stars_sea.enderport.client.gui.screen.ingame;

import com.github.stars_sea.enderport.network.server.listener.EnderPortServerListeners;
import com.github.stars_sea.enderport.util.PosNbtHelper;
import com.github.stars_sea.enderport.world.Location;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

@Environment(EnvType.CLIENT)
public class NamingEnderScrollScreen extends Screen {
    private String enderScrollName;
    private String biomeName;
    private final Location location;
    private ButtonWidget doneButtonWidget;
    private TextFieldWidget enderScrollNameField;

    public NamingEnderScrollScreen(@NotNull Location location) {
        super(Text.translatable("gui.screen.name_ender_scroll.title"));
        this.location        = location;
        this.enderScrollName = location.toString();
    }

    @Override public boolean shouldPause() { return false; }
    @Override public boolean shouldCloseOnEsc() { return true; }

    @Override
    protected void init() {
        this.biomeName       = getBiomeName();
        enderScrollNameField = addDrawableChild(new TextFieldWidget(
                textRenderer, width / 2 - 100, height / 4 + 40, 200, 20,
                Text.translatable("item.enderport.ender_scroll")
        ));
        doneButtonWidget = addDrawableChild(ButtonWidget.builder(ScreenTexts.DONE, b -> finishEditing())
                .dimensions(width / 2 - 100, height / 4 + 120, 200, 20).build());

        enderScrollNameField.setMaxLength(128);
        enderScrollNameField.setText(enderScrollName);
        enderScrollNameField.setChangedListener(this::onTextChanged);

        setInitialFocus(enderScrollNameField);
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        renderBackground(matrices);
        // Title
        drawCenteredText(matrices, textRenderer, title, width / 2, 40, 0xFFFFFF);

        // Location details
        drawCenteredText(matrices, textRenderer, Text.of(location.toString()), width / 2, height / 4 + 70, 0xFFFFFF);
        if (biomeName != null)
            drawCenteredText(matrices, textRenderer, Text.of(biomeName), width / 2, height / 4 + 80, 0xFFFFFF);

        DiffuseLighting.enableGuiDepthLighting();
        super.render(matrices, mouseX, mouseY, delta);
    }

    @Override
    public void removed() {
        PacketByteBuf buf = PacketByteBufs.create()
                .writeString(enderScrollName)
                .writeNbt(PosNbtHelper.getLocationNbt(location));
        ClientPlayNetworking.send(EnderPortServerListeners.RENAME_ENDER_SCROLL_PACKET_ID, buf);
    }

    private void onTextChanged(String text) {
        doneButtonWidget.active = !enderScrollNameField.getText().isEmpty();
        enderScrollName = text;
    }

    private void finishEditing() {
        Objects.requireNonNull(client).setScreen(null);
    }

    @Nullable
    private String getBiomeName() {
        ClientWorld world = Objects.requireNonNull(client).world;
        return world != null ? world.getBiome(location.mutable()).getKeyOrValue().map(
                key -> key.getValue().toString(),
                biome_ -> null
        ) : null;
    }
}
