<template>
  <div class="item-page">
    <h2>商品订单页面</h2>
    <div v-if="loading">加载中...</div>
    <div v-else-if="error" class="error">{{ error }}</div>
    <div v-else>
      <div class="order-list">
        <div v-for="order in orderList" :key="order.outTradeNo" class="order-item"
          :class="{ 'success-order': order.tradeStatus === 'TRADE_SUCCESS' }">
          <p><strong>订单号:</strong> {{ order.outTradeNo }}</p>
          <p><strong>订单金额:</strong> {{ formatAmount(order.invoiceAmount) }}元</p>
          <p><strong>商品名称:</strong> {{ order.subject }}</p>
          <p><strong>商品描述:</strong> {{ order.body }}</p>
          <p><strong>剩余支付时间:</strong>
            <span style="color: red;"
              :class="{ 'expire-soon': parseInt(order.remainingTime) < 10 && parseInt(order.remainingTime) > 0 }">
              {{ isOrderPayable(order) ? `${order.remainingTime}秒` : '已过期' }}
            </span>
          </p>
          <p v-if="order.tradeStatus === 'TRADE_CLOSED'"><strong>状态:</strong> <span style="color: red;">已关闭</span></p>
          <p v-if="order.tradeStatus === 'TRADE_SUCCESS'"><strong>状态:</strong> <span style="color: green;">已支付</span>
          </p>
          <div>
            <button @click="goToPayment(order)" :disabled="!isOrderPayable(order)" class="payment-btn"
              :class="{ 'disabled-btn': !isOrderPayable(order), 'success-btn': order.tradeStatus === 'TRADE_SUCCESS' }">
              {{ getButtonText(order) }}
            </button>
          </div>
        </div>
      </div>
      <button @click="submitOrder" :disabled="loading" class="submit-button">创建新订单</button>
    </div>
  </div>
</template>

<script setup lang='ts'>
import axios from 'axios';
import { onMounted, onUnmounted, ref } from 'vue';
import { useRouter } from 'vue-router';

const router = useRouter();

interface OrderType {
  outTradeNo: string;
  invoiceAmount: string;
  subject: string;
  body: string;
  remainingTime: string; // 添加倒计时属性
  tradeStatus?: string;  // 添加交易状态属性
}

// 订单列表
const orderList = ref<OrderType[]>([]);
const loading = ref(false);
const error = ref<string | null>(null);

// 将金额从分转换为元
const formatAmount = (amount: string): string => {
  const amountNum = parseInt(amount);
  return (amountNum / 100).toFixed(2);
};

// 开始倒计时逻辑
const startCountdowns = () => {
  const interval = setInterval(() => {
    orderList.value = orderList.value.map(order => {
      const time = parseInt(order.remainingTime);
      if (time > 0) {
        return { ...order, remainingTime: String(time - 1) };
      }
      return order;
    });
  }, 1000); // 每秒更新一次

  // 组件卸载时清除定时器
  onUnmounted(() => {
    clearInterval(interval);
  });
};

// 提交（获取）随机订单
const submitOrder = async () => {
  loading.value = true;
  error.value = null;

  try {
    const response = await axios.get('http://localhost:8080/alipay/submit-order');
    console.log("后端返回的参数为:", response.data);

    if (response.data.code === 200) {
      // 使用 value 属性添加新订单
      orderList.value = [...orderList.value, response.data.data];
    } else {
      error.value = response.data.message || '创建订单失败';
    }
  } catch (err: any) {
    console.error('提交订单时出错:', err);
    error.value = err.message || '请求出错';
  } finally {
    loading.value = false;
  }
};

// 获取订单缓冲列表
const getOrderBufferList = async () => {
  loading.value = true;
  error.value = null;

  try {
    const response = await axios.get('http://localhost:8080/alipay/order-buffer-list');
    console.log("后端返回的参数为:", response.data);

    if (response.data.code === 200) {
      orderList.value = response.data.data;
    } else {
      error.value = response.data.message || '获取订单列表失败';
    }
  } catch (err: any) {
    console.error('获取订单列表时出错:', err);
    error.value = err.message || '请求出错';
  } finally {
    loading.value = false;
  }
}

// 判断订单是否可支付
const isOrderPayable = (order: OrderType): boolean => {
  return parseInt(order.remainingTime) > 0 &&
    order.tradeStatus !== 'TRADE_CLOSED' &&
    order.tradeStatus !== 'TRADE_SUCCESS';
};

// 获取按钮文本
const getButtonText = (order: OrderType): string => {
  if (order.tradeStatus === 'TRADE_CLOSED') {
    return '订单已关闭';
  }
  if (order.tradeStatus === 'TRADE_SUCCESS') {
    return '已支付';
  }
  if (parseInt(order.remainingTime) <= 0) {
    return '已过期';
  }
  return '前往支付';
};

// 前往支付
const goToPayment = (order: OrderType) => {
  // 检查是否可以支付
  if (!isOrderPayable(order)) {
    return;
  }

  router.push({
    path: '/payment',
    query: {
      outTradeNo: order.outTradeNo,
      invoiceAmount: order.invoiceAmount,
      subject: order.subject,
      body: order.body,
      remainingTime: order.remainingTime,
      tradeStatus: order.tradeStatus
    }
  });
};

onMounted(() => {
  // 页面加载时获取订单列表
  getOrderBufferList();
  // 启动倒计时
  startCountdowns();
});
</script>

<style scoped>
.item-page {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}

.order-list {
  margin-bottom: 20px;
}

.order-item {
  border: 1px solid #ddd;
  padding: 15px;
  margin-bottom: 15px;
  border-radius: 4px;
}

.payment-btn {
  background-color: #1890ff;
  color: white;
  padding: 8px 16px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.disabled-btn {
  background-color: #cccccc;
  cursor: not-allowed;
}

.submit-button {
  background-color: #4CAF50;
  color: white;
  padding: 10px 15px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.submit-button:disabled {
  background-color: #cccccc;
}

.error {
  color: red;
  margin-bottom: 15px;
}

.expire-soon {
  color: #ff4d4f;
  font-weight: bold;
  animation: blink 1s infinite;
}

@keyframes blink {
  0% {
    opacity: 1;
  }

  50% {
    opacity: 0.5;
  }

  100% {
    opacity: 1;
  }
}

/* 可以添加一个关闭状态的样式 */
.trade-closed {
  background-color: #ff4d4f;
}

.success-order {
  border: 1px solid #52c41a;
}

.success-btn {
  cursor: default !important;
}
</style>