import { initializeApp, getApps, getApp } from 'firebase/app';
// @ts-ignore
import { initializeAuth, getAuth, getReactNativePersistence } from 'firebase/auth';
import AsyncStorage from '@react-native-async-storage/async-storage';

const firebaseConfig = {
  apiKey: "AIzaSyB1gM9Oklmrm9IZUwb7kM34Y8Dzk_I4NX0",
  authDomain: "callingcrm-d-472b1.firebaseapp.com",
  projectId: "callingcrm-d-472b1",
  storageBucket: "callingcrm-d-472b1.firebasestorage.app",
  messagingSenderId: "624482364548",
  appId: "1:624482364548:android:4a10c352f6f32dd5f3a6d1",
};

export const app = getApps().length > 0 ? getApp() : initializeApp(firebaseConfig);

let firebaseAuth;
try {
  firebaseAuth = initializeAuth(app, {
    persistence: getReactNativePersistence(AsyncStorage),
  });
} catch (e) {
  firebaseAuth = getAuth(app);
}

export const auth = firebaseAuth;
