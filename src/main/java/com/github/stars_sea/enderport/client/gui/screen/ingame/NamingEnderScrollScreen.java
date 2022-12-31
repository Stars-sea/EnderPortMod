package com.github.stars_sea.enderport.client.gui.screen.ingame;

import com.github.stars_sea.enderport.item.EnderPortItems;
import com.github.stars_sea.enderport.network.client.SendToServer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Language;

import java.util.Objects;

@Environment(EnvType.CLIENT)
public class NamingEnderScrollScreen extends Screen {
    private String enderScrollName;
    private ButtonWidget doneButtonWidget;
    private TextFieldWidget enderScrollNameField;

    public NamingEnderScrollScreen() {
        super(Text.translatable("gui.screen.name_ender_scroll.title"));
        this.enderScrollName = getDefaultName();
    }

    @Override public boolean shouldPause() { return false; }
    @Override public boolean shouldCloseOnEsc() { return !enderScrollName.isEmpty(); }

    @Override
    protected void init() {
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
        DiffuseLighting.enableGuiDepthLighting();
        super.render(matrices, mouseX, mouseY, delta);
    }

    @Override
    public void removed() {
        SendToServer.sendRenameEnderScroll(enderScrollName, !getDefaultName().equals(enderScrollName));
    }

    private void onTextChanged(String text) {
        doneButtonWidget.active = !enderScrollNameField.getText().isEmpty();
        enderScrollName = text;
    }

    private void finishEditing() {
        Objects.requireNonNull(client).setScreen(null);
    }

    private static String getDefaultName() {
        return Language.getInstance().get(EnderPortItems.ENDER_SCROLL.getTranslationKey());
    }
}
