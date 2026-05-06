import axios from 'axios'
import { LOCALES } from '../i18n/locales.js'

export const USERS = {
  '1': { password: '1', name: 'Иванов И И' },
  '2': { password: '2', name: 'Петров П П' }
}

export default {
  name: 'ForkliftDirectory',
  data() {
    return {
      lang: 'ru',
      forklifts: [],
      selectedForklift: null,
      downtimes: [],
      searchNumber: '',
      editingId: null,
      isAdding: false,
      editForm: {
        brand: '',
        number: '',
        loadCapacity: 0
      },
      newForklift: {
        brand: '',
        number: '',
        loadCapacity: 0
      },
      currentUser: '',
      isLoggedIn: false,
      showLoginModal: false,
      loginForm: {
        username: '',
        password: ''
      },
      loginError: '',
      showIncidentModal: false,
      editingDowntime: null,
      downtimeForm: {
        startTime: '',
        endTime: '',
        description: ''
      }
    }
  },
  computed: {
    t() {
      return (key) => LOCALES[this.lang][key] || key
    },
    canEdit() {
      return this.editingId === null && !this.isAdding && this.isLoggedIn
    },
    canSave() {
      return this.editForm.brand && this.editForm.number && this.editForm.loadCapacity > 0
    },
    canDelete() {
      return this.editingId === null && !this.isAdding && this.isLoggedIn
    }
  },
  mounted() {
    this.loadForklifts()
    this.setDefaultStartTime()
    this.restoreAuth()
    window.addEventListener('keydown', this.handleKeyDown)
  },
  beforeDestroy() {
    window.removeEventListener('keydown', this.handleKeyDown)
  },
  methods: {
    restoreAuth() {
      const savedUser = localStorage.getItem('forklift_user')
      if (savedUser) {
        this.currentUser = savedUser
        this.isLoggedIn = true
      }
    },

    doLogin() {
      const user = USERS[this.loginForm.username]
      if (user && user.password === this.loginForm.password) {
        this.currentUser = user.name
        this.isLoggedIn = true
        localStorage.setItem('forklift_user', user.name)
        this.showLoginModal = false
        this.loginForm = { username: '', password: '' }
        this.loginError = ''
      } else {
        this.loginError = this.t('loginError')
      }
    },

    logout() {
      this.isLoggedIn = false
      this.currentUser = ''
      localStorage.removeItem('forklift_user')
      this.selectedForklift = null
      this.downtimes = []
    },

    handleKeyDown(e) {
      if (e.key === 'Escape' && this.selectedForklift) {
        this.closeDowntimesPanel()
      }
    },

    closeDowntimesPanel() {
      this.selectedForklift = null
      this.downtimes = []
    },

    async loadForklifts() {
      try {
        const response = await axios.get('/api/forklifts')
        this.forklifts = response.data
      } catch (error) {
        console.error(this.t('errorLoad'), error)
      }
    },

    async searchForklifts() {
      try {
        const response = await axios.get('/api/forklifts/search', {
          params: { number: this.searchNumber }
        })
        this.forklifts = response.data
      } catch (error) {
        console.error('Ошибка поиска:', error)
      }
    },

    resetSearch() {
      this.searchNumber = ''
      this.loadForklifts()
    },

    selectForklift(forklift) {
      this.selectedForklift = forklift
      this.loadDowntimes(forklift.id)
    },

    async loadDowntimes(forkliftId) {
      try {
        const response = await axios.get(`/api/downtimes/forklift/${forkliftId}`)
        this.downtimes = response.data
      } catch (error) {
        console.error('Ошибка загрузки простоев:', error)
      }
    },

    startEdit(forklift) {
      this.editingId = forklift.id
      this.editForm = {
        brand: forklift.brand,
        number: forklift.number,
        loadCapacity: forklift.loadCapacity,
        modifiedBy: this.currentUser
      }
    },

    async saveEdit() {
      try {
        await axios.put(`/api/forklifts/${this.editingId}`, {
          ...this.editForm,
          modifiedBy: this.currentUser
        })
        this.editingId = null
        this.loadForklifts()
      } catch (error) {
        console.error(this.t('errorSave'), error)
        alert(this.t('errorSave'))
      }
    },

    cancelEdit() {
      if (confirm(this.t('confirmCancel'))) {
        this.editingId = null
        this.isAdding = false
        this.loadForklifts()
      }
    },

    startAdd() {
      this.isAdding = true
      this.newForklift = {
        brand: '',
        number: '',
        loadCapacity: 0
      }
    },

    async saveNew() {
      try {
        await axios.post('/api/forklifts', {
          ...this.newForklift,
          modifiedBy: this.currentUser
        })
        this.isAdding = false
        this.loadForklifts()
      } catch (error) {
        console.error(this.t('errorAdd'), error)
        alert(this.t('errorAdd'))
      }
    },

    cancelAdd() {
      if (confirm(this.t('confirmCancel'))) {
        this.isAdding = false
      }
    },

    async deleteForklift(id) {
      if (!confirm(this.t('confirmDelete'))) return

      try {
        await axios.delete(`/api/forklifts/${id}`)
        this.loadForklifts()
        if (this.selectedForklift?.id === id) {
          this.selectedForklift = null
          this.downtimes = []
        }
      } catch (error) {
        console.error(this.t('errorDelete'), error)
        if (error.response?.status === 409) {
          alert(this.t('deleteForkliftBlocked'))
        } else {
          alert(this.t('errorDelete'))
        }
      }
    },

    setDefaultStartTime() {
      const now = new Date()
      now.setMinutes(now.getMinutes() - now.getTimezoneOffset())
      this.downtimeForm.startTime = now.toISOString().slice(0, 16)
    },

    editDowntime(downtime) {
      this.editingDowntime = downtime
      this.downtimeForm = {
        startTime: downtime.startTime.slice(0, 16),
        endTime: downtime.endTime ? downtime.endTime.slice(0, 16) : '',
        description: downtime.description
      }
      this.showIncidentModal = true
    },

    async saveDowntime() {
      if (!this.downtimeForm.startTime) {
        alert(this.t('startTimeRequired'))
        return
      }

      const data = {
        forkliftId: this.selectedForklift.id,
        startTime: this.downtimeForm.startTime,
        endTime: this.downtimeForm.endTime || null,
        description: this.downtimeForm.description,
        modifiedBy: this.currentUser
      }

      try {
        if (this.editingDowntime) {
          await axios.put(`/api/downtimes/${this.editingDowntime.id}`, {
            ...data,
            modifiedBy: this.currentUser
          })
        } else {
          await axios.post('/api/downtimes', {
            ...data,
            modifiedBy: this.currentUser
          })
        }
        this.closeIncidentModal()
        this.loadDowntimes(this.selectedForklift.id)
      } catch (error) {
        console.error(this.t('errorSaveDowntime'), error)
        alert(this.t('errorSaveDowntime'))
      }
    },

    async deleteDowntime(id) {
      if (!confirm(this.t('confirmDeleteDowntime'))) return

      try {
        await axios.delete(`/api/downtimes/${id}`)
        this.loadDowntimes(this.selectedForklift.id)
      } catch (error) {
        console.error('Ошибка удаления простоя:', error)
        alert(this.t('errorDelete'))
      }
    },

    closeIncidentModal() {
      this.showIncidentModal = false
      this.editingDowntime = null
      this.downtimeForm = {
        startTime: '',
        endTime: '',
        description: ''
      }
      this.setDefaultStartTime()
    },

    formatDate(dateStr) {
      if (!dateStr) return '-'
      const date = new Date(dateStr)
      return date.toLocaleString(this.lang === 'ru' ? 'ru-RU' : 'en-US', {
        day: '2-digit',
        month: '2-digit',
        year: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
      })
    },

    formatDateTime(dateStr) {
      return this.formatDate(dateStr)
    },

    formatDowntimeDuration(downtime) {
      const start = new Date(downtime.startTime)
      const end = downtime.endTime ? new Date(downtime.endTime) : new Date()
      const diffMinutes = Math.floor((end - start) / 60000)
      const hours = Math.floor(diffMinutes / 60)
      const minutes = diffMinutes % 60
      if (this.lang === 'ru') {
        return `${hours} ч ${minutes} мин`
      }
      return `${hours} h ${minutes} min`
    }
  }
}