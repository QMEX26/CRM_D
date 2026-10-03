import fs from 'fs';
import path from 'path';
import { fileURLToPath } from 'url';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

const BACKEND_URL = "http://127.0.0.1:8080/api/v1";
const FIREBASE_API_KEY = "AIzaSyB1gM9Oklmrm9IZUwb7kM34Y8Dzk_I4NX0";

console.log(`================================================================`);
console.log(`SUBSCRIPTION 7-DAY FREE TRIAL & EXPIRY VERIFICATION TEST`);
console.log(`Target Backend: ${BACKEND_URL}`);
console.log(`================================================================\n`);

async function loginFirebase(email, password) {
  const res = await fetch(`https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key=${FIREBASE_API_KEY}`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ email, password, returnSecureToken: true }),
  });
  const data = await res.json();
  if (!res.ok) {
    throw new Error(`Firebase login failed for ${email}: ${JSON.stringify(data.error?.message || data)}`);
  }
  return data.idToken;
}

function assert(condition, message) {
  if (!condition) {
    console.error(`❌ FAILED: ${message}`);
    throw new Error(`Assertion failed: ${message}`);
  } else {
    console.log(`  ✓ ${message}`);
  }
}

async function runSubscriptionTests() {
  try {
    // -------------------------------------------------------------------------
    // TEST 1: ADMIN LOGIN TO PROVISION / REGISTER NEW USER
    // -------------------------------------------------------------------------
    console.log('[1/4] LOGGING IN AS ADMIN...');
    const adminToken = await loginFirebase('admin@crm.com', 'admin123');
    const adminHeaders = {
      'Authorization': `Bearer ${adminToken}`,
      'Content-Type': 'application/json'
    };
    console.log('  ✓ Admin logged in successfully.');

    // -------------------------------------------------------------------------
    // TEST 2: PROVISION NEW USER & VERIFY 7-DAY FREE TRIAL
    // -------------------------------------------------------------------------
    const uniqueEmail = `test_agent_${Date.now()}@crm.com`;
    console.log(`\n[2/4] CREATING NEW AGENT: ${uniqueEmail} ...`);

    const createRes = await fetch(`${BACKEND_URL}/users`, {
      method: 'POST',
      headers: adminHeaders,
      body: JSON.stringify({
        name: 'Auto Trial Test Agent',
        email: uniqueEmail,
        password: 'Password123!',
        phone: '+91 99999 ' + Math.floor(10000 + Math.random() * 90000),
        roleName: 'ROLE_USER'
      })
    });

    const createData = await createRes.json();
    assert(createRes.status === 201 || createRes.status === 200, `User creation returned HTTP ${createRes.status}`);
    console.log(`  ✓ New user created with ID: ${createData.data?.id}`);

    // Approve the new user so they can log in
    await fetch(`${BACKEND_URL}/users/${createData.data?.id}/status`, {
      method: 'PATCH',
      headers: adminHeaders,
      body: JSON.stringify({ status: 'ACTIVE' })
    });
    console.log('  ✓ User approved to ACTIVE status.');

    // Log in as the newly created user
    console.log(`\nLogging in as newly created user (${uniqueEmail})...`);
    const newUserToken = await loginFirebase(uniqueEmail, 'Password123!');
    const newUserHeaders = {
      'Authorization': `Bearer ${newUserToken}`,
      'Content-Type': 'application/json'
    };

    console.log('Fetching /subscription/my-subscription for new user...');
    const subRes = await fetch(`${BACKEND_URL}/subscription/my-subscription`, { headers: newUserHeaders });
    const subData = await subRes.json();
    console.log(`HTTP Status: ${subRes.status}`);
    console.log('Raw Payload:', JSON.stringify(subData, null, 2));

    console.log('\n--- NEW USER SUBSCRIPTION METADATA ---');
    console.log(`Status:          ${subData.data?.status}`);
    console.log(`Is Trial:        ${subData.data?.isTrial}`);
    console.log(`Is Active:       ${subData.data?.isActive}`);
    console.log(`Is Expired:      ${subData.data?.isExpired}`);
    console.log(`Days Remaining:  ${subData.data?.daysRemaining}`);
    console.log(`Trial Start:     ${subData.data?.trialStartAt}`);
    console.log(`Trial End:       ${subData.data?.trialEndAt}`);
    console.log(`Plan Name:       ${subData.data?.plan?.name}`);
    console.log(`Plan Price:      ₹${subData.data?.plan?.price}`);
    console.log('--------------------------------------\n');

    assert(subData.data?.status === 'FREE_TRIAL', `New user status is FREE_TRIAL (got: ${subData.data?.status})`);
    assert(subData.data?.isTrial === true, 'New user isTrial flag is TRUE');
    assert(subData.data?.isExpired === false, 'New user isExpired flag is FALSE');
    assert(subData.data?.daysRemaining >= 6 && subData.data?.daysRemaining <= 7, `Days remaining is 7 days (got: ${subData.data?.daysRemaining})`);
    assert(subData.data?.trialEndAt !== null, 'Trial end date is present');
    assert(subData.data?.plan?.name === 'USER_MONTHLY', 'Applicable plan is USER_MONTHLY');

    // -------------------------------------------------------------------------
    // TEST 3: VERIFY EXPIRED USER (expired@crm.com)
    // -------------------------------------------------------------------------
    console.log('\n[3/4] TESTING EXPIRED USER: expired@crm.com ...');
    const expiredToken = await loginFirebase('expired@crm.com', 'agent123');
    const expiredHeaders = {
      'Authorization': `Bearer ${expiredToken}`,
      'Content-Type': 'application/json'
    };

    const expiredSubRes = await fetch(`${BACKEND_URL}/subscription/my-subscription`, { headers: expiredHeaders });
    const expiredSubData = await expiredSubRes.json();

    console.log('\n--- EXPIRED USER SUBSCRIPTION METADATA ---');
    console.log(`Status:          ${expiredSubData.data?.status}`);
    console.log(`Is Trial:        ${expiredSubData.data?.isTrial}`);
    console.log(`Is Active:       ${expiredSubData.data?.isActive}`);
    console.log(`Is Expired:      ${expiredSubData.data?.isExpired}`);
    console.log(`Days Remaining:  ${expiredSubData.data?.daysRemaining}`);
    console.log(`Trial Start:     ${expiredSubData.data?.trialStartAt}`);
    console.log(`Trial End:       ${expiredSubData.data?.trialEndAt}`);
    console.log('------------------------------------------\n');

    assert(expiredSubData.data?.status === 'EXPIRED', `Expired user status is EXPIRED (got: ${expiredSubData.data?.status})`);
    assert(expiredSubData.data?.isActive === false, 'Expired user isActive is FALSE');
    assert(expiredSubData.data?.isExpired === true, 'Expired user isExpired is TRUE');
    assert(expiredSubData.data?.daysRemaining === 0, `Expired user days remaining is 0 (got: ${expiredSubData.data?.daysRemaining})`);

    // -------------------------------------------------------------------------
    // TEST 4: RAZORPAY ORDER GENERATION FOR EXPIRED USER UPGRADE
    // -------------------------------------------------------------------------
    console.log('\n[4/4] TESTING RAZORPAY RENEWAL ORDER GENERATION FOR EXPIRED USER...');
    const orderRes = await fetch(`${BACKEND_URL}/subscription/create-order`, {
      method: 'POST',
      headers: expiredHeaders,
      body: JSON.stringify({})
    });
    const orderData = await orderRes.json();
    assert(orderRes.status === 201 || orderRes.status === 200, `Order creation returned HTTP ${orderRes.status}`);
    console.log(`  ✓ Razorpay Order ID created: ${orderData.data?.orderId}`);
    console.log(`  ✓ Amount in Paise: ${orderData.data?.amount} (${orderData.data?.currency})`);
    console.log(`  ✓ Key ID: ${orderData.data?.keyId}`);

    console.log('\n================================================================');
    console.log('🎉 ALL SUBSCRIPTION & FREE TRIAL TESTS PASSED 100% PERFECTLY!');
    console.log('================================================================');

  } catch (err) {
    console.error('\n❌ Subscription test failed:', err.message);
    process.exit(1);
  }
}

runSubscriptionTests();
