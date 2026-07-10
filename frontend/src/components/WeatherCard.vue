<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import type { WeatherData } from '@/api'

const props = defineProps<{
  data: WeatherData | null
}>()

const expanded = ref(false)
const now = ref(new Date())

let timeTimer: ReturnType<typeof setInterval> | null = null

onMounted(() => {
  timeTimer = setInterval(() => {
    now.value = new Date()
  }, 1000)
})

onUnmounted(() => {
  if (timeTimer) clearInterval(timeTimer)
})

/* ─── solar term ─── */

const SOLAR_TERMS = [
  { key: '小寒', m: 1, d: 5 },
  { key: '大寒', m: 1, d: 20 },
  { key: '立春', m: 2, d: 4 },
  { key: '雨水', m: 2, d: 19 },
  { key: '惊蛰', m: 3, d: 6 },
  { key: '春分', m: 3, d: 21 },
  { key: '清明', m: 4, d: 5 },
  { key: '谷雨', m: 4, d: 20 },
  { key: '立夏', m: 5, d: 6 },
  { key: '小满', m: 5, d: 21 },
  { key: '芒种', m: 6, d: 6 },
  { key: '夏至', m: 6, d: 21 },
  { key: '小暑', m: 7, d: 7 },
  { key: '大暑', m: 7, d: 23 },
  { key: '立秋', m: 8, d: 8 },
  { key: '处暑', m: 8, d: 23 },
  { key: '白露', m: 9, d: 8 },
  { key: '秋分', m: 9, d: 23 },
  { key: '寒露', m: 10, d: 8 },
  { key: '霜降', m: 10, d: 23 },
  { key: '立冬', m: 11, d: 7 },
  { key: '小雪', m: 11, d: 22 },
  { key: '大雪', m: 12, d: 7 },
  { key: '冬至', m: 12, d: 22 }
]

const solarTerm = computed(() => {
  const m = now.value.getMonth() + 1
  const d = now.value.getDate()
  let term = SOLAR_TERMS[0].key
  for (const t of SOLAR_TERMS) {
    if (t.m < m || (t.m === m && t.d <= d)) {
      term = t.key
    } else {
      break
    }
  }
  return term
})

/* ─── time formatting ─── */

const fullDateTime = computed(() => {
  const y = now.value.getFullYear()
  const mo = (now.value.getMonth() + 1).toString().padStart(2, '0')
  const d = now.value.getDate().toString().padStart(2, '0')
  const h = now.value.getHours().toString().padStart(2, '0')
  const mi = now.value.getMinutes().toString().padStart(2, '0')
  const s = now.value.getSeconds().toString().padStart(2, '0')
  return `${y}-${mo}-${d} ${h}:${mi}:${s}`
})

/* ─── weather helpers ─── */

const isDaytime = computed(() => {
  const h = now.value.getHours()
  return h >= 6 && h < 18
})

const weatherIcon = computed(() => {
  if (!props.data) return ''
  const code = props.data.current.weatherCode
  if (code === 0) return isDaytime.value ? '☀' : '☾'
  if (code <= 2) return '◑'
  if (code === 3) return '☁'
  if (code >= 95) return '⚡'
  if (code >= 51 && code <= 57) return '〰'
  if (code >= 61 && code <= 82) return '💧'
  if (code >= 71 && code <= 77) return '❄'
  if (code >= 85 && code <= 86) return '❄'
  if (code >= 45 && code <= 48) return '⊞'
  return ''
})

/* ─── color hint based on weather ─── */

const accentGradient = computed(() => {
  if (!props.data) return 'linear-gradient(135deg, rgba(42,42,42,0.6), rgba(26,26,26,0.4))'
  const code = props.data.current.weatherCode
  if (code === 0) return 'linear-gradient(135deg, rgba(61,90,128,0.5), rgba(26,42,58,0.3))'
  if (code <= 3) return 'linear-gradient(135deg, rgba(74,74,74,0.5), rgba(42,42,42,0.3))'
  if (code >= 95) return 'linear-gradient(135deg, rgba(58,26,26,0.5), rgba(42,42,42,0.3))'
  if (code >= 61 && code <= 82) return 'linear-gradient(135deg, rgba(26,42,58,0.5), rgba(26,26,42,0.3))'
  if (code >= 71 && code <= 77) return 'linear-gradient(135deg, rgba(42,58,74,0.5), rgba(26,42,42,0.3))'
  return 'linear-gradient(135deg, rgba(42,42,42,0.5), rgba(26,26,26,0.3))'
})

const tempColor = computed(() => {
  if (!props.data) return '#888'
  const t = props.data.current.temperature
  if (t >= 30) return '#ff6b6b'
  if (t >= 20) return '#ffa94d'
  if (t >= 10) return '#5b8def'
  return '#74c0fc'
})
</script>

