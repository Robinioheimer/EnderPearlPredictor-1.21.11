package com.robin.enderpearlpredictor;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.text.Text;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class EnderPearlPredictorClient implements ClientModInitializer {
    private static boolean enabled = true;
    private static boolean landingPoint = true;
    private static double maxTicks = 80;
    private static double step = 0.5;

    private static KeyBinding toggleKey;
    private static KeyBinding menuKey;

    @Override
    public void onInitializeClient() {
        toggleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.enderpearlpredictor.toggle", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_P, "category.enderpearlpredictor"));
        menuKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.enderpearlpredictor.menu", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_O, "category.enderpearlpredictor"));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (toggleKey.wasPressed()) {
                enabled = !enabled;
                if (client.player != null) client.player.sendMessage(Text.literal("Pearl Predictor: " + (enabled ? "AN" : "AUS")), true);
            }
            while (menuKey.wasPressed()) {
                client.setScreen(new PredictorConfigScreen());
            }
            if (enabled) emitPreview(client);
        });
    }

    private static void emitPreview(MinecraftClient client) {
        if (client.world == null || client.cameraEntity == null) return;
        List<Vec3d> points = simulate(client.world, client.cameraEntity.getEyePos(), client.cameraEntity.getRotationClient());
        for (int i = 0; i < points.size(); i++) {
            Vec3d p = points.get(i);
            // Client-side marker particles create a visible trajectory without any extra rendering library.
            if (i % 2 == 0) client.world.addParticleClient(ParticleTypes.END_ROD, p.x, p.y, p.z, 0, 0, 0);
        }
        if (landingPoint && !points.isEmpty()) {
            Vec3d p = points.get(points.size() - 1);
            for (int i = 0; i < 4; i++) client.world.addParticleClient(ParticleTypes.END_ROD, p.x, p.y + 0.05 + i * 0.04, p.z, 0, 0, 0);
        }
    }

    private static List<Vec3d> simulate(ClientWorld world, Vec3d start, net.minecraft.util.math.Vec3d rotationDummy) {
        // Convert camera rotation to Minecraft's projectile velocity.
        float yaw = world.getClient().cameraEntity.getYaw();
        float pitch = world.getClient().cameraEntity.getPitch();
        double yawRad = Math.toRadians(yaw);
        double pitchRad = Math.toRadians(pitch);
        Vec3d look = new Vec3d(-Math.sin(yawRad) * Math.cos(pitchRad), -Math.sin(pitchRad), Math.cos(yawRad) * Math.cos(pitchRad)).normalize();

        // Ender pearls use a velocity close to 1.5 blocks/tick and gravity around 0.03.
        Vec3d pos = start.add(look.multiply(0.2));
        Vec3d velocity = look.multiply(1.5);
        List<Vec3d> points = new ArrayList<>();

        for (int tick = 0; tick < maxTicks; tick++) {
            Vec3d next = pos.add(velocity);
            HitResult hit = world.raycast(new net.minecraft.world.RaycastContext(pos, next,
                    net.minecraft.world.RaycastContext.ShapeType.COLLIDER,
                    net.minecraft.world.RaycastContext.FluidHandling.NONE,
                    world.getClient().cameraEntity));
            if (hit.getType() != HitResult.Type.MISS) {
                points.add(hit.getPos());
                break;
            }
            pos = next;
            points.add(pos);
            velocity = velocity.multiply(0.99, 0.99, 0.99).add(0, -0.03, 0);
        }
        return points;
    }

    public static boolean isEnabled() { return enabled; }
    public static boolean isLandingPoint() { return landingPoint; }
    public static void setEnabled(boolean value) { enabled = value; }
    public static void setLandingPoint(boolean value) { landingPoint = value; }
    public static double getMaxTicks() { return maxTicks; }
    public static void setMaxTicks(double value) { maxTicks = Math.max(20, Math.min(160, value)); }
    public static double getStep() { return step; }
    public static void setStep(double value) { step = Math.max(0.1, Math.min(2.0, value)); }
}
