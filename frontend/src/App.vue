<template>
    <header class="hero">
    <div class="hero-title">
      <h1>实验室设备借还报修系统</h1>
    </div>

    <div class="account-bar">
      <div class="notification-trigger">
        <button class="icon-button" title="通知" aria-label="通知" @click="loadNotifications">
          <svg viewBox="0 0 24 24" aria-hidden="true">
            <path d="M18 8a6 6 0 0 0-12 0c0 7-3 7-3 9h18c0-2-3-2-3-9" />
            <path d="M13.7 21a2 2 0 0 1-3.4 0" />
          </svg>
          <span v-if="unreadNotificationCount" class="notification-badge">{{ unreadNotificationCount }}</span>
        </button>

        <div class="notification-menu">
          <div class="notification-head">
            <div>
              <strong>通知中心</strong>
              <span>{{ unreadNotificationCount }} 条未读</span>
            </div>
            <div class="notification-actions">
              <button class="mini ghost" @click="loadNotifications">刷新</button>
              <button class="mini" :disabled="!unreadNotificationCount" @click="markAllNotificationsRead">一键已读</button>
            </div>
          </div>
          <div class="notification-list compact">
            <div v-for="item in latestNotifications" :key="item.id" class="notification-item" :class="{ unread: !item.readFlag }">
              <div>
                <div class="notification-title-row">
                  <strong>{{ item.title }}</strong>
                  <StatusBadge :text="item.readFlag ? '已读' : '未读'" :status="item.readFlag ? 'RETURNED' : 'PENDING'" />
                </div>
                <p>{{ item.content }}</p>
              </div>
              <button v-if="!item.readFlag" class="mini" @click="markRead(item.id)">已读</button>
            </div>
            <p v-if="!notifications.length" class="notification-empty">暂无通知。</p>
            <p v-else-if="notifications.length > latestNotifications.length" class="notification-empty">仅显示最近 {{ latestNotifications.length }} 条。</p>
          </div>
        </div>
      </div>

      <label class="role-pill" for="userSelect">
        <span>角色</span>
        <strong>{{ currentUser?.roleLabel || '请选择' }}</strong>
        <select id="userSelect" v-model.number="currentUserId" @change="refreshAll">
          <option v-for="user in users" :key="user.id" :value="user.id">
            {{ user.realName }}（{{ user.roleLabel }}）
          </option>
        </select>
        <svg viewBox="0 0 24 24" aria-hidden="true"><path d="m6 9 6 6 6-6" /></svg>
      </label>

      <div class="account-chip">
        <div class="avatar">{{ currentUserInitial }}</div>
        <div class="account-text">
          <strong>{{ currentUser?.realName || '未选择用户' }}</strong>
          <span>{{ currentUser?.department || 'User' }}</span>
        </div>
      </div>
    </div>
  </header>

  <main>
    <section class="panel toolbar">
      <div>
        <h2>设备列表</h2>
        <p>设备状态由状态模式控制，只有可借用设备才能进入借用流程。</p>
      </div>
      <div class="search-box">
        <input v-model.trim="keyword" placeholder="输入设备名称或编号" @keyup.enter="loadEquipment" />
        <button @click="loadEquipment">查询</button>
      </div>
    </section>

    <section class="grid">
      <article v-for="item in orderedEquipment" :key="item.id" class="card">
        <div class="item-head">
          <h3>{{ item.name }}</h3>
          <StatusBadge :text="item.statusLabel" :status="item.status" />
        </div>
        <div class="meta">
          <StatusBadge :text="`#${item.id}`" />
          <StatusBadge :text="item.code" />
          <StatusBadge :text="item.categoryLabel" />
        </div>
        <p>实验室：{{ item.labRoom }}</p>
        <p>价值：￥{{ item.value }}　负责人：{{ item.manager || '-' }}</p>
        <p>{{ item.description || '' }}</p>
      </article>
      <p v-if="!equipment.length" class="empty">暂无设备。</p>
    </section>

    <section class="columns">
      <div class="panel">
        <h2>提交借用申请</h2>
        <form @submit.prevent="submitBorrow">
          <label>借用设备
            <select v-model.number="borrowForm.equipmentId" required>
              <option disabled value="">请选择可借用设备</option>
              <option v-for="item in availableEquipment" :key="item.id" :value="item.id">
                #{{ item.id }} {{ item.name }}（{{ item.code }}）
              </option>
            </select>
          </label>
          <label>开始日期<input v-model="borrowForm.startDate" type="date" required /></label>
          <label>预计归还<input v-model="borrowForm.expectedReturnDate" type="date" required /></label>
          <label>申请时间<input v-model="borrowForm.applyTime" type="datetime-local" readonly /></label>
          <label>用途<textarea v-model.trim="borrowForm.purpose" required placeholder="说明课程实验或科研用途"></textarea></label>
          <p v-if="!availableEquipment.length" class="form-tip">当前没有可借用设备，请先归还或完成维修后再提交。</p>
          <button type="submit" :disabled="!canSubmitBorrow">提交申请</button>
        </form>
      </div>
      <div class="panel">
        <h2>提交设备报修</h2>
        <form @submit.prevent="submitRepair">
          <label>设备 ID<input v-model.trim="repairForm.equipmentId" required placeholder="例如 2" /></label>
          <label>故障描述<textarea v-model.trim="repairForm.faultDescription" required placeholder="描述故障现象"></textarea></label>
          <button type="submit">提交报修</button>
        </form>
      </div>
    </section>

    <section class="panel">
      <div class="section-head">
        <div>
          <h2>{{ isStudent ? '借用与归还' : '借用审批与归还' }}</h2>
          <p>{{ isStudent ? '这里展示我的借用申请和可归还设备。' : '职责链自动计算所需审批角色，策略模式计算逾期费用。' }}</p>
        </div>
        <button @click="loadBorrows">刷新</button>
      </div>
      <div class="list">
        <div v-for="item in borrows" :key="item.id" class="item">
          <div class="item-head">
            <h3>{{ item.equipmentName }}（{{ item.equipmentCode }}）</h3>
            <StatusBadge :text="item.statusLabel" :status="item.status" />
          </div>
          <div class="meta">
            <StatusBadge :text="`申请人：${item.applicantName}`" />
            <StatusBadge v-if="!isStudent" :text="`需${item.requiredApproverRoleLabel}审批`" />
            <StatusBadge v-if="!isStudent && item.approverName" :text="`审批人：${item.approverName}`" />
          </div>
          <p>时间：{{ item.startDate }} 至 {{ item.expectedReturnDate }}；用途：{{ item.purpose }}</p>
          <p>实际归还：{{ item.actualReturnDate || '-' }}；逾期费用：￥{{ item.overdueFee }}</p>
          <p v-if="item.rejectReason">驳回原因：{{ item.rejectReason }}</p>
          <div class="item-actions">
            <button v-if="!isStudent && canApproveBorrow(item)" class="success" @click="approveBorrow(item.id)">审批通过</button>
            <button v-if="!isStudent && canApproveBorrow(item)" class="danger" @click="rejectBorrow(item.id)">驳回</button>
            <button v-if="canReturnBorrow(item)" class="secondary" @click="returnBorrow(item.id)">登记归还</button>
            <span v-if="!canApproveBorrow(item) && !canReturnBorrow(item)" class="form-tip">{{ borrowActionHint(item) }}</span>
          </div>
        </div>
        <p v-if="!borrows.length" class="empty">{{ isStudent ? '暂无我的借用记录。' : '暂无借用申请。' }}</p>
      </div>
    </section>

    <section class="panel">
      <div class="section-head">
        <div>
          <h2>维修工单</h2>
          <p>报修、开始维修、完成维修会触发观察者生成通知和日志。</p>
        </div>
        <button @click="loadRepairs">刷新</button>
      </div>
      <div class="list">
        <div v-for="item in repairs" :key="item.id" class="item">
          <div class="item-head">
            <h3>{{ item.equipmentName }}（{{ item.equipmentCode }}）</h3>
            <StatusBadge :text="item.statusLabel" :status="item.status" />
          </div>
          <div class="meta">
            <StatusBadge :text="`报修人：${item.reporterName}`" />
            <StatusBadge v-if="item.handlerName" :text="`处理人：${item.handlerName}`" />
          </div>
          <p>故障：{{ item.faultDescription }}</p>
          <p>结果：{{ item.repairResult || '-' }}</p>
          <div class="item-actions">
            <button v-if="canHandleRepair && item.status === 'PENDING'" @click="startRepair(item.id)">开始维修</button>
            <button v-if="canHandleRepair && item.status === 'PROCESSING'" class="success" @click="completeRepair(item.id)">完成维修</button>
            <span v-if="!canHandleRepair || item.status === 'COMPLETED'" class="form-tip">{{ repairActionHint(item) }}</span>
          </div>
        </div>
        <p v-if="!repairs.length" class="empty">暂无维修工单。</p>
      </div>
    </section>

    <section v-if="isLabAdmin" class="panel">
      <h2>系统日志</h2>
      <div class="list compact">
        <div v-for="item in logs" :key="item.id" class="item">
          <StatusBadge :text="item.eventType" />
          <p>{{ item.content }}</p>
        </div>
        <p v-if="!logs.length" class="empty">暂无日志。</p>
      </div>
    </section>
  </main>

  <div id="toast" :class="{ show: toastMessage }">{{ toastMessage }}</div>
