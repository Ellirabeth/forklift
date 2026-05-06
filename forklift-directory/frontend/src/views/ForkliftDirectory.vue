<template>
  <div class="app-container">
    <!-- Верхняя панель: язык + авторизация -->
    <div class="top-bar">
      <div class="top-bar-left">
        <label class="lang-label">{{ t('languageLabel') }}:</label>
        <select class="lang-select" v-model="lang">
          <option value="ru">{{ t('russian') }}</option>
          <option value="en">{{ t('english') }}</option>
        </select>
      </div>
      <div class="top-bar-right">
        <span v-if="isLoggedIn" class="user-info">
          {{ t('user') }}: {{ currentUser }}
        </span>
        <button v-if="!isLoggedIn" class="login-btn" @click="showLoginModal = true">
          {{ t('login') }}
        </button>
        <button v-else class="logout-btn" @click="logout">
          {{ t('logout') }}
        </button>
      </div>
    </div>

    <div class="main-layout">
      <!-- Левая часть: 70% - таблица погрузчиков -->
      <div class="left-panel">
        <div class="forklift-directory">
          <h2>{{ t('title') }}</h2>
          
          <!-- Панель поиска -->
          <div class="search-panel">
            <input 
              v-model="searchNumber" 
              type="text" 
              :placeholder="t('searchPlaceholder')"
              @keyup.enter="searchForklifts"
            />
            <button @click="searchForklifts" class="search-btn">🔍 {{ t('search') }}</button>
            <button @click="resetSearch" class="reset-filter-btn">❌ <u>{{ t('resetFilter') }}</u></button>
          </div>

          <!-- Кнопки "Добавить" и "Отменить" над таблицей -->
          <div class="table-actions">
            <button @click="startAdd" :disabled="!isLoggedIn || isAdding || editingId !== null">
              {{ t('add') }}
            </button>
            <button @click="cancelEdit" :disabled="editingId === null && !isAdding">
              {{ t('cancel') }}
            </button>
          </div>

          <!-- Таблица погрузчиков -->
          <table class="forklift-table">
            <thead>
              <tr>
                <th>{{ t('id') }}</th>
                <th>{{ t('brand') }}</th>
                <th>{{ t('number') }}</th>
                <th>{{ t('loadCapacity') }}</th>
                <th>{{ t('details') }}</th>
                <th>{{ t('lastModified1') }} <br> {{ t('lastModified2') }}</th>
                <th>{{ t('user') }}</th>
                <th>{{ t('actions') }}</th>
              </tr>
            </thead>
            <tbody>
              <tr 
                v-for="forklift in forklifts" 
                :key="forklift.id"
                :class="{ selected: selectedForklift?.id === forklift.id }"
                @click="selectForklift(forklift)"
              >
                <td>{{ forklift.id }}</td>
                <td>
                  <input 
                    v-if="editingId === forklift.id" 
                    v-model="editForm.brand" 
                    type="text"
                  />
                  <span v-else>{{ forklift.brand }}</span>
                </td>
                <td>
                  <input 
                    v-if="editingId === forklift.id" 
                    v-model="editForm.number" 
                    type="text"
                  />
                  <span v-else>{{ forklift.number }}</span>
                </td>
                <td>
                  <input 
                    v-if="editingId === forklift.id" 
                    v-model="editForm.loadCapacity" 
                    type="number" 
                    step="0.001"
                  />
                  <span v-else>{{ forklift.loadCapacity }}</span>
                </td>
                <td>{{ forklift.hasDowntimes ? 'true' : 'false' }}</td>
                <td>{{ formatDate(forklift.lastModified1)}} <br> {{formatDate(forklift.lastModified2)}}</td>
                <td>{{ forklift.modifiedBy || '-' }}</td>
                <td>
                  <button 
                    v-if="editingId !== forklift.id" 
                    @click.stop="startEdit(forklift)"
                    :disabled="!canEdit"
                  >
                    ✏
                  </button>
                  <button 
                    v-if="editingId === forklift.id" 
                    @click.stop="saveEdit"
                    :disabled="!canSave"
                  >
                    💾
                  </button>
                  <button 
                    @click.stop="deleteForklift(forklift.id)"
                    :disabled="!canDelete"
                  >
                    ✖
                  </button>
                </td>
              </tr>
              <!-- Добавление новой записи -->
              <tr v-if="isAdding">
                <td>{{ t('new') }}</td>
                <td><input v-model="newForklift.brand" :placeholder="t('brand')" /></td>
                <td><input v-model="newForklift.number" :placeholder="t('number')" /></td>
                <td><input v-model.number="newForklift.loadCapacity" type="number" step="0.001" :placeholder="t('loadCapacity')" /></td>
                <td></td>
                <td>{{ formatDate(new Date()) }}</td>
                <td>{{ currentUser }}</td>
                <td>
                  <button @click="saveNew">💾</button>
                  <button @click="cancelAdd">✖</button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- Правая часть: 30% - простои -->
      <div v-if="selectedForklift" class="right-panel">
        <div class="downtimes-section">
          <div class="downtimes-header">
            <h3>{{ t('downtimesTitle') }} - {{ selectedForklift.number }}</h3>
            <button class="close-btn" @click="closeDowntimesPanel">✖</button>
          </div>
          
          <div class="downtime-toolbar">
            <button @click="showIncidentModal = true; editingDowntime = null" :disabled="!isLoggedIn">
              {{ t('addDowntime') }}
            </button>
          </div>

          <table class="downtime-table">
            <thead>
              <tr>
                <th>{{ t('downtimeId') }}</th>
                <th>{{ t('startTime') }}</th>
                <th>{{ t('endTime') }}</th>
                <th>{{ t('duration') }}</th>
                <th>{{ t('reason') }}</th>
                <th v-if="isLoggedIn">{{ t('actions') }}</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="downtime in downtimes" :key="downtime.id">
                <td>{{ downtime.id }}</td>
                <td>{{ formatDateTime(downtime.startTime) }}</td>
                <td>{{ downtime.endTime ? formatDateTime(downtime.endTime) : '-' }}</td>
                <td>{{ formatDowntimeDuration(downtime) }}</td>
                <td>{{ downtime.description }}</td>
                <td v-if="isLoggedIn">
                  <button @click.stop="editDowntime(downtime)">✏</button>
                  <button @click.stop="deleteDowntime(downtime.id)">✖</button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>

    <!-- Модальное окно для инцидента (простоя) -->
    <div v-if="showIncidentModal" class="modal-overlay" @click.self="closeIncidentModal">
      <div class="modal-content">
        <h3>{{ editingDowntime ? t('editIncident') : t('newIncident') }}</h3>
        <p class="modal-hint">{{ t('incidentHint') }}</p>
        
        <div class="form-group">
          <label>{{ t('startTimeLabel') }}</label>
          <input 
            type="datetime-local" 
            v-model="downtimeForm.startTime"
            required
          />
        </div>
        
        <div class="form-group">
          <label>{{ t('endTimeLabel') }}</label>
          <input 
            type="datetime-local" 
            v-model="downtimeForm.endTime"
          />
        </div>
        
        <div class="form-group">
          <label>{{ t('descriptionLabel') }}</label>
          <textarea v-model="downtimeForm.description" rows="3"></textarea>
        </div>
        
        <div class="modal-buttons">
          <button @click="saveDowntime">{{ t('save') }}</button>
          <button @click="closeIncidentModal">{{ t('exit') }}</button>
        </div>
      </div>
    </div>

    <!-- Модальное окно логина -->
    <div v-if="showLoginModal" class="modal-overlay" @click.self="showLoginModal = false">
      <div class="modal-content">
        <h3>{{ t('login') }}</h3>
        <p class="modal-hint">{{ t('loginDefault') }}</p>
        <div class="form-group">
          <label>{{ t('username') }}</label>
          <input v-model="loginForm.username" type="text" />
        </div>
        <div class="form-group">
          <label>{{ t('password') }}</label>
          <input v-model="loginForm.password" type="password" @keyup.enter="doLogin" />
        </div>
        <div v-if="loginError" class="login-error">{{ loginError }}</div>
        <div class="modal-buttons">
          <button @click="doLogin">{{ t('login') }}</button>
          <button @click="showLoginModal = false">{{ t('cancel') }}</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import forkliftLogic from './forkliftLogic.js'
export default forkliftLogic
</script>

<style>
@import '../styles/forklift-directory.css';
</style>