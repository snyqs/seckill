<script setup>
import { onMounted, onBeforeUnmount, ref, computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import dayjs from 'dayjs';
import { fetchActivityDetail } from '../../api/activity';
import { fetchProductDetail } from '../../api/product';
import { createSeckillOrder } from '../../api/order';
import { useUserStore } from '../../store/user';

const route = useRoute();
const router = useRouter();
const userStore = useUserStore();
const id = route.params.id;

const activity = ref(null);
const product = ref(null);
const loading = ref(false);
const joinLoading = ref(false);
const quantity = ref(1);
let timer = null;
const joinError = ref('');
const fallbackImages = [
  'https://images.unsplash.com/photo-1505740420928-5e560c06d30e?auto=format&fit=crop&w=1200&q=60',
  'https://images.unsplash.com/photo-1498050108023-c5249f4df085?auto=format&fit=crop&w=1200&q=60',
  'https://images.unsplash.com/photo-1523275335684-37898b6baf30?auto=format&fit=crop&w=1200&q=60',
  'https://images.unsplash.com/photo-1512499617640-c2f999098c01?auto=format&fit=crop&w=1200&q=60',
];
const pickImage = (key) => fallbackImages[key % fallbackImages.length];

const load = async () => {
  loading.value = true;
  try {
    const data = await fetchActivityDetail(id);
    activity.value = data;
    if (data?.productId) {
      product.value = await fetchProductDetail(data.productId);
    }
  } finally {
    loading.value = false;
  }
};

const statusText = computed(() => {
  if (!activity.value) return '';
  const status = activity.value.status;
  if (status === 0) return '未开始';
  if (status === 1) return '进行中';
  return '已结束';
});

const countdown = computed(() => {
  if (!activity.value) return '';
  const now = dayjs();
  if (activity.value.status === 0) {
    const diff = dayjs(activity.value.startTime).diff(now, 'second');
    if (diff <= 0) return '即将开始';
    const h = Math.floor(diff / 3600);
    const m = Math.floor((diff % 3600) / 60);
    const s = diff % 60;
    return `距离开始 ${h}小时${m}分${s}秒`;
  }
  if (activity.value.status === 2) return '已结束';
  const diff = dayjs(activity.value.endTime).diff(now, 'second');
  const h = Math.max(0, Math.floor(diff / 3600));
  const m = Math.max(0, Math.floor((diff % 3600) / 60));
  const s = Math.max(0, diff % 60);
  return `距离结束 ${h}小时${m}分${s}秒`;
});

const canJoin = computed(() => {
  if (!activity.value) return false;
  if (joinError.value) return false;
  return activity.value.status === 1 && (activity.value.remainingStock ?? activity.value.seckillStock) > 0;
});

const handleJoin = async () => {
  if (!activity.value || !product.value) return;
  if (!userStore.isLoggedIn) {
    ElMessageBox.confirm('需要登录后才能下单，是否前往登录？', '提示', { type: 'warning' })
      .then(() => router.push({ name: 'Login', query: { redirect: route.fullPath } }))
      .catch(() => {});
    return;
  }
  joinLoading.value = true;
  try {
    const orderId = await createSeckillOrder({
      activityId: activity.value.id,
      productId: product.value.id,
      quantity: quantity.value,
      userId: userStore.user?.id,
    });
    ElMessage.success('下单成功，订单ID：' + orderId);
    router.push('/orders');
    joinError.value = '';
  } catch (e) {
    const msg = e?.msg || e?.message || '';
    if (msg.includes('seckill_order') || msg.includes('不存在')) {
      joinError.value = '订单服务未就绪（缺少 seckill_order 表），请联系后台';
    } else {
      joinError.value = msg || '下单失败';
    }
    ElMessage.error(joinError.value);
  } finally {
    joinLoading.value = false;
  }
};

onMounted(() => {
  load();
  timer = setInterval(() => {
    activity.value = activity.value ? { ...activity.value } : null;
  }, 1000);
});

onBeforeUnmount(() => {
  if (timer) clearInterval(timer);
});

const formatTime = (t) => (t ? dayjs(t).format('YYYY-MM-DD HH:mm:ss') : '-');
</script>

<template>
  <div class="page" v-loading="loading">
    <el-breadcrumb separator="/">
      <el-breadcrumb-item to="/">首页</el-breadcrumb-item>
      <el-breadcrumb-item to="/seckill">秒杀活动</el-breadcrumb-item>
      <el-breadcrumb-item>{{ activity?.name || '详情' }}</el-breadcrumb-item>
    </el-breadcrumb>

    <div v-if="activity" class="detail">
      <div class="gallery">
        <el-image
          :src="activity.imgUrl || pickImage(activity.id)"
          fit="cover"
          @error="(e) => (e.target.src = pickImage(activity.id))"
        />
      </div>
      <div class="info">
        <div class="flex-between">
          <h2>{{ activity.activityName }}</h2>
          <el-tag :type="activity.status === 1 ? 'success' : activity.status === 0 ? 'info' : 'warning'">
            {{ activity.statusDesc || statusText }}
          </el-tag>
        </div>
        <p class="muted">{{ activity.productDesc }}</p>
        <div class="price-box">
          <div>
            <div class="label">秒杀价</div>
            <div class="seckill">￥{{ activity.seckillPrice }}</div>
          </div>
          <div>
            <div class="label">原价</div>
            <div class="origin">￥{{ activity.originalPrice }}</div>
          </div>
          <div>
            <div class="label">库存</div>
            <div class="stock">{{ activity.remainingStock ?? activity.seckillStock }}</div>
          </div>
        </div>
        <div class="muted">开始：{{ formatTime(activity.startTime) }}</div>
        <div class="muted">结束：{{ formatTime(activity.endTime) }}</div>
        <div class="countdown">{{ countdown }}</div>
        <el-alert v-if="joinError" type="warning" :title="joinError" :closable="false" show-icon style="margin: 10px 0" />
        <div class="actions">
          <el-input-number v-model="quantity" :min="1" :max="5" size="small" />
          <el-button type="primary" :disabled="!canJoin" :loading="joinLoading" @click="handleJoin">
            立即秒杀
          </el-button>
          <el-tag v-if="!canJoin" type="danger">未开始或已结束</el-tag>
        </div>
      </div>
    </div>

    <el-divider />

    <div v-if="product" class="product">
      <h3>商品信息</h3>
      <p><strong>{{ product.name }}</strong></p>
      <p class="muted">{{ product.description }}</p>
      <div class="muted">价格：￥{{ product.price }} · 库存 {{ product.stock }}</div>
    </div>
  </div>
</template>

<style scoped>
.detail {
  display: grid;
  grid-template-columns: 1.3fr 2fr;
  gap: 18px;
  margin-top: 12px;
}

.gallery {
  background: #fff;
  padding: 12px;
  border-radius: 12px;
  border: 1px solid #e5e7eb;
  display: flex;
  justify-content: center;
  align-items: center;
}

.gallery :deep(.el-image) {
  width: 100%;
  height: 340px;
  object-fit: cover;
  border-radius: 10px;
}

.placeholder {
  width: 100%;
  height: 340px;
  border: 1px dashed #d1d5db;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #9ca3af;
}

.info {
  background: #fff;
  border-radius: 12px;
  padding: 16px;
  border: 1px solid #e5e7eb;
}

.price-box {
  display: flex;
  gap: 16px;
  padding: 12px;
  background: #f9fafb;
  border-radius: 10px;
}

.label {
  color: #6b7280;
  font-size: 13px;
}

.seckill {
  color: #ef4444;
  font-weight: 800;
  font-size: 22px;
}

.origin {
  text-decoration: line-through;
  color: #6b7280;
}

.stock {
  font-weight: 700;
}

.countdown {
  margin-top: 10px;
  font-weight: 600;
  color: #2563eb;
}

.actions {
  margin-top: 12px;
  display: flex;
  gap: 10px;
  align-items: center;
}

.product {
  background: #fff;
  padding: 12px;
  border-radius: 12px;
  border: 1px solid #e5e7eb;
}

@media (max-width: 960px) {
  .detail {
    grid-template-columns: 1fr;
  }
}
</style>
