import { onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { navItems } from '@/navigation'

export function useKeyboardShortcuts() {
  const router = useRouter()

  function handleKeydown(e: KeyboardEvent) {
    if (!e.altKey) return

    const index = parseInt(e.key) - 1
    if (index >= 0 && index < navItems.length) {
      e.preventDefault()
      router.push(navItems[index].path)
    }
  }

  onMounted(() => {
    document.addEventListener('keydown', handleKeydown)
  })

  onUnmounted(() => {
    document.removeEventListener('keydown', handleKeydown)
  })
}