</template>

<script setup>
import { computed, defineComponent, h, onMounted, reactive, ref } from 'vue'

const users = ref([])
const currentUserId = ref(null)
const keyword = ref('')
const equipment = ref([])
const borrows = ref([])
const repairs = ref([])
const notifications = ref([])
const logs = ref([])
const toastMessage = ref('')
let toastTimer = null

const approvalRanks = {
  STUDENT: 0,
  LAB_ADMIN: 1,
  TEACHER: 2,
  DEAN: 3
}

const manageRoles = new Set(['LAB_ADMIN', 'TEACHER', 'DEAN'])

const borrowForm = reactive({
  equipmentId: '',
  startDate: dateInput(),
  expectedReturnDate: dateInput(3),
  applyTime: dateTimeInput(),
  purpose: ''
})

const repairForm = reactive({
  equipmentId: '',
  faultDescription: ''
})

const statusClass = {
  AVAILABLE: 'ok',
  BORROWED: 'warn',
  REPAIRING: 'warn',
  SCRAPPED: 'bad',
  APPROVED: 'ok',
  PENDING: 'warn',
  REJECTED: 'bad',
  RETURNED: 'ok',
  COMPLETED: 'ok',
  PROCESSING: 'warn'
}

const StatusBadge = defineComponent({
  name: 'StatusBadge',
  props: {
    text: { type: [String, Number], default: '' },
    status: { type: String, default: '' }
  },
  setup(props) {
    return () => h('span', { class: ['badge', statusClass[props.status] || ''] }, props.text)
  }
})

