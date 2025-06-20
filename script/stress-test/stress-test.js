import http from 'k6/http';
import { check, sleep } from 'k6';
import { uuidv4 } from 'https://jslib.k6.io/k6-utils/1.4.0/index.js';

// 配置测试选项
export const options = {
  stages: [
    { duration: '30s', target: 50 },  // 30秒内逐步增加到50个虚拟用户
    { duration: '1m', target: 50 },   // 保持50个用户1分钟
    { duration: '30s', target: 100 }, // 30秒内增加到100个用户
    { duration: '1m', target: 100 },  // 保持100个用户1分钟
    { duration: '30s', target: 0 },   // 30秒内逐步减少到0
  ],
  thresholds: {
    http_req_duration: ['p(95)<500'], // 95%的请求应该在500ms内完成
    http_req_failed: ['rate<0.01'],   // 失败率应低于1%
  },
};

// 测试执行函数
export default function () {
  const baseUrl = 'http://localhost:8080/api/transactions';
  const accountId = `account_${uuidv4().substring(0, 8)}`;

  // 1. 创建交易
  const createPayload = JSON.stringify({
    accountId: accountId,
    amount: Math.random() * 1000,
    type: Math.random() > 0.5 ? 'DEPOSIT' : 'WITHDRAWAL',
    description: 'Load test transaction',
  });

  const createParams = {
    headers: {
      'Content-Type': 'application/json',
    },
  };

  const createRes = http.post(baseUrl, createPayload, createParams);
  check(createRes, {
    'create transaction status is 201': (r) => r.status === 201,
  });

  // 如果创建成功，执行后续操作
  if (createRes.status === 201) {
    const transactionId = createRes.json('id');

    // 2. 查询交易
    const getRes = http.get(`${baseUrl}/${transactionId}`);
    check(getRes, {
      'get transaction status is 200': (r) => r.status === 200,
    });

    // 3. 更新交易
    const updatePayload = JSON.stringify({
      accountId: accountId,
      amount: Math.random() * 1000,
      type: 'TRANSFER',
      description: 'Updated transaction',
    });

    const updateRes = http.put(`${baseUrl}/${transactionId}`, updatePayload, createParams);
    check(updateRes, {
      'update transaction status is 200': (r) => r.status === 200,
    });

    // 4. 删除交易
    const deleteRes = http.del(`${baseUrl}/${transactionId}`);
    check(deleteRes, {
      'delete transaction status is 204': (r) => r.status === 204,
    });
  }

  // 5. 查询交易列表
  const listRes = http.get(`${baseUrl}?page=0&size=10`);
  check(listRes, {
    'list transactions status is 200': (r) => r.status === 200,
  });

  // 添加随机延迟
  sleep(Math.random() * 2);
}