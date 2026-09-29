import { Vibration, Platform } from 'react-native';

let audioCtx: any = null;
let isAlarmPlaying = false;
let alarmIntervalId: any = null;

function getAudioContext(): any {
  if (Platform.OS !== 'web' || typeof window === 'undefined') return null;
  if (!audioCtx) {
    const AudioContextClass = (window as any).AudioContext || (window as any).webkitAudioContext;
    if (AudioContextClass) {
      audioCtx = new AudioContextClass();
    }
  }
  if (audioCtx && audioCtx.state === 'suspended') {
    audioCtx.resume().catch(() => {});
  }
  return audioCtx;
}

export function playMobileAlarmBurst() {
  try {
    // Physical vibration alarm pattern
    Vibration.vibrate([0, 400, 150, 400]);

    const ctx = getAudioContext();
    if (!ctx) return;

    if (ctx.state === 'suspended') {
      ctx.resume().catch(() => {});
    }

    const now = ctx.currentTime;
    const beeps = [
      { start: 0.0, end: 0.18, freq1: 960, freq2: 1440 },
      { start: 0.24, end: 0.42, freq1: 960, freq2: 1440 },
      { start: 0.48, end: 0.66, freq1: 1100, freq2: 1650 },
    ];

    beeps.forEach(({ start, end, freq1, freq2 }) => {
      const startTime = now + start;
      const endTime = now + end;

      const osc = ctx.createOscillator();
      osc.type = 'sawtooth';
      osc.frequency.setValueAtTime(freq1, startTime);
      osc.frequency.exponentialRampToValueAtTime(freq2, endTime);

      const gainNode = ctx.createGain();
      gainNode.gain.setValueAtTime(0.001, startTime);
      gainNode.gain.linearRampToValueAtTime(0.5, startTime + 0.03);
      gainNode.gain.setValueAtTime(0.5, endTime - 0.03);
      gainNode.gain.linearRampToValueAtTime(0.001, endTime);

      osc.connect(gainNode);
      gainNode.connect(ctx.destination);

      osc.start(startTime);
      osc.stop(endTime);
    });
  } catch (err) {
    console.warn('Mobile alarm audio warning:', err);
  }
}

export function startMobileEmergencyAlarm() {
  if (isAlarmPlaying) return;
  isAlarmPlaying = true;

  // Start continuous vibration alarm loop on native
  try {
    Vibration.vibrate([0, 600, 300, 600], true);
  } catch (e) {
    console.warn('Vibration error:', e);
  }

  // Play audio burst
  playMobileAlarmBurst();

  // Repeat audio burst every 2.5 seconds
  alarmIntervalId = setInterval(() => {
    if (isAlarmPlaying) {
      playMobileAlarmBurst();
    }
  }, 2500);
}

export function stopMobileEmergencyAlarm() {
  isAlarmPlaying = false;
  try {
    Vibration.cancel();
  } catch (e) {}

  if (alarmIntervalId !== null) {
    clearInterval(alarmIntervalId);
    alarmIntervalId = null;
  }
}