const currentUser = computed(() => {
  const user = users.value.find((item) => item.id === Number(currentUserId.value))
  return user || null
})

const currentUserLabel = computed(() => {
  const user = currentUser.value
  return user ? `${user.realName} / ${user.roleLabel}` : '未选择'
})

const currentUserInitial = computed(() => currentUser.value?.realName?.slice(0, 1) || '用')
const unreadNotificationCount = computed(() => notifications.value.filter((item) => !item.readFlag).length)
const latestNotifications = computed(() => notifications.value.slice(0, 4))

const isStudent = computed(() => currentUser.value?.role === 'STUDENT')
const isLabAdmin = computed(() => currentUser.value?.role === 'LAB_ADMIN')

const equipmentNameOrder = ['数字万用表', 'Arduino 传感器套件']
const orderedEquipment = computed(() => {
  return [...equipment.value].sort((left, right) => {
    const leftManagerPriority = left.manager === '王老师' ? 0 : 1
    const rightManagerPriority = right.manager === '王老师' ? 0 : 1
    if (leftManagerPriority !== rightManagerPriority) return leftManagerPriority - rightManagerPriority

    const leftNameOrder = equipmentNameOrder.includes(left.name) ? equipmentNameOrder.indexOf(left.name) : equipmentNameOrder.length
    const rightNameOrder = equipmentNameOrder.includes(right.name) ? equipmentNameOrder.indexOf(right.name) : equipmentNameOrder.length
    if (leftNameOrder !== rightNameOrder) return leftNameOrder - rightNameOrder

    return Number(left.id) - Number(right.id)
  })
})

