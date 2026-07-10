<script setup lang="ts">
</script>

<template>
  <div class="app-ambient">
    <div class="orb orb-1" />
    <div class="orb orb-2" />
    <div class="orb orb-3" />
    <div class="fabric-layer fabric-warp" />
    <div class="fabric-layer fabric-weft" />
    <div class="fabric-layer fabric-diagonal" />
    <router-view />
  </div>
</template>

<style scoped>
.app-ambient {
  width: 100%;
  height: 100%;
  position: relative;
  background:
    radial-gradient(ellipse at 20% 0%, rgba(91, 141, 239, 0.04) 0%, transparent 60%),
    radial-gradient(ellipse at 80% 100%, rgba(166, 139, 239, 0.04) 0%, transparent 60%),
    radial-gradient(ellipse at 50% 50%, rgba(91, 141, 239, 0.02) 0%, transparent 50%);
}

.orb {
  position: fixed;
  border-radius: 50%;
  filter: blur(120px);
  pointer-events: none;
  z-index: 0;
}

/* ─── Fabric weave texture layers ─── */
.fabric-layer {
  position: fixed;
  inset: -100%;
  z-index: 0;
  pointer-events: none;
  opacity: 0.7;
}

.fabric-warp {
  background: repeating-linear-gradient(
    90deg,
    transparent,
    transparent 7px,
    rgba(180, 200, 255, 0.35) 7px,
    rgba(180, 200, 255, 0.35) 7.5px
  );
  animation: fabric-drift-1 35s linear infinite;
}

.fabric-weft {
  background: repeating-linear-gradient(
    0deg,
    transparent,
    transparent 8px,
    rgba(200, 220, 255, 0.25) 8px,
    rgba(200, 220, 255, 0.25) 8.5px
  );
  animation: fabric-drift-2 28s linear infinite;
}

.fabric-diagonal {
  background: repeating-linear-gradient(
    45deg,
    transparent,
    transparent 10px,
    rgba(220, 235, 255, 0.15) 10px,
    rgba(220, 235, 255, 0.15) 10.5px
  );
  animation: fabric-drift-3 40s linear infinite;
}

@keyframes fabric-drift-1 {
  0%   { transform: translate(-3%, -2%) rotate(0deg); }
  25%  { transform: translate(3%, -5%) rotate(0.5deg); }
  50%  { transform: translate(5%, 2%) rotate(-0.3deg); }
  75%  { transform: translate(-2%, 4%) rotate(0.4deg); }
  100% { transform: translate(-3%, -2%) rotate(0deg); }
}

@keyframes fabric-drift-2 {
  0%   { transform: translate(2%, 3%) rotate(0deg); }
  25%  { transform: translate(-4%, 2%) rotate(-0.4deg); }
  50%  { transform: translate(-2%, -4%) rotate(0.3deg); }
  75%  { transform: translate(4%, -2%) rotate(-0.5deg); }
  100% { transform: translate(2%, 3%) rotate(0deg); }
}

@keyframes fabric-drift-3 {
  0%   { transform: translate(0%, -3%) rotate(0deg); }
  33%  { transform: translate(4%, 2%) rotate(0.6deg); }
  66%  { transform: translate(-3%, 3%) rotate(-0.4deg); }
  100% { transform: translate(0%, -3%) rotate(0deg); }
}

.orb-1 {
  width: 600px;
  height: 600px;
  background: radial-gradient(circle, rgba(91, 141, 239, 0.12), transparent);
  top: -200px;
  left: -200px;
  animation: float-1 25s ease-in-out infinite;
}

.orb-2 {
  width: 500px;
  height: 500px;
  background: radial-gradient(circle, rgba(166, 139, 239, 0.08), transparent);
  bottom: -150px;
  right: -150px;
  animation: float-2 30s ease-in-out infinite;
}

.orb-3 {
  width: 400px;
  height: 400px;
  background: radial-gradient(circle, rgba(91, 141, 239, 0.06), transparent);
  top: 50%;
  left: 50%;
  animation: float-3 20s ease-in-out infinite;
}

@keyframes float-1 {
  0%, 100% { transform: translate(0, 0); }
  25% { transform: translate(200px, 300px); }
  50% { transform: translate(400px, 100px); }
  75% { transform: translate(100px, 400px); }
}

@keyframes float-2 {
  0%, 100% { transform: translate(0, 0); }
  25% { transform: translate(-200px, -200px); }
  50% { transform: translate(-400px, 100px); }
  75% { transform: translate(-100px, -300px); }
}

@keyframes float-3 {
  0%, 100% { transform: translate(-50%, -50%) scale(1); }
  33% { transform: translate(-30%, -60%) scale(1.1); }
  66% { transform: translate(-60%, -30%) scale(0.9); }
}
</style>
