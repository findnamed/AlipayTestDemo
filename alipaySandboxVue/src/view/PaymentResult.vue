<template>
  <div>
    <h2>支付结果</h2>
    <div v-if="paymentSuccess" style="color: green">
      <p>支付成功！</p>
      <p>您的订单已经支付完成。</p>
    </div>
    <div v-else style="color: red">
      <p>支付失败或已取消</p>
      <p>请重新尝试支付。</p>
    </div>

    <button @click="goToHomePage">
      返回首页
    </button>
  </div>
</template>

<script setup lang='ts'>
import { ref, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';

const route = useRoute();
const router = useRouter();
const paymentSuccess = ref(false);

onMounted(() => {
  // Get the success parameter from URL
  const success = route.query.success === 'true';
  paymentSuccess.value = success;
});

const goToHomePage = () => {
  router.push('/');
};
</script>
<style scoped>
/* Add your custom styles here if needed */
</style>