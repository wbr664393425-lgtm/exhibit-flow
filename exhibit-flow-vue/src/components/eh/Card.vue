<template>
	<div class="eh-card" :class="{ 'eh-card--clickable': !!clickable }" @click="onClick">
		<slot />
	</div>
</template>

<script setup lang="ts">
const props = defineProps<{ clickable?: boolean }>();
const emit = defineEmits<{ (e: 'click'): void }>();
function onClick() {
	if (props.clickable) emit('click');
}
</script>
