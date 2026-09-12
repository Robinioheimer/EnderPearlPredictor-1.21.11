package com.robin.enderpearlpredictor;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class PredictorConfigScreen extends Screen {
    private ButtonWidget enabledButton;
    private ButtonWidget landingButton;
    private ButtonWidget rangeButton;

    public PredictorConfigScreen() {
        super(Text.literal("Ender Pearl Predictor"));
    }

    @Override
    protected void init() {
        int cx = width / 2;
        enabledButton = addDrawableChild(ButtonWidget.builder(label("Flugbahn", EnderPearlPredictorClient.isEnabled()), b -> {
            EnderPearlPredictorClient.setEnabled(!EnderPearlPredictorClient.isEnabled());
            b.setMessage(label("Flugbahn", EnderPearlPredictorClient.isEnabled()));
        }).dimensions(cx - 100, 70, 200, 20).build());

        landingButton = addDrawableChild(ButtonWidget.builder(label("Einschlagspunkt", EnderPearlPredictorClient.isLandingPoint()), b -> {
            EnderPearlPredictorClient.setLandingPoint(!EnderPearlPredictorClient.isLandingPoint());
            b.setMessage(label("Einschlagspunkt", EnderPearlPredictorClient.isLandingPoint()));
        }).dimensions(cx - 100, 98, 200, 20).build());

        rangeButton = addDrawableChild(ButtonWidget.builder(Text.literal("Reichweite: " + (int) EnderPearlPredictorClient.getMaxTicks() + " Ticks"), b -> {
            double next = EnderPearlPredictorClient.getMaxTicks() >= 120 ? 60 : EnderPearlPredictorClient.getMaxTicks() + 20;
            EnderPearlPredictorClient.setMaxTicks(next);
            b.setMessage(Text.literal("Reichweite: " + (int) next + " Ticks"));
        }).dimensions(cx - 100, 126, 200, 20).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("Schließen"), b -> close()).dimensions(cx - 100, 166, 200, 20).build());
    }

    private static Text label(String name, boolean state) { return Text.literal(name + ": " + (state ? "AN" : "AUS")); }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(textRenderer, title, width / 2, 35, 0xFFFFFF);
        context.drawCenteredTextWithShadow(textRenderer,
                Text.literal("P = an/aus   |   O = Menü"), width / 2, height - 25, 0xAAAAAA);
        super.render(context, mouseX, mouseY, delta);
    }
}