<template>
  <div
    class="weather-card"
    :class="{ 'weather-card--expanded': expanded }"
    :style="{ background: accentGradient }"
    @click="expanded = !expanded"
  >
    <!-- ─── collapsed view ─── -->
    <div class="weather-mini">
      <div class="mini-datetime">{{ fullDateTime }}</div>
      <div class="mini-weather" v-if="data">
        <span class="mini-desc">{{ data.current.weatherDesc }}</span>
        <span class="mini-range">{{ Math.round(data.daily[0].tempMin) }}° ~ {{ Math.round(data.daily[0].tempMax) }}°</span>
      </div>
      <div class="mini-weather" v-else>
        <span class="mini-desc">--</span>
      </div>
    </div>

    <!-- ─── expanded view ─── -->
    <Transition name="weather-fade">
      <div v-show="expanded" class="weather-detail">
        <!-- header row -->
        <div class="detail-header">
          <span class="detail-location">威海环翠</span>
          <span class="detail-term">{{ solarTerm }}</span>
        </div>

        <!-- current main -->
        <div class="detail-main">
          <span class="main-temp" :style="{ color: tempColor }">
            {{ data ? Math.round(data.current.temperature) : '--' }}°
          </span>
          <span class="sec-humidity" v-if="data">湿度 {{ data.current.humidity }}%</span>
        </div>

        <div class="detail-divider" />

        <!-- daily forecast list -->
        <div class="detail-daily" v-if="data">
          <div class="daily-row" v-for="day in data.daily.slice(0, 5)" :key="day.date">
            <span class="daily-date">{{ day.date.slice(5) }}</span>
            <span class="daily-icon">{{ weatherIcon }}</span>
            <span class="daily-desc">{{ day.weatherDesc }}</span>
            <span class="daily-temps">
              <span class="daily-high">{{ Math.round(day.tempMax) }}°</span>
              <span class="daily-low">{{ Math.round(day.tempMin) }}°</span>
            </span>
          </div>
        </div>

        <div class="detail-hint">点击收起</div>
      </div>
    </Transition>
  </div>
</template>

<style scoped>
.weather-card {
  position: fixed;
  top: 20px;
  right: 20px;
  z-index: 199;
  border-radius: 14px;
  border: 1px solid rgba(255, 255, 255, 0.08);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  color: #d4d4d4;
  cursor: pointer;
  user-select: none;
  transition: all 0.4s cubic-bezier(0.34, 1.56, 0.64, 1);
  overflow: hidden;
  min-width: 170px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.2);
}

.weather-card:hover {
  border-color: rgba(255, 255, 255, 0.14);
}

/* ─── collapsed ─── */
.weather-mini {
  display: flex;
  flex-direction: column;
  padding: 10px 16px;
  gap: 2px;
}

.mini-datetime {
  font-size: 12px;
  font-weight: 500;
  color: rgba(210, 210, 210, 0.85);
  font-variant-numeric: tabular-nums;
  letter-spacing: 0.03em;
}

.mini-weather {
  display: flex;
  align-items: baseline;
  gap: 8px;
}

.mini-desc {
  font-size: 15px;
  font-weight: 600;
  color: #e8e8e8;
  letter-spacing: 0.05em;
}

.mini-range {
  font-size: 13px;
  font-weight: 500;
  color: rgba(170, 170, 170, 0.8);
}

/* ─── expanded ─── */
.weather-detail {
  padding: 0 16px 14px;
}

.detail-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 10px;
}

.detail-location {
  font-size: 16px;
  font-weight: 500;
  letter-spacing: 0.05em;
}

.detail-term {
  font-size: 11px;
  color: rgba(136, 136, 136, 0.7);
  background: rgba(255, 255, 255, 0.04);
  padding: 2px 8px;
  border-radius: 4px;
}

.detail-main {
  display: flex;
  align-items: baseline;
  gap: 12px;
  margin-bottom: 8px;
}

.main-temp {
  font-size: 44px;
  font-weight: 300;
  letter-spacing: -1px;
  line-height: 1;
  transition: color 0.3s;
}

.sec-humidity {
  font-size: 13px;
  color: rgba(153, 153, 153, 0.7);
}

.detail-divider {
  height: 1px;
  background: rgba(255, 255, 255, 0.06);
  margin: 12px 0;
}

/* daily forecast */
.detail-daily {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.daily-row {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  padding: 4px 0;
}

.daily-date {
  width: 40px;
  color: rgba(136, 136, 136, 0.6);
  flex-shrink: 0;
}

.daily-icon {
  width: 20px;
  font-size: 14px;
  text-align: center;
  opacity: 0.5;
  flex-shrink: 0;
}

.daily-desc {
  flex: 1;
  color: rgba(170, 170, 170, 0.7);
}

.daily-temps {
  display: flex;
  gap: 8px;
  font-variant-numeric: tabular-nums;
}

.daily-high {
  color: #d4d4d4;
  font-weight: 400;
}

.daily-low {
  color: rgba(119, 119, 119, 0.6);
}

.detail-hint {
  text-align: center;
  font-size: 11px;
  color: rgba(85, 85, 85, 0.5);
  margin-top: 10px;
}

/* ─── transition ─── */
.weather-fade-enter-active,
.weather-fade-leave-active {
  transition: opacity 0.25s ease, max-height 0.35s ease;
  overflow: hidden;
}

.weather-fade-enter-from,
.weather-fade-leave-to {
  opacity: 0;
  max-height: 0;
}

.weather-fade-enter-to,
.weather-fade-leave-from {
  opacity: 1;
  max-height: 500px;
}
</style>
