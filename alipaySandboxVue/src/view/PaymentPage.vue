<template>
  <div>
    <h2>支付宝沙盒支付测试</h2>
    <div class="order-info" v-if="orderDetails">
      <p><strong>订单号:</strong> {{ orderDetails.outTradeNo }}</p>
      <p><strong>金额:</strong> {{ formatAmount(orderDetails.invoiceAmount) }}元</p>
      <p><strong>商品:</strong> {{ orderDetails.subject }}</p>
    </div>
    <button @click="handlePayment" :disabled="loading">
      {{ loading ? '处理中...' : `支付${orderDetails ? formatAmount(orderDetails.invoiceAmount) : ''}元` }}
    </button>

    <div v-if="error" style="color: red; margin-top: 10px">
      错误: {{ error }}
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRoute } from 'vue-router';
import axios from 'axios';

const route = useRoute();

interface PaymentRequestType {
  outTradeNo: string;
  totalAmount: number;
  subject: string;
  body: string;
}

interface OrderDetails {
  outTradeNo: string;
  invoiceAmount: string;
  subject: string;
  body: string;
}

const loading = ref(false);
const error = ref<string | null>(null);
const orderDetails = ref<OrderDetails | null>(null);

// 将金额从分转换为元
const formatAmount = (amount: string): string => {
  const amountNum = parseInt(amount);
  return (amountNum / 100).toFixed(2);
};

onMounted(() => {
  // 从路由参数获取订单信息
  if (route.query.outTradeNo) {
    orderDetails.value = {
      outTradeNo: route.query.outTradeNo as string,
      invoiceAmount: route.query.invoiceAmount as string,
      subject: route.query.subject as string,
      body: route.query.body as string
    };
  }
});

const handlePayment = async () => {
  // 处理交易
  loading.value = true;
  error.value = null;

  try {
    if (!orderDetails.value) {
      throw new Error('订单信息不完整');
    }

    const paymentRequest: PaymentRequestType = {
      outTradeNo: orderDetails.value.outTradeNo,
      totalAmount: parseInt(orderDetails.value.invoiceAmount) / 100, // 转换为元
      subject: orderDetails.value.subject,
      body: orderDetails.value.body
    };

    // 发起支付请求
    const response = await axios.post('http://localhost:8080/alipay/order', paymentRequest);
    console.log("后端返回的参数为:", response.data);

    if (response.data.code === 200) {
      // 创建一个临时的div来存放支付宝返回的表单
      const div = document.createElement('div');
      div.innerHTML = response.data.data;
      document.body.appendChild(div);

      // 在当前页面中进行支付
      const form = document.forms[0];
      if (form) {
        // 移除 target="_blank"，使表单在当前页面提交
        form.removeAttribute('target'); // 或使用 form.target = '_self';
        form.submit();
      }

      // 提交后不需要立即移除div，因为页面会跳转
    } else {
      error.value = response.data.message || '创建订单失败';
    }
  } catch (err: any) {
    error.value = err.message || '发生错误';
  } finally {
    loading.value = false;
  }
};
</script>

<style scoped>
.order-info {
  margin: 20px 0;
  padding: 15px;
  border: 1px solid #ddd;
  border-radius: 4px;
  /* background-color: #f9f9f9; */
}
</style>