const availableEquipment = computed(() => orderedEquipment.value.filter((item) => item.status === 'AVAILABLE'))

const canSubmitBorrow = computed(() => {
  return Boolean(currentUser.value && borrowForm.equipmentId && borrowForm.startDate && borrowForm.expectedReturnDate && borrowForm.purpose)
})

const canHandleRepair = computed(() => manageRoles.has(currentUser.value?.role))

function requestHeaders() {
  return {
    'Content-Type': 'application/json',
    'X-User-Id': currentUserId.value
  }
}

async function api(url, options = {}) {
  const response = await fetch(url, options)
  if (!response.ok) {
    const error = await response.json().catch(async () => ({ message: await response.text().catch(() => '请求失败') }))
    throw new Error(error.message || `请求失败：${response.status}`)
  }
  return response.json()
}

function dateInput(offsetDays = 0) {
  const date = new Date()
  date.setDate(date.getDate() + offsetDays)
  return date.toISOString().slice(0, 10)
}

function dateTimeInput() {
  const date = new Date()
  date.setMinutes(date.getMinutes() - date.getTimezoneOffset())
  return date.toISOString().slice(0, 16)
}

function showToast(message) {
  toastMessage.value = message
  clearTimeout(toastTimer)
  toastTimer = setTimeout(() => {
    toastMessage.value = ''
  }, 2600)
}

async function safeRun(task, successMessage) {
  try {
    await task()
    if (successMessage) showToast(successMessage)
  } catch (error) {
    showToast(error.message)
  }
}

async function initUsers() {
  users.value = await api('/api/users')
  currentUserId.value = users.value[0]?.id ?? null
}

async function loadEquipment() {
  const query = keyword.value ? `?keyword=${encodeURIComponent(keyword.value)}` : ''
  equipment.value = await api(`/api/equipment${query}`)
  if (!borrowForm.equipmentId || !availableEquipment.value.some((item) => item.id === Number(borrowForm.equipmentId))) {
    borrowForm.equipmentId = availableEquipment.value[0]?.id ?? ''
  }
}

async function loadBorrows() {
  if (!currentUserId.value) return
  borrows.value = await api('/api/borrows', { headers: requestHeaders() })
}

async function loadRepairs() {
  repairs.value = await api('/api/repairs')
}

async function loadNotifications() {
  if (!currentUserId.value) return
  notifications.value = await api('/api/notifications', { headers: requestHeaders() })
}

async function loadLogs() {
  if (!isLabAdmin.value) {
    logs.value = []
    return
  }
  logs.value = await api('/api/notifications/logs')
}

async function refreshAll() {
  await Promise.all([loadEquipment(), loadBorrows(), loadRepairs(), loadNotifications(), loadLogs()])
}

function resetBorrowForm() {
  borrowForm.equipmentId = availableEquipment.value[0]?.id ?? ''
  borrowForm.startDate = dateInput()
  borrowForm.expectedReturnDate = dateInput(3)
  borrowForm.applyTime = dateTimeInput()
  borrowForm.purpose = ''
}

function resetRepairForm() {
  repairForm.equipmentId = ''
  repairForm.faultDescription = ''
}

async function submitBorrow() {
  await safeRun(async () => {
    await api('/api/borrows', {
      method: 'POST',
      headers: requestHeaders(),
      body: JSON.stringify({
        equipmentId: Number(borrowForm.equipmentId),
        startDate: borrowForm.startDate,
        expectedReturnDate: borrowForm.expectedReturnDate,
        purpose: borrowForm.purpose
      })
    })
    resetBorrowForm()
    await refreshAll()
  }, '借用申请已提交')
}

