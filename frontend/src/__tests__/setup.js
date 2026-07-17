import { config } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import { beforeEach, vi } from 'vitest'
import { ref } from 'vue'

config.global.stubs = {
  'router-link': {
    template: '<a :href="to"><slot /></a>',
    props: ['to'],
  },
}

vi.mock('vue-router', async () => {
  const actual = await vi.importActual('vue-router')
  return {
    ...actual,
    useRouter: () => ({
      push: vi.fn(),
      replace: vi.fn(),
      currentRoute: ref({ path: '/', query: {} }),
    }),
    useRoute: () => ref({ path: '/', query: {} }),
  }
})

beforeEach(() => {
  setActivePinia(createPinia())
  localStorage.clear()
})
