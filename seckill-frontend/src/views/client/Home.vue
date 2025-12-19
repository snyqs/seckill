<script setup>
import { onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import { fetchActivities } from '../../api/activity';
import { fetchProducts } from '../../api/product';
import dayjs from 'dayjs';

const router = useRouter();
const activities = ref([]);
const products = ref([]);
const loading = ref(false);
const fallbackImages = [
  'https://images.unsplash.com/photo-1505740420928-5e560c06d30e?auto=format&fit=crop&w=1200&q=60',
  'https://images.unsplash.com/photo-1498050108023-c5249f4df085?auto=format&fit=crop&w=1200&q=60',
  'https://images.unsplash.com/photo-1523275335684-37898b6baf30?auto=format&fit=crop&w=1200&q=60',
  'https://images.unsplash.com/photo-1512499617640-c2f999098c01?auto=format&fit=crop&w=1200&q=60',
];
const pickImage = (key) => fallbackImages[key % fallbackImages.length];

const loadData = async () => {
  loading.value = true;
  try {
    const [acts, prods] = await Promise.all([
      fetchActivities({ pageNum: 1, pageSize: 3 }),
      fetchProducts({ pageNum: 1, pageSize: 6 }),
    ]);
    activities.value = acts?.records || [];
    products.value = prods?.records || [];
  } finally {
    loading.value = false;
  }
};

onMounted(loadData);

const formatTime = (t) => (t ? dayjs(t).format('YYYY-MM-DD HH:mm') : '-');
</script>

<template>
  <div class="page">
    <section class="hero">
      <div>
        <p class="badge">在线秒杀系统 · Vue3</p>
        <h1>限时抢购，实时库存，安全防刷</h1>
        <p class="muted">
          覆盖商品浏览、活动倒计时、下单与订单管理，支持管理员创建商品与秒杀活动。
        </p>
        <div class="actions">
          <el-button type="primary" size="large" @click="router.push('/seckill')">立即抢购</el-button>
          <el-button size="large" @click="router.push('/products')">浏览商品</el-button>
        </div>
      </div>
      <div class="hero-card">
        <p>活动快照</p>
        <div class="list" v-loading="loading">
          <div v-for="item in activities" :key="item.id" class="activity" @click="router.push(`/seckill/${item.id}`)">
            <div>
              <strong>{{ item.activityName }}</strong>
              <div class="muted">
                {{ formatTime(item.startTime) }} 开始 · 库存 {{ item.seckillStock }}
              </div>
            </div>
            <el-tag :type="item.status === 1 ? 'success' : item.status === 0 ? 'info' : 'warning'">
              {{ item.statusDesc || (item.status === 1 ? '进行中' : item.status === 0 ? '未开始' : '已结束') }}
            </el-tag>
          </div>
          <div v-if="!activities.length && !loading" class="muted">暂无活动</div>
        </div>
      </div>
    </section>

    <section class="section">
      <div class="flex-between">
        <h3>正在进行的秒杀</h3>
        <el-link type="primary" @click="router.push('/seckill')">查看全部</el-link>
      </div>
      <el-row :gutter="12" v-loading="loading">
        <el-col v-for="item in activities" :key="item.id" :xs="24" :sm="12" :md="8">
          <el-card class="card">
            <img
              :src="item.imgUrl || pickImage(item.id)"
              class="cover"
              alt=""
              @error="(e) => (e.target.src = pickImage(item.id))"
            />
            <h4>{{ item.activityName }}</h4>
            <p class="muted">{{ item.productName }}</p>
            <div class="price">
              <span class="seckill">￥{{ item.seckillPrice }}</span>
              <span class="origin">￥{{ item.originalPrice }}</span>
            </div>
            <div class="muted small">开始：{{ formatTime(item.startTime) }}</div>
            <el-button type="primary" size="small" style="margin-top: 10px" @click="router.push(`/seckill/${item.id}`)">
              立即查看
            </el-button>
          </el-card>
        </el-col>
      </el-row>
    </section>

    <section class="section">
      <div class="flex-between">
        <h3>精选商品</h3>
        <el-link type="primary" @click="router.push('/products')">更多</el-link>
      </div>
      <el-row :gutter="12" v-loading="loading">
        <el-col v-for="item in products" :key="item.id" :xs="24" :sm="12" :md="6">
          <el-card class="card product">
            <img
              :src="item.imgUrl || pickImage(item.id)"
              class="cover"
              alt=""
              @error="(e) => (e.target.src = pickImage(item.id))"
            />
            <h4>{{ item.productName }}</h4>
            <p class="muted small">{{ item.productDesc }}</p>
            <div class="price">
              <span class="seckill">￥{{ item.price }}</span>
              <span class="muted small">库存 {{ item.stock }}</span>
            </div>
            <el-button size="small" @click="router.push(`/seckill`)">去看看秒杀</el-button>
          </el-card>
        </el-col>
      </el-row>
    </section>
  </div>
</template>

<style scoped>
.hero {
  display: grid;
  grid-template-columns: 2fr 1.2fr;
  gap: 18px;
  padding: 12px 6px 16px;
  align-items: center;
  background: linear-gradient(120deg, rgba(99, 102, 241, 0.08), rgba(34, 197, 94, 0.08));
  border-radius: 16px;
}

.badge {
  display: inline-block;
  padding: 6px 10px;
  border-radius: 999px;
  background: #eef2ff;
  color: #4338ca;
  font-weight: 600;
}

.hero h1 {
  margin: 10px 0 8px;
}

.actions {
  display: flex;
  gap: 12px;
  margin-top: 14px;
}

.hero-card {
  background: #fff;
  border-radius: 18px;
  padding: 16px;
  box-shadow: 0 20px 50px rgba(15, 23, 42, 0.08);
  animation: float 6s ease-in-out infinite;
}

.list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.activity {
  padding: 12px;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  display: flex;
  justify-content: space-between;
  gap: 10px;
  cursor: pointer;
}

.activity:hover {
  border-color: #6366f1;
}

.section {
  margin-top: 18px;
}

.card {
  margin-bottom: 12px;
}

.product h4 {
  margin: 8px 0 4px;
}

.cover {
  width: 100%;
  height: 180px;
  object-fit: cover;
  border-radius: 10px;
  margin-bottom: 8px;
}

.price {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-top: 8px;
}

.seckill {
  color: #ef4444;
  font-size: 18px;
  font-weight: 700;
}

.origin {
  text-decoration: line-through;
  color: #6b7280;
}

.small {
  font-size: 13px;
}

@media (max-width: 960px) {
  .hero {
    grid-template-columns: 1fr;
  }
}

@keyframes float {
  0% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-6px);
  }
  100% {
    transform: translateY(0);
  }
}
</style>
