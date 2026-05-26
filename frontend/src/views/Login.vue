<template>
  <div class="login-page">
    <div class="login-panel">
      <div class="login-brand">
        <div class="login-mark">Se</div>
        <div>
          <h1>新疆特色作物富硒品质预测与智慧决策平台</h1>
          <p>智慧农业大数据 · 土壤环境评估 · 富硒品质预测</p>
        </div>
      </div>
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top" @keyup.enter="submit">
        <el-form-item label="账号" prop="username">
          <el-input v-model="form.username" size="large" placeholder="请输入账号" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" size="large" placeholder="请输入密码" type="password" show-password />
        </el-form-item>
        <el-button type="success" size="large" :loading="loading" class="login-button" @click="submit">登录系统</el-button>
      </el-form>
      <div class="login-meta">测试账号：admin / 123456</div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const auth = useAuthStore()
const formRef = ref<FormInstance>()
const loading = ref(false)
const form = reactive({
  username: 'admin',
  password: '123456'
})
const rules: FormRules = {
  username: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

async function submit() {
  await formRef.value?.validate()
  loading.value = true
  try {
    await auth.login(form.username, form.password)
    ElMessage.success('登录成功')
    router.replace('/dashboard')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  display: grid;
  place-items: center;
  min-height: 100vh;
  padding: 24px;
  background:
    linear-gradient(rgba(13, 42, 35, 0.72), rgba(13, 42, 35, 0.68)),
    url("https://images.unsplash.com/photo-1492496913980-501348b61469?auto=format&fit=crop&w=1800&q=80") center/cover no-repeat;
}

.login-panel {
  width: min(460px, 100%);
  padding: 32px;
  border: 1px solid rgba(255, 255, 255, 0.28);
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.94);
  box-shadow: 0 24px 70px rgba(7, 24, 18, 0.24);
}

.login-brand {
  display: flex;
  gap: 14px;
  align-items: center;
  margin-bottom: 26px;
}

.login-mark {
  display: grid;
  place-items: center;
  width: 52px;
  height: 52px;
  border-radius: 8px;
  background: #2f855a;
  color: #ffffff;
  font-size: 22px;
  font-weight: 800;
}

h1 {
  margin: 0;
  color: #17352d;
  font-size: 22px;
  line-height: 1.35;
}

p {
  margin: 8px 0 0;
  color: #64748b;
  font-size: 13px;
}

.login-button {
  width: 100%;
  margin-top: 6px;
}

.login-meta {
  margin-top: 18px;
  color: #64748b;
  font-size: 13px;
  text-align: center;
}
</style>