function canApproveBorrow(item) {
  if (isStudent.value || item.status !== 'PENDING' || !currentUser.value) return false
  return (approvalRanks[currentUser.value.role] ?? -1) >= (approvalRanks[item.requiredApproverRole] ?? 99)
}

function canReturnBorrow(item) {
  if (item.status !== 'APPROVED' || !currentUser.value) return false
  return manageRoles.has(currentUser.value.role) || item.applicantName === currentUser.value.realName
}

function borrowActionHint(item) {
  if (isStudent.value && item.status === 'PENDING') return '申请已提交，等待管理员审批。'
  if (item.status === 'PENDING') {
    return `待${item.requiredApproverRoleLabel}审批；当前用户无审批权限时请切换到对应角色或更高级角色。`
  }
  if (item.status === 'APPROVED') return '已审批通过，可由申请人或管理员登记归还。'
  if (item.status === 'RETURNED') return '该申请已归还完成。'
  if (item.status === 'REJECTED') return '该申请已被驳回，不能继续审批。'
  return '当前状态暂无可执行操作。'
}

function repairActionHint(item) {
  if (!canHandleRepair.value) return '维修处理需切换到实验室管理员、指导教师或学院负责人。'
  if (item.status === 'COMPLETED') return '该工单已完成。'
  return '当前状态暂无可执行操作。'
}

async function submitRepair() {
  await safeRun(async () => {
    await api('/api/repairs', {
      method: 'POST',
      headers: requestHeaders(),
      body: JSON.stringify({ ...repairForm })
    })
    resetRepairForm()
    await refreshAll()
  }, '报修工单已提交')
}

async function approveBorrow(id) {
  await safeRun(async () => {
    await api(`/api/borrows/${id}/approve`, {
      method: 'POST',
      headers: requestHeaders(),
      body: JSON.stringify({ comment: '审批同意' })
    })
    await refreshAll()
  }, '审批通过')
}

async function rejectBorrow(id) {
  const reason = window.prompt('请输入驳回原因', '设备使用时间冲突')
  if (!reason) return
  await safeRun(async () => {
    await api(`/api/borrows/${id}/reject`, {
      method: 'POST',
      headers: requestHeaders(),
      body: JSON.stringify({ reason })
    })
    await refreshAll()
  }, '已驳回申请')
}

async function returnBorrow(id) {
  const actualReturnDate = window.prompt('请输入实际归还日期', new Date().toISOString().slice(0, 10))
  if (!actualReturnDate) return
  await safeRun(async () => {
    await api(`/api/borrows/${id}/return`, {
      method: 'POST',
      headers: requestHeaders(),
      body: JSON.stringify({ actualReturnDate })
    })
    await refreshAll()
  }, '归还登记完成')
}

async function startRepair(id) {
  await safeRun(async () => {
    await api(`/api/repairs/${id}/start`, { method: 'POST', headers: requestHeaders() })
    await refreshAll()
  }, '已开始维修')
}

async function completeRepair(id) {
  const repairResult = window.prompt('请输入维修结果', '已更换故障部件，测试正常')
  if (!repairResult) return
  await safeRun(async () => {
    await api(`/api/repairs/${id}/complete`, {
      method: 'POST',
      headers: requestHeaders(),
      body: JSON.stringify({ repairResult })
    })
    await refreshAll()
  }, '维修已完成')
}

async function markRead(id) {
  await safeRun(async () => {
    await api(`/api/notifications/${id}/read`, { method: 'POST', headers: requestHeaders() })
    await loadNotifications()
  })
}

async function markAllNotificationsRead() {
  const unreadIds = notifications.value.filter((item) => !item.readFlag).map((item) => item.id)
  if (!unreadIds.length) {
    showToast('暂无未读通知')
    return
  }
  await safeRun(async () => {
    await Promise.all(unreadIds.map((id) => api(`/api/notifications/${id}/read`, {
      method: 'POST',
      headers: requestHeaders()
    })))
    await loadNotifications()
  }, '全部通知已读')
}

onMounted(async () => {
  await safeRun(async () => {
    await initUsers()
    await refreshAll()
  })
})
</script>














