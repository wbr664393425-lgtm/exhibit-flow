<template>
  <svg :viewBox="`0 0 ${W} ${h}`" :style="{ width: '100%', height: `${h}px` }" preserveAspectRatio="xMidYMid meet">
    <defs>
      <linearGradient id="eh-line-grad" x1="0" y1="0" x2="0" y2="1">
        <stop offset="0%" stop-color="#c96442" stop-opacity="0.15" />
        <stop offset="100%" stop-color="#c96442" stop-opacity="0" />
      </linearGradient>
    </defs>
    <line
      v-for="(r, i) in [0, 0.25, 0.5, 0.75, 1]"
      :key="`g-${i}`"
      :x1="PL"
      :y1="(PT + cH * (1 - r)).toFixed(1)"
      :x2="PL + cW"
      :y2="(PT + cH * (1 - r)).toFixed(1)"
      stroke="#e8e6dc"
      stroke-width="0.8"
    />
    <line
      v-for="(_, i) in data"
      :key="`v-${i}`"
      :x1="px(i).toFixed(1)"
      :y1="PT"
      :x2="px(i).toFixed(1)"
      :y2="PT + cH"
      stroke="#e8e6dc"
      stroke-width="0.5"
      stroke-dasharray="3,3"
    />
    <path v-if="area1" :d="area1" fill="url(#eh-line-grad)" />
    <path
      v-if="path2"
      :d="path2"
      fill="none"
      stroke="#e8e6dc"
      stroke-width="1.5"
      stroke-linecap="round"
      stroke-linejoin="round"
      stroke-dasharray="4,3"
    />
    <path
      v-if="path1"
      :d="path1"
      fill="none"
      stroke="#c96442"
      stroke-width="2"
      stroke-linecap="round"
      stroke-linejoin="round"
    />
    <template v-for="(d, i) in data" :key="`pt-${i}`">
      <circle
        v-if="d.v1 > 0"
        :cx="px(i).toFixed(1)"
        :cy="py(d.v1).toFixed(1)"
        r="3"
        fill="#c96442"
        stroke="#ffffff"
        stroke-width="1.5"
      />
    </template>
    <text
      v-for="(d, i) in data"
      :key="`lb-${i}`"
      :x="px(i).toFixed(1)"
      :y="h - 6"
      text-anchor="middle"
      font-size="9"
      fill="#87867f"
      font-family="inherit"
    >{{ d.label }}</text>
    <text
      v-for="(v, i) in yLabels"
      :key="`y-${i}`"
      :x="PL - 4"
      :y="(py(v) + 3).toFixed(1)"
      text-anchor="end"
      font-size="9"
      fill="#87867f"
      font-family="inherit"
    >{{ v }}</text>
  </svg>
</template>

<script setup lang="ts">
import { computed } from 'vue';

export interface Point { label: string; v1: number; v2: number; }
const props = withDefaults(defineProps<{ data: Point[]; h?: number }>(), { h: 160 });

const W = 400;
const PL = 36;
const PR = 16;
const PT = 12;
const PB = 28;

const cW = computed(() => W - PL - PR);
const cH = computed(() => props.h - PT - PB);

const maxV = computed(() => {
  const vals = props.data.flatMap((d) => [d.v1, d.v2]).filter((v) => v > 0);
  return Math.max(...vals, 1);
});

function px(i: number) {
  return PL + (i / (props.data.length - 1)) * cW.value;
}
function py(v: number) {
  return PT + cH.value - (v / maxV.value) * cH.value;
}

const path1 = computed(() =>
  props.data
    .filter((d) => d.v1 > 0)
    .map((d) => {
      const i = props.data.indexOf(d);
      return `${i === 0 ? 'M' : 'L'}${px(i).toFixed(1)},${py(d.v1).toFixed(1)}`;
    })
    .join(' ')
);
const path2 = computed(() =>
  props.data
    .filter((d) => d.v2 > 0)
    .map((d) => {
      const i = props.data.indexOf(d);
      return `${i === 0 ? 'M' : 'L'}${px(i).toFixed(1)},${py(d.v2).toFixed(1)}`;
    })
    .join(' ')
);
const area1 = computed(() => {
  if (!path1.value) return '';
  const count = props.data.filter((d) => d.v1 > 0).length;
  return `${path1.value} L${px(count - 1).toFixed(1)},${(PT + cH.value).toFixed(1)} L${px(0).toFixed(1)},${(PT + cH.value).toFixed(1)} Z`;
});

const yLabels = computed(() => [Math.round(maxV.value * 0.5), maxV.value]);
</script>